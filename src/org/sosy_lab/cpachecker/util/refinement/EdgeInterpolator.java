/*
 * CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2015  Dirk Beyer
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
package org.sosy_lab.cpachecker.util.refinement;

import java.util.Deque;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cpa.arg.path.ARGPath;
import org.sosy_lab.cpachecker.cpa.arg.path.PathPosition;
import org.sosy_lab.cpachecker.exceptions.CPAException;

/** Classes implementing this interface are able to derive interpolants from edges. */
public interface EdgeInterpolator<S extends ForgetfulState<?>, I extends Interpolant<S, I>> {

  I deriveInterpolant(
      ARGPath errorPath,
      CFAEdge currentEdge,
      Deque<S> callstack,
      PathPosition offset,
      I inputInterpolant
  ) throws CPAException, InterruptedException;

  int getNumberOfInterpolationQueries();
}
