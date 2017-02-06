/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2016  Dirk Beyer
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 *  CPAchecker web page:
 *    http://cpachecker.sosy-lab.org
 */
package org.sosy_lab.cpachecker.core.algorithm.pdr.ctigar;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.cfa.types.c.CNumericTypes;
import org.sosy_lab.cpachecker.cfa.types.c.CType;
import org.sosy_lab.cpachecker.core.algorithm.pdr.transition.Block;
import org.sosy_lab.cpachecker.core.algorithm.pdr.transition.ForwardTransition;
import org.sosy_lab.cpachecker.exceptions.CPAException;
import org.sosy_lab.cpachecker.util.AbstractStates;
import org.sosy_lab.cpachecker.util.predicates.pathformula.PathFormula;
import org.sosy_lab.cpachecker.util.predicates.pathformula.PathFormulaManager;
import org.sosy_lab.cpachecker.util.predicates.pathformula.SSAMap;
import org.sosy_lab.cpachecker.util.predicates.pathformula.SSAMap.SSAMapBuilder;
import org.sosy_lab.cpachecker.util.predicates.smt.BitvectorFormulaManagerView;
import org.sosy_lab.cpachecker.util.predicates.smt.BooleanFormulaManagerView;
import org.sosy_lab.cpachecker.util.predicates.smt.FormulaManagerView;
import org.sosy_lab.java_smt.api.BitvectorFormula;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.SolverException;

/**
 * A transition system with a global transition relation, modeling all transitions in CFA in one
 * boolean formula. It basically is the disjunction of all transitions between block heads in the
 * CFA. A program counter variable is conjoined to all block transitions to encode the block start
 * and end locations. Also provides formulas for the initial condition and the safety property.
 */
public class TransitionSystem {

  static final String PROGRAM_COUNTER_VARIABLE_NAME = "__CPAchecker_pc";
  private static final CType PROGRAM_COUNTER_TYPE = CNumericTypes.UNSIGNED_INT;

  private static final int STANDARD_UNPRIMED_SSA = 1;
  private static final int PC_PRIMED_SSA = 2; // Only need two different indices for pc

  private final BooleanFormula transitionRelation;
  private final BooleanFormula initialCondition;
  private final BooleanFormula safetyProperty;
  private final Map<Integer, CFANode> idToLocation;
  private final Set<CFANode> targetLocs;
  private final Set<String> programVariableNames;
  private final Map<String, CType> programVariableTypes;
  private final Set<CFANode> terminalNodes;
  private final Set<CFANode> nonTerminalLocs;

  // Those are final after the transition system is created.
  private PathFormula unprimedContext;
  private PathFormula primedContext;
  private int highestSSA;

  /**
   * Creates a new TransitionSystem for the given CFA.
   *
   * @param pCFA The CFA that this transition relation is based on.
   * @param pForwardTransition The component that computes the single block transitions.
   * @param pFmgr The used formula manager.
   * @param pPfmgr The used path formula manager.
   * @param pMainEntry The initial location.
   * @throws CPAException If the analysis creating the blocks encounters an exception.
   * @throws InterruptedException If the computation of the blocks is interrupted.
   * @throws SolverException If the solver failed during construction of the transition formula.
   */
  public TransitionSystem(
      CFA pCFA,
      ForwardTransition pForwardTransition,
      FormulaManagerView pFmgr,
      PathFormulaManager pPfmgr,
      CFANode pMainEntry)
      throws CPAException, InterruptedException, SolverException {

    Objects.requireNonNull(pCFA);
    Objects.requireNonNull(pFmgr);
    Objects.requireNonNull(pPfmgr);
    Objects.requireNonNull(pForwardTransition);
    Objects.requireNonNull(pMainEntry);
    BooleanFormulaManagerView bfmgr = pFmgr.getBooleanFormulaManager();
    BitvectorFormulaManagerView bvfmgr = pFmgr.getBitvectorFormulaManager();

    this.primedContext = pPfmgr.makeEmptyPathFormula();
    this.unprimedContext = pPfmgr.makeEmptyPathFormula();
    this.highestSSA = 1;
    this.terminalNodes = new HashSet<>();
    this.nonTerminalLocs = new HashSet<>();
    this.programVariableNames = new HashSet<>();
    this.programVariableTypes = new HashMap<>();
    this.idToLocation = new TreeMap<>();
    this.initialCondition = makeProgramcounterFormula(getID(pMainEntry), bvfmgr, pCFA);
    this.targetLocs = new HashSet<>();
    this.transitionRelation =
        createTransitionRelation(
            pForwardTransition, pFmgr, bfmgr, bvfmgr, pPfmgr, pCFA, pMainEntry);
    this.safetyProperty = createSafetyProperty(bfmgr, bvfmgr, pCFA);
  }

