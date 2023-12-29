package org.apache.commons.math.analysis;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.util.FastMath;

public abstract class ComposableFunction implements UnivariateRealFunction {
   public static final ComposableFunction ZERO = new ComposableFunction() {
      @Override
      public double value(double d) {
         return 0.0;
      }
   };
   public static final ComposableFunction ONE = new ComposableFunction() {
      @Override
      public double value(double d) {
         return 1.0;
      }
   };
   public static final ComposableFunction IDENTITY = new ComposableFunction() {
      @Override
      public double value(double d) {
         return d;
      }
   };
   public static final ComposableFunction ABS = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.abs(d);
      }
   };
   public static final ComposableFunction NEGATE = new ComposableFunction() {
      @Override
      public double value(double d) {
         return -d;
      }
   };
   public static final ComposableFunction INVERT = new ComposableFunction() {
      @Override
      public double value(double d) {
         return 1.0 / d;
      }
   };
   public static final ComposableFunction SIN = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.sin(d);
      }
   };
   public static final ComposableFunction SQRT = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.sqrt(d);
      }
   };
   public static final ComposableFunction SINH = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.sinh(d);
      }
   };
   public static final ComposableFunction EXP = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.exp(d);
      }
   };
   public static final ComposableFunction EXPM1 = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.expm1(d);
      }
   };
   public static final ComposableFunction ASIN = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.asin(d);
      }
   };
   public static final ComposableFunction ATAN = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.atan(d);
      }
   };
   public static final ComposableFunction TAN = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.tan(d);
      }
   };
   public static final ComposableFunction TANH = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.tanh(d);
      }
   };
   public static final ComposableFunction CBRT = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.cbrt(d);
      }
   };
   public static final ComposableFunction CEIL = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.ceil(d);
      }
   };
   public static final ComposableFunction FLOOR = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.floor(d);
      }
   };
   public static final ComposableFunction LOG = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.log(d);
      }
   };
   public static final ComposableFunction LOG10 = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.log10(d);
      }
   };
   public static final ComposableFunction LOG1P = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.log1p(d);
      }
   };
   public static final ComposableFunction COS = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.cos(d);
      }
   };
   public static final ComposableFunction ACOS = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.acos(d);
      }
   };
   public static final ComposableFunction COSH = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.cosh(d);
      }
   };
   public static final ComposableFunction RINT = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.rint(d);
      }
   };
   public static final ComposableFunction SIGNUM = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.signum(d);
      }
   };
   public static final ComposableFunction ULP = new ComposableFunction() {
      @Override
      public double value(double d) {
         return FastMath.ulp(d);
      }
   };

   public ComposableFunction of(final UnivariateRealFunction f) {
      return new ComposableFunction() {
         @Override
         public double value(double x) throws FunctionEvaluationException {
            return ComposableFunction.this.value(f.value(x));
         }
      };
   }

   public ComposableFunction postCompose(final UnivariateRealFunction f) {
      return new ComposableFunction() {
         @Override
         public double value(double x) throws FunctionEvaluationException {
            return f.value(ComposableFunction.this.value(x));
         }
      };
   }

   public ComposableFunction combine(final UnivariateRealFunction f, final BivariateRealFunction combiner) {
      return new ComposableFunction() {
         @Override
         public double value(double x) throws FunctionEvaluationException {
            return combiner.value(ComposableFunction.this.value(x), f.value(x));
         }
      };
   }

   public ComposableFunction add(final UnivariateRealFunction f) {
      return new ComposableFunction() {
         @Override
         public double value(double x) throws FunctionEvaluationException {
            return ComposableFunction.this.value(x) + f.value(x);
         }
      };
   }

   public ComposableFunction add(final double a) {
      return new ComposableFunction() {
         @Override
         public double value(double x) throws FunctionEvaluationException {
            return ComposableFunction.this.value(x) + a;
         }
      };
   }

   public ComposableFunction subtract(final UnivariateRealFunction f) {
      return new ComposableFunction() {
         @Override
         public double value(double x) throws FunctionEvaluationException {
            return ComposableFunction.this.value(x) - f.value(x);
         }
      };
   }

   public ComposableFunction multiply(final UnivariateRealFunction f) {
      return new ComposableFunction() {
         @Override
         public double value(double x) throws FunctionEvaluationException {
            return ComposableFunction.this.value(x) * f.value(x);
         }
      };
   }

   public ComposableFunction multiply(final double scaleFactor) {
      return new ComposableFunction() {
         @Override
         public double value(double x) throws FunctionEvaluationException {
            return ComposableFunction.this.value(x) * scaleFactor;
         }
      };
   }

   public ComposableFunction divide(final UnivariateRealFunction f) {
      return new ComposableFunction() {
         @Override
         public double value(double x) throws FunctionEvaluationException {
            return ComposableFunction.this.value(x) / f.value(x);
         }
      };
   }

   public MultivariateRealFunction asCollector(final BivariateRealFunction combiner, final double initialValue) {
      return new MultivariateRealFunction() {
         @Override
         public double value(double[] point) throws FunctionEvaluationException, IllegalArgumentException {
            double result = initialValue;

            for(double entry : point) {
               result = combiner.value(result, ComposableFunction.this.value(entry));
            }

            return result;
         }
      };
   }

   public MultivariateRealFunction asCollector(BivariateRealFunction combiner) {
      return this.asCollector(combiner, 0.0);
   }

   public MultivariateRealFunction asCollector(double initialValue) {
      return this.asCollector(BinaryFunction.ADD, initialValue);
   }

   public MultivariateRealFunction asCollector() {
      return this.asCollector(BinaryFunction.ADD, 0.0);
   }

   @Override
   public abstract double value(double var1) throws FunctionEvaluationException;
}
