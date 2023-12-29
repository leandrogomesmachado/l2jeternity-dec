package org.eclipse.jdt.internal.compiler.codegen;

import org.eclipse.jdt.core.compiler.CharOperation;

public class CharArrayCache {
   public char[][] keyTable;
   public int[] valueTable;
   int elementSize = 0;
   int threshold;

   public CharArrayCache() {
      this(9);
   }

   public CharArrayCache(int initialCapacity) {
      this.threshold = initialCapacity * 2 / 3;
      this.keyTable = new char[initialCapacity][];
      this.valueTable = new int[initialCapacity];
   }

   public void clear() {
      for(int i = this.keyTable.length; --i >= 0; this.valueTable[i] = 0) {
         this.keyTable[i] = null;
      }

      this.elementSize = 0;
   }

   public boolean containsKey(char[] key) {
      int length = this.keyTable.length;
      int index = CharOperation.hashCode(key) % length;

      while(this.keyTable[index] != null) {
         if (CharOperation.equals(this.keyTable[index], key)) {
            return true;
         }

         if (++index == length) {
            index = 0;
         }
      }

      return false;
   }

   public int get(char[] key) {
      int length = this.keyTable.length;
      int index = CharOperation.hashCode(key) % length;

      while(this.keyTable[index] != null) {
         if (CharOperation.equals(this.keyTable[index], key)) {
            return this.valueTable[index];
         }

         if (++index == length) {
            index = 0;
         }
      }

      return -1;
   }

   public int putIfAbsent(char[] key, int value) {
      int length = this.keyTable.length;
      int index = CharOperation.hashCode(key) % length;

      while(this.keyTable[index] != null) {
         if (CharOperation.equals(this.keyTable[index], key)) {
            return this.valueTable[index];
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

      return -value;
   }

   private int put(char[] key, int value) {
      int length = this.keyTable.length;
      int index = CharOperation.hashCode(key) % length;

      while(this.keyTable[index] != null) {
         if (CharOperation.equals(this.keyTable[index], key)) {
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
      CharArrayCache newHashtable = new CharArrayCache(this.keyTable.length * 2);
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

   public void remove(char[] key) {
      int length = this.keyTable.length;
      int index = CharOperation.hashCode(key) % length;

      while(this.keyTable[index] != null) {
         if (CharOperation.equals(this.keyTable[index], key)) {
            this.valueTable[index] = 0;
            this.keyTable[index] = null;
            return;
         }

         if (++index == length) {
            index = 0;
         }
      }
   }

   public char[] returnKeyFor(int value) {
      int i = this.keyTable.length;

      while(i-- > 0) {
         if (this.valueTable[i] == value) {
            return this.keyTable[i];
         }
      }

      return null;
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
