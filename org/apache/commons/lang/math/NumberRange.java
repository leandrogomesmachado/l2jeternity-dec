package org.apache.commons.lang.math;

import java.io.Serializable;

public final class NumberRange extends Range implements Serializable {
   private static final long serialVersionUID = 71849363892710L;
   private final Number min;
   private final Number max;
   private transient int hashCode = 0;
   private transient String toString = null;

   public NumberRange(Number num) {
      if (num == null) {
         throw new IllegalArgumentException("The number must not be null");
      } else if (!(num instanceof Comparable)) {
         throw new IllegalArgumentException("The number must implement Comparable");
      } else if (num instanceof Double && ((Double)num).isNaN()) {
         throw new IllegalArgumentException("The number must not be NaN");
      } else if (num instanceof Float && ((Float)num).isNaN()) {
         throw new IllegalArgumentException("The number must not be NaN");
      } else {
         this.min = num;
         this.max = num;
      }
   }

   public NumberRange(Number num1, Number num2) {
      if (num1 != null && num2 != null) {
         if (num1.getClass() != num2.getClass()) {
            throw new IllegalArgumentException("The numbers must be of the same type");
         } else if (!(num1 instanceof Comparable)) {
            throw new IllegalArgumentException("The numbers must implement Comparable");
         } else {
            if (num1 instanceof Double) {
               if (((Double)num1).isNaN() || ((Double)num2).isNaN()) {
                  throw new IllegalArgumentException("The number must not be NaN");
               }
            } else if (num1 instanceof Float && (((Float)num1).isNaN() || ((Float)num2).isNaN())) {
               throw new IllegalArgumentException("The number must not be NaN");
            }

            int compare = ((Comparable)num1).compareTo(num2);
            if (compare == 0) {
               this.min = num1;
               this.max = num1;
            } else if (compare > 0) {
               this.min = num2;
               this.max = num1;
            } else {
               this.min = num1;
               this.max = num2;
            }
         }
      } else {
         throw new IllegalArgumentException("The numbers must not be null");
      }
   }

   public Number getMinimumNumber() {
      return this.min;
   }

   public Number getMaximumNumber() {
      return this.max;
   }

   public boolean containsNumber(Number number) {
      if (number == null) {
         return false;
      } else if (number.getClass() != this.min.getClass()) {
         throw new IllegalArgumentException("The number must be of the same type as the range numbers");
      } else {
         int compareMin = ((Comparable)this.min).compareTo(number);
         int compareMax = ((Comparable)this.max).compareTo(number);
         return compareMin <= 0 && compareMax >= 0;
      }
   }

   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof NumberRange)) {
         return false;
      } else {
         NumberRange range = (NumberRange)obj;
         return this.min.equals(range.min) && this.max.equals(range.max);
      }
   }

   public int hashCode() {
      if (this.hashCode == 0) {
         this.hashCode = 17;
         this.hashCode = 37 * this.hashCode + this.getClass().hashCode();
         this.hashCode = 37 * this.hashCode + this.min.hashCode();
         this.hashCode = 37 * this.hashCode + this.max.hashCode();
      }

      return this.hashCode;
   }

   public String toString() {
      if (this.toString == null) {
         StringBuffer buf = new StringBuffer(32);
         buf.append("Range[");
         buf.append(this.min);
         buf.append(',');
         buf.append(this.max);
         buf.append(']');
         this.toString = buf.toString();
      }

      return this.toString;
   }
}
