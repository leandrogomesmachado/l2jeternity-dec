package org.eclipse.jdt.internal.compiler.util;

import org.eclipse.jdt.core.compiler.CharOperation;

public final class SimpleSetOfCharArray implements Cloneable {
   public char[][] values;
   public int elementSize;
   public int threshold;

   public SimpleSetOfCharArray() {
      this(13);
   }

   public SimpleSetOfCharArray(int size) {
      if (size < 3) {
         size = 3;
      }

      this.elementSize = 0;
      this.threshold = size + 1;
      this.values = new char[2 * size + 1][];
   }

   public Object add(char[] object) {
      int length = this.values.length;
      int index = (CharOperation.hashCode(object) & 2147483647) % length;

      char[] current;
      while((current = this.values[index]) != null) {
         if (CharOperation.equals(current, object)) {
            return this.values[index] = object;
         }

         if (++index == length) {
            index = 0;
         }
      }

      this.values[index] = object;
      if (++this.elementSize > this.threshold) {
         this.rehash();
      }

      return object;
   }

   public void asArray(Object[] copy) {
      if (this.elementSize != copy.length) {
         throw new IllegalArgumentException();
      } else {
         int index = this.elementSize;
         int i = 0;

         for(int l = this.values.length; i < l && index > 0; ++i) {
            if (this.values[i] != null) {
               --index;
               copy[index] = this.values[i];
            }
         }
      }
   }

   public void clear() {
      int i = this.values.length;

      while(--i >= 0) {
         this.values[i] = null;
      }

      this.elementSize = 0;
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      SimpleSetOfCharArray result = (SimpleSetOfCharArray)super.clone();
      result.elementSize = this.elementSize;
      result.threshold = this.threshold;
      int length = this.values.length;
      result.values = new char[length][];
      System.arraycopy(this.values, 0, result.values, 0, length);
      return result;
   }

   public char[] get(char[] object) {
      int length = this.values.length;
      int index = (CharOperation.hashCode(object) & 2147483647) % length;

      char[] current;
      while((current = this.values[index]) != null) {
         if (CharOperation.equals(current, object)) {
            return current;
         }

         if (++index == length) {
            index = 0;
         }
      }

      this.values[index] = object;
      if (++this.elementSize > this.threshold) {
         this.rehash();
      }

      return object;
   }

   public boolean includes(char[] object) {
      int length = this.values.length;
      int index = (CharOperation.hashCode(object) & 2147483647) % length;

      char[] current;
      while((current = this.values[index]) != null) {
         if (CharOperation.equals(current, object)) {
            return true;
         }

         if (++index == length) {
            index = 0;
         }
      }

      return false;
   }

   public char[] remove(char[] object) {
      int length = this.values.length;
      int index = (CharOperation.hashCode(object) & 2147483647) % length;

      char[] current;
      while((current = this.values[index]) != null) {
         if (CharOperation.equals(current, object)) {
            --this.elementSize;
            char[] oldValue = this.values[index];
            this.values[index] = null;
            if (this.values[index + 1 == length ? 0 : index + 1] != null) {
               this.rehash();
            }

            return oldValue;
         }

         if (++index == length) {
            index = 0;
         }
      }

      return null;
   }

   private void rehash() {
      SimpleSetOfCharArray newSet = new SimpleSetOfCharArray(this.elementSize * 2);
      int i = this.values.length;

      while(--i >= 0) {
         char[] current;
         if ((current = this.values[i]) != null) {
            newSet.add(current);
         }
      }

      this.values = newSet.values;
      this.elementSize = newSet.elementSize;
      this.threshold = newSet.threshold;
   }

   @Override
   public String toString() {
      String s = "";
      int i = 0;

      for(int l = this.values.length; i < l; ++i) {
         char[] object;
         if ((object = this.values[i]) != null) {
            s = s + new String(object) + "\n";
         }
      }

      return s;
   }
}
