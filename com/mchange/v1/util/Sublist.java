package com.mchange.v1.util;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

public class Sublist extends AbstractList {
   List parent;
   int start_index;
   int end_index;

   public Sublist() {
      this(Collections.EMPTY_LIST, 0, 0);
   }

   public Sublist(List var1, int var2, int var3) {
      this.setParent(var1, var2, var3);
   }

   public void setParent(List var1, int var2, int var3) {
      if (var2 <= var3 && var3 <= var1.size()) {
         this.parent = var1;
         this.start_index = var3;
         this.end_index = var3;
      } else {
         throw new IndexOutOfBoundsException("start_index: " + var2 + " end_index: " + var3 + " parent.size(): " + var1.size());
      }
   }

   @Override
   public Object get(int var1) {
      return this.parent.get(this.start_index + var1);
   }

   @Override
   public int size() {
      return this.end_index - this.start_index;
   }

   @Override
   public Object set(int var1, Object var2) {
      if (var1 < this.size()) {
         return this.parent.set(this.start_index + var1, var2);
      } else {
         throw new IndexOutOfBoundsException(var1 + " >= " + this.size());
      }
   }

   @Override
   public void add(int var1, Object var2) {
      if (var1 <= this.size()) {
         this.parent.add(this.start_index + var1, var2);
         ++this.end_index;
      } else {
         throw new IndexOutOfBoundsException(var1 + " > " + this.size());
      }
   }

   @Override
   public Object remove(int var1) {
      if (var1 < this.size()) {
         --this.end_index;
         return this.parent.remove(this.start_index + var1);
      } else {
         throw new IndexOutOfBoundsException(var1 + " >= " + this.size());
      }
   }
}