  /**
   * Collects all blocks in the CFA, adds the program counter to the block formula and creates a big
   * disjunction of those formulas. Takes care of adjusting the ssa indices for primed and unprimed
   * variables in different block formulas.
   */
  private BooleanFormula createTransitionRelation(
      ForwardTransition pForwardTransition,
      FormulaManagerView pFmgr,
      BooleanFormulaManagerView pBfmgr,
      BitvectorFormulaManagerView pBvfmgr,
      PathFormulaManager pPfmgr,
      CFA pCFA,
      CFANode pMainEntry)
      throws CPAException, InterruptedException {

    Collection<Block> exploredBlocks = getForwardReachableBlocks(pMainEntry, pForwardTransition);

    // If there are no blocks, the transition formula is "false".
    if (exploredBlocks.isEmpty()) {
      return pBfmgr.makeFalse();
    }

    /*
     * Create final pointer target set, as well as primed and unprimed ssa map including ALL
     * variables at max index (found in getForwardReachableBlocks()) and pc at 2.
     */
    SSAMapBuilder globalPrimedSSAMapBuilder = SSAMap.emptySSAMap().builder();
    SSAMapBuilder globalUnprimedSSAMapBuilder = SSAMap.emptySSAMap().builder();
    Block first = exploredBlocks.iterator().next();
    PathFormula withMergedPointerTargetSet = first.getPrimedContext();

    for (Block block : exploredBlocks) {

      // Merges pointer target sets.
      withMergedPointerTargetSet =
          pPfmgr.makeOr(withMergedPointerTargetSet, block.getPrimedContext());

      // Find locations without successor blocks.
      if (pForwardTransition.getBlocksFrom(block.getSuccessorLocation()).isEmpty()) {
        terminalNodes.add(block.getSuccessorLocation());
      }

      nonTerminalLocs.add(block.getPredecessorLocation());

      // Create ssa maps with lowest / highest indices for all variables.
      SSAMap blockPrimedMap = block.getPrimedContext().getSsa();
      for (String varName : blockPrimedMap.allVariables()) {
        globalPrimedSSAMapBuilder.setIndex(varName, blockPrimedMap.getType(varName), highestSSA);
        globalUnprimedSSAMapBuilder.setIndex(
            varName, blockPrimedMap.getType(varName), STANDARD_UNPRIMED_SSA);
      }
    }
    globalPrimedSSAMapBuilder.setIndex(
        PROGRAM_COUNTER_VARIABLE_NAME, PROGRAM_COUNTER_TYPE, PC_PRIMED_SSA);
    globalUnprimedSSAMapBuilder.setIndex(
        PROGRAM_COUNTER_VARIABLE_NAME, PROGRAM_COUNTER_TYPE, STANDARD_UNPRIMED_SSA);

    // The final contexts.
    this.primedContext =
        new PathFormula(
            pBfmgr.makeTrue(),
            globalPrimedSSAMapBuilder.build(),
            withMergedPointerTargetSet.getPointerTargetSet(),
            withMergedPointerTargetSet.getLength());
    this.unprimedContext =
        new PathFormula(
            pBfmgr.makeTrue(),
            globalUnprimedSSAMapBuilder.build(),
            withMergedPointerTargetSet.getPointerTargetSet(),
            withMergedPointerTargetSet.getLength());

    // Create big disjunction of updated block formulas.
    Iterator<Block> exploredBlockIterator = exploredBlocks.iterator();
    first = exploredBlockIterator.next();
    PathFormula transitionRelation =
        withCorrectionTermsAndPC(first, pFmgr, pBfmgr, pBvfmgr, pPfmgr, pCFA);

    while (exploredBlockIterator.hasNext()) {
      Block next = exploredBlockIterator.next();
      PathFormula adjusted = withCorrectionTermsAndPC(next, pFmgr, pBfmgr, pBvfmgr, pPfmgr, pCFA);

      // This takes care of disjoining the formulas themselves and merging pointer target sets.
      transitionRelation = pPfmgr.makeOr(transitionRelation, adjusted);
    }

    return transitionRelation.getFormula();
  }

