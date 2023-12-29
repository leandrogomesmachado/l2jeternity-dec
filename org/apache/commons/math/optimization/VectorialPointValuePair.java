package org.apache.commons.math.optimization;

import java.io.Serializable;

public class VectorialPointValuePair implements Serializable {
   private static final long serialVersionUID = 1003888396256744753L;
   private final double[] point;
   private final double[] value;

   public VectorialPointValuePair(double[] point, double[] value) {
      this.point = point == null ? null : (double[])point.clone();
      this.value = value == null ? null : (double[])value.clone();
   }

   public VectorialPointValuePair(double[] point, double[] value, boolean copyArray) {
      this.point = copyArray ? (point == null ? null : (double[])point.clone()) : point;
      this.value = copyArray ? (value == null ? null : (double[])value.clone()) : value;
   }

   public double[] getPoint() {
      return this.point == null ? null : (double[])this.point.clone();
   }

   public double[] getPointRef() {
      return this.point;
   }

   public double[] getValue() {
      return this.value == null ? null : (double[])this.value.clone();
   }

   public double[] getValueRef() {
      return this.value;
   }
}
