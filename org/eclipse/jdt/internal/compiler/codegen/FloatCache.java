package org.eclipse.jdt.internal.compiler.codegen;

public class FloatCache {
   private float[] keyTable;
   private int[] valueTable;
   private int elementSize = 0;

   public FloatCache() {
      this(13);
   }

   public FloatCache(int initialCapacity) {
      this.keyTable = new float[initialCapacity];
      this.valueTable = new int[initialCapacity];
   }

   public void clear() {
      for(int i = this.keyTable.length; --i >= 0; this.valueTable[i] = 0) {
         this.keyTable[i] = 0.0F;
      }

      this.elementSize = 0;
   }

   public boolean containsKey(float key) {
      if (key == 0.0F) {
         int i = 0;

         for(int max = this.elementSize; i < max; ++i) {
            if (this.keyTable[i] == 0.0F) {
               int value1 = Float.floatToIntBits(key);
               int value2 = Float.floatToIntBits(this.keyTable[i]);
               if (value1 == Integer.MIN_VALUE && value2 == Integer.MIN_VALUE) {
                  return true;
               }

               if (value1 == 0 && value2 == 0) {
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

   public int put(float key, int value) {
      if (this.elementSize == this.keyTable.length) {
         System.arraycopy(this.keyTable, 0, this.keyTable = new float[this.elementSize * 2], 0, this.elementSize);
         System.arraycopy(this.valueTable, 0, this.valueTable = new int[this.elementSize * 2], 0, this.elementSize);
      }

      this.keyTable[this.elementSize] = key;
      this.valueTable[this.elementSize] = value;
      ++this.elementSize;
      return value;
   }

   public int putIfAbsent(float key, int value) {
      if (key == 0.0F) {
         int i = 0;

         for(int max = this.elementSize; i < max; ++i) {
            if (this.keyTable[i] == 0.0F) {
               int value1 = Float.floatToIntBits(key);
               int value2 = Float.floatToIntBits(this.keyTable[i]);
               if (value1 == Integer.MIN_VALUE && value2 == Integer.MIN_VALUE) {
                  return this.valueTable[i];
               }

               if (value1 == 0 && value2 == 0) {
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
         System.arraycopy(this.keyTable, 0, this.keyTable = new float[this.elementSize * 2], 0, this.elementSize);
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
         if (this.keyTable[i] != 0.0F || this.keyTable[i] == 0.0F && this.valueTable[i] != 0) {
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
