package org.napile.primitive.maps.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import org.napile.primitive.Variables;
import org.napile.primitive.collections.LongCollection;
import org.napile.primitive.collections.abstracts.AbstractLongCollection;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.iterators.LongIterator;
import org.napile.primitive.maps.CIntLongMap;
import org.napile.primitive.maps.IntLongMap;
import org.napile.primitive.maps.abstracts.AbstractIntLongMap;
import org.napile.primitive.pair.IntLongPair;
import org.napile.primitive.pair.impl.IntLongPairImpl;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.abstracts.AbstractIntSet;

public class CHashIntLongMap extends AbstractIntLongMap implements CIntLongMap, Serializable {
   static final int DEFAULT_INITIAL_CAPACITY = 16;
   static final float DEFAULT_LOAD_FACTOR = 0.75F;
   static final int DEFAULT_CONCURRENCY_LEVEL = 16;
   static final int MAXIMUM_CAPACITY = 1073741824;
   static final int MAX_SEGMENTS = 65536;
   static final int RETRIES_BEFORE_LOCK = 2;
   final int segmentMask;
   final int segmentShift;
   final CHashIntLongMap.Segment[] segments;
   transient IntSet keySet;
   transient Set<IntLongPair> entrySet;
   transient LongCollection values;

   private static int hash(int h) {
      h += h << 15 ^ -12931;
      h ^= h >>> 10;
      h += h << 3;
      h ^= h >>> 6;
      h += (h << 2) + (h << 14);
      return h ^ h >>> 16;
   }

   final CHashIntLongMap.Segment segmentFor(int hash) {
      return this.segments[hash >>> this.segmentShift & this.segmentMask];
   }

