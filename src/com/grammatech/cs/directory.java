/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class directory implements Comparable<directory> {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected directory(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(directory obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_directory(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(directory obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public int compareTo(directory other) {
    return csJNI.directory_compareTo(swigCPtr, this, directory.getCPtr(other), other);
  }

  public int hashCode() {
    return csJNI.directory_hashCode(swigCPtr, this);
  }

  public boolean equals(directory other) throws result {
    return csJNI.directory_equals(swigCPtr, this, directory.getCPtr(other), other);
  }

  public directory_children_iterator children() throws result {
    return new directory_children_iterator(csJNI.directory_children(swigCPtr, this), true);
  }

  public directory_files_iterator files() throws result {
    return new directory_files_iterator(csJNI.directory_files(swigCPtr, this), true);
  }

  public directory parent() throws result {
    return new directory(csJNI.directory_parent(swigCPtr, this), true);
  }

  public long depth() throws result {
    return csJNI.directory_depth(swigCPtr, this);
  }

  public String name() throws result {
    return csJNI.directory_name(swigCPtr, this);
  }

  public String normalized_name() throws result {
    return csJNI.directory_normalized_name(swigCPtr, this);
  }

  public String as_string() throws result {
    return csJNI.directory_as_string(swigCPtr, this);
  }

  public String toString() {
    return csJNI.directory_toString(swigCPtr, this);
  }

}