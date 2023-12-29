package org.eclipse.jdt.internal.compiler.impl;

public class DoubleConstant extends Constant {
   private double value;

   public static Constant fromValue(double value) {
      return new DoubleConstant(value);
   }

   private DoubleConstant(double value) {
      this.value = value;
   }

   @Override
   public byte byteValue() {
      return (byte)((int)this.value);
   }

   @Override
   public char charValue() {
      return (char)((int)this.value);
   }

   @Override
   public double doubleValue() {
      return this.value;
   }

   @Override
   public float floatValue() {
      return (float)this.value;
   }

   @Override
   public int intValue() {
      return (int)this.value;
   }

   @Override
   public long longValue() {
      return (long)this.value;
   }

   @Override
   public short shortValue() {
      return (short)((int)this.value);
   }

   @Override
   public String stringValue() {
      return String.valueOf(this.value);
   }

   @Override
   public String toString() {
      return this == NotAConstant ? "(Constant) NotAConstant" : "(double)" + this.value;
   }

   @Override
   public int typeID() {
      return 8;
   }

   @Override
   public int hashCode() {
      long temp = Double.doubleToLongBits(this.value);
      return (int)(temp ^ temp >>> 32);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         DoubleConstant other = (DoubleConstant)obj;
         return Double.doubleToLongBits(this.value) == Double.doubleToLongBits(other.value);
      }
   }
}
