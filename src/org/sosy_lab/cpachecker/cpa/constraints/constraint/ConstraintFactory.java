/*
 * CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2014  Dirk Beyer
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
package org.sosy_lab.cpachecker.cpa.constraints.constraint;

import org.sosy_lab.common.log.LogManagerWithoutDuplicates;
import org.sosy_lab.cpachecker.cfa.ast.AIdExpression;
import org.sosy_lab.cpachecker.cfa.ast.c.CBinaryExpression;
import org.sosy_lab.cpachecker.cfa.ast.c.CUnaryExpression;
import org.sosy_lab.cpachecker.cfa.ast.java.JBinaryExpression;
import org.sosy_lab.cpachecker.cfa.ast.java.JUnaryExpression;
import org.sosy_lab.cpachecker.cfa.types.MachineModel;
import org.sosy_lab.cpachecker.cfa.types.Type;
import org.sosy_lab.cpachecker.cfa.types.c.CBasicType;
import org.sosy_lab.cpachecker.cfa.types.c.CNumericTypes;
import org.sosy_lab.cpachecker.cfa.types.c.CSimpleType;
import org.sosy_lab.cpachecker.cfa.types.c.CType;
import org.sosy_lab.cpachecker.cfa.types.java.JBasicType;
import org.sosy_lab.cpachecker.cfa.types.java.JSimpleType;
import org.sosy_lab.cpachecker.cfa.types.java.JType;
import org.sosy_lab.cpachecker.cpa.value.ValueAnalysisState;
import org.sosy_lab.cpachecker.cpa.value.symbolic.CExpressionTransformer;
import org.sosy_lab.cpachecker.cpa.value.symbolic.ExpressionTransformer;
import org.sosy_lab.cpachecker.cpa.value.symbolic.JExpressionTransformer;
import org.sosy_lab.cpachecker.cpa.value.symbolic.type.SymbolicExpression;
import org.sosy_lab.cpachecker.cpa.value.symbolic.type.SymbolicValueFactory;
import org.sosy_lab.cpachecker.cpa.value.type.BooleanValue;
import org.sosy_lab.cpachecker.cpa.value.type.NumericValue;
import org.sosy_lab.cpachecker.exceptions.UnrecognizedCodeException;

import com.google.common.base.Optional;

/**
 * Factory for creating {@link org.sosy_lab.cpachecker.cpa.constraints.constraint.Constraint} objects.
 */
public class ConstraintFactory {

  private final MachineModel machineModel;
  private final LogManagerWithoutDuplicates logger;
  private final String functionName;
  private final Optional<ValueAnalysisState> valueState;

  private SymbolicValueFactory expressionFactory;


  private ConstraintFactory(String pFunctionName, Optional<ValueAnalysisState> pValueState, MachineModel pMachineModel,
      LogManagerWithoutDuplicates pLogger) {

    machineModel = pMachineModel;
    logger = pLogger;
    functionName = pFunctionName;
    valueState = pValueState;
    expressionFactory = SymbolicValueFactory.getInstance();
  }

  public static ConstraintFactory getInstance(String pFunctionName, Optional<ValueAnalysisState> pValueState,
      MachineModel pMachineModel, LogManagerWithoutDuplicates pLogger) {
    return new ConstraintFactory(pFunctionName, pValueState, pMachineModel, pLogger);
  }

  public Constraint createNegativeConstraint(CBinaryExpression pExpression) throws UnrecognizedCodeException {
    Constraint positiveConstraint = createPositiveConstraint(pExpression);

    if (positiveConstraint == null) {
      return null;
    } else {
      return createNot(positiveConstraint);
    }
  }

  public Constraint createNegativeConstraint(JUnaryExpression pExpression) throws UnrecognizedCodeException {
    Constraint positiveConstraint = createPositiveConstraint(pExpression);

    if (positiveConstraint == null) {
      return null;
    } else {
      return createNot(positiveConstraint);
    }
  }

