package com.mchange.util.impl;

import com.mchange.util.IntChecklist;
import com.mchange.util.IntEnumeration;

public class HashIntChecklist implements IntChecklist {
   private static final Object DUMMY = new Object();
   IntObjectHash ioh = new IntObjectHash();

   @Override
   public void check(int var1) {
      this.ioh.put(var1, DUMMY);
   }

   @Override
   public void uncheck(int var1) {
      this.ioh.remove(var1);
   }

   @Override
   public boolean isChecked(int var1) {
      return this.ioh.containsInt(var1);
   }

   @Override
   public void clear() {
      this.ioh.clear();
   }

   @Override
   public int countChecked() {
      return this.ioh.getSize();
   }

   @Override
   public int[] getChecked() {
      synchronized(this.ioh) {
         int[] var2 = new int[this.ioh.getSize()];
         IntEnumeration var3 = this.ioh.ints();

         for(int var4 = 0; var3.hasMoreInts(); ++var4) {
            var2[var4] = var3.nextInt();
         }

         return var2;
      }
   }

   @Override
   public IntEnumeration checked() {
      return this.ioh.ints();
   }
}
