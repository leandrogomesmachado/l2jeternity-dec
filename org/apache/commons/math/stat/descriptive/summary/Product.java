package org.apache.commons.math.stat.descriptive.summary;

import java.io.Serializable;
import org.apache.commons.math.stat.descriptive.AbstractStorelessUnivariateStatistic;
import org.apache.commons.math.stat.descriptive.WeightedEvaluation;
import org.apache.commons.math.util.FastMath;

public class Product extends AbstractStorelessUnivariateStatistic implements Serializable, WeightedEvaluation {
   private static final long serialVersionUID = 2824226005990582538L;
   private long n;
   private double value;

   public Product() {
      this.n = 0L;
      this.value = Double.NaN;
   }

   public Product(Product original) {
      copy(original, this);
   }

   @Override
   public void increment(double d) {
      if (this.n == 0L) {
         this.value = d;
      } else {
         this.value *= d;
      }

      ++this.n;
   }

   @Override
   public double getResult() {
      return this.value;
   }

   @Override
   public long getN() {
      return this.n;
   }

   @Override
   public void clear() {
      this.value = Double.NaN;
      this.n = 0L;
   }

   @Override
   public double evaluate(double[] values, int begin, int length) {
      double product = Double.NaN;
      if (this.test(values, begin, length)) {
         product = 1.0;

         for(int i = begin; i < begin + length; ++i) {
            product *= values[i];
         }
      }

      return product;
   }

   @Override
   public double evaluate(double[] values, double[] weights, int begin, int length) {
      double product = Double.NaN;
      if (this.test(values, weights, begin, length)) {
         product = 1.0;

         for(int i = begin; i < begin + length; ++i) {
            product *= FastMath.pow(values[i], weights[i]);
         }
      }

      return product;
   }

   @Override
   public double evaluate(double[] values, double[] weights) {
      return this.evaluate(values, weights, 0, values.length);
   }

   public Product copy() {
      Product result = new Product();
      copy(this, result);
      return result;
   }

   public static void copy(Product source, Product dest) {
      dest.setData(source.getDataRef());
      dest.n = source.n;
      dest.value = source.value;
   }
}
