package org.eclipse.jdt.internal.compiler.codegen;

public class DoubleCache {
   private double[] keyTable;
   private int[] valueTable;
   private int elementSize = 0;

   public DoubleCache() {
      this(13);
   }

   public DoubleCache(int initialCapacity) {
      this.keyTable = new double[initialCapacity];
      this.valueTable = new int[initialCapacity];
   }

   public void clear() {
      for(int i = this.keyTable.length; --i >= 0; this.valueTable[i] = 0) {
         this.keyTable[i] = 0.0;
      }

      this.elementSize = 0;
   }

   public boolean containsKey(double key) {
      if (key == 0.0) {
         int i = 0;

         for(int max = this.elementSize; i < max; ++i) {
            if (this.keyTable[i] == 0.0) {
               long value1 = Double.doubleToLongBits(key);
               long value2 = Double.doubleToLongBits(this.keyTable[i]);
               if (value1 == Long.MIN_VALUE && value2 == Long.MIN_VALUE) {
                  return true;
               }

               if (value1 == 0L && value2 == 0L) {
                  return true;
               }
            }
         }
      } else {
         int i = 0;

         for(int max = this.elementSize; i < max; ++i) {
            if (this.keyTable[i] == key) {
               return true;
            }
         }
      }

      return false;
   }

   public int put(double key, int value) {
      if (this.elementSize == this.keyTable.length) {
         System.arraycopy(this.keyTable, 0, this.keyTable = new double[this.elementSize * 2], 0, this.elementSize);
         System.arraycopy(this.valueTable, 0, this.valueTable = new int[this.elementSize * 2], 0, this.elementSize);
      }

      this.keyTable[this.elementSize] = key;
      this.valueTable[this.elementSize] = value;
      ++this.elementSize;
      return value;
   }

   public int putIfAbsent(double key, int value) {
      if (key == 0.0) {
         int i = 0;

         for(int max = this.elementSize; i < max; ++i) {
            if (this.keyTable[i] == 0.0) {
               long value1 = Double.doubleToLongBits(key);
               long value2 = Double.doubleToLongBits(this.keyTable[i]);
               if (value1 == Long.MIN_VALUE && value2 == Long.MIN_VALUE) {
                  return this.valueTable[i];
               }

               if (value1 == 0L && value2 == 0L) {
                  return this.valueTable[i];
               }
            }
         }
      } else {
         int i = 0;

         for(int max = this.elementSize; i < max; ++i) {
            if (this.keyTable[i] == key) {
               return this.valueTable[i];
            }
         }
      }

      if (this.elementSize == this.keyTable.length) {
         System.arraycopy(this.keyTable, 0, this.keyTable = new double[this.elementSize * 2], 0, this.elementSize);
         System.arraycopy(this.valueTable, 0, this.valueTable = new int[this.elementSize * 2], 0, this.elementSize);
      }

      this.keyTable[this.elementSize] = key;
      this.valueTable[this.elementSize] = value;
      ++this.elementSize;
      return -value;
   }

   @Override
   public String toString() {
      int max = this.elementSize;
      StringBuffer buf = new StringBuffer();
      buf.append("{");

      for(int i = 0; i < max; ++i) {
         if (this.keyTable[i] != 0.0 || this.keyTable[i] == 0.0 && this.valueTable[i] != 0) {
            buf.append(this.keyTable[i]).append("->").append(this.valueTable[i]);
         }

         if (i < max) {
            buf.append(", ");
         }
      }

      buf.append("}");
      return buf.toString();
   }
}
