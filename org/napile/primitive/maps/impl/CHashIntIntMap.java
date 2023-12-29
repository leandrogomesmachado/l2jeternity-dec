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
import org.napile.primitive.collections.IntCollection;
import org.napile.primitive.collections.abstracts.AbstractIntCollection;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.maps.CIntIntMap;
import org.napile.primitive.maps.IntIntMap;
import org.napile.primitive.maps.abstracts.AbstractIntIntMap;
import org.napile.primitive.pair.IntIntPair;
import org.napile.primitive.pair.impl.IntIntPairImpl;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.abstracts.AbstractIntSet;

public class CHashIntIntMap extends AbstractIntIntMap implements CIntIntMap, Serializable {
   static final int DEFAULT_INITIAL_CAPACITY = 16;
   static final float DEFAULT_LOAD_FACTOR = 0.75F;
   static final int DEFAULT_CONCURRENCY_LEVEL = 16;
   static final int MAXIMUM_CAPACITY = 1073741824;
   static final int MAX_SEGMENTS = 65536;
   static final int RETRIES_BEFORE_LOCK = 2;
   final int segmentMask;
   final int segmentShift;
   final CHashIntIntMap.Segment[] segments;
   transient IntSet keySet;
   transient Set<IntIntPair> entrySet;
   transient IntCollection values;

   private static int hash(int h) {
      h += h << 15 ^ -12931;
      h ^= h >>> 10;
      h += h << 3;
      h ^= h >>> 6;
      h += (h << 2) + (h << 14);
      return h ^ h >>> 16;
   }

   final CHashIntIntMap.Segment segmentFor(int hash) {
      return this.segments[hash >>> this.segmentShift & this.segmentMask];
   }

