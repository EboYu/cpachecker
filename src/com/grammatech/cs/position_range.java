/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class position_range {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected position_range(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(position_range obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_position_range(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(position_range obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public position_range(int_pair first, int_pair second) throws result {
    this(csJNI.new_position_range__SWIG_0(int_pair.getCPtr(first), first, int_pair.getCPtr(second), second), true);
  }

  public position_range(position_range p) throws result {
    this(csJNI.new_position_range__SWIG_1(position_range.getCPtr(p), p), true);
  }

  public int_pair get_first() throws result {
    return new int_pair(csJNI.position_range_get_first(swigCPtr, this), false);
  }

  public int_pair get_second() throws result {
    return new int_pair(csJNI.position_range_get_second(swigCPtr, this), false);
  }

}