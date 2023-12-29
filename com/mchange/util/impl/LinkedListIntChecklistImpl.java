package com.mchange.util.impl;

import com.mchange.util.IntChecklist;
import com.mchange.util.IntEnumeration;
import java.util.NoSuchElementException;

public class LinkedListIntChecklistImpl implements IntChecklist {
   private final LLICIRecord headRecord = new LLICIRecord();
   private int num_checked = 0;

   @Override
   public void check(int var1) {
      LLICIRecord var2 = this.findPrevious(var1);
      if (var2.next == null || var2.next.contained != var1) {
         LLICIRecord var3 = new LLICIRecord();
         var3.next = var2.next;
         var3.contained = var1;
         var2.next = var3;
         ++this.num_checked;
      }
   }

   @Override
   public void uncheck(int var1) {
      LLICIRecord var2 = this.findPrevious(var1);
      if (var2.next != null && var2.next.contained == var1) {
         var2.next = var2.next.next;
         --this.num_checked;
      }
   }

   @Override
   public boolean isChecked(int var1) {
      LLICIRecord var2 = this.findPrevious(var1);
      return var2.next != null && var2.next.contained == var1;
   }

   @Override
   public void clear() {
      this.headRecord.next = null;
      this.num_checked = 0;
   }

   @Override
   public int countChecked() {
      return this.num_checked;
   }

   @Override
   public int[] getChecked() {
      LLICIRecord var1 = this.headRecord;
      int[] var2 = new int[this.num_checked];

      for(int var3 = 0; var1.next != null; var1 = var1.next) {
         var2[var3++] = var1.next.contained;
      }

      return var2;
   }

   @Override
   public IntEnumeration checked() {
      return new IntEnumerationHelperBase() {
         LLICIRecord finger = LinkedListIntChecklistImpl.this.headRecord;

         @Override
         public int nextInt() {
            try {
               this.finger = this.finger.next;
               return this.finger.contained;
            } catch (NullPointerException var2) {
               throw new NoSuchElementException();
            }
         }

         @Override
         public boolean hasMoreInts() {
            return this.finger.next != null;
         }
      };
   }

   private LLICIRecord findPrevious(int var1) {
      LLICIRecord var2 = this.headRecord;

      while(var2.next != null && var2.next.contained < var1) {
         var2 = var2.next;
      }

      return var2;
   }
}
