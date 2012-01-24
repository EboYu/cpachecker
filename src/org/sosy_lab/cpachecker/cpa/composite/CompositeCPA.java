/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2012  Dirk Beyer
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
package org.sosy_lab.cpachecker.cpa.composite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.configuration.Option;
import org.sosy_lab.common.configuration.Options;
import org.sosy_lab.cpachecker.cfa.objectmodel.CFANode;
import org.sosy_lab.cpachecker.core.defaults.AbstractCPAFactory;
import org.sosy_lab.cpachecker.core.defaults.MergeSepOperator;
import org.sosy_lab.cpachecker.core.interfaces.AbstractDomain;
import org.sosy_lab.cpachecker.core.interfaces.AbstractElement;
import org.sosy_lab.cpachecker.core.interfaces.CPAFactory;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysis;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysisWithABM;
import org.sosy_lab.cpachecker.core.interfaces.MergeOperator;
import org.sosy_lab.cpachecker.core.interfaces.Precision;
import org.sosy_lab.cpachecker.core.interfaces.PrecisionAdjustment;
import org.sosy_lab.cpachecker.core.interfaces.Reducer;
import org.sosy_lab.cpachecker.core.interfaces.Statistics;
import org.sosy_lab.cpachecker.core.interfaces.StatisticsProvider;
import org.sosy_lab.cpachecker.core.interfaces.StopOperator;
import org.sosy_lab.cpachecker.core.interfaces.TransferRelation;
import org.sosy_lab.cpachecker.core.interfaces.WrapperCPA;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class CompositeCPA implements ConfigurableProgramAnalysis, StatisticsProvider, WrapperCPA, ConfigurableProgramAnalysisWithABM
{

  @Options(prefix="cpa.composite")
  private static class CompositeOptions {
    @Option(toUppercase=true, values={"PLAIN", "AGREE"},
        description="which composite merge operator to use (plain or agree)\n"
          + "Both delegate to the component cpas, but agree only allows "
          + "merging if all cpas agree on this. This is probably what you want.")
    private String merge = "AGREE";
  }

  private static class CompositeCPAFactory extends AbstractCPAFactory {

    private ImmutableList<ConfigurableProgramAnalysis> cpas = null;

    @Override
    public ConfigurableProgramAnalysis createInstance() throws InvalidConfigurationException {
      Preconditions.checkState(cpas != null, "CompositeCPA needs wrapped CPAs!");

      CompositeOptions options = new CompositeOptions();
      getConfiguration().inject(options);

      ImmutableList.Builder<AbstractDomain> domains = ImmutableList.builder();
      ImmutableList.Builder<TransferRelation> transferRelations = ImmutableList.builder();
      ImmutableList.Builder<MergeOperator> mergeOperators = ImmutableList.builder();
      ImmutableList.Builder<StopOperator> stopOperators = ImmutableList.builder();
      ImmutableList.Builder<PrecisionAdjustment> precisionAdjustments = ImmutableList.builder();

      boolean mergeSep = true;

      for (ConfigurableProgramAnalysis sp : cpas) {
        domains.add(sp.getAbstractDomain());
        transferRelations.add(sp.getTransferRelation());
        stopOperators.add(sp.getStopOperator());
        precisionAdjustments.add(sp.getPrecisionAdjustment());

        MergeOperator merge = sp.getMergeOperator();
        if (merge != MergeSepOperator.getInstance()) {
          mergeSep = false;
        }
        mergeOperators.add(merge);
      }

      ImmutableList<StopOperator> stopOps = stopOperators.build();

      MergeOperator compositeMerge;
      if (mergeSep) {
        compositeMerge = MergeSepOperator.getInstance();
      } else {

        if (options.merge.equals("AGREE")) {
          compositeMerge = new CompositeMergeAgreeOperator(mergeOperators.build(), stopOps);
        } else if (options.merge.equals("PLAIN")) {
          compositeMerge = new CompositeMergePlainOperator(mergeOperators.build());
        } else {
          throw new AssertionError();
        }
      }

      CompositeDomain compositeDomain = new CompositeDomain(domains.build());
      CompositeTransferRelation compositeTransfer = new CompositeTransferRelation(transferRelations.build());
      CompositeStopOperator compositeStop = new CompositeStopOperator(stopOps);
      CompositePrecisionAdjustment compositePrecisionAdjustment = new CompositePrecisionAdjustment(precisionAdjustments.build(), compositeStop);

      return new CompositeCPA(compositeDomain, compositeTransfer, compositeMerge, compositeStop,
          compositePrecisionAdjustment, cpas);
    }

    @Override
    public CPAFactory setChild(ConfigurableProgramAnalysis pChild)
        throws UnsupportedOperationException {
      throw new UnsupportedOperationException("Use CompositeCPA to wrap several CPAs!");
    }

    @Override
    public CPAFactory setChildren(List<ConfigurableProgramAnalysis> pChildren) {
      Preconditions.checkNotNull(pChildren);
      Preconditions.checkArgument(!pChildren.isEmpty());
      Preconditions.checkState(cpas == null);

      cpas = ImmutableList.copyOf(pChildren);
      return this;
    }
  }

  public static CPAFactory factory() {
    return new CompositeCPAFactory();
  }

  private final AbstractDomain abstractDomain;
  private final TransferRelation transferRelation;
  private final MergeOperator mergeOperator;
  private final StopOperator stopOperator;
  private final PrecisionAdjustment precisionAdjustment;
  private final Reducer reducer;

  private final ImmutableList<ConfigurableProgramAnalysis> cpas;

  protected CompositeCPA (AbstractDomain abstractDomain,
      TransferRelation transferRelation,
      MergeOperator mergeOperator,
      StopOperator stopOperator,
      PrecisionAdjustment precisionAdjustment,
      ImmutableList<ConfigurableProgramAnalysis> cpas)
  {
    this.abstractDomain = abstractDomain;
    this.transferRelation = transferRelation;
    this.mergeOperator = mergeOperator;
    this.stopOperator = stopOperator;
    this.precisionAdjustment = precisionAdjustment;
    this.cpas = cpas;

    List<Reducer> wrappedReducers = new ArrayList<Reducer>();
    for (ConfigurableProgramAnalysis cpa : cpas) {
      if (cpa instanceof ConfigurableProgramAnalysisWithABM) {
        wrappedReducers.add(((ConfigurableProgramAnalysisWithABM) cpa).getReducer());
      } else {
        wrappedReducers.clear();
        break;
      }
    }
    if (!wrappedReducers.isEmpty()) {
      reducer = new CompositeReducer(wrappedReducers);
    } else {
      reducer = null;
    }
  }

  @Override
  public AbstractDomain getAbstractDomain() {
    return abstractDomain;
  }

  @Override
  public TransferRelation getTransferRelation() {
    return transferRelation;
  }

  @Override
  public MergeOperator getMergeOperator() {
    return mergeOperator;
  }

  @Override
  public StopOperator getStopOperator() {
    return stopOperator;
  }

  @Override
  public PrecisionAdjustment getPrecisionAdjustment () {
    return precisionAdjustment;
  }

  @Override
  public Reducer getReducer() {
    return reducer;
  }

  @Override
  public AbstractElement getInitialElement (CFANode node) {
    Preconditions.checkNotNull(node);

    ImmutableList.Builder<AbstractElement> initialElements = ImmutableList.builder();
    for (ConfigurableProgramAnalysis sp : cpas) {
      initialElements.add(sp.getInitialElement(node));
    }

    return new CompositeElement(initialElements.build());
  }

  @Override
  public Precision getInitialPrecision (CFANode node) {
    Preconditions.checkNotNull(node);

    ImmutableList.Builder<Precision> initialPrecisions = ImmutableList.builder();
    for (ConfigurableProgramAnalysis sp : cpas) {
      initialPrecisions.add(sp.getInitialPrecision(node));
    }
    return new CompositePrecision(initialPrecisions.build());
  }

  @Override
  public void collectStatistics(Collection<Statistics> pStatsCollection) {
    for (ConfigurableProgramAnalysis cpa: cpas) {
      if (cpa instanceof StatisticsProvider) {
        ((StatisticsProvider)cpa).collectStatistics(pStatsCollection);
      }
    }
  }

  @Override
  public <T extends ConfigurableProgramAnalysis> T retrieveWrappedCpa(Class<T> pType) {
    if (pType.isAssignableFrom(getClass())) {
      return pType.cast(this);
    }
    for (ConfigurableProgramAnalysis cpa : cpas) {
      if (pType.isAssignableFrom(cpa.getClass())) {
        return pType.cast(cpa);
      } else if (cpa instanceof WrapperCPA) {
        T result = ((WrapperCPA)cpa).retrieveWrappedCpa(pType);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  @Override
  public ImmutableList<? extends ConfigurableProgramAnalysis> getWrappedCPAs() {
    return cpas;
  }
}