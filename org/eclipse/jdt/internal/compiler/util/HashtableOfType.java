package org.eclipse.jdt.internal.compiler.util;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

public final class HashtableOfType {
   public char[][] keyTable;
   public ReferenceBinding[] valueTable;
   public int elementSize = 0;
   int threshold;

   public HashtableOfType() {
      this(3);
   }

   public HashtableOfType(int size) {
      this.threshold = size;
      int extraRoom = (int)((float)size * 1.75F);
      if (this.threshold == extraRoom) {
         ++extraRoom;
      }

      this.keyTable = new char[extraRoom][];
      this.valueTable = new ReferenceBinding[extraRoom];
   }

   public boolean containsKey(char[] key) {
      int length = this.keyTable.length;
      int index = CharOperation.hashCode(key) % length;
      int keyLength = key.length;

      char[] currentKey;
      while((currentKey = this.keyTable[index]) != null) {
         if (currentKey.length == keyLength && CharOperation.equals(currentKey, key)) {
            return true;
         }

         if (++index == length) {
            index = 0;
         }
      }

      return false;
   }

   public ReferenceBinding get(char[] key) {
      int length = this.keyTable.length;
      int index = CharOperation.hashCode(key) % length;
      int keyLength = key.length;

      char[] currentKey;
      while((currentKey = this.keyTable[index]) != null) {
         if (currentKey.length == keyLength && CharOperation.equals(currentKey, key)) {
            return this.valueTable[index];
         }

         if (++index == length) {
            index = 0;
         }
      }

      return null;
   }

   public ReferenceBinding getput(char[] key, ReferenceBinding value) {
      ReferenceBinding retVal = null;
      int length = this.keyTable.length;
      int index = CharOperation.hashCode(key) % length;
      int keyLength = key.length;

      char[] currentKey;
      while((currentKey = this.keyTable[index]) != null) {
         if (currentKey.length == keyLength && CharOperation.equals(currentKey, key)) {
            retVal = this.valueTable[index];
            this.valueTable[index] = value;
            return retVal;
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

      return retVal;
   }

   public ReferenceBinding put(char[] key, ReferenceBinding value) {
      int length = this.keyTable.length;
      int index = CharOperation.hashCode(key) % length;
      int keyLength = key.length;

      char[] currentKey;
      while((currentKey = this.keyTable[index]) != null) {
         if (currentKey.length == keyLength && CharOperation.equals(currentKey, key)) {
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
      HashtableOfType newHashtable = new HashtableOfType(this.elementSize < 100 ? 100 : this.elementSize * 2);
      int i = this.keyTable.length;

      while(--i >= 0) {
         char[] currentKey;
         if ((currentKey = this.keyTable[i]) != null) {
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
         ReferenceBinding type;
         if ((type = this.valueTable[i]) != null) {
            s = s + type.toString() + "\n";
         }
      }

      return s;
   }
}
