/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class labeled_pdg_edge {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected labeled_pdg_edge(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(labeled_pdg_edge obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_labeled_pdg_edge(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(labeled_pdg_edge obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public labeled_pdg_edge(point first, pdg_edge_label second) throws result {
    this(csJNI.new_labeled_pdg_edge__SWIG_0(point.getCPtr(first), first, pdg_edge_label.getCPtr(second), second), true);
  }

  public labeled_pdg_edge(labeled_pdg_edge p) throws result {
    this(csJNI.new_labeled_pdg_edge__SWIG_1(labeled_pdg_edge.getCPtr(p), p), true);
  }

  public point get_first() throws result {
    return new point(csJNI.labeled_pdg_edge_get_first(swigCPtr, this), false);
  }

  public pdg_edge_label get_second() throws result {
    return new pdg_edge_label(csJNI.labeled_pdg_edge_get_second(swigCPtr, this), false);
  }

}