/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class xr_occurrence_iterator {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected xr_occurrence_iterator(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(xr_occurrence_iterator obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_xr_occurrence_iterator(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(xr_occurrence_iterator obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public boolean at_end() throws result {
    return csJNI.xr_occurrence_iterator_at_end(swigCPtr, this);
  }

  public xr_tuple current() throws result {
    return new xr_tuple(csJNI.xr_occurrence_iterator_current(swigCPtr, this), true);
  }

  public boolean equals(xr_occurrence_iterator other) throws result {
    return csJNI.xr_occurrence_iterator_equals(swigCPtr, this, xr_occurrence_iterator.getCPtr(other), other);
  }

  public void advance() throws result {
    csJNI.xr_occurrence_iterator_advance(swigCPtr, this);
  }

  public String as_string() throws result {
    return csJNI.xr_occurrence_iterator_as_string(swigCPtr, this);
  }

  public String toString() {
    return csJNI.xr_occurrence_iterator_toString(swigCPtr, this);
  }

}
