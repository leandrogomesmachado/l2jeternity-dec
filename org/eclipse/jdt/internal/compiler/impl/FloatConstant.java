package org.eclipse.jdt.internal.compiler.impl;

public class FloatConstant extends Constant {
   float value;

   public static Constant fromValue(float value) {
      return new FloatConstant(value);
   }

   private FloatConstant(float value) {
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
      return (double)this.value;
   }

   @Override
   public float floatValue() {
      return this.value;
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
      return "(float)" + this.value;
   }

   @Override
   public int typeID() {
      return 9;
   }

   @Override
   public int hashCode() {
      return Float.floatToIntBits(this.value);
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
         FloatConstant other = (FloatConstant)obj;
         return Float.floatToIntBits(this.value) == Float.floatToIntBits(other.value);
      }
   }
}
