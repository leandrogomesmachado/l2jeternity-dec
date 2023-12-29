package org.eclipse.jdt.internal.compiler.impl;

public class ShortConstant extends Constant {
   private short value;

   public static Constant fromValue(short value) {
      return new ShortConstant(value);
   }

   private ShortConstant(short value) {
      this.value = value;
   }

   @Override
   public byte byteValue() {
      return (byte)this.value;
   }

   @Override
   public char charValue() {
      return (char)this.value;
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
      return this.value;
   }

   @Override
   public long longValue() {
      return (long)this.value;
   }

   @Override
   public short shortValue() {
      return this.value;
   }

   @Override
   public String stringValue() {
      return String.valueOf(this.value);
   }

   @Override
   public String toString() {
      return "(short)" + this.value;
   }

   @Override
   public int typeID() {
      return 4;
   }

   @Override
   public int hashCode() {
      return this.value;
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
         ShortConstant other = (ShortConstant)obj;
         return this.value == other.value;
      }
   }
}
