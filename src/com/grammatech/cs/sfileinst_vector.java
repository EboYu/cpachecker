/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class sfileinst_vector {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected sfileinst_vector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(sfileinst_vector obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_sfileinst_vector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(sfileinst_vector obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public sfileinst_vector() throws result {
    this(csJNI.new_sfileinst_vector(), true);
  }

  public long size() throws result {
    return csJNI.sfileinst_vector_size(swigCPtr, this);
  }

  public long capacity() throws result {
    return csJNI.sfileinst_vector_capacity(swigCPtr, this);
  }

  public void reserve(long n) throws result {
    csJNI.sfileinst_vector_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() throws result {
    return csJNI.sfileinst_vector_isEmpty(swigCPtr, this);
  }

  public void clear() throws result {
    csJNI.sfileinst_vector_clear(swigCPtr, this);
  }

  public void add(sfileinst x) throws result {
    csJNI.sfileinst_vector_add(swigCPtr, this, sfileinst.getCPtr(x), x);
  }

  public sfileinst get(int i) throws result {
    return new sfileinst(csJNI.sfileinst_vector_get(swigCPtr, this, i), false);
  }

  public void set(int i, sfileinst val) throws result {
    csJNI.sfileinst_vector_set(swigCPtr, this, i, sfileinst.getCPtr(val), val);
  }

}
