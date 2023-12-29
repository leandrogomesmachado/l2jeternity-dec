package org.eclipse.jdt.internal.compiler.util;

public final class HashtableOfInt {
   public int[] keyTable;
   public Object[] valueTable;
   public int elementSize = 0;
   int threshold;

   public HashtableOfInt() {
      this(13);
   }

   public HashtableOfInt(int size) {
      this.threshold = size;
      int extraRoom = (int)((float)size * 1.75F);
      if (this.threshold == extraRoom) {
         ++extraRoom;
      }

      this.keyTable = new int[extraRoom];
      this.valueTable = new Object[extraRoom];
   }

   public boolean containsKey(int key) {
      int length = this.keyTable.length;
      int index = key % length;

      int currentKey;
      while((currentKey = this.keyTable[index]) != 0) {
         if (currentKey == key) {
            return true;
         }

         if (++index == length) {
            index = 0;
         }
      }

      return false;
   }

   public Object get(int key) {
      int length = this.keyTable.length;
      int index = key % length;

      int currentKey;
      while((currentKey = this.keyTable[index]) != 0) {
         if (currentKey == key) {
            return this.valueTable[index];
         }

         if (++index == length) {
            index = 0;
         }
      }

      return null;
   }

   public Object put(int key, Object value) {
      int length = this.keyTable.length;
      int index = key % length;

      int currentKey;
      while((currentKey = this.keyTable[index]) != 0) {
         if (currentKey == key) {
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
      HashtableOfInt newHashtable = new HashtableOfInt(this.elementSize * 2);
      int i = this.keyTable.length;

      while(--i >= 0) {
         int currentKey;
         if ((currentKey = this.keyTable[i]) != 0) {
            newHashtable.put(currentKey, this.valueTable[i]);
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
      String s = "";
      int i = 0;

      for(int length = this.valueTable.length; i < length; ++i) {
         Object object;
         if ((object = this.valueTable[i]) != null) {
            s = s + this.keyTable[i] + " -> " + object.toString() + "\n";
         }
      }

      return s;
   }
}
