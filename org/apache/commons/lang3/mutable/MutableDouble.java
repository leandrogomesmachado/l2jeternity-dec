package org.apache.commons.lang3.mutable;

public class MutableDouble extends Number implements Comparable<MutableDouble>, Mutable<Number> {
   private static final long serialVersionUID = 1587163916L;
   private double value;

   public MutableDouble() {
   }

   public MutableDouble(double value) {
      this.value = value;
   }

   public MutableDouble(Number value) {
      this.value = value.doubleValue();
   }

   public MutableDouble(String value) throws NumberFormatException {
      this.value = Double.parseDouble(value);
   }

   public Double getValue() {
      return this.value;
   }

   public void setValue(double value) {
      this.value = value;
   }

   public void setValue(Number value) {
      this.value = value.doubleValue();
   }

   public boolean isNaN() {
      return Double.isNaN(this.value);
   }

   public boolean isInfinite() {
      return Double.isInfinite(this.value);
   }

   public void increment() {
      ++this.value;
   }

   public double getAndIncrement() {
      return (double)(this.value++);
   }

   public double incrementAndGet() {
      ++this.value;
      return this.value;
   }

   public void decrement() {
      --this.value;
   }

   public double getAndDecrement() {
      return (double)(this.value--);
   }

   public double decrementAndGet() {
      --this.value;
      return this.value;
   }

   public void add(double operand) {
      this.value += operand;
   }

   public void add(Number operand) {
      this.value += operand.doubleValue();
   }

   public void subtract(double operand) {
      this.value -= operand;
   }

   public void subtract(Number operand) {
      this.value -= operand.doubleValue();
   }

   public double addAndGet(double operand) {
      this.value += operand;
      return this.value;
   }

   public double addAndGet(Number operand) {
      this.value += operand.doubleValue();
      return this.value;
   }

   public double getAndAdd(double operand) {
      double last = this.value;
      this.value += operand;
      return last;
   }

   public double getAndAdd(Number operand) {
      double last = this.value;
      this.value += operand.doubleValue();
      return last;
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
   public float floatValue() {
      return (float)this.value;
   }

   @Override
   public double doubleValue() {
      return this.value;
   }

   public Double toDouble() {
      return this.doubleValue();
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof MutableDouble && Double.doubleToLongBits(((MutableDouble)obj).value) == Double.doubleToLongBits(this.value);
   }

   @Override
   public int hashCode() {
      long bits = Double.doubleToLongBits(this.value);
      return (int)(bits ^ bits >>> 32);
   }

   public int compareTo(MutableDouble other) {
      return Double.compare(this.value, other.value);
   }

   @Override
   public String toString() {
      return String.valueOf(this.value);
   }
}