  /**
   * Performs a DFS and collects all blocks that are forward reachable from the provided starting
   * location. After this, all variable names are known, as well as the globally highest ssa index.
   */
  private Collection<Block> getForwardReachableBlocks(
      CFANode pStartPoint, ForwardTransition pForwardTransition)
      throws CPAException, InterruptedException {
    Collection<Block> exploredBlocks = new LinkedList<>();
    Deque<Block> blockTraversalStack = new LinkedList<>();

    for (Block block : pForwardTransition.getBlocksFrom(pStartPoint)) {
      blockTraversalStack.push(block);
      exploredBlocks.add(block);
      processBlock(block);
    }

    while (!blockTraversalStack.isEmpty()) {
      Block currentBlock = blockTraversalStack.pop();

      // Continue recursively with new successors of current block.
      CFANode currentBlockSuccessorLocation = currentBlock.getSuccessorLocation();
      for (Block block : pForwardTransition.getBlocksFrom(currentBlockSuccessorLocation)) {
        if (!isBlockContainedIn(block, exploredBlocks)) {
          blockTraversalStack.push(block);
          exploredBlocks.add(block);
          processBlock(block);
        }
      }
    }
    return exploredBlocks;
  }

  /**
   * Saves block successor location if it is a target location, adds all variables to known program
   * variables and updates highest known ssa index.
   */
  private void processBlock(Block pBlock) {
    if (AbstractStates.IS_TARGET_STATE.apply(pBlock.getSuccessor())) {
      targetLocs.add(pBlock.getSuccessorLocation());
    }
    SSAMap primedMap = pBlock.getPrimedContext().getSsa();
    programVariableNames.addAll(primedMap.allVariables());
    for (String varName : primedMap.allVariables()) {
      highestSSA = Math.max(highestSSA, primedMap.getIndex(varName));
      programVariableTypes.putIfAbsent(varName, primedMap.getType(varName));
    }
  }

