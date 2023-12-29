package org.json.zip;

import java.util.HashMap;
import org.json.Kim;

class Keep implements None, PostMortem {
   private int capacity;
   protected int length;
   private Object[] list;
   private HashMap<Object, Integer> map;
   private int power;
   private long[] ticks;

   public Keep(int bits) {
      this.capacity = 1 << bits;
      this.length = 0;
      this.power = 0;
      this.ticks = new long[this.capacity];
      this.list = new Object[this.capacity];
      this.map = new HashMap<>(this.capacity);
   }

   public static long age(long ticks) {
      return ticks >= 32L ? 16L : ticks / 2L;
   }

   public int bitsize() {
      while(1 << this.power < this.length) {
         ++this.power;
      }

      return this.power;
   }

   public void tick(int integer) {
      this.ticks[integer]++;
   }

   private void compact() {
      int from = 0;

      int to;
      for(to = 0; from < this.capacity; ++from) {
         Object key = this.list[from];
         long usage = age(this.ticks[from]);
         if (usage > 0L) {
            this.ticks[to] = usage;
            this.list[to] = key;
            this.map.put(key, to);
            ++to;
         } else {
            this.map.remove(key);
         }
      }

      if (to < this.capacity) {
         this.length = to;
      } else {
         this.map.clear();
         this.length = 0;
      }

      this.power = 0;
   }

   public int find(Object key) {
      Object o = this.map.get(key);
      return o instanceof Integer ? (Integer)o : -1;
   }

   @Override
   public boolean postMortem(PostMortem pm) {
      Keep that = (Keep)pm;
      if (this.length != that.length) {
         JSONzip.log(this.length + " <> " + that.length);
         return false;
      } else {
         for(int i = 0; i < this.length; ++i) {
            boolean b;
            if (this.list[i] instanceof Kim) {
               b = this.list[i].equals(that.list[i]);
            } else {
               Object o = this.list[i];
               Object q = that.list[i];
               if (o instanceof Number) {
                  o = o.toString();
               }

               if (q instanceof Number) {
                  q = q.toString();
               }

               b = o.equals(q);
            }

            if (!b) {
               JSONzip.log("\n[" + i + "]\n " + this.list[i] + "\n " + that.list[i] + "\n " + this.ticks[i] + "\n " + that.ticks[i]);
               return false;
            }
         }

         return true;
      }
   }

   public void register(Object value) {
      if (this.length >= this.capacity) {
         this.compact();
      }

      this.list[this.length] = value;
      this.map.put(value, this.length);
      this.ticks[this.length] = 1L;
      ++this.length;
   }

   public Object value(int integer) {
      return this.list[integer];
   }
}
