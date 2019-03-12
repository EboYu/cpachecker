/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class procedure_metric {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected procedure_metric(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(procedure_metric obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_procedure_metric(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(procedure_metric obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public int compareTo(procedure_metric other) {
    return csJNI.procedure_metric_compareTo(swigCPtr, this, procedure_metric.getCPtr(other), other);
  }

  public int hashCode() {
    return csJNI.procedure_metric_hashCode(swigCPtr, this);
  }

  public boolean equals(procedure_metric other) throws result {
    return csJNI.procedure_metric_equals(swigCPtr, this, procedure_metric.getCPtr(other), other);
  }

  public String as_string() throws result {
    return csJNI.procedure_metric_as_string(swigCPtr, this);
  }

  public String toString() {
    return csJNI.procedure_metric_toString(swigCPtr, this);
  }

  public String tag() throws result {
    return csJNI.procedure_metric_tag(swigCPtr, this);
  }

  public String description() throws result {
    return csJNI.procedure_metric_description(swigCPtr, this);
  }

  public double value(procedure elt) throws result {
    return csJNI.procedure_metric_value(swigCPtr, this, procedure.getCPtr(elt), elt);
  }

  public metric_dependency_result get_dependencies() throws result {
    return new metric_dependency_result(csJNI.procedure_metric_get_dependencies(swigCPtr, this), true);
  }

}