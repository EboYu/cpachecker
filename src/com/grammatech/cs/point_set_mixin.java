/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.grammatech.cs;

public class point_set_mixin {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected point_set_mixin(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(point_set_mixin obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csJNI.delete_point_set_mixin(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long getCPtrAndDisown(point_set_mixin obj) {
    if (obj != null) obj.swigCMemOwn= false;
    return getCPtr(obj);
  }

  public int_pair_set to_int_pair_set_in_compunit(compunit u) throws result {
    return new int_pair_set(csJNI.point_set_mixin_to_int_pair_set_in_compunit(swigCPtr, this, compunit.getCPtr(u), u), true);
  }

  public long intersect_size(point_set other) throws result {
    return csJNI.point_set_mixin_intersect_size(swigCPtr, this, point_set.getCPtr(other), other);
  }

  public boolean cycle(pdg_edge_kind k) throws result {
    return csJNI.point_set_mixin_cycle(swigCPtr, this, pdg_edge_kind.getCPtr(k), k);
  }

  public compunit_points_vector categorize() throws result {
    return new compunit_points_vector(csJNI.point_set_mixin_categorize(swigCPtr, this), true);
  }

  public compunit_points_pair_vector categorize_by_file() throws result {
    return new compunit_points_pair_vector(csJNI.point_set_mixin_categorize_by_file(swigCPtr, this), true);
  }

  public boolean intersects_procedure(procedure p) throws result {
    return csJNI.point_set_mixin_intersects_procedure(swigCPtr, this, procedure.getCPtr(p), p);
  }

  public procedure_vector procedures() throws result {
    return new procedure_vector(csJNI.point_set_mixin_procedures(swigCPtr, this), true);
  }

  public point_vector sort() throws result {
    return new point_vector(csJNI.point_set_mixin_sort__SWIG_0(swigCPtr, this), true);
  }

  public point_vector sort(String file_name) throws result {
    return new point_vector(csJNI.point_set_mixin_sort__SWIG_1(swigCPtr, this, file_name), true);
  }

  public point_set predecessors(depfilter f, long threshold) throws result {
    return new point_set(csJNI.point_set_mixin_predecessors__SWIG_0(swigCPtr, this, depfilter.getCPtr(f), f, threshold), true);
  }

  public point_set predecessors(depfilter f) throws result {
    return new point_set(csJNI.point_set_mixin_predecessors__SWIG_1(swigCPtr, this, depfilter.getCPtr(f), f), true);
  }

  public point_set successors(depfilter f, long threshold) throws result {
    return new point_set(csJNI.point_set_mixin_successors__SWIG_0(swigCPtr, this, depfilter.getCPtr(f), f, threshold), true);
  }

  public point_set successors(depfilter f) throws result {
    return new point_set(csJNI.point_set_mixin_successors__SWIG_1(swigCPtr, this, depfilter.getCPtr(f), f), true);
  }

  public point_set predecessors(long threshold) throws result {
    return new point_set(csJNI.point_set_mixin_predecessors__SWIG_2(swigCPtr, this, threshold), true);
  }

  public point_set predecessors() throws result {
    return new point_set(csJNI.point_set_mixin_predecessors__SWIG_3(swigCPtr, this), true);
  }

  public point_set successors(long threshold) throws result {
    return new point_set(csJNI.point_set_mixin_successors__SWIG_2(swigCPtr, this, threshold), true);
  }

  public point_set successors() throws result {
    return new point_set(csJNI.point_set_mixin_successors__SWIG_3(swigCPtr, this), true);
  }

  public point_set backward_slice(pdg_edge_kind k) throws result {
    return new point_set(csJNI.point_set_mixin_backward_slice__SWIG_0(swigCPtr, this, pdg_edge_kind.getCPtr(k), k), true);
  }

  public point_set backward_slice() throws result {
    return new point_set(csJNI.point_set_mixin_backward_slice__SWIG_1(swigCPtr, this), true);
  }

  public point_set forward_slice(pdg_edge_kind k) throws result {
    return new point_set(csJNI.point_set_mixin_forward_slice__SWIG_0(swigCPtr, this, pdg_edge_kind.getCPtr(k), k), true);
  }

  public point_set forward_slice() throws result {
    return new point_set(csJNI.point_set_mixin_forward_slice__SWIG_1(swigCPtr, this), true);
  }

  public point_set backward_slice_out(pdg_edge_kind k) throws result {
    return new point_set(csJNI.point_set_mixin_backward_slice_out__SWIG_0(swigCPtr, this, pdg_edge_kind.getCPtr(k), k), true);
  }

  public point_set backward_slice_out() throws result {
    return new point_set(csJNI.point_set_mixin_backward_slice_out__SWIG_1(swigCPtr, this), true);
  }

  public point_set forward_slice_out(pdg_edge_kind k) throws result {
    return new point_set(csJNI.point_set_mixin_forward_slice_out__SWIG_0(swigCPtr, this, pdg_edge_kind.getCPtr(k), k), true);
  }

  public point_set forward_slice_out() throws result {
    return new point_set(csJNI.point_set_mixin_forward_slice_out__SWIG_1(swigCPtr, this), true);
  }

  public point_set backward_slice_in(pdg_edge_kind k) throws result {
    return new point_set(csJNI.point_set_mixin_backward_slice_in__SWIG_0(swigCPtr, this, pdg_edge_kind.getCPtr(k), k), true);
  }

  public point_set backward_slice_in() throws result {
    return new point_set(csJNI.point_set_mixin_backward_slice_in__SWIG_1(swigCPtr, this), true);
  }

  public point_set forward_slice_in(pdg_edge_kind k) throws result {
    return new point_set(csJNI.point_set_mixin_forward_slice_in__SWIG_0(swigCPtr, this, pdg_edge_kind.getCPtr(k), k), true);
  }

  public point_set forward_slice_in() throws result {
    return new point_set(csJNI.point_set_mixin_forward_slice_in__SWIG_1(swigCPtr, this), true);
  }

  public point_set chop(point_set targets, pdg_edge_kind k) throws result {
    return new point_set(csJNI.point_set_mixin_chop__SWIG_0(swigCPtr, this, point_set.getCPtr(targets), targets, pdg_edge_kind.getCPtr(k), k), true);
  }

  public point_set chop(point_set targets) throws result {
    return new point_set(csJNI.point_set_mixin_chop__SWIG_1(swigCPtr, this, point_set.getCPtr(targets), targets), true);
  }

  public point_set truncated_chop(point_set targets, pdg_edge_kind k) throws result {
    return new point_set(csJNI.point_set_mixin_truncated_chop__SWIG_0(swigCPtr, this, point_set.getCPtr(targets), targets, pdg_edge_kind.getCPtr(k), k), true);
  }

  public point_set truncated_chop(point_set targets) throws result {
    return new point_set(csJNI.point_set_mixin_truncated_chop__SWIG_1(swigCPtr, this, point_set.getCPtr(targets), targets), true);
  }

  public point_set fast_chop(point_set targets, pdg_edge_kind k) throws result {
    return new point_set(csJNI.point_set_mixin_fast_chop__SWIG_0(swigCPtr, this, point_set.getCPtr(targets), targets, pdg_edge_kind.getCPtr(k), k), true);
  }

  public point_set fast_chop(point_set targets) throws result {
    return new point_set(csJNI.point_set_mixin_fast_chop__SWIG_1(swigCPtr, this, point_set.getCPtr(targets), targets), true);
  }

  public point_set var_predecessors(symbol_set variables, depfilter f, long threshold) throws result {
    return new point_set(csJNI.point_set_mixin_var_predecessors__SWIG_0(swigCPtr, this, symbol_set.getCPtr(variables), variables, depfilter.getCPtr(f), f, threshold), true);
  }

  public point_set var_predecessors(symbol_set variables, depfilter f) throws result {
    return new point_set(csJNI.point_set_mixin_var_predecessors__SWIG_1(swigCPtr, this, symbol_set.getCPtr(variables), variables, depfilter.getCPtr(f), f), true);
  }

  public point_set var_successors(symbol_set variables, depfilter f, long threshold) throws result {
    return new point_set(csJNI.point_set_mixin_var_successors__SWIG_0(swigCPtr, this, symbol_set.getCPtr(variables), variables, depfilter.getCPtr(f), f, threshold), true);
  }

  public point_set var_successors(symbol_set variables, depfilter f) throws result {
    return new point_set(csJNI.point_set_mixin_var_successors__SWIG_1(swigCPtr, this, symbol_set.getCPtr(variables), variables, depfilter.getCPtr(f), f), true);
  }

  public point_set var_backward_slice(symbol_set variables, pdg_edge_kind k) throws result {
    return new point_set(csJNI.point_set_mixin_var_backward_slice__SWIG_0(swigCPtr, this, symbol_set.getCPtr(variables), variables, pdg_edge_kind.getCPtr(k), k), true);
  }

  public point_set var_backward_slice(symbol_set variables) throws result {
    return new point_set(csJNI.point_set_mixin_var_backward_slice__SWIG_1(swigCPtr, this, symbol_set.getCPtr(variables), variables), true);
  }

  public point_set var_forward_slice(symbol_set variables, pdg_edge_kind k) throws result {
    return new point_set(csJNI.point_set_mixin_var_forward_slice__SWIG_0(swigCPtr, this, symbol_set.getCPtr(variables), variables, pdg_edge_kind.getCPtr(k), k), true);
  }

  public point_set var_forward_slice(symbol_set variables) throws result {
    return new point_set(csJNI.point_set_mixin_var_forward_slice__SWIG_1(swigCPtr, this, symbol_set.getCPtr(variables), variables), true);
  }

  public point_set var_chop(symbol_set variables, point_set targets, symbol_set target_variables, pdg_edge_kind k) throws result {
    return new point_set(csJNI.point_set_mixin_var_chop__SWIG_0(swigCPtr, this, symbol_set.getCPtr(variables), variables, point_set.getCPtr(targets), targets, symbol_set.getCPtr(target_variables), target_variables, pdg_edge_kind.getCPtr(k), k), true);
  }

  public point_set var_chop(symbol_set variables, point_set targets, symbol_set target_variables) throws result {
    return new point_set(csJNI.point_set_mixin_var_chop__SWIG_1(swigCPtr, this, symbol_set.getCPtr(variables), variables, point_set.getCPtr(targets), targets, symbol_set.getCPtr(target_variables), target_variables), true);
  }

  public point_set var_truncated_chop(symbol_set variables, point_set targets, symbol_set target_variables, pdg_edge_kind k) throws result {
    return new point_set(csJNI.point_set_mixin_var_truncated_chop__SWIG_0(swigCPtr, this, symbol_set.getCPtr(variables), variables, point_set.getCPtr(targets), targets, symbol_set.getCPtr(target_variables), target_variables, pdg_edge_kind.getCPtr(k), k), true);
  }

  public point_set var_truncated_chop(symbol_set variables, point_set targets, symbol_set target_variables) throws result {
    return new point_set(csJNI.point_set_mixin_var_truncated_chop__SWIG_1(swigCPtr, this, symbol_set.getCPtr(variables), variables, point_set.getCPtr(targets), targets, symbol_set.getCPtr(target_variables), target_variables), true);
  }

  public point_set var_fast_chop(symbol_set variables, point_set targets, symbol_set target_variables, pdg_edge_kind k) throws result {
    return new point_set(csJNI.point_set_mixin_var_fast_chop__SWIG_0(swigCPtr, this, symbol_set.getCPtr(variables), variables, point_set.getCPtr(targets), targets, symbol_set.getCPtr(target_variables), target_variables, pdg_edge_kind.getCPtr(k), k), true);
  }

  public point_set var_fast_chop(symbol_set variables, point_set targets, symbol_set target_variables) throws result {
    return new point_set(csJNI.point_set_mixin_var_fast_chop__SWIG_1(swigCPtr, this, symbol_set.getCPtr(variables), variables, point_set.getCPtr(targets), targets, symbol_set.getCPtr(target_variables), target_variables), true);
  }

  public filtered_predsucc_return_pair filtered_predecessors(predsucc_filter filter, pdg_edge_kind kind, long threshold, boolean once) throws java.lang.Exception {
    return new filtered_predsucc_return_pair(csJNI.point_set_mixin_filtered_predecessors__SWIG_0(swigCPtr, this, predsucc_filter.getCPtr(filter), filter, pdg_edge_kind.getCPtr(kind), kind, threshold, once), true);
  }

  public filtered_predsucc_return_pair filtered_predecessors(predsucc_filter filter, pdg_edge_kind kind, long threshold) throws java.lang.Exception {
    return new filtered_predsucc_return_pair(csJNI.point_set_mixin_filtered_predecessors__SWIG_1(swigCPtr, this, predsucc_filter.getCPtr(filter), filter, pdg_edge_kind.getCPtr(kind), kind, threshold), true);
  }

  public filtered_predsucc_return_pair filtered_predecessors(predsucc_filter filter, pdg_edge_kind kind) throws java.lang.Exception {
    return new filtered_predsucc_return_pair(csJNI.point_set_mixin_filtered_predecessors__SWIG_2(swigCPtr, this, predsucc_filter.getCPtr(filter), filter, pdg_edge_kind.getCPtr(kind), kind), true);
  }

  public filtered_predsucc_return_pair filtered_predecessors(predsucc_filter filter) throws java.lang.Exception {
    return new filtered_predsucc_return_pair(csJNI.point_set_mixin_filtered_predecessors__SWIG_3(swigCPtr, this, predsucc_filter.getCPtr(filter), filter), true);
  }

  public filtered_predsucc_return_pair filtered_successors(predsucc_filter filter, pdg_edge_kind kind, long threshold, boolean once) throws java.lang.Exception {
    return new filtered_predsucc_return_pair(csJNI.point_set_mixin_filtered_successors__SWIG_0(swigCPtr, this, predsucc_filter.getCPtr(filter), filter, pdg_edge_kind.getCPtr(kind), kind, threshold, once), true);
  }

  public filtered_predsucc_return_pair filtered_successors(predsucc_filter filter, pdg_edge_kind kind, long threshold) throws java.lang.Exception {
    return new filtered_predsucc_return_pair(csJNI.point_set_mixin_filtered_successors__SWIG_1(swigCPtr, this, predsucc_filter.getCPtr(filter), filter, pdg_edge_kind.getCPtr(kind), kind, threshold), true);
  }

  public filtered_predsucc_return_pair filtered_successors(predsucc_filter filter, pdg_edge_kind kind) throws java.lang.Exception {
    return new filtered_predsucc_return_pair(csJNI.point_set_mixin_filtered_successors__SWIG_2(swigCPtr, this, predsucc_filter.getCPtr(filter), filter, pdg_edge_kind.getCPtr(kind), kind), true);
  }

  public filtered_predsucc_return_pair filtered_successors(predsucc_filter filter) throws java.lang.Exception {
    return new filtered_predsucc_return_pair(csJNI.point_set_mixin_filtered_successors__SWIG_3(swigCPtr, this, predsucc_filter.getCPtr(filter), filter), true);
  }

}