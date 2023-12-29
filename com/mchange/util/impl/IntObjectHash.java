package com.mchange.util.impl;

import com.mchange.util.IntEnumeration;
import com.mchange.util.IntObjectMap;
import java.util.NoSuchElementException;

public class IntObjectHash implements IntObjectMap {
   IOHRecord[] records;
   int init_capacity;
   float load_factor;
   int threshold;
   int size;

   public IntObjectHash(int var1, float var2) {
      this.init_capacity = var1;
      this.load_factor = var2;
      this.clear();
   }

   public IntObjectHash() {
      this(101, 0.75F);
   }

   @Override
   public synchronized Object get(int var1) {
      int var2 = this.getIndex(var1);
      Object var3 = null;
      if (this.records[var2] != null) {
         var3 = this.records[var2].get(var1);
      }

      return var3;
   }

   @Override
   public synchronized void put(int var1, Object var2) {
      if (var2 == null) {
         throw new NullPointerException("Null values not permitted.");
      } else {
         int var3 = this.getIndex(var1);
         if (this.records[var3] == null) {
            this.records[var3] = new IOHRecord(var3);
         }

         boolean var4 = this.records[var3].add(var1, var2, true);
         if (!var4) {
            ++this.size;
         }

         if (this.size > this.threshold) {
            this.rehash();
         }
      }
   }

   @Override
   public synchronized boolean putNoReplace(int var1, Object var2) {
      if (var2 == null) {
         throw new NullPointerException("Null values not permitted.");
      } else {
         int var3 = this.getIndex(var1);
         if (this.records[var3] == null) {
            this.records[var3] = new IOHRecord(var3);
         }

         boolean var4 = this.records[var3].add(var1, var2, false);
         if (var4) {
            return false;
         } else {
            ++this.size;
            if (this.size > this.threshold) {
               this.rehash();
            }

            return true;
         }
      }
   }

   @Override
   public int getSize() {
      return this.size;
   }

   @Override
   public synchronized boolean containsInt(int var1) {
      int var2 = this.getIndex(var1);
      return this.records[var2] != null && this.records[var2].findInt(var1) != null;
   }

   private int getIndex(int var1) {
      return Math.abs(var1 % this.records.length);
   }

   @Override
   public synchronized Object remove(int var1) {
      IOHRecord var2 = this.records[this.getIndex(var1)];
      Object var3 = var2 == null ? null : var2.remove(var1);
      if (var3 != null) {
         --this.size;
      }

      return var3;
   }

   @Override
   public synchronized void clear() {
      this.records = new IOHRecord[this.init_capacity];
      this.threshold = (int)(this.load_factor * (float)this.init_capacity);
      this.size = 0;
   }

   @Override
   public synchronized IntEnumeration ints() {
      return new IntEnumerationHelperBase() {
         int index = -1;
         IOHRecElem finger;

         {
            this.nextIndex();
         }

         @Override
         public boolean hasMoreInts() {
            return this.index < IntObjectHash.this.records.length;
         }

         @Override
         public int nextInt() {
            try {
               int var1 = this.finger.num;
               this.findNext();
               return var1;
            } catch (NullPointerException var2) {
               throw new NoSuchElementException();
            }
         }

         private void findNext() {
            if (this.finger.next != null) {
               this.finger = this.finger.next;
            } else {
               this.nextIndex();
            }
         }

         private void nextIndex() {
            try {
               int var1 = IntObjectHash.this.records.length;

               do {
                  ++this.index;
               } while(IntObjectHash.this.records[this.index] == null && this.index <= var1);

               this.finger = IntObjectHash.this.records[this.index].next;
            } catch (ArrayIndexOutOfBoundsException var2) {
               this.finger = null;
            }
         }
      };
   }

   protected void rehash() {
      IOHRecord[] var1 = new IOHRecord[this.records.length * 2];

      for(int var2 = 0; var2 < this.records.length; ++var2) {
         if (this.records[var2] != null) {
            var1[var2] = this.records[var2];
            var1[var2 * 2] = this.records[var2].split(var1.length);
         }
      }

      this.records = var1;
      this.threshold = (int)(this.load_factor * (float)this.records.length);
   }
}
