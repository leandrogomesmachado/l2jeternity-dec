package org.eclipse.jdt.internal.compiler.codegen;

public class ObjectCache {
   public Object[] keyTable;
   public int[] valueTable;
   int elementSize = 0;
   int threshold;

   public ObjectCache() {
      this(13);
   }

   public ObjectCache(int initialCapacity) {
      this.threshold = (int)((float)initialCapacity * 0.66F);
      this.keyTable = new Object[initialCapacity];
      this.valueTable = new int[initialCapacity];
   }

   public void clear() {
      for(int i = this.keyTable.length; --i >= 0; this.valueTable[i] = 0) {
         this.keyTable[i] = null;
      }

      this.elementSize = 0;
   }

   public boolean containsKey(Object key) {
      int index = this.hashCode(key);
      int length = this.keyTable.length;

      while(this.keyTable[index] != null) {
         if (this.keyTable[index] == key) {
            return true;
         }

         if (++index == length) {
            index = 0;
         }
      }

      return false;
   }

   public int get(Object key) {
      int index = this.hashCode(key);
      int length = this.keyTable.length;

      while(this.keyTable[index] != null) {
         if (this.keyTable[index] == key) {
            return this.valueTable[index];
         }

         if (++index == length) {
            index = 0;
         }
      }

      return -1;
   }

   public int hashCode(Object key) {
      return (key.hashCode() & 2147483647) % this.keyTable.length;
   }

   public int put(Object key, int value) {
      int index = this.hashCode(key);
      int length = this.keyTable.length;

      while(this.keyTable[index] != null) {
         if (this.keyTable[index] == key) {
            return this.valueTable[index] = value;
         }

         if (++index == length) {
            index = 0;
         }
      }

      this.keyTable[index] = key;
      this.valueTable[index] = value;
      if (++this.elementSize > this.threshold) {
         this.rehash();
      }

      return value;
   }

   private void rehash() {
      ObjectCache newHashtable = new ObjectCache(this.keyTable.length * 2);
      int i = this.keyTable.length;

      while(--i >= 0) {
         if (this.keyTable[i] != null) {
            newHashtable.put(this.keyTable[i], this.valueTable[i]);
         }
      }

      this.keyTable = newHashtable.keyTable;
      this.valueTable = newHashtable.valueTable;
      this.threshold = newHashtable.threshold;
   }

   public int size() {
      return this.elementSize;
   }

   @Override
   public String toString() {
      int max = this.size();
      StringBuffer buf = new StringBuffer();
      buf.append("{");

      for(int i = 0; i < max; ++i) {
         if (this.keyTable[i] != null) {
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
