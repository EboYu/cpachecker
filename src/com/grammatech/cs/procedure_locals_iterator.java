/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class procedure_locals_iterator {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected procedure_locals_iterator(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(procedure_locals_iterator obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_procedure_locals_iterator(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(procedure_locals_iterator obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public boolean at_end() throws result {
    return csJNI.procedure_locals_iterator_at_end(swigCPtr, this);
  }

  public symbol current() throws result {
    return new symbol(csJNI.procedure_locals_iterator_current(swigCPtr, this), true);
  }

  public boolean equals(procedure_locals_iterator other) throws result {
    return csJNI.procedure_locals_iterator_equals(swigCPtr, this, procedure_locals_iterator.getCPtr(other), other);
  }

  public void advance() throws result {
    csJNI.procedure_locals_iterator_advance(swigCPtr, this);
  }

  public String as_string() throws result {
    return csJNI.procedure_locals_iterator_as_string(swigCPtr, this);
  }

  public String toString() {
    return csJNI.procedure_locals_iterator_toString(swigCPtr, this);
  }

}