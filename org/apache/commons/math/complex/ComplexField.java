package org.apache.commons.math.complex;

import java.io.Serializable;
import org.apache.commons.math.Field;

public class ComplexField implements Field<Complex>, Serializable {
   private static final long serialVersionUID = -6130362688700788798L;

   private ComplexField() {
   }

   public static ComplexField getInstance() {
      return ComplexField.LazyHolder.INSTANCE;
   }

   public Complex getOne() {
      return Complex.ONE;
   }

   public Complex getZero() {
      return Complex.ZERO;
   }

   private Object readResolve() {
      return ComplexField.LazyHolder.INSTANCE;
   }

   private static class LazyHolder {
      private static final ComplexField INSTANCE = new ComplexField();
   }
}