   public CHashIntLongMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
      if (loadFactor > 0.0F && initialCapacity >= 0 && concurrencyLevel > 0) {
         if (concurrencyLevel > 65536) {
            concurrencyLevel = 65536;
         }

         int sshift = 0;

         int ssize;
         for(ssize = 1; ssize < concurrencyLevel; ssize <<= 1) {
            ++sshift;
         }

         this.segmentShift = 32 - sshift;
         this.segmentMask = ssize - 1;
         this.segments = CHashIntLongMap.Segment.newArray(ssize);
         if (initialCapacity > 1073741824) {
            initialCapacity = 1073741824;
         }

         int c = initialCapacity / ssize;
         if (c * ssize < initialCapacity) {
            ++c;
         }

         int cap = 1;

         while(cap < c) {
            cap <<= 1;
         }

         for(int i = 0; i < this.segments.length; ++i) {
            this.segments[i] = new CHashIntLongMap.Segment(cap, loadFactor);
         }
      } else {
         throw new IllegalArgumentException();
      }
   }

   public CHashIntLongMap(int initialCapacity, float loadFactor) {
      this(initialCapacity, loadFactor, 16);
   }

   public CHashIntLongMap(int initialCapacity) {
      this(initialCapacity, 0.75F, 16);
   }

   public CHashIntLongMap() {
      this(16, 0.75F, 16);
   }

   public CHashIntLongMap(IntLongMap m) {
      this(Math.max((int)((float)m.size() / 0.75F) + 1, 16), 0.75F, 16);
      this.putAll(m);
   }

   @Override
   public boolean isEmpty() {
      CHashIntLongMap.Segment[] segments = this.segments;
      int[] mc = new int[segments.length];
      int mcsum = 0;

      for(int i = 0; i < segments.length; ++i) {
         if (segments[i].count != 0) {
            return false;
         }

         mcsum += mc[i] = segments[i].modCount;
      }

      if (mcsum != 0) {
         for(int i = 0; i < segments.length; ++i) {
            if (segments[i].count != 0 || mc[i] != segments[i].modCount) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   public int size() {
      CHashIntLongMap.Segment[] segments = this.segments;
      long sum = 0L;
      long check = 0L;
      int[] mc = new int[segments.length];

      for(int k = 0; k < 2; ++k) {
         check = 0L;
         sum = 0L;
         int mcsum = 0;

         for(int i = 0; i < segments.length; ++i) {
            sum += (long)segments[i].count;
            mcsum += mc[i] = segments[i].modCount;
         }

         if (mcsum != 0) {
            for(int i = 0; i < segments.length; ++i) {
               check += (long)segments[i].count;
               if (mc[i] != segments[i].modCount) {
                  check = -1L;
                  break;
               }
            }
         }

         if (check == sum) {
            break;
         }
      }

      if (check != sum) {
         sum = 0L;

         for(int i = 0; i < segments.length; ++i) {
            segments[i].lock();
         }

         for(int i = 0; i < segments.length; ++i) {
            sum += (long)segments[i].count;
         }

         for(int i = 0; i < segments.length; ++i) {
            segments[i].unlock();
         }
      }

      return sum > 2147483647L ? Integer.MAX_VALUE : (int)sum;
   }

   @Override
   public long get(int key) {
      int hash = hash(key);
      return this.segmentFor(hash).get(key, hash);
   }

   @Override
   public boolean containsKey(int key) {
      int hash = hash(key);
      return this.segmentFor(hash).containsKey(key, hash);
   }

   // $VF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   @Override
   public boolean containsValue(long value) {
      CHashIntLongMap.Segment[] segments = this.segments;
      int[] mc = new int[segments.length];

      for(int k = 0; k < 2; ++k) {
         int sum = 0;
         int mcsum = 0;

         for(int i = 0; i < segments.length; ++i) {
            int c = segments[i].count;
            mcsum += mc[i] = segments[i].modCount;
            if (segments[i].containsValue(value)) {
               return true;
            }
         }

         boolean cleanSweep = true;
         if (mcsum != 0) {
            for(int i = 0; i < segments.length; ++i) {
               int c = segments[i].count;
               if (mc[i] != segments[i].modCount) {
                  cleanSweep = false;
                  break;
               }
            }
         }

         if (cleanSweep) {
            return false;
         }
      }

      for(int i = 0; i < segments.length; ++i) {
         segments[i].lock();
      }

      boolean found = false;
      boolean var14 = false /* VF: Semaphore variable */;

      try {
         var14 = true;
         int var18 = 0;

         while(true) {
            if (var18 >= segments.length) {
               var14 = false;
               break;
            }

            if (segments[var18].containsValue(value)) {
               found = true;
               var14 = false;
               break;
            }

            ++var18;
         }
      } finally {
         if (var14) {
            for(int i = 0; i < segments.length; ++i) {
               segments[i].unlock();
            }
         }
      }

      for(int i = 0; i < segments.length; ++i) {
         segments[i].unlock();
      }

      return found;
   }

   @Override
   public long put(int key, long value) {
      int hash = hash(key);
      return this.segmentFor(hash).put(key, hash, value, false);
   }

   @Override
   public long putIfAbsent(int key, long value) {
      int hash = hash(key);
      return this.segmentFor(hash).put(key, hash, value, true);
   }

   @Override
   public void putAll(IntLongMap m) {
      for(IntLongPair e : m.entrySet()) {
         this.put(e.getKey(), e.getValue());
      }
   }

   @Override
   public long remove(int key) {
      int hash = hash(key);
      return this.segmentFor(hash).remove(key, hash);
   }

   @Override
   public boolean remove(int key, long value) {
      int hash = hash(key);
      return this.segmentFor(hash).removeWithValue(key, hash, value);
   }

   @Override
   public boolean replace(int key, long oldValue, long newValue) {
      int hash = hash(key);
      return this.segmentFor(hash).replace(key, hash, oldValue, newValue);
   }

   @Override
   public long replace(int key, long value) {
      int hash = hash(key);
      return this.segmentFor(hash).replace(key, hash, value);
   }

   @Override
   public void clear() {
      for(int i = 0; i < this.segments.length; ++i) {
         this.segments[i].clear();
      }
   }

   @Override
   public int[] keys() {
      return this.keySet().toArray();
   }

   @Override
   public int[] keys(int[] array) {
      return this.keySet().toArray(array);
   }

   @Override
   public IntSet keySet() {
      IntSet ks = this.keySet;
      return ks != null ? ks : (this.keySet = new CHashIntLongMap.KeySet());
   }

   @Override
   public long[] values() {
      return this.valueCollection().toArray();
   }

   @Override
   public long[] values(long[] array) {
      return this.valueCollection().toArray(array);
   }

   @Override
   public LongCollection valueCollection() {
      LongCollection vs = this.values;
      return vs != null ? vs : (this.values = new CHashIntLongMap.Values());
   }

   @Override
   public Set<IntLongPair> entrySet() {
      Set<IntLongPair> es = this.entrySet;
      return es != null ? es : (this.entrySet = new CHashIntLongMap.EntrySet());
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      s.defaultWriteObject();

      for(int k = 0; k < this.segments.length; ++k) {
         CHashIntLongMap.Segment seg = this.segments[k];
         seg.lock();

         try {
            CHashIntLongMap.HashEntry[] tab = seg.table;

            for(int i = 0; i < tab.length; ++i) {
               for(CHashIntLongMap.HashEntry e = tab[i]; e != null; e = e.next) {
                  s.writeInt(e.key);
                  s.writeObject(e.value);
               }
            }
         } finally {
            seg.unlock();
         }
      }

      s.writeObject(null);
      s.writeObject(null);
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();

      for(int i = 0; i < this.segments.length; ++i) {
         this.segments[i].setTable(new CHashIntLongMap.HashEntry[1]);
      }

      while(true) {
         try {
            int key = s.readInt();
            long value = s.readLong();
            this.put(key, value);
         } catch (IOException var5) {
            return;
         }
      }
   }

   final class EntryIterator extends CHashIntLongMap.HashIterator implements Iterator<IntLongPair> {
      public IntLongPair next() {
         CHashIntLongMap.HashEntry e = super.nextEntry();
         return CHashIntLongMap.this.new WriteThroughEntry(e.key, e.value);
      }
   }

   final class EntrySet extends AbstractSet<IntLongPair> {
      @Override
      public Iterator<IntLongPair> iterator() {
         return CHashIntLongMap.this.new EntryIterator();
      }

      @Override
      public boolean contains(Object o) {
         if (!(o instanceof IntLongPair)) {
            return false;
         } else {
            IntLongPair e = (IntLongPair)o;
            long v = CHashIntLongMap.this.get(e.getKey());
            return v == e.getValue();
         }
      }

      @Override
      public boolean remove(Object o) {
         if (!(o instanceof IntLongPair)) {
            return false;
         } else {
            IntLongPair e = (IntLongPair)o;
            return CHashIntLongMap.this.remove(e.getKey(), e.getValue());
         }
      }

      @Override
      public int size() {
         return CHashIntLongMap.this.size();
      }

      @Override
      public void clear() {
         CHashIntLongMap.this.clear();
      }
   }

   static final class HashEntry {
      final int key;
      final int hash;
      volatile long value;
      final CHashIntLongMap.HashEntry next;

      HashEntry(int key, CHashIntLongMap.HashEntry next, long value) {
         this.key = key;
         this.hash = CHashIntLongMap.hash(key);
         this.next = next;
         this.value = value;
      }

      static CHashIntLongMap.HashEntry[] newArray(int i) {
         return new CHashIntLongMap.HashEntry[i];
      }
   }

   abstract class HashIterator {
      int nextSegmentIndex = CHashIntLongMap.this.segments.length - 1;
      int nextTableIndex = -1;
      CHashIntLongMap.HashEntry[] currentTable;
      CHashIntLongMap.HashEntry nextEntry;
      CHashIntLongMap.HashEntry lastReturned;

      HashIterator() {
         this.advance();
      }

      final void advance() {
         if (this.nextEntry == null || (this.nextEntry = this.nextEntry.next) == null) {
            while(this.nextTableIndex >= 0) {
               if ((this.nextEntry = this.currentTable[this.nextTableIndex--]) != null) {
                  return;
               }
            }

            while(this.nextSegmentIndex >= 0) {
               CHashIntLongMap.Segment seg = CHashIntLongMap.this.segments[this.nextSegmentIndex--];
               if (seg.count != 0) {
                  this.currentTable = seg.table;

                  for(int j = this.currentTable.length - 1; j >= 0; --j) {
                     if ((this.nextEntry = this.currentTable[j]) != null) {
                        this.nextTableIndex = j - 1;
                        return;
                     }
                  }
               }
            }
         }
      }

      public boolean hasNext() {
         return this.nextEntry != null;
      }

      CHashIntLongMap.HashEntry nextEntry() {
         if (this.nextEntry == null) {
            throw new NoSuchElementException();
         } else {
            this.lastReturned = this.nextEntry;
            this.advance();
            return this.lastReturned;
         }
      }

      public void remove() {
         if (this.lastReturned == null) {
            throw new IllegalStateException();
         } else {
            CHashIntLongMap.this.remove(this.lastReturned.key);
            this.lastReturned = null;
         }
      }
   }

   final class KeyIterator extends CHashIntLongMap.HashIterator implements IntIterator {
      @Override
      public int next() {
         return super.nextEntry().key;
      }
   }

   final class KeySet extends AbstractIntSet {
      @Override
      public IntIterator iterator() {
         return CHashIntLongMap.this.new KeyIterator();
      }

      @Override
      public int size() {
         return CHashIntLongMap.this.size();
      }

      @Override
      public boolean contains(int o) {
         return CHashIntLongMap.this.containsKey(o);
      }

      @Override
      public boolean remove(int o) {
         int hash = CHashIntLongMap.hash(o);
         return CHashIntLongMap.this.segmentFor(hash).removeTrueIfRemoved(o, hash);
      }

      @Override
      public void clear() {
         CHashIntLongMap.this.clear();
      }
   }

   static final class Segment extends ReentrantLock implements Serializable {
      private static final long serialVersionUID = 2249069246763182397L;
      transient volatile int count;
      transient int modCount;
      transient int threshold;
      transient volatile CHashIntLongMap.HashEntry[] table;
      final float loadFactor;

      Segment(int initialCapacity, float lf) {
         this.loadFactor = lf;
         this.setTable(CHashIntLongMap.HashEntry.newArray(initialCapacity));
      }

      static CHashIntLongMap.Segment[] newArray(int i) {
         return new CHashIntLongMap.Segment[i];
      }

      void setTable(CHashIntLongMap.HashEntry[] newTable) {
         this.threshold = (int)((float)newTable.length * this.loadFactor);
         this.table = newTable;
      }

      CHashIntLongMap.HashEntry getFirst(int hash) {
         CHashIntLongMap.HashEntry[] tab = this.table;
         return tab[hash & tab.length - 1];
      }

      long readValueUnderLock(CHashIntLongMap.HashEntry e) {
         this.lock();

         long var2;
         try {
            var2 = e.value;
         } finally {
            this.unlock();
         }

         return var2;
      }

      long get(int key, int hash) {
         if (this.count != 0) {
            for(CHashIntLongMap.HashEntry e = this.getFirst(hash); e != null; e = e.next) {
               if (e.key == key) {
                  return e.value;
               }
            }
         }

         return (long)Variables.RETURN_LONG_VALUE_IF_NOT_FOUND;
      }

      boolean containsKey(int key, int hash) {
         if (this.count != 0) {
            for(CHashIntLongMap.HashEntry e = this.getFirst(hash); e != null; e = e.next) {
               if (e.key == key) {
                  return true;
               }
            }
         }

         return false;
      }

      boolean containsValue(long value) {
         if (this.count != 0) {
            CHashIntLongMap.HashEntry[] tab = this.table;
            int len = tab.length;

            for(int i = 0; i < len; ++i) {
               for(CHashIntLongMap.HashEntry e = tab[i]; e != null; e = e.next) {
                  long v = e.value;
                  if (value == v) {
                     return true;
                  }
               }
            }
         }

         return false;
      }

      boolean replace(int key, int hash, long oldValue, long newValue) {
         this.lock();

         boolean var9;
         try {
            CHashIntLongMap.HashEntry e = this.getFirst(hash);

            while(e != null && key != e.key) {
               e = e.next;
            }

            boolean replaced = false;
            if (e != null && oldValue == e.value) {
               replaced = true;
               e.value = newValue;
            }

            var9 = replaced;
         } finally {
            this.unlock();
         }

         return var9;
      }

      long replace(int key, int hash, long newValue) {
         this.lock();

         long var8;
         try {
            CHashIntLongMap.HashEntry e = this.getFirst(hash);

            while(e != null && key != e.key) {
               e = e.next;
            }

            long oldValue = (long)Variables.RETURN_LONG_VALUE_IF_NOT_FOUND;
            if (e != null) {
               oldValue = e.value;
               e.value = newValue;
            }

            var8 = oldValue;
         } finally {
            this.unlock();
         }

         return var8;
      }

      long put(int key, int hash, long value, boolean onlyIfAbsent) {
         this.lock();

         long var13;
         try {
            int c = this.count;
            if (c++ > this.threshold) {
               this.rehash();
            }

            CHashIntLongMap.HashEntry[] tab = this.table;
            int index = hash & tab.length - 1;
            CHashIntLongMap.HashEntry first = tab[index];
            CHashIntLongMap.HashEntry e = first;

            while(e != null && key != e.key) {
               e = e.next;
            }

            long oldValue;
            if (e != null) {
               oldValue = e.value;
               if (!onlyIfAbsent) {
                  e.value = value;
               }
            } else {
               oldValue = (long)Variables.RETURN_LONG_VALUE_IF_NOT_FOUND;
               ++this.modCount;
               tab[index] = new CHashIntLongMap.HashEntry(key, first, value);
               this.count = c;
            }

            var13 = oldValue;
         } finally {
            this.unlock();
         }

         return var13;
      }

      void rehash() {
         CHashIntLongMap.HashEntry[] oldTable = this.table;
         int oldCapacity = oldTable.length;
         if (oldCapacity < 1073741824) {
            CHashIntLongMap.HashEntry[] newTable = CHashIntLongMap.HashEntry.newArray(oldCapacity << 1);
            this.threshold = (int)((float)newTable.length * this.loadFactor);
            int sizeMask = newTable.length - 1;

            for(int i = 0; i < oldCapacity; ++i) {
               CHashIntLongMap.HashEntry e = oldTable[i];
               if (e != null) {
                  CHashIntLongMap.HashEntry next = e.next;
                  int idx = e.hash & sizeMask;
                  if (next == null) {
                     newTable[idx] = e;
                  } else {
                     CHashIntLongMap.HashEntry lastRun = e;
                     int lastIdx = idx;

                     for(CHashIntLongMap.HashEntry last = next; last != null; last = last.next) {
                        int k = last.hash & sizeMask;
                        if (k != lastIdx) {
                           lastIdx = k;
                           lastRun = last;
                        }
                     }

                     newTable[lastIdx] = lastRun;

                     for(CHashIntLongMap.HashEntry p = e; p != lastRun; p = p.next) {
                        int k = p.hash & sizeMask;
                        CHashIntLongMap.HashEntry n = newTable[k];
                        newTable[k] = new CHashIntLongMap.HashEntry(p.key, n, p.value);
                     }
                  }
               }
            }

            this.table = newTable;
         }
      }

      long remove(int key, int hash) {
         this.lock();

         long var15;
         try {
            int c = this.count - 1;
            CHashIntLongMap.HashEntry[] tab = this.table;
            int index = hash & tab.length - 1;
            CHashIntLongMap.HashEntry first = tab[index];
            CHashIntLongMap.HashEntry e = first;

            while(e != null && key != e.key) {
               e = e.next;
            }

            long oldValue = (long)Variables.RETURN_LONG_VALUE_IF_NOT_FOUND;
            if (e != null) {
               oldValue = e.value;
               ++this.modCount;
               CHashIntLongMap.HashEntry newFirst = e.next;

               for(CHashIntLongMap.HashEntry p = first; p != e; p = p.next) {
                  newFirst = new CHashIntLongMap.HashEntry(p.key, newFirst, p.value);
               }

               tab[index] = newFirst;
               this.count = c;
            }

            var15 = oldValue;
         } finally {
            this.unlock();
         }

         return var15;
      }

      boolean removeTrueIfRemoved(int key, int hash) {
         this.lock();

         boolean var13;
         try {
            int c = this.count - 1;
            CHashIntLongMap.HashEntry[] tab = this.table;
            int index = hash & tab.length - 1;
            CHashIntLongMap.HashEntry first = tab[index];
            CHashIntLongMap.HashEntry e = first;

            while(e != null && key != e.key) {
               e = e.next;
            }

            if (e != null) {
               ++this.modCount;
               CHashIntLongMap.HashEntry newFirst = e.next;

               for(CHashIntLongMap.HashEntry p = first; p != e; p = p.next) {
                  newFirst = new CHashIntLongMap.HashEntry(p.key, newFirst, p.value);
               }

               tab[index] = newFirst;
               this.count = c;
            }

            var13 = e != null;
         } finally {
            this.unlock();
         }

         return var13;
      }

      boolean removeWithValue(int key, int hash, long value) {
         this.lock();

         boolean var17;
         try {
            int c = this.count - 1;
            CHashIntLongMap.HashEntry[] tab = this.table;
            int index = hash & tab.length - 1;
            CHashIntLongMap.HashEntry first = tab[index];
            CHashIntLongMap.HashEntry e = first;

            while(e != null && key != e.key) {
               e = e.next;
            }

            if (e != null) {
               long v = e.value;
               if (value == v) {
                  ++this.modCount;
                  CHashIntLongMap.HashEntry newFirst = e.next;

                  for(CHashIntLongMap.HashEntry p = first; p != e; p = p.next) {
                     newFirst = new CHashIntLongMap.HashEntry(p.key, newFirst, p.value);
                  }

                  tab[index] = newFirst;
                  this.count = c;
               }
            }

            var17 = e != null;
         } finally {
            this.unlock();
         }

         return var17;
      }

      void clear() {
         if (this.count != 0) {
            this.lock();

            try {
               CHashIntLongMap.HashEntry[] tab = this.table;

               for(int i = 0; i < tab.length; ++i) {
                  tab[i] = null;
               }

               ++this.modCount;
               this.count = 0;
            } finally {
               this.unlock();
            }
         }
      }
   }

   final class ValueIterator extends CHashIntLongMap.HashIterator implements LongIterator {
      @Override
      public long next() {
         return super.nextEntry().value;
      }
   }

   final class Values extends AbstractLongCollection {
      @Override
      public LongIterator iterator() {
         return CHashIntLongMap.this.new ValueIterator();
      }

      @Override
      public int size() {
         return CHashIntLongMap.this.size();
      }

      @Override
      public boolean contains(long o) {
         return CHashIntLongMap.this.containsValue(o);
      }

      @Override
      public void clear() {
         CHashIntLongMap.this.clear();
      }
   }

   final class WriteThroughEntry extends IntLongPairImpl {
      WriteThroughEntry(int k, long v) {
         super(k, v);
      }

      @Override
      public long setValue(long value) {
         long v = super.setValue(value);
         CHashIntLongMap.this.put(this.getKey(), value);
         return v;
      }
   }
}
