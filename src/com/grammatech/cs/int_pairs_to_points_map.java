/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class int_pairs_to_points_map {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected int_pairs_to_points_map(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(int_pairs_to_points_map obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_int_pairs_to_points_map(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(int_pairs_to_points_map obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public int_pairs_to_points_map(compunit cu) throws result {
    this(csJNI.new_int_pairs_to_points_map(compunit.getCPtr(cu), cu), true);
  }

  public int hashCode() {
    return csJNI.int_pairs_to_points_map_hashCode(swigCPtr, this);
  }

  public point_set get(int_pair i) throws result {
    return new point_set(csJNI.int_pairs_to_points_map_get(swigCPtr, this, int_pair.getCPtr(i), i), true);
  }

  public boolean contains(int_pair i) throws result {
    return csJNI.int_pairs_to_points_map_contains(swigCPtr, this, int_pair.getCPtr(i), i);
  }

  public String toString() {
    return csJNI.int_pairs_to_points_map_toString(swigCPtr, this);
  }

  public String as_string() throws result {
    return csJNI.int_pairs_to_points_map_as_string(swigCPtr, this);
  }

  public boolean equals(int_pairs_to_points_map b) throws result {
    return csJNI.int_pairs_to_points_map_equals(swigCPtr, this, int_pairs_to_points_map.getCPtr(b), b);
  }

}