/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class procedure_and_points {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected procedure_and_points(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(procedure_and_points obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_procedure_and_points(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(procedure_and_points obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public procedure_and_points(procedure first, point_set second) throws result {
    this(csJNI.new_procedure_and_points__SWIG_0(procedure.getCPtr(first), first, point_set.getCPtr(second), second), true);
  }

  public procedure_and_points(procedure_and_points p) throws result {
    this(csJNI.new_procedure_and_points__SWIG_1(procedure_and_points.getCPtr(p), p), true);
  }

  public procedure get_first() throws result {
    return new procedure(csJNI.procedure_and_points_get_first(swigCPtr, this), false);
  }

  public point_set get_second() throws result {
    return new point_set(csJNI.procedure_and_points_get_second(swigCPtr, this), false);
  }

}