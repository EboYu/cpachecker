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
package org.sosy_lab.cpachecker.cpa.functionpointer;

import java.util.Collection;
import java.util.Collections;

import org.sosy_lab.cpachecker.core.interfaces.AbstractElement;
import org.sosy_lab.cpachecker.core.interfaces.Precision;
import org.sosy_lab.cpachecker.core.interfaces.StopOperator;
import org.sosy_lab.cpachecker.exceptions.CPAException;

class FunctionPointerStopOperator implements StopOperator {

  private final StopOperator wrappedStop;

  public FunctionPointerStopOperator(StopOperator pWrappedStop) {
    wrappedStop = pWrappedStop;
  }

  @Override
  public boolean stop(AbstractElement pElement,
      Collection<AbstractElement> pReached, Precision pPrecision) throws CPAException {

    FunctionPointerElement fpElement = (FunctionPointerElement)pElement;

    for (AbstractElement reachedElement : pReached) {
      FunctionPointerElement fpReachedElement = (FunctionPointerElement)reachedElement;
      if (stop(fpElement, fpReachedElement, pPrecision)) {
        return true;
      }
    }
    return false;

  }

  private boolean stop(FunctionPointerElement pElement, FunctionPointerElement pReachedElement, Precision pPrecision)
                                                      throws CPAException {

    if (!pElement.isLessOrEqualThan(pReachedElement)) {
      return false;
    }

    AbstractElement wrappedElement = pElement.getWrappedElement();
    AbstractElement wrappedReachedElement = pReachedElement.getWrappedElement();

    return wrappedStop.stop(wrappedElement, Collections.singleton(wrappedReachedElement), pPrecision);
  }
}
