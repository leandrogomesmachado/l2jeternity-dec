package org.apache.commons.lang.mutable;

public class MutableByte extends Number implements Comparable, Mutable {
   private static final long serialVersionUID = -1585823265L;
   private byte value;

   public MutableByte() {
   }

   public MutableByte(byte value) {
      this.value = value;
   }

   public MutableByte(Number value) {
      this.value = value.byteValue();
   }

   public Object getValue() {
      return new Byte(this.value);
   }

   public void setValue(byte value) {
      this.value = value;
   }

   public void setValue(Object value) {
      this.setValue(((Number)value).byteValue());
   }

   public byte byteValue() {
      return this.value;
   }

   public int intValue() {
      return this.value;
   }

   public long longValue() {
      return (long)this.value;
   }

   public float floatValue() {
      return (float)this.value;
   }

   public double doubleValue() {
      return (double)this.value;
   }

   public Byte toByte() {
      return new Byte(this.byteValue());
   }

   public void increment() {
      ++this.value;
   }

   public void decrement() {
      --this.value;
   }

   public void add(byte operand) {
      this.value += operand;
   }

   public void add(Number operand) {
      this.value += operand.byteValue();
   }

   public void subtract(byte operand) {
      this.value -= operand;
   }

   public void subtract(Number operand) {
      this.value -= operand.byteValue();
   }

   public boolean equals(Object obj) {
      if (obj instanceof MutableByte) {
         return this.value == ((MutableByte)obj).byteValue();
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.value;
   }

   public int compareTo(Object obj) {
      MutableByte other = (MutableByte)obj;
      byte anotherVal = other.value;
      return this.value < anotherVal ? -1 : (this.value == anotherVal ? 0 : 1);
   }

   public String toString() {
      return String.valueOf(this.value);
   }
}
