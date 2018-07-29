/*
 * CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2018  Dirk Beyer
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
package org.sosy_lab.cpachecker.util.predicates.pathformula.jstoformula;

import static org.sosy_lab.cpachecker.util.predicates.pathformula.jstoformula.Types.BOOLEAN_TYPE;
import static org.sosy_lab.cpachecker.util.predicates.pathformula.jstoformula.Types.FUNCTION_TYPE;
import static org.sosy_lab.cpachecker.util.predicates.pathformula.jstoformula.Types.JS_TYPE_TYPE;
import static org.sosy_lab.cpachecker.util.predicates.pathformula.jstoformula.Types.NUMBER_TYPE;
import static org.sosy_lab.cpachecker.util.predicates.pathformula.jstoformula.Types.VARIABLE_TYPE;

import org.sosy_lab.cpachecker.util.predicates.smt.FunctionFormulaManagerView;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.FloatingPointFormula;
import org.sosy_lab.java_smt.api.FunctionDeclaration;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;

class TypedValues {
  private final FunctionDeclaration<IntegerFormula> typeofDeclaration;
  private final FunctionFormulaManagerView ffmgr;
  private final FunctionDeclaration<BooleanFormula> booleanValueDeclaration;
  private final FunctionDeclaration<FloatingPointFormula> numberValueDeclaration;
  private final FunctionDeclaration<IntegerFormula> functionValueDeclaration;
  private final FunctionDeclaration<IntegerFormula> stringValueDeclaration;

  TypedValues(final FunctionFormulaManagerView pFfmgr) {
    ffmgr = pFfmgr;
    typeofDeclaration = pFfmgr.declareUF("typeof", JS_TYPE_TYPE, VARIABLE_TYPE);
    booleanValueDeclaration = pFfmgr.declareUF("booleanValue", BOOLEAN_TYPE, VARIABLE_TYPE);
    numberValueDeclaration = pFfmgr.declareUF("numberValue", NUMBER_TYPE, VARIABLE_TYPE);
    functionValueDeclaration = pFfmgr.declareUF("functionValue", FUNCTION_TYPE, VARIABLE_TYPE);
    stringValueDeclaration = pFfmgr.declareUF("stringValue", FUNCTION_TYPE, VARIABLE_TYPE);
  }

  IntegerFormula typeof(final IntegerFormula pVariable) {
    return ffmgr.callUF(typeofDeclaration, pVariable);
  }

  BooleanFormula booleanValue(final IntegerFormula pVariable) {
    return ffmgr.callUF(booleanValueDeclaration, pVariable);
  }

  FloatingPointFormula numberValue(final IntegerFormula pVariable) {
    return ffmgr.callUF(numberValueDeclaration, pVariable);
  }

  IntegerFormula functionValue(final IntegerFormula pVariable) {
    return ffmgr.callUF(functionValueDeclaration, pVariable);
  }

  IntegerFormula stringValue(final IntegerFormula pVariable) {
    return ffmgr.callUF(stringValueDeclaration, pVariable);
  }
}
