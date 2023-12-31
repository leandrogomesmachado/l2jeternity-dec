package org.apache.commons.math.util;

import java.io.Serializable;
import org.apache.commons.math.Field;

public class BigRealField implements Field<BigReal>, Serializable {
   private static final long serialVersionUID = 4756431066541037559L;

   private BigRealField() {
   }

   public static BigRealField getInstance() {
      return BigRealField.LazyHolder.INSTANCE;
   }

   public BigReal getOne() {
      return BigReal.ONE;
   }

   public BigReal getZero() {
      return BigReal.ZERO;
   }

   private Object readResolve() {
      return BigRealField.LazyHolder.INSTANCE;
   }

   private static class LazyHolder {
      private static final BigRealField INSTANCE = new BigRealField();
   }
}
