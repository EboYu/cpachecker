/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class depfilter_procedure_directive implements Comparable<depfilter_procedure_directive> {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected depfilter_procedure_directive(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(depfilter_procedure_directive obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_depfilter_procedure_directive(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(depfilter_procedure_directive obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public depfilter_procedure_directive(long _inner) throws result {
    this(csJNI.new_depfilter_procedure_directive(_inner), true);
  }

  public int compareTo(depfilter_procedure_directive other) {
    return csJNI.depfilter_procedure_directive_compareTo(swigCPtr, this, depfilter_procedure_directive.getCPtr(other), other);
  }

  public boolean equals(depfilter_procedure_directive other) throws result {
    return csJNI.depfilter_procedure_directive_equals(swigCPtr, this, depfilter_procedure_directive.getCPtr(other), other);
  }

  public int hashCode() {
    return csJNI.depfilter_procedure_directive_hashCode(swigCPtr, this);
  }

  public String as_string() throws result {
    return csJNI.depfilter_procedure_directive_as_string(swigCPtr, this);
  }

  public String toString() {
    return csJNI.depfilter_procedure_directive_toString(swigCPtr, this);
  }

  public String name() throws result {
    return csJNI.depfilter_procedure_directive_name(swigCPtr, this);
  }

  public static depfilter_procedure_directive getDIRECTIVE_SHOW() throws result {
    long cPtr = csJNI.depfilter_procedure_directive_DIRECTIVE_SHOW_get();
    return (cPtr == 0) ? null : new depfilter_procedure_directive(cPtr, false);
  }

  public static depfilter_procedure_directive getDIRECTIVE_SKIP() throws result {
    long cPtr = csJNI.depfilter_procedure_directive_DIRECTIVE_SKIP_get();
    return (cPtr == 0) ? null : new depfilter_procedure_directive(cPtr, false);
  }

  public static depfilter_procedure_directive getDIRECTIVE_STOP() throws result {
    long cPtr = csJNI.depfilter_procedure_directive_DIRECTIVE_STOP_get();
    return (cPtr == 0) ? null : new depfilter_procedure_directive(cPtr, false);
  }

  public static depfilter_procedure_directive getDIRECTIVE_OPAQUE() throws result {
    long cPtr = csJNI.depfilter_procedure_directive_DIRECTIVE_OPAQUE_get();
    return (cPtr == 0) ? null : new depfilter_procedure_directive(cPtr, false);
  }

}