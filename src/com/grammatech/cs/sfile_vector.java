/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class sfile_vector {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected sfile_vector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(sfile_vector obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_sfile_vector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(sfile_vector obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public sfile_vector() throws result {
    this(csJNI.new_sfile_vector(), true);
  }

  public long size() throws result {
    return csJNI.sfile_vector_size(swigCPtr, this);
  }

  public long capacity() throws result {
    return csJNI.sfile_vector_capacity(swigCPtr, this);
  }

  public void reserve(long n) throws result {
    csJNI.sfile_vector_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() throws result {
    return csJNI.sfile_vector_isEmpty(swigCPtr, this);
  }

  public void clear() throws result {
    csJNI.sfile_vector_clear(swigCPtr, this);
  }

  public void add(sfile x) throws result {
    csJNI.sfile_vector_add(swigCPtr, this, sfile.getCPtr(x), x);
  }

  public sfile get(int i) throws result {
    return new sfile(csJNI.sfile_vector_get(swigCPtr, this, i), false);
  }

  public void set(int i, sfile val) throws result {
    csJNI.sfile_vector_set(swigCPtr, this, i, sfile.getCPtr(val), val);
  }

}