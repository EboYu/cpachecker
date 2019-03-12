/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class basic_block_vector {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected basic_block_vector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(basic_block_vector obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_basic_block_vector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(basic_block_vector obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public basic_block_vector() throws result {
    this(csJNI.new_basic_block_vector(), true);
  }

  public long size() throws result {
    return csJNI.basic_block_vector_size(swigCPtr, this);
  }

  public long capacity() throws result {
    return csJNI.basic_block_vector_capacity(swigCPtr, this);
  }

  public void reserve(long n) throws result {
    csJNI.basic_block_vector_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() throws result {
    return csJNI.basic_block_vector_isEmpty(swigCPtr, this);
  }

  public void clear() throws result {
    csJNI.basic_block_vector_clear(swigCPtr, this);
  }

  public void add(basic_block x) throws result {
    csJNI.basic_block_vector_add(swigCPtr, this, basic_block.getCPtr(x), x);
  }

  public basic_block get(int i) throws result {
    return new basic_block(csJNI.basic_block_vector_get(swigCPtr, this, i), false);
  }

  public void set(int i, basic_block val) throws result {
    csJNI.basic_block_vector_set(swigCPtr, this, i, basic_block.getCPtr(val), val);
  }

}