/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class exceptional_return {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected exceptional_return(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(exceptional_return obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_exceptional_return(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(exceptional_return obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public exceptional_return(point first, String second) throws result {
    this(csJNI.new_exceptional_return__SWIG_0(point.getCPtr(first), first, second), true);
  }

  public exceptional_return(exceptional_return p) throws result {
    this(csJNI.new_exceptional_return__SWIG_1(exceptional_return.getCPtr(p), p), true);
  }

  public point get_first() throws result {
    return new point(csJNI.exceptional_return_get_first(swigCPtr, this), false);
  }

  public String get_second() throws result {
    return csJNI.exceptional_return_get_second(swigCPtr, this);
  }

}
