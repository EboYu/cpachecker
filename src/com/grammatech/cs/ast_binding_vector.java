/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class ast_binding_vector {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected ast_binding_vector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ast_binding_vector obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_ast_binding_vector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(ast_binding_vector obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public ast_binding_vector() throws result {
    this(csJNI.new_ast_binding_vector(), true);
  }

  public long size() throws result {
    return csJNI.ast_binding_vector_size(swigCPtr, this);
  }

  public long capacity() throws result {
    return csJNI.ast_binding_vector_capacity(swigCPtr, this);
  }

  public void reserve(long n) throws result {
    csJNI.ast_binding_vector_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() throws result {
    return csJNI.ast_binding_vector_isEmpty(swigCPtr, this);
  }

  public void clear() throws result {
    csJNI.ast_binding_vector_clear(swigCPtr, this);
  }

  public void add(string_ast_field_pair x) throws result {
    csJNI.ast_binding_vector_add(swigCPtr, this, string_ast_field_pair.getCPtr(x), x);
  }

  public string_ast_field_pair get(int i) throws result {
    return new string_ast_field_pair(csJNI.ast_binding_vector_get(swigCPtr, this, i), false);
  }

  public void set(int i, string_ast_field_pair val) throws result {
    csJNI.ast_binding_vector_set(swigCPtr, this, i, string_ast_field_pair.getCPtr(val), val);
  }

}