package org.apache.commons.math.stat.descriptive;

import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.MathUtils;

public abstract class AbstractStorelessUnivariateStatistic extends AbstractUnivariateStatistic implements StorelessUnivariateStatistic {
   @Override
   public double evaluate(double[] values) {
      if (values == null) {
         throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
      } else {
         return this.evaluate(values, 0, values.length);
      }
   }

   @Override
   public double evaluate(double[] values, int begin, int length) {
      if (this.test(values, begin, length)) {
         this.clear();
         this.incrementAll(values, begin, length);
      }

      return this.getResult();
   }

   @Override
   public abstract StorelessUnivariateStatistic copy();

   @Override
   public abstract void clear();

   @Override
   public abstract double getResult();

   @Override
   public abstract void increment(double var1);

   @Override
   public void incrementAll(double[] values) {
      if (values == null) {
         throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
      } else {
         this.incrementAll(values, 0, values.length);
      }
   }

   @Override
   public void incrementAll(double[] values, int begin, int length) {
      if (this.test(values, begin, length)) {
         int k = begin + length;

         for(int i = begin; i < k; ++i) {
            this.increment(values[i]);
         }
      }
   }

   @Override
   public boolean equals(Object object) {
      if (object == this) {
         return true;
      } else if (!(object instanceof AbstractStorelessUnivariateStatistic)) {
         return false;
      } else {
         AbstractStorelessUnivariateStatistic stat = (AbstractStorelessUnivariateStatistic)object;
         return MathUtils.equalsIncludingNaN(stat.getResult(), this.getResult()) && MathUtils.equalsIncludingNaN((float)stat.getN(), (float)this.getN());
      }
   }

   @Override
   public int hashCode() {
      return 31 * (31 + MathUtils.hash(this.getResult())) + MathUtils.hash((double)this.getN());
   }
}
