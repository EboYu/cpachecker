/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class sfileinst_line_pair {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected sfileinst_line_pair(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(sfileinst_line_pair obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_sfileinst_line_pair(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(sfileinst_line_pair obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public sfileinst_line_pair(sfileinst first, long second) throws result {
    this(csJNI.new_sfileinst_line_pair__SWIG_0(sfileinst.getCPtr(first), first, second), true);
  }

  public sfileinst_line_pair(sfileinst_line_pair p) throws result {
    this(csJNI.new_sfileinst_line_pair__SWIG_1(sfileinst_line_pair.getCPtr(p), p), true);
  }

  public sfileinst get_first() throws result {
    return new sfileinst(csJNI.sfileinst_line_pair_get_first(swigCPtr, this), false);
  }

  public long get_second() throws result {
    return csJNI.sfileinst_line_pair_get_second(swigCPtr, this);
  }

}