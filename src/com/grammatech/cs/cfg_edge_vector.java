/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class cfg_edge_vector {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected cfg_edge_vector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(cfg_edge_vector obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_cfg_edge_vector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(cfg_edge_vector obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public cfg_edge_vector() throws result {
    this(csJNI.new_cfg_edge_vector(), true);
  }

  public long size() throws result {
    return csJNI.cfg_edge_vector_size(swigCPtr, this);
  }

  public long capacity() throws result {
    return csJNI.cfg_edge_vector_capacity(swigCPtr, this);
  }

  public void reserve(long n) throws result {
    csJNI.cfg_edge_vector_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() throws result {
    return csJNI.cfg_edge_vector_isEmpty(swigCPtr, this);
  }

  public void clear() throws result {
    csJNI.cfg_edge_vector_clear(swigCPtr, this);
  }

  public void add(cfg_edge x) throws result {
    csJNI.cfg_edge_vector_add(swigCPtr, this, cfg_edge.getCPtr(x), x);
  }

  public cfg_edge get(int i) throws result {
    return new cfg_edge(csJNI.cfg_edge_vector_get(swigCPtr, this, i), false);
  }

  public void set(int i, cfg_edge val) throws result {
    csJNI.cfg_edge_vector_set(swigCPtr, this, i, cfg_edge.getCPtr(val), val);
  }

}