  public Constraint createNegativeConstraint(JBinaryExpression pExpression) throws UnrecognizedCodeException {
    Constraint positiveConstraint = createPositiveConstraint(pExpression);

    if (positiveConstraint == null) {
      return null;
    } else {
      return createNot(positiveConstraint);
    }
  }

  public Constraint createNegativeConstraint(AIdExpression pExpression) throws UnrecognizedCodeException {
   Constraint positiveConstraint = createPositiveConstraint(pExpression);

    if (positiveConstraint == null) {
      return null;
    } else {
      return createNot(positiveConstraint);
    }
  }


  public Constraint createPositiveConstraint(CUnaryExpression pExpression) throws UnrecognizedCodeException {
    CUnaryExpression.UnaryOperator operator = pExpression.getOperator();

    switch (operator) {
      case SIZEOF:
      case ALIGNOF:
      case AMPER:
        return null;

      default:
        SymbolicExpression operandExpression = getCTransformer().transform(pExpression);

        if (operandExpression == null) {
          return null;
        } else {
          return transformValueToConstraint(operandExpression, pExpression.getExpressionType());
        }
    }
  }

  public Constraint createPositiveConstraint(CBinaryExpression pExpression) throws UnrecognizedCodeException {
    final CExpressionTransformer transformer = getCTransformer();

    assert isConstraint(pExpression);
    return (Constraint) transformer.transform(pExpression);
  }

  private boolean isConstraint(CBinaryExpression pExpression) {
    switch (pExpression.getOperator()) {
      case EQUALS:
      case NOT_EQUALS:
      case GREATER_EQUAL:
      case GREATER_THAN:
      case LESS_EQUAL:
      case LESS_THAN:
        return true;
      default:
        return false;
    }
  }

  public Constraint createPositiveConstraint(JUnaryExpression pExpression) throws UnrecognizedCodeException {
    assert pExpression.getOperator() == JUnaryExpression.UnaryOperator.NOT;

    return (Constraint) getJavaTransformer().transform(pExpression);
  }

  public Constraint createPositiveConstraint(JBinaryExpression pExpression) throws UnrecognizedCodeException {
    final JExpressionTransformer transformer = getJavaTransformer();

    assert isConstraint(pExpression);
    return (Constraint) pExpression.accept(transformer);
  }

  private boolean isConstraint(JBinaryExpression pExpression) {
    switch (pExpression.getOperator()) {
      case GREATER_THAN:
      case GREATER_EQUAL:
      case LESS_THAN:
      case LESS_EQUAL:
      case NOT_EQUALS:
      case EQUALS:
        return true;

      default:
        return false;
    }
  }

  public Constraint createPositiveConstraint(AIdExpression pExpression) throws UnrecognizedCodeException {
    ExpressionTransformer transformer = new ExpressionTransformer(functionName, valueState);
    SymbolicExpression symbolicExpression = transformer.visit(pExpression);

    if (symbolicExpression == null) {
      return null;
    } else if (symbolicExpression instanceof Constraint) {
      return (Constraint) symbolicExpression;

    } else {
      assert false : "Value is no constraint!";
      return transformValueToConstraint(symbolicExpression, pExpression.getExpressionType());
    }
  }

  private Constraint transformValueToConstraint(SymbolicExpression pExpression, Type expressionType) {

    if (isNumeric(expressionType)) {
      // 1 <= pExpression
      return createLessOrEqual(getOneConstant(expressionType), pExpression, expressionType,
          getCalculationType(expressionType));

    } else if (isBoolean(expressionType)) {
      assert expressionType instanceof JType : "Expression is boolean but not a constraint in C!";
      return expressionFactory.equal(pExpression, getTrueValueConstant(), expressionType, expressionType);

    } else {
      throw new AssertionError("Unexpected type " + expressionType);
    }

  }

  private JExpressionTransformer getJavaTransformer() {
    return new JExpressionTransformer(functionName, valueState);
  }

  private CExpressionTransformer getCTransformer() {
    return new CExpressionTransformer(functionName, valueState, machineModel, logger);
  }

