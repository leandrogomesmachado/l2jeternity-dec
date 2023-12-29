package org.apache.commons.math.analysis;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.util.FastMath;

@Deprecated
public abstract class BinaryFunction implements BivariateRealFunction {
   public static final BinaryFunction ADD = new BinaryFunction() {
      @Override
      public double value(double x, double y) {
         return x + y;
      }
   };
   public static final BinaryFunction SUBTRACT = new BinaryFunction() {
      @Override
      public double value(double x, double y) {
         return x - y;
      }
   };
   public static final BinaryFunction MULTIPLY = new BinaryFunction() {
      @Override
      public double value(double x, double y) {
         return x * y;
      }
   };
   public static final BinaryFunction DIVIDE = new BinaryFunction() {
      @Override
      public double value(double x, double y) {
         return x / y;
      }
   };
   public static final BinaryFunction POW = new BinaryFunction() {
      @Override
      public double value(double x, double y) {
         return FastMath.pow(x, y);
      }
   };
   public static final BinaryFunction ATAN2 = new BinaryFunction() {
      @Override
      public double value(double x, double y) {
         return FastMath.atan2(x, y);
      }
   };

   @Override
   public abstract double value(double var1, double var3) throws FunctionEvaluationException;

   public ComposableFunction fix1stArgument(final double fixedX) {
      return new ComposableFunction() {
         @Override
         public double value(double x) throws FunctionEvaluationException {
            return BinaryFunction.this.value(fixedX, x);
         }
      };
   }

   public ComposableFunction fix2ndArgument(final double fixedY) {
      return new ComposableFunction() {
         @Override
         public double value(double x) throws FunctionEvaluationException {
            return BinaryFunction.this.value(x, fixedY);
         }
      };
   }
}
