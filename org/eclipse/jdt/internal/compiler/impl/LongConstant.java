package org.eclipse.jdt.internal.compiler.impl;

public class LongConstant extends Constant {
   private static final LongConstant ZERO = new LongConstant(0L);
   private static final LongConstant MIN_VALUE = new LongConstant(Long.MIN_VALUE);
   private long value;

   public static Constant fromValue(long value) {
      if (value == 0L) {
         return ZERO;
      } else {
         return value == Long.MIN_VALUE ? MIN_VALUE : new LongConstant(value);
      }
   }

   private LongConstant(long value) {
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
      return (float)this.value;
   }

   @Override
   public int intValue() {
      return (int)this.value;
   }

   @Override
   public long longValue() {
      return this.value;
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
      return "(long)" + this.value;
   }

   @Override
   public int typeID() {
      return 7;
   }

   @Override
   public int hashCode() {
      return (int)(this.value ^ this.value >>> 32);
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
         LongConstant other = (LongConstant)obj;
         return this.value == other.value;
      }
   }
}
