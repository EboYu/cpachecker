/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class labeled_pdg_edge_vector {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected labeled_pdg_edge_vector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(labeled_pdg_edge_vector obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_labeled_pdg_edge_vector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(labeled_pdg_edge_vector obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public labeled_pdg_edge_vector() throws result {
    this(csJNI.new_labeled_pdg_edge_vector(), true);
  }

  public long size() throws result {
    return csJNI.labeled_pdg_edge_vector_size(swigCPtr, this);
  }

  public long capacity() throws result {
    return csJNI.labeled_pdg_edge_vector_capacity(swigCPtr, this);
  }

  public void reserve(long n) throws result {
    csJNI.labeled_pdg_edge_vector_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() throws result {
    return csJNI.labeled_pdg_edge_vector_isEmpty(swigCPtr, this);
  }

  public void clear() throws result {
    csJNI.labeled_pdg_edge_vector_clear(swigCPtr, this);
  }

  public void add(labeled_pdg_edge x) throws result {
    csJNI.labeled_pdg_edge_vector_add(swigCPtr, this, labeled_pdg_edge.getCPtr(x), x);
  }

  public labeled_pdg_edge get(int i) throws result {
    return new labeled_pdg_edge(csJNI.labeled_pdg_edge_vector_get(swigCPtr, this, i), false);
  }

  public void set(int i, labeled_pdg_edge val) throws result {
    csJNI.labeled_pdg_edge_vector_set(swigCPtr, this, i, labeled_pdg_edge.getCPtr(val), val);
  }

}
