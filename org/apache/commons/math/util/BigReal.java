package org.apache.commons.math.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import org.apache.commons.math.Field;
import org.apache.commons.math.FieldElement;

public class BigReal implements FieldElement<BigReal>, Comparable<BigReal>, Serializable {
   public static final BigReal ZERO = new BigReal(BigDecimal.ZERO);
   public static final BigReal ONE = new BigReal(BigDecimal.ONE);
   private static final long serialVersionUID = 4984534880991310382L;
   private final BigDecimal d;
   private RoundingMode roundingMode = RoundingMode.HALF_UP;
   private int scale = 64;

   public BigReal(BigDecimal val) {
      this.d = val;
   }

   public BigReal(BigInteger val) {
      this.d = new BigDecimal(val);
   }

   public BigReal(BigInteger unscaledVal, int scale) {
      this.d = new BigDecimal(unscaledVal, scale);
   }

   public BigReal(BigInteger unscaledVal, int scale, MathContext mc) {
      this.d = new BigDecimal(unscaledVal, scale, mc);
   }

   public BigReal(BigInteger val, MathContext mc) {
      this.d = new BigDecimal(val, mc);
   }

   public BigReal(char[] in) {
      this.d = new BigDecimal(in);
   }

   public BigReal(char[] in, int offset, int len) {
      this.d = new BigDecimal(in, offset, len);
   }

   public BigReal(char[] in, int offset, int len, MathContext mc) {
      this.d = new BigDecimal(in, offset, len, mc);
   }

   public BigReal(char[] in, MathContext mc) {
      this.d = new BigDecimal(in, mc);
   }

   public BigReal(double val) {
      this.d = new BigDecimal(val);
   }

   public BigReal(double val, MathContext mc) {
      this.d = new BigDecimal(val, mc);
   }

   public BigReal(int val) {
      this.d = new BigDecimal(val);
   }

   public BigReal(int val, MathContext mc) {
      this.d = new BigDecimal(val, mc);
   }

   public BigReal(long val) {
      this.d = new BigDecimal(val);
   }

   public BigReal(long val, MathContext mc) {
      this.d = new BigDecimal(val, mc);
   }

   public BigReal(String val) {
      this.d = new BigDecimal(val);
   }

   public BigReal(String val, MathContext mc) {
      this.d = new BigDecimal(val, mc);
   }

   public RoundingMode getRoundingMode() {
      return this.roundingMode;
   }

   public void setRoundingMode(RoundingMode roundingMode) {
      this.roundingMode = roundingMode;
   }

   public int getScale() {
      return this.scale;
   }

   public void setScale(int scale) {
      this.scale = scale;
   }

   public BigReal add(BigReal a) {
      return new BigReal(this.d.add(a.d));
   }

   public BigReal subtract(BigReal a) {
      return new BigReal(this.d.subtract(a.d));
   }

   public BigReal divide(BigReal a) throws ArithmeticException {
      return new BigReal(this.d.divide(a.d, this.scale, this.roundingMode));
   }

   public BigReal multiply(BigReal a) {
      return new BigReal(this.d.multiply(a.d));
   }

   public int compareTo(BigReal a) {
      return this.d.compareTo(a.d);
   }

   public double doubleValue() {
      return this.d.doubleValue();
   }

   public BigDecimal bigDecimalValue() {
      return this.d;
   }

   @Override
   public boolean equals(Object other) {
      if (this == other) {
         return true;
      } else {
         return other instanceof BigReal ? this.d.equals(((BigReal)other).d) : false;
      }
   }

   @Override
   public int hashCode() {
      return this.d.hashCode();
   }

   @Override
   public Field<BigReal> getField() {
      return BigRealField.getInstance();
   }
}
