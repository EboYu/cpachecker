/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class basic_block implements Comparable<basic_block> {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected basic_block(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(basic_block obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_basic_block(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(basic_block obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public int compareTo(basic_block other) {
    return csJNI.basic_block_compareTo(swigCPtr, this, basic_block.getCPtr(other), other);
  }

  public int hashCode() {
    return csJNI.basic_block_hashCode(swigCPtr, this);
  }

  public boolean equals(basic_block other) throws result {
    return csJNI.basic_block_equals(swigCPtr, this, basic_block.getCPtr(other), other);
  }

  public point first_point() throws result {
    return new point(csJNI.basic_block_first_point(swigCPtr, this), true);
  }

  public point last_point() throws result {
    return new point(csJNI.basic_block_last_point(swigCPtr, this), true);
  }

  public point_vector points() throws result {
    return new point_vector(csJNI.basic_block_points(swigCPtr, this), true);
  }

  public basic_block_cfg_edge_set predecessors() throws result {
    return new basic_block_cfg_edge_set(csJNI.basic_block_predecessors(swigCPtr, this), true);
  }

  public basic_block_cfg_edge_set successors() throws result {
    return new basic_block_cfg_edge_set(csJNI.basic_block_successors(swigCPtr, this), true);
  }

  public String as_string() throws result {
    return csJNI.basic_block_as_string(swigCPtr, this);
  }

  public String toString() {
    return csJNI.basic_block_toString(swigCPtr, this);
  }

}
