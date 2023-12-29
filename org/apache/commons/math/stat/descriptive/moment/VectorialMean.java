package org.apache.commons.math.stat.descriptive.moment;

import java.io.Serializable;
import java.util.Arrays;
import org.apache.commons.math.DimensionMismatchException;

public class VectorialMean implements Serializable {
   private static final long serialVersionUID = 8223009086481006892L;
   private final Mean[] means;

   public VectorialMean(int dimension) {
      this.means = new Mean[dimension];

      for(int i = 0; i < dimension; ++i) {
         this.means[i] = new Mean();
      }
   }

   public void increment(double[] v) throws DimensionMismatchException {
      if (v.length != this.means.length) {
         throw new DimensionMismatchException(v.length, this.means.length);
      } else {
         for(int i = 0; i < v.length; ++i) {
            this.means[i].increment(v[i]);
         }
      }
   }

   public double[] getResult() {
      double[] result = new double[this.means.length];

      for(int i = 0; i < result.length; ++i) {
         result[i] = this.means[i].getResult();
      }

      return result;
   }

   public long getN() {
      return this.means.length == 0 ? 0L : this.means[0].getN();
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.means);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof VectorialMean)) {
         return false;
      } else {
         VectorialMean other = (VectorialMean)obj;
         return Arrays.equals((Object[])this.means, (Object[])other.means);
      }
   }
}
