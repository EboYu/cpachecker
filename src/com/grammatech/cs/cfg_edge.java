/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class cfg_edge {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected cfg_edge(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(cfg_edge obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_cfg_edge(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(cfg_edge obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public cfg_edge(point first, edge_label second) throws result {
    this(csJNI.new_cfg_edge__SWIG_0(point.getCPtr(first), first, edge_label.getCPtr(second), second), true);
  }

  public cfg_edge(cfg_edge p) throws result {
    this(csJNI.new_cfg_edge__SWIG_1(cfg_edge.getCPtr(p), p), true);
  }

  public point get_first() throws result {
    return new point(csJNI.cfg_edge_get_first(swigCPtr, this), false);
  }

  public edge_label get_second() throws result {
    return new edge_label(csJNI.cfg_edge_get_second(swigCPtr, this), false);
  }

}