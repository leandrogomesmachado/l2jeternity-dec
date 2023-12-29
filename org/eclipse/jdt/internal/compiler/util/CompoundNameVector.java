package org.eclipse.jdt.internal.compiler.util;

import org.eclipse.jdt.core.compiler.CharOperation;

public final class CompoundNameVector {
   static int INITIAL_SIZE = 10;
   public int size;
   int maxSize = INITIAL_SIZE;
   char[][][] elements;

   public CompoundNameVector() {
      this.size = 0;
      this.elements = new char[this.maxSize][][];
   }

   public void add(char[][] newElement) {
      if (this.size == this.maxSize) {
         System.arraycopy(this.elements, 0, this.elements = new char[this.maxSize *= 2][][], 0, this.size);
      }

      this.elements[this.size++] = newElement;
   }

   public void addAll(char[][][] newElements) {
      if (this.size + newElements.length >= this.maxSize) {
         this.maxSize = this.size + newElements.length;
         System.arraycopy(this.elements, 0, this.elements = new char[this.maxSize][][], 0, this.size);
      }

      System.arraycopy(newElements, 0, this.elements, this.size, newElements.length);
      this.size += newElements.length;
   }

   public boolean contains(char[][] element) {
      int i = this.size;

      while(--i >= 0) {
         if (CharOperation.equals(element, this.elements[i])) {
            return true;
         }
      }

      return false;
   }

   public char[][] elementAt(int index) {
      return this.elements[index];
   }

   public char[][] remove(char[][] element) {
      int i = this.size;

      while(--i >= 0) {
         if (element == this.elements[i]) {
            System.arraycopy(this.elements, i + 1, this.elements, i, --this.size - i);
            this.elements[this.size] = null;
            return element;
         }
      }

      return null;
   }

   public void removeAll() {
      int i = this.size;

      while(--i >= 0) {
         this.elements[i] = null;
      }

      this.size = 0;
   }

   @Override
   public String toString() {
      StringBuffer buffer = new StringBuffer();

      for(int i = 0; i < this.size; ++i) {
         buffer.append(CharOperation.toString(this.elements[i])).append("\n");
      }

      return buffer.toString();
   }
}