  /**
   * Adds correction terms of the following form for variables v.
   *
   * <ul>
   *   <li> If primed context has no v, conjoins v_1 = v_max (v doesn't change in this block)
   *   <li> If primed context has index i &lt; max, conjoins v_i = v_max
   *   <li> If unprimed context has has index i &gt; 1, conjoins v_1 = v_i
   * </ul>
   *
   * Adds formulas for program counter before and after block transition.
   *
   * @return A path formula with the above mentioned adjustments to the block transition formula.
   *     The ssa map contains the indices for all primed variables including those not in the
   *     original block formula and the pc. The pointer target set is the same as before.
   */
  private PathFormula withCorrectionTermsAndPC(
      Block pBlock,
      FormulaManagerView pFmgr,
      BooleanFormulaManagerView pBfmgr,
      BitvectorFormulaManagerView pBvfmgr,
      PathFormulaManager pPfmgr,
      CFA pCFA) {

    BooleanFormula extendedBlockFormula = pBlock.getFormula();
    PathFormula unprimedBlockContext = pBlock.getUnprimedContext();
    PathFormula primedBlockContext = pBlock.getPrimedContext();
    SSAMap unprimedSSAMap = unprimedBlockContext.getSsa();
    SSAMap primedSSAMap = primedBlockContext.getSsa();

    for (String varName : programVariableNames) {

      // Add general correction v@1 = v@max, if v is not in block formula (= not in primed context),
      if (!primedSSAMap.containsVariable(varName)) {
        BooleanFormula correctionterm =
            pBvfmgr.equal(
                makeVar(pPfmgr, pFmgr, this.unprimedContext, varName, STANDARD_UNPRIMED_SSA),
                makeVar(pPfmgr, pFmgr, this.primedContext, varName, highestSSA));
        extendedBlockFormula = pBfmgr.and(extendedBlockFormula, correctionterm);
      } else {

        // Add low correction v@1 = v@? if unprimed context for v has higher index (? > 1).
        if (unprimedSSAMap.containsVariable(varName)
            && unprimedSSAMap.getIndex(varName) > STANDARD_UNPRIMED_SSA) {
          BooleanFormula correctionterm =
              pBvfmgr.equal(
                  makeVar(pPfmgr, pFmgr, unprimedBlockContext, varName, STANDARD_UNPRIMED_SSA),
                  makeVar(
                      pPfmgr,
                      pFmgr,
                      unprimedBlockContext,
                      varName,
                      unprimedSSAMap.getIndex(varName)));
          extendedBlockFormula = pBfmgr.and(extendedBlockFormula, correctionterm);
        }

        // Add high correction v@? = v@max if primed context for v has lower index (? < max),
        // and update primed ssa map.
        if (primedSSAMap.getIndex(varName) < highestSSA) {
          BooleanFormula correctionterm =
              pBvfmgr.equal(
                  makeVar(
                      pPfmgr, pFmgr, primedBlockContext, varName, primedSSAMap.getIndex(varName)),
                  makeVar(pPfmgr, pFmgr, primedBlockContext, varName, highestSSA));
          extendedBlockFormula = pBfmgr.and(extendedBlockFormula, correctionterm);
        }
      }
    }

    // Add program counter.
    int predID = getID(pBlock.getPredecessorLocation());
    int succID = getID(pBlock.getSuccessorLocation());
    BooleanFormula pcBefore =
        pFmgr.instantiate(
            makeProgramcounterFormula(predID, pBvfmgr, pCFA),
            SSAMap.emptySSAMap().withDefault(STANDARD_UNPRIMED_SSA));
    BooleanFormula pcAfter =
        pFmgr.instantiate(
            makeProgramcounterFormula(succID, pBvfmgr, pCFA),
            SSAMap.emptySSAMap().withDefault(PC_PRIMED_SSA));
    extendedBlockFormula = pBfmgr.and(pcBefore, extendedBlockFormula, pcAfter);

    return pPfmgr.makeNewPathFormula(
        primedBlockContext.updateFormula(extendedBlockFormula), this.primedContext.getSsa());
  }

  /** Creates the variable with the given name, type, and index. */
  private BitvectorFormula makeVar(
      PathFormulaManager pPfmgr,
      FormulaManagerView pFmgr,
      PathFormula pContext,
      String pName,
      int pIndex) {
    BitvectorFormula var =
        (BitvectorFormula)
            pPfmgr.makeFormulaForVariable( // TODO ??? no function param?
                pContext, pName, pContext.getSsa().getType(pName), false);
    return pFmgr.instantiate(var, SSAMap.emptySSAMap().withDefault(pIndex));
  }

  private static boolean isBlockContainedIn(Block pBlock, Collection<Block> pCollection) {
    return pCollection.stream().anyMatch(pBlock::equalsIgnoreReachedSet);
  }