  private boolean isNumeric(Type pType) {
    if (pType instanceof CType) {
      CType canonicalType = ((CType) pType).getCanonicalType();
      if (canonicalType instanceof CSimpleType) {
        switch (((CSimpleType) canonicalType).getType()) {
          case FLOAT:
          case INT:
            return true;
          default:
            // DO NOTHING, false is returned below
        }
      }

      return false;
    } else if (pType instanceof JSimpleType) {
      switch (((JSimpleType)pType).getType()) {
        case BYTE:
        case CHAR:
        case SHORT:
        case INT:
        case LONG:
        case FLOAT:
        case DOUBLE:
          return true;
        default:
          // DO NOTHING, false is returned below
      }

      return false;
    } else {
      throw new AssertionError("Unexpected type " + pType);
    }
  }

  private boolean isBoolean(Type pType) {
    if (pType instanceof CType) {
      CType canonicalType = ((CType) pType).getCanonicalType();

      return canonicalType instanceof CSimpleType
          && ((CSimpleType) canonicalType).getType() == CBasicType.BOOL;
    }

    if(pType instanceof JSimpleType) {
      return ((JSimpleType)pType).getType() == JBasicType.BOOLEAN;
    } else {
      throw new AssertionError("Unexpected type " + pType);
    }
  }

  private SymbolicExpression getOneConstant(Type pType) {
    return expressionFactory.asConstant(new NumericValue(1L), pType);
  }

  private SymbolicExpression getTrueValueConstant() {
    return expressionFactory.asConstant(BooleanValue.valueOf(true), JSimpleType.getBoolean());
  }

  private Type getCalculationType(Type pType) {
    if (pType instanceof CType) {
      return getCCalculationType((CType) pType);
    } else if (pType instanceof JType) {
      return getJCalculationType((JType) pType);
    } else {
      throw new AssertionError("Unexpected type " + pType);
    }
  }

  private Type getCCalculationType(CType pType) {
    CType canonicalType = pType.getCanonicalType();

    if (canonicalType instanceof CSimpleType) {
      switch (((CSimpleType)canonicalType).getType()) {
        case CHAR:
        case INT:
          return CNumericTypes.SIGNED_INT;
        default:
          return pType;
      }
    } else {
      throw new AssertionError("Unexpected type " + pType);
    }
  }

  private Type getJCalculationType(JType pType) {
    return pType;
  }

  private UnaryConstraint createNot(Constraint pConstraint) {
    // We use ConstraintExpression as Constraints, so this should be possible
    return createNot((SymbolicExpression) pConstraint);
  }

  private UnaryConstraint createNot(SymbolicExpression pSymbolicExpression) {
    return (UnaryConstraint)
        expressionFactory.logicalNot(pSymbolicExpression, pSymbolicExpression.getType());
  }

  private Constraint createLess(SymbolicExpression pLeftOperand, SymbolicExpression pRightOperand,
      Type pExpressionType, Type pCalculationType) {

    return (Constraint)expressionFactory.lessThan(pLeftOperand, pRightOperand, pExpressionType, pCalculationType);
  }

  private Constraint createLessOrEqual(SymbolicExpression pLeftOperand, SymbolicExpression pRightOperand,
      Type pExpressionType, Type pCalculationType) {

    return (Constraint)expressionFactory.lessThanOrEqual(pLeftOperand, pRightOperand, pExpressionType,
        pCalculationType);
  }

  private Constraint createNotEqual(SymbolicExpression pLeftOperand, SymbolicExpression pRightOperand,
      Type pExpressionType, Type pCalculationType) {

    return (Constraint)expressionFactory.notEqual(pLeftOperand, pRightOperand, pExpressionType, pCalculationType);
  }

  private Constraint createEqual(SymbolicExpression pLeftOperand, SymbolicExpression pRightOperand,
      Type pExpressionType, Type pCalculationType) {

    return expressionFactory.equal(pLeftOperand, pRightOperand, pExpressionType, pCalculationType);
  }
}