   public CHashIntIntMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
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
         this.segments = CHashIntIntMap.Segment.newArray(ssize);
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
            this.segments[i] = new CHashIntIntMap.Segment(cap, loadFactor);
         }
      } else {
         throw new IllegalArgumentException();
      }
   }

   public CHashIntIntMap(int initialCapacity, float loadFactor) {
      this(initialCapacity, loadFactor, 16);
   }

   public CHashIntIntMap(int initialCapacity) {
      this(initialCapacity, 0.75F, 16);
   }

   public CHashIntIntMap() {
      this(16, 0.75F, 16);
   }

   public CHashIntIntMap(IntIntMap m) {
      this(Math.max((int)((float)m.size() / 0.75F) + 1, 16), 0.75F, 16);
      this.putAll(m);
   }

   @Override
   public boolean isEmpty() {
      CHashIntIntMap.Segment[] segments = this.segments;
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
      CHashIntIntMap.Segment[] segments = this.segments;
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
   public int get(int key) {
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
   public boolean containsValue(int value) {
      CHashIntIntMap.Segment[] segments = this.segments;
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
      boolean var13 = false /* VF: Semaphore variable */;

      try {
         var13 = true;
         int var17 = 0;

         while(true) {
            if (var17 >= segments.length) {
               var13 = false;
               break;
            }

            if (segments[var17].containsValue(value)) {
               found = true;
               var13 = false;
               break;
            }

            ++var17;
         }
      } finally {
         if (var13) {
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
   public int put(int key, int value) {
      int hash = hash(key);
      return this.segmentFor(hash).put(key, hash, value, false);
   }

   @Override
   public int putIfAbsent(int key, int value) {
      int hash = hash(key);
      return this.segmentFor(hash).put(key, hash, value, true);
   }

   @Override
   public void putAll(IntIntMap m) {
      for(IntIntPair e : m.entrySet()) {
         this.put(e.getKey(), e.getValue());
      }
   }

   @Override
   public int remove(int key) {
      int hash = hash(key);
      return this.segmentFor(hash).remove(key, hash);
   }

   @Override
   public boolean remove(int key, int value) {
      int hash = hash(key);
      return this.segmentFor(hash).removeWithValue(key, hash, value);
   }

   @Override
   public boolean replace(int key, int oldValue, int newValue) {
      int hash = hash(key);
      return this.segmentFor(hash).replace(key, hash, oldValue, newValue);
   }

   @Override
   public int replace(int key, int value) {
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
      return ks != null ? ks : (this.keySet = new CHashIntIntMap.KeySet());
   }

   @Override
   public int[] values() {
      return this.valueCollection().toArray();
   }

   @Override
   public int[] values(int[] array) {
      return this.valueCollection().toArray(array);
   }

   @Override
   public IntCollection valueCollection() {
      IntCollection vs = this.values;
      return vs != null ? vs : (this.values = new CHashIntIntMap.Values());
   }

   @Override
   public Set<IntIntPair> entrySet() {
      Set<IntIntPair> es = this.entrySet;
      return es != null ? es : (this.entrySet = new CHashIntIntMap.EntrySet());
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      s.defaultWriteObject();

      for(int k = 0; k < this.segments.length; ++k) {
         CHashIntIntMap.Segment seg = this.segments[k];
         seg.lock();

         try {
            CHashIntIntMap.HashEntry[] tab = seg.table;

            for(int i = 0; i < tab.length; ++i) {
               for(CHashIntIntMap.HashEntry e = tab[i]; e != null; e = e.next) {
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
         this.segments[i].setTable(new CHashIntIntMap.HashEntry[1]);
      }

      while(true) {
         try {
            int key = s.readInt();
            int value = s.readInt();
            this.put(key, value);
         } catch (IOException var4) {
            return;
         }
      }
   }

   final class EntryIterator extends CHashIntIntMap.HashIterator implements Iterator<IntIntPair> {
      public IntIntPair next() {
         CHashIntIntMap.HashEntry e = super.nextEntry();
         return CHashIntIntMap.this.new WriteThroughEntry(e.key, e.value);
      }
   }

   final class EntrySet extends AbstractSet<IntIntPair> {
      @Override
      public Iterator<IntIntPair> iterator() {
         return CHashIntIntMap.this.new EntryIterator();
      }

      @Override
      public boolean contains(Object o) {
         if (!(o instanceof IntIntPair)) {
            return false;
         } else {
            IntIntPair e = (IntIntPair)o;
            int v = CHashIntIntMap.this.get(e.getKey());
            return v == e.getValue();
         }
      }

      @Override
      public boolean remove(Object o) {
         if (!(o instanceof IntIntPair)) {
            return false;
         } else {
            IntIntPair e = (IntIntPair)o;
            return CHashIntIntMap.this.remove(e.getKey(), e.getValue());
         }
      }

      @Override
      public int size() {
         return CHashIntIntMap.this.size();
      }

      @Override
      public void clear() {
         CHashIntIntMap.this.clear();
      }
   }

   static final class HashEntry {
      final int key;
      final int hash;
      volatile int value;
      final CHashIntIntMap.HashEntry next;

      HashEntry(int key, CHashIntIntMap.HashEntry next, int value) {
         this.key = key;
         this.hash = CHashIntIntMap.hash(key);
         this.next = next;
         this.value = value;
      }

      static CHashIntIntMap.HashEntry[] newArray(int i) {
         return new CHashIntIntMap.HashEntry[i];
      }
   }

   abstract class HashIterator {
      int nextSegmentIndex = CHashIntIntMap.this.segments.length - 1;
      int nextTableIndex = -1;
      CHashIntIntMap.HashEntry[] currentTable;
      CHashIntIntMap.HashEntry nextEntry;
      CHashIntIntMap.HashEntry lastReturned;

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
               CHashIntIntMap.Segment seg = CHashIntIntMap.this.segments[this.nextSegmentIndex--];
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

      CHashIntIntMap.HashEntry nextEntry() {
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
            CHashIntIntMap.this.remove(this.lastReturned.key);
            this.lastReturned = null;
         }
      }
   }

   final class KeyIterator extends CHashIntIntMap.HashIterator implements IntIterator {
      @Override
      public int next() {
         return super.nextEntry().key;
      }
   }

   final class KeySet extends AbstractIntSet {
      @Override
      public IntIterator iterator() {
         return CHashIntIntMap.this.new KeyIterator();
      }

      @Override
      public int size() {
         return CHashIntIntMap.this.size();
      }

      @Override
      public boolean contains(int o) {
         return CHashIntIntMap.this.containsKey(o);
      }

      @Override
      public boolean remove(int o) {
         int hash = CHashIntIntMap.hash(o);
         return CHashIntIntMap.this.segmentFor(hash).removeTrueIfRemoved(o, hash);
      }

      @Override
      public void clear() {
         CHashIntIntMap.this.clear();
      }
   }

   static final class Segment extends ReentrantLock implements Serializable {
      private static final long serialVersionUID = 2249069246763182397L;
      transient volatile int count;
      transient int modCount;
      transient int threshold;
      transient volatile CHashIntIntMap.HashEntry[] table;
      final float loadFactor;

      Segment(int initialCapacity, float lf) {
         this.loadFactor = lf;
         this.setTable(CHashIntIntMap.HashEntry.newArray(initialCapacity));
      }

      static CHashIntIntMap.Segment[] newArray(int i) {
         return new CHashIntIntMap.Segment[i];
      }

      void setTable(CHashIntIntMap.HashEntry[] newTable) {
         this.threshold = (int)((float)newTable.length * this.loadFactor);
         this.table = newTable;
      }

      CHashIntIntMap.HashEntry getFirst(int hash) {
         CHashIntIntMap.HashEntry[] tab = this.table;
         return tab[hash & tab.length - 1];
      }

      int readValueUnderLock(CHashIntIntMap.HashEntry e) {
         this.lock();

         int var2;
         try {
            var2 = e.value;
         } finally {
            this.unlock();
         }

         return var2;
      }

      int get(int key, int hash) {
         if (this.count != 0) {
            for(CHashIntIntMap.HashEntry e = this.getFirst(hash); e != null; e = e.next) {
               if (e.key == key) {
                  return e.value;
               }
            }
         }

         return Variables.RETURN_INT_VALUE_IF_NOT_FOUND;
      }

      boolean containsKey(int key, int hash) {
         if (this.count != 0) {
            for(CHashIntIntMap.HashEntry e = this.getFirst(hash); e != null; e = e.next) {
               if (e.key == key) {
                  return true;
               }
            }
         }

         return false;
      }

      boolean containsValue(int value) {
         if (this.count != 0) {
            CHashIntIntMap.HashEntry[] tab = this.table;
            int len = tab.length;

            for(int i = 0; i < len; ++i) {
               for(CHashIntIntMap.HashEntry e = tab[i]; e != null; e = e.next) {
                  int v = e.value;
                  if (value == v) {
                     return true;
                  }
               }
            }
         }

         return false;
      }

      boolean replace(int key, int hash, int oldValue, int newValue) {
         this.lock();

         boolean var7;
         try {
            CHashIntIntMap.HashEntry e = this.getFirst(hash);

            while(e != null && key != e.key) {
               e = e.next;
            }

            boolean replaced = false;
            if (e != null && oldValue == e.value) {
               replaced = true;
               e.value = newValue;
            }

            var7 = replaced;
         } finally {
            this.unlock();
         }

         return var7;
      }

      int replace(int key, int hash, int newValue) {
         this.lock();

         int var6;
         try {
            CHashIntIntMap.HashEntry e = this.getFirst(hash);

            while(e != null && key != e.key) {
               e = e.next;
            }

            int oldValue = Variables.RETURN_INT_VALUE_IF_NOT_FOUND;
            if (e != null) {
               oldValue = e.value;
               e.value = newValue;
            }

            var6 = oldValue;
         } finally {
            this.unlock();
         }

         return var6;
      }

      int put(int key, int hash, int value, boolean onlyIfAbsent) {
         this.lock();

         int var11;
         try {
            int c = this.count;
            if (c++ > this.threshold) {
               this.rehash();
            }

            CHashIntIntMap.HashEntry[] tab = this.table;
            int index = hash & tab.length - 1;
            CHashIntIntMap.HashEntry first = tab[index];
            CHashIntIntMap.HashEntry e = first;

            while(e != null && key != e.key) {
               e = e.next;
            }

            int oldValue;
            if (e != null) {
               oldValue = e.value;
               if (!onlyIfAbsent) {
                  e.value = value;
               }
            } else {
               oldValue = Variables.RETURN_INT_VALUE_IF_NOT_FOUND;
               ++this.modCount;
               tab[index] = new CHashIntIntMap.HashEntry(key, first, value);
               this.count = c;
            }

            var11 = oldValue;
         } finally {
            this.unlock();
         }

         return var11;
      }

      void rehash() {
         CHashIntIntMap.HashEntry[] oldTable = this.table;
         int oldCapacity = oldTable.length;
         if (oldCapacity < 1073741824) {
            CHashIntIntMap.HashEntry[] newTable = CHashIntIntMap.HashEntry.newArray(oldCapacity << 1);
            this.threshold = (int)((float)newTable.length * this.loadFactor);
            int sizeMask = newTable.length - 1;

            for(int i = 0; i < oldCapacity; ++i) {
               CHashIntIntMap.HashEntry e = oldTable[i];
               if (e != null) {
                  CHashIntIntMap.HashEntry next = e.next;
                  int idx = e.hash & sizeMask;
                  if (next == null) {
                     newTable[idx] = e;
                  } else {
                     CHashIntIntMap.HashEntry lastRun = e;
                     int lastIdx = idx;

                     for(CHashIntIntMap.HashEntry last = next; last != null; last = last.next) {
                        int k = last.hash & sizeMask;
                        if (k != lastIdx) {
                           lastIdx = k;
                           lastRun = last;
                        }
                     }

                     newTable[lastIdx] = lastRun;

                     for(CHashIntIntMap.HashEntry p = e; p != lastRun; p = p.next) {
                        int k = p.hash & sizeMask;
                        CHashIntIntMap.HashEntry n = newTable[k];
                        newTable[k] = new CHashIntIntMap.HashEntry(p.key, n, p.value);
                     }
                  }
               }
            }

            this.table = newTable;
         }
      }

      int remove(int key, int hash) {
         this.lock();

         int var14;
         try {
            int c = this.count - 1;
            CHashIntIntMap.HashEntry[] tab = this.table;
            int index = hash & tab.length - 1;
            CHashIntIntMap.HashEntry first = tab[index];
            CHashIntIntMap.HashEntry e = first;

            while(e != null && key != e.key) {
               e = e.next;
            }

            int oldValue = Variables.RETURN_INT_VALUE_IF_NOT_FOUND;
            if (e != null) {
               oldValue = e.value;
               ++this.modCount;
               CHashIntIntMap.HashEntry newFirst = e.next;

               for(CHashIntIntMap.HashEntry p = first; p != e; p = p.next) {
                  newFirst = new CHashIntIntMap.HashEntry(p.key, newFirst, p.value);
               }

               tab[index] = newFirst;
               this.count = c;
            }

            var14 = oldValue;
         } finally {
            this.unlock();
         }

         return var14;
      }

      boolean removeTrueIfRemoved(int key, int hash) {
         this.lock();

         boolean var13;
         try {
            int c = this.count - 1;
            CHashIntIntMap.HashEntry[] tab = this.table;
            int index = hash & tab.length - 1;
            CHashIntIntMap.HashEntry first = tab[index];
            CHashIntIntMap.HashEntry e = first;

            while(e != null && key != e.key) {
               e = e.next;
            }

            if (e != null) {
               ++this.modCount;
               CHashIntIntMap.HashEntry newFirst = e.next;

               for(CHashIntIntMap.HashEntry p = first; p != e; p = p.next) {
                  newFirst = new CHashIntIntMap.HashEntry(p.key, newFirst, p.value);
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

      boolean removeWithValue(int key, int hash, int value) {
         this.lock();

         int v;
         try {
            int c = this.count - 1;
            CHashIntIntMap.HashEntry[] tab = this.table;
            int index = hash & tab.length - 1;
            CHashIntIntMap.HashEntry first = tab[index];
            CHashIntIntMap.HashEntry e = first;

            while(e != null && key != e.key) {
               e = e.next;
            }

            if (e != null) {
               v = e.value;
               if (value == v) {
                  ++this.modCount;
                  CHashIntIntMap.HashEntry newFirst = e.next;

                  for(CHashIntIntMap.HashEntry p = first; p != e; p = p.next) {
                     newFirst = new CHashIntIntMap.HashEntry(p.key, newFirst, p.value);
                  }

                  tab[index] = newFirst;
                  this.count = c;
               }
            }

            v = e != null;
         } finally {
            this.unlock();
         }

         return (boolean)v;
      }

      void clear() {
         if (this.count != 0) {
            this.lock();

            try {
               CHashIntIntMap.HashEntry[] tab = this.table;

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

   final class ValueIterator extends CHashIntIntMap.HashIterator implements IntIterator {
      @Override
      public int next() {
         return super.nextEntry().value;
      }
   }

   final class Values extends AbstractIntCollection {
      @Override
      public IntIterator iterator() {
         return CHashIntIntMap.this.new ValueIterator();
      }

      @Override
      public int size() {
         return CHashIntIntMap.this.size();
      }

      @Override
      public boolean contains(int o) {
         return CHashIntIntMap.this.containsValue(o);
      }

      @Override
      public void clear() {
         CHashIntIntMap.this.clear();
      }
   }

   final class WriteThroughEntry extends IntIntPairImpl {
      WriteThroughEntry(int k, int v) {
         super(k, v);
      }

      @Override
      public int setValue(int value) {
         int v = super.setValue(value);
         CHashIntIntMap.this.put(this.getKey(), value);
         return v;
      }
   }
}