  /** Returns the formula (pc=pLocationNumber). */
  private BooleanFormula makeProgramcounterFormula(
      int pLocationNumber, BitvectorFormulaManagerView pBvfmgr, CFA pCFA) {
    int bitLength =
        pCFA.getMachineModel().getSizeof(PROGRAM_COUNTER_TYPE)
            * pCFA.getMachineModel().getSizeofCharInBits();
    BitvectorFormula pc = pBvfmgr.makeVariable(bitLength, PROGRAM_COUNTER_VARIABLE_NAME);
    BitvectorFormula value = pBvfmgr.makeBitvector(bitLength, pLocationNumber);
    return pBvfmgr.equal(pc, value);
  }

  /** SafetyProperty = (pc != l_E1) & (pc != l_E2) ... for all error locations. */
  private BooleanFormula createSafetyProperty(
      BooleanFormulaManagerView pBfmgr, BitvectorFormulaManagerView pBvfmgr, CFA pCFA) {
    BooleanFormula safetyProperty = pBfmgr.makeTrue();
    for (CFANode errorLocation : targetLocs) {
      int id = getID(errorLocation);
      safetyProperty =
          pBfmgr.and(safetyProperty, pBfmgr.not(makeProgramcounterFormula(id, pBvfmgr, pCFA)));
    }
    return safetyProperty;
  }

  /**
   * Returns a unique identifier for each node that is used as value for the program counter. Also
   * caches this mapping.
   */
  private int getID(CFANode pLocation) {
    int id = pLocation.getNodeNumber();
    idToLocation.putIfAbsent(id, pLocation);
    return id;
  }


  /**
   * Returns a set of all names for variables occurring in this transition system. The program
   * counter is excluded.
   *
   * <p>If the dedicated name for the program counter is needed, use {@link #programCounterName()}.
   */
  public Set<String> allVariableNames() {
    return programVariableNames;
  }

  /**
   * Returns the global transition relation as a single boolean formula.
   *
   * @return A boolean formula describing the transition relation.
   */
  public BooleanFormula getTransitionRelationFormula() {
    return transitionRelation;
  }

  /**
   * Returns the location with the specified identifier used in the transition relation.
   *
   * @param pID The identifier of the wanted location.
   * @return An Optional containing the CFANode with this id, or an empty Optional if no such
   *     CFANode exists.
   */
  public Optional<CFANode> getNodeForID(int pID) {
    return Optional.ofNullable(idToLocation.get(pID));
  }

  /**
   * Returns the used String representation of the program counter variable.
   *
   * @return The String representation of the program counter variable.
   */
  public String programCounterName() {
    return PROGRAM_COUNTER_VARIABLE_NAME;
  }

  /**
   * Returns the set of all target location for this transition system.
   *
   * @return A set of all target locations.
   */
  public Set<CFANode> getTargetLocations() {
    return targetLocs;
  }

  /**
   * Returns a path formula containing the ssa map for the primed variables, i.e. the variables
   * after one transition step.
   *
   * @return A path formula with the primed ssa indices.
   */
  public PathFormula getPrimedContext() {
    return primedContext;
  }

  /**
   * Returns a path formula containing the ssa map for the unprimed variables, i.e. the variables at
   * the start of a transition step.
   *
   * @return A path formula with the unprimed ssa indices.
   */
  public PathFormula getUnprimedContext() {
    return unprimedContext;
  }

  /**
   * Returns a formula describing the initial condition (pc = start location) for this transition
   * system.
   *
   * @return The initial condition (pc = start location).
   */
  public BooleanFormula getInitialCondition() {
    return initialCondition;
  }

  /**
   * Returns a formula describing the safety property for this transition system. This formula
   * encodes that the program counter is never at a target location: (pc != error location 1) & (pc
   * != error location 2) & ... for all error locations.
   *
   * @return The safety property of this transition system.
   */
  public BooleanFormula getSafetyProperty() {
    return safetyProperty;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Initial condition : \n").append(initialCondition);
    sb.append("\nSafety property : \n").append(safetyProperty);
    sb.append("\nTransition formula : \n").append(transitionRelation);
    return sb.toString();
  }

}