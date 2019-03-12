/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class basic_block_set extends basic_block_set_mixin {
  private long swigCPtr;

  protected basic_block_set(long cPtr, boolean cMemoryOwn) {
    super(csJNI.basic_block_set_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(basic_block_set obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_basic_block_set(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  protected static long getCPtrAndDisown(basic_block_set obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public basic_block_set(basic_block_set other) throws result {
    this(csJNI.new_basic_block_set__SWIG_0(basic_block_set.getCPtr(other), other), true);
  }

  public set_kind kind() throws result {
    return new set_kind(csJNI.basic_block_set_kind(swigCPtr, this), true);
  }

  public basic_block_set_iterator begin() throws result {
    return new basic_block_set_iterator(csJNI.basic_block_set_begin(swigCPtr, this), true);
  }

  public basic_block_set_iterator end() throws result {
    return new basic_block_set_iterator(csJNI.basic_block_set_end(swigCPtr, this), true);
  }

  public basic_block_set_iterator cbegin() throws result {
    return new basic_block_set_iterator(csJNI.basic_block_set_cbegin(swigCPtr, this), true);
  }

  public basic_block_set_iterator cend() throws result {
    return new basic_block_set_iterator(csJNI.basic_block_set_cend(swigCPtr, this), true);
  }

  public boolean empty() throws result {
    return csJNI.basic_block_set_empty(swigCPtr, this);
  }

  public long size() throws result {
    return csJNI.basic_block_set_size(swigCPtr, this);
  }

  public long count(basic_block e) throws result {
    return csJNI.basic_block_set_count(swigCPtr, this, basic_block.getCPtr(e), e);
  }

  public basic_block_set_iterator find(basic_block e) throws result {
    return new basic_block_set_iterator(csJNI.basic_block_set_find(swigCPtr, this, basic_block.getCPtr(e), e), true);
  }

  public int hashCode() {
    return csJNI.basic_block_set_hashCode(swigCPtr, this);
  }

  public basic_block_vector to_vector() throws result {
    return new basic_block_vector(csJNI.basic_block_set_to_vector(swigCPtr, this), true);
  }

  public basic_block_set() throws result {
    this(csJNI.new_basic_block_set__SWIG_1(), true);
  }

  public basic_block_set(set_kind k) throws result {
    this(csJNI.new_basic_block_set__SWIG_2(set_kind.getCPtr(k), k), true);
  }

  public basic_block_set(basic_block_vector v) throws result {
    this(csJNI.new_basic_block_set__SWIG_3(basic_block_vector.getCPtr(v), v), true);
  }

  public boolean add(basic_block v) throws result {
    return csJNI.basic_block_set_add(swigCPtr, this, basic_block.getCPtr(v), v);
  }

  public long erase(basic_block v) throws result {
    return csJNI.basic_block_set_erase(swigCPtr, this, basic_block.getCPtr(v), v);
  }

  public void clear() throws result {
    csJNI.basic_block_set_clear(swigCPtr, this);
  }

  public basic_block_set union_(basic_block_set b) throws result {
    return new basic_block_set(csJNI.basic_block_set_union_(swigCPtr, this, basic_block_set.getCPtr(b), b), true);
  }

  public void union_p(basic_block_set b) throws result {
    csJNI.basic_block_set_union_p(swigCPtr, this, basic_block_set.getCPtr(b), b);
  }

  public basic_block_set intersect(basic_block_set b) throws result {
    return new basic_block_set(csJNI.basic_block_set_intersect(swigCPtr, this, basic_block_set.getCPtr(b), b), true);
  }

  public basic_block_set subtract(basic_block_set b) throws result {
    return new basic_block_set(csJNI.basic_block_set_subtract(swigCPtr, this, basic_block_set.getCPtr(b), b), true);
  }

  public String as_string() throws result {
    return csJNI.basic_block_set_as_string(swigCPtr, this);
  }

  public String toString() {
    return csJNI.basic_block_set_toString(swigCPtr, this);
  }

  public boolean equals(basic_block_set other) throws result {
    return csJNI.basic_block_set_equals(swigCPtr, this, basic_block_set.getCPtr(other), other);
  }

}