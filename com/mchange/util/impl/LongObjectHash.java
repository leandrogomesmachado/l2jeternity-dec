package com.mchange.util.impl;

import com.mchange.util.LongObjectMap;

public class LongObjectHash implements LongObjectMap {
   LOHRecord[] records;
   float load_factor;
   long threshold;
   long size;

   public LongObjectHash(int var1, float var2) {
      this.records = new LOHRecord[var1];
      this.load_factor = var2;
      this.threshold = (long)(var2 * (float)var1);
   }

   public LongObjectHash() {
      this(101, 0.75F);
   }

   @Override
   public synchronized Object get(long var1) {
      int var3 = (int)(var1 % (long)this.records.length);
      Object var4 = null;
      if (this.records[var3] != null) {
         var4 = this.records[var3].get(var1);
      }

      return var4;
   }

   @Override
   public synchronized void put(long var1, Object var3) {
      int var4 = (int)(var1 % (long)this.records.length);
      if (this.records[var4] == null) {
         this.records[var4] = new LOHRecord((long)var4);
      }

      boolean var5 = this.records[var4].add(var1, var3, true);
      if (!var5) {
         ++this.size;
      }

      if (this.size > this.threshold) {
         this.rehash();
      }
   }

   @Override
   public synchronized boolean putNoReplace(long var1, Object var3) {
      int var4 = (int)(var1 % (long)this.records.length);
      if (this.records[var4] == null) {
         this.records[var4] = new LOHRecord((long)var4);
      }

      boolean var5 = this.records[var4].add(var1, var3, false);
      if (var5) {
         return false;
      } else {
         ++this.size;
         if (this.size > this.threshold) {
            this.rehash();
         }

         return true;
      }
   }

   @Override
   public long getSize() {
      return this.size;
   }

   @Override
   public synchronized boolean containsLong(long var1) {
      int var3 = (int)(var1 % (long)this.records.length);
      return this.records[var3] != null && this.records[var3].findLong(var1) != null;
   }

   @Override
   public synchronized Object remove(long var1) {
      LOHRecord var3 = this.records[(int)(var1 % (long)this.records.length)];
      Object var4 = var3 == null ? null : var3.remove(var1);
      if (var4 != null) {
         --this.size;
      }

      return var4;
   }

   protected void rehash() {
      if ((long)this.records.length * 2L > 2147483647L) {
         throw new Error("Implementation of LongObjectHash allows a capacity of only 2147483647");
      } else {
         LOHRecord[] var1 = new LOHRecord[this.records.length * 2];

         for(int var2 = 0; var2 < this.records.length; ++var2) {
            if (this.records[var2] != null) {
               var1[var2] = this.records[var2];
               var1[var2 * 2] = this.records[var2].split(var1.length);
            }
         }

         this.records = var1;
         this.threshold = (long)(this.load_factor * (float)this.records.length);
      }
   }
}
