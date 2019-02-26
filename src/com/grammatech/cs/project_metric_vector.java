/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class project_metric_vector {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected project_metric_vector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(project_metric_vector obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_project_metric_vector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(project_metric_vector obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public project_metric_vector() throws result {
    this(csJNI.new_project_metric_vector(), true);
  }

  public long size() throws result {
    return csJNI.project_metric_vector_size(swigCPtr, this);
  }

  public long capacity() throws result {
    return csJNI.project_metric_vector_capacity(swigCPtr, this);
  }

  public void reserve(long n) throws result {
    csJNI.project_metric_vector_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() throws result {
    return csJNI.project_metric_vector_isEmpty(swigCPtr, this);
  }

  public void clear() throws result {
    csJNI.project_metric_vector_clear(swigCPtr, this);
  }

  public void add(project_metric x) throws result {
    csJNI.project_metric_vector_add(swigCPtr, this, project_metric.getCPtr(x), x);
  }

  public project_metric get(int i) throws result {
    return new project_metric(csJNI.project_metric_vector_get(swigCPtr, this, i), false);
  }

  public void set(int i, project_metric val) throws result {
    csJNI.project_metric_vector_set(swigCPtr, this, i, project_metric.getCPtr(val), val);
  }

}
