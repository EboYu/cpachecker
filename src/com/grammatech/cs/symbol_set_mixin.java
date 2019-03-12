/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class symbol_set_mixin {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected symbol_set_mixin(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(symbol_set_mixin obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_symbol_set_mixin(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(symbol_set_mixin obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public point_set used_points() throws result {
    return new point_set(csJNI.symbol_set_mixin_used_points(swigCPtr, this), true);
  }

  public point_set killed_points() throws result {
    return new point_set(csJNI.symbol_set_mixin_killed_points(swigCPtr, this), true);
  }

  public point_set cond_killed_points() throws result {
    return new point_set(csJNI.symbol_set_mixin_cond_killed_points(swigCPtr, this), true);
  }

  public point_set may_killed_points() throws result {
    return new point_set(csJNI.symbol_set_mixin_may_killed_points(swigCPtr, this), true);
  }

}