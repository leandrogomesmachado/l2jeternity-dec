package org.napile.primitive.maps.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.maps.CIntObjectMap;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.abstracts.AbstractIntObjectMap;
import org.napile.primitive.pair.IntObjectPair;
import org.napile.primitive.pair.impl.IntObjectPairImpl;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.abstracts.AbstractIntSet;

public class CHashIntObjectMap<V> extends AbstractIntObjectMap<V> implements CIntObjectMap<V>, Serializable {
   static final int DEFAULT_INITIAL_CAPACITY = 16;
   static final float DEFAULT_LOAD_FACTOR = 0.75F;
   static final int DEFAULT_CONCURRENCY_LEVEL = 16;
   static final int MAXIMUM_CAPACITY = 1073741824;
   static final int MAX_SEGMENTS = 65536;
   static final int RETRIES_BEFORE_LOCK = 2;
   final int segmentMask;
   final int segmentShift;
   final CHashIntObjectMap.Segment<V>[] segments;
   transient IntSet keySet;
   transient Set<IntObjectPair<V>> entrySet;
   transient Collection<V> values;

   private static int hash(int h) {
      h += h << 15 ^ -12931;
      h ^= h >>> 10;
      h += h << 3;
      h ^= h >>> 6;
      h += (h << 2) + (h << 14);
      return h ^ h >>> 16;
   }

   final CHashIntObjectMap.Segment<V> segmentFor(int hash) {
      return this.segments[hash >>> this.segmentShift & this.segmentMask];
   }

   public CHashIntObjectMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
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
         this.segments = CHashIntObjectMap.Segment.newArray(ssize);
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
            this.segments[i] = new CHashIntObjectMap.Segment<>(cap, loadFactor);
         }
      } else {
         throw new IllegalArgumentException();
      }
   }

   public CHashIntObjectMap(int initialCapacity, float loadFactor) {
      this(initialCapacity, loadFactor, 16);
   }

   public CHashIntObjectMap(int initialCapacity) {
      this(initialCapacity, 0.75F, 16);
   }

   public CHashIntObjectMap() {
      this(16, 0.75F, 16);
   }

   public CHashIntObjectMap(IntObjectMap<? extends V> m) {
      this(Math.max((int)((float)m.size() / 0.75F) + 1, 16), 0.75F, 16);
      this.putAll(m);
   }

   @Override
   public boolean isEmpty() {
      CHashIntObjectMap.Segment<V>[] segments = this.segments;
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
      CHashIntObjectMap.Segment<V>[] segments = this.segments;
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
   public V get(int key) {
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
   public boolean containsValue(Object value) {
      if (value == null) {
         throw new NullPointerException();
      } else {
         CHashIntObjectMap.Segment<V>[] segments = this.segments;
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
   }

   @Override
   public V put(int key, V value) {
      if (value == null) {
         throw new NullPointerException();
      } else {
         int hash = hash(key);
         return this.segmentFor(hash).put(key, hash, value, false);
      }
   }

   @Override
   public V putIfAbsent(int key, V value) {
      if (value == null) {
         throw new NullPointerException();
      } else {
         int hash = hash(key);
         return this.segmentFor(hash).put(key, hash, value, true);
      }
   }

   @Override
   public void putAll(IntObjectMap<? extends V> m) {
      for(IntObjectPair<? extends V> e : m.entrySet()) {
         this.put(e.getKey(), e.getValue());
      }
   }

   @Override
   public V remove(int key) {
      int hash = hash(key);
      return this.segmentFor(hash).remove(key, hash, null);
   }

   @Override
   public boolean remove(int key, Object value) {
      if (value == null) {
         return false;
      } else {
         int hash = hash(key);
         return this.segmentFor(hash).remove(key, hash, value) != null;
      }
   }

   @Override
   public boolean replace(int key, V oldValue, V newValue) {
      if (oldValue != null && newValue != null) {
         int hash = hash(key);
         return this.segmentFor(hash).replace(key, hash, oldValue, newValue);
      } else {
         throw new NullPointerException();
      }
   }

   @Override
   public V replace(int key, V value) {
      if (value == null) {
         throw new NullPointerException();
      } else {
         int hash = hash(key);
         return this.segmentFor(hash).replace(key, hash, value);
      }
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
      return ks != null ? ks : (this.keySet = new CHashIntObjectMap.KeySet());
   }

   @Override
   public Object[] values() {
      return this.valueCollection().toArray();
   }

   @Override
   public V[] values(V[] array) {
      return this.valueCollection().toArray(array);
   }

   @Override
   public Collection<V> valueCollection() {
      Collection<V> vs = this.values;
      return vs != null ? vs : (this.values = new CHashIntObjectMap.Values());
   }

   @Override
   public Set<IntObjectPair<V>> entrySet() {
      Set<IntObjectPair<V>> es = this.entrySet;
      return es != null ? es : (this.entrySet = new CHashIntObjectMap.EntrySet());
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      s.defaultWriteObject();

      for(int k = 0; k < this.segments.length; ++k) {
         CHashIntObjectMap.Segment<V> seg = this.segments[k];
         seg.lock();

         try {
            CHashIntObjectMap.HashEntry<V>[] tab = seg.table;

            for(int i = 0; i < tab.length; ++i) {
               for(CHashIntObjectMap.HashEntry<V> e = tab[i]; e != null; e = e.next) {
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
         this.segments[i].setTable(new CHashIntObjectMap.HashEntry[1]);
      }

      while(true) {
         try {
            int key = s.readInt();
            V value = (V)s.readObject();
            this.put(key, value);
         } catch (IOException var4) {
            return;
         } catch (ClassNotFoundException var5) {
            var5.printStackTrace();
         }
      }
   }

   final class EntryIterator extends CHashIntObjectMap<V>.HashIterator implements Iterator<IntObjectPair<V>> {
      public IntObjectPair<V> next() {
         CHashIntObjectMap.HashEntry<V> e = super.nextEntry();
         return CHashIntObjectMap.this.new WriteThroughEntry(e.key, e.value);
      }
   }

   final class EntrySet extends AbstractSet<IntObjectPair<V>> {
      @Override
      public Iterator<IntObjectPair<V>> iterator() {
         return CHashIntObjectMap.this.new EntryIterator();
      }

      @Override
      public boolean contains(Object o) {
         if (!(o instanceof IntObjectPair)) {
            return false;
         } else {
            IntObjectPair<?> e = (IntObjectPair)o;
            V v = CHashIntObjectMap.this.get(e.getKey());
            return v != null && v.equals(e.getValue());
         }
      }

      @Override
      public boolean remove(Object o) {
         if (!(o instanceof IntObjectPair)) {
            return false;
         } else {
            IntObjectPair<?> e = (IntObjectPair)o;
            return CHashIntObjectMap.this.remove(e.getKey(), e.getValue());
         }
      }

      @Override
      public int size() {
         return CHashIntObjectMap.this.size();
      }

      @Override
      public void clear() {
         CHashIntObjectMap.this.clear();
      }
   }

   static final class HashEntry<V> {
      final int key;
      final int hash;
      volatile V value;
      final CHashIntObjectMap.HashEntry<V> next;

      HashEntry(int key, CHashIntObjectMap.HashEntry<V> next, V value) {
         this.key = key;
         this.hash = CHashIntObjectMap.hash(key);
         this.next = next;
         this.value = value;
      }

      static <V> CHashIntObjectMap.HashEntry<V>[] newArray(int i) {
         return new CHashIntObjectMap.HashEntry[i];
      }
   }

   abstract class HashIterator {
      int nextSegmentIndex = CHashIntObjectMap.this.segments.length - 1;
      int nextTableIndex = -1;
      CHashIntObjectMap.HashEntry<V>[] currentTable;
      CHashIntObjectMap.HashEntry<V> nextEntry;
      CHashIntObjectMap.HashEntry<V> lastReturned;

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
               CHashIntObjectMap.Segment<V> seg = CHashIntObjectMap.this.segments[this.nextSegmentIndex--];
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

      CHashIntObjectMap.HashEntry<V> nextEntry() {
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
            CHashIntObjectMap.this.remove(this.lastReturned.key);
            this.lastReturned = null;
         }
      }
   }

   final class KeyIterator extends CHashIntObjectMap<V>.HashIterator implements IntIterator {
      @Override
      public int next() {
         return super.nextEntry().key;
      }
   }

   final class KeySet extends AbstractIntSet {
      @Override
      public IntIterator iterator() {
         return CHashIntObjectMap.this.new KeyIterator();
      }

      @Override
      public int size() {
         return CHashIntObjectMap.this.size();
      }

      @Override
      public boolean contains(int o) {
         return CHashIntObjectMap.this.containsKey(o);
      }

      @Override
      public boolean remove(int o) {
         return CHashIntObjectMap.this.remove(o) != null;
      }

      @Override
      public void clear() {
         CHashIntObjectMap.this.clear();
      }
   }

   static final class Segment<V> extends ReentrantLock implements Serializable {
      private static final long serialVersionUID = 2249069246763182397L;
      transient volatile int count;
      transient int modCount;
      transient int threshold;
      transient volatile CHashIntObjectMap.HashEntry<V>[] table;
      final float loadFactor;

      Segment(int initialCapacity, float lf) {
         this.loadFactor = lf;
         this.setTable(CHashIntObjectMap.HashEntry.newArray(initialCapacity));
      }

      static final <V> CHashIntObjectMap.Segment<V>[] newArray(int i) {
         return new CHashIntObjectMap.Segment[i];
      }

      void setTable(CHashIntObjectMap.HashEntry<V>[] newTable) {
         this.threshold = (int)((float)newTable.length * this.loadFactor);
         this.table = newTable;
      }

      CHashIntObjectMap.HashEntry<V> getFirst(int hash) {
         CHashIntObjectMap.HashEntry<V>[] tab = this.table;
         return tab[hash & tab.length - 1];
      }

      V readValueUnderLock(CHashIntObjectMap.HashEntry<V> e) {
         this.lock();

         Object var2;
         try {
            var2 = e.value;
         } finally {
            this.unlock();
         }

         return (V)var2;
      }

      V get(int key, int hash) {
         if (this.count != 0) {
            for(CHashIntObjectMap.HashEntry<V> e = this.getFirst(hash); e != null; e = e.next) {
               if (e.key == key) {
                  V v = e.value;
                  if (v != null) {
                     return v;
                  }

                  return this.readValueUnderLock(e);
               }
            }
         }

         return null;
      }

      boolean containsKey(int key, int hash) {
         if (this.count != 0) {
            for(CHashIntObjectMap.HashEntry<V> e = this.getFirst(hash); e != null; e = e.next) {
               if (e.key == key) {
                  return true;
               }
            }
         }

         return false;
      }

      boolean containsValue(Object value) {
         if (this.count != 0) {
            CHashIntObjectMap.HashEntry<V>[] tab = this.table;
            int len = tab.length;

            for(int i = 0; i < len; ++i) {
               for(CHashIntObjectMap.HashEntry<V> e = tab[i]; e != null; e = e.next) {
                  V v = e.value;
                  if (v == null) {
                     v = this.readValueUnderLock(e);
                  }

                  if (value.equals(v)) {
                     return true;
                  }
               }
            }
         }

         return false;
      }

      boolean replace(int key, int hash, V oldValue, V newValue) {
         this.lock();

         boolean var7;
         try {
            CHashIntObjectMap.HashEntry<V> e = this.getFirst(hash);

            while(e != null && key != e.key) {
               e = e.next;
            }

            boolean replaced = false;
            if (e != null && oldValue.equals(e.value)) {
               replaced = true;
               e.value = newValue;
            }

            var7 = replaced;
         } finally {
            this.unlock();
         }

         return var7;
      }

      V replace(int key, int hash, V newValue) {
         this.lock();

         Object var6;
         try {
            CHashIntObjectMap.HashEntry<V> e = this.getFirst(hash);

            while(e != null && key != e.key) {
               e = e.next;
            }

            V oldValue = null;
            if (e != null) {
               oldValue = e.value;
               e.value = newValue;
            }

            var6 = oldValue;
         } finally {
            this.unlock();
         }

         return (V)var6;
      }

      V put(int key, int hash, V value, boolean onlyIfAbsent) {
         this.lock();

         Object var11;
         try {
            int c = this.count;
            if (c++ > this.threshold) {
               this.rehash();
            }

            CHashIntObjectMap.HashEntry<V>[] tab = this.table;
            int index = hash & tab.length - 1;
            CHashIntObjectMap.HashEntry<V> first = tab[index];
            CHashIntObjectMap.HashEntry<V> e = first;

            while(e != null && key != e.key) {
               e = e.next;
            }

            V oldValue;
            if (e != null) {
               oldValue = e.value;
               if (!onlyIfAbsent) {
                  e.value = value;
               }
            } else {
               oldValue = null;
               ++this.modCount;
               tab[index] = new CHashIntObjectMap.HashEntry<>(key, first, value);
               this.count = c;
            }

            var11 = oldValue;
         } finally {
            this.unlock();
         }

         return (V)var11;
      }

      void rehash() {
         CHashIntObjectMap.HashEntry<V>[] oldTable = this.table;
         int oldCapacity = oldTable.length;
         if (oldCapacity < 1073741824) {
            CHashIntObjectMap.HashEntry<V>[] newTable = CHashIntObjectMap.HashEntry.newArray(oldCapacity << 1);
            this.threshold = (int)((float)newTable.length * this.loadFactor);
            int sizeMask = newTable.length - 1;

            for(int i = 0; i < oldCapacity; ++i) {
               CHashIntObjectMap.HashEntry<V> e = oldTable[i];
               if (e != null) {
                  CHashIntObjectMap.HashEntry<V> next = e.next;
                  int idx = e.hash & sizeMask;
                  if (next == null) {
                     newTable[idx] = e;
                  } else {
                     CHashIntObjectMap.HashEntry<V> lastRun = e;
                     int lastIdx = idx;

                     for(CHashIntObjectMap.HashEntry<V> last = next; last != null; last = last.next) {
                        int k = last.hash & sizeMask;
                        if (k != lastIdx) {
                           lastIdx = k;
                           lastRun = last;
                        }
                     }

                     newTable[lastIdx] = lastRun;

                     for(CHashIntObjectMap.HashEntry<V> p = e; p != lastRun; p = p.next) {
                        int k = p.hash & sizeMask;
                        CHashIntObjectMap.HashEntry<V> n = newTable[k];
                        newTable[k] = new CHashIntObjectMap.HashEntry<>(p.key, n, p.value);
                     }
                  }
               }
            }

            this.table = newTable;
         }
      }

      V remove(int key, int hash, Object value) {
         this.lock();

         V v;
         try {
            int c = this.count - 1;
            CHashIntObjectMap.HashEntry<V>[] tab = this.table;
            int index = hash & tab.length - 1;
            CHashIntObjectMap.HashEntry<V> first = tab[index];
            CHashIntObjectMap.HashEntry<V> e = first;

            while(e != null && key != e.key) {
               e = e.next;
            }

            V oldValue = null;
            if (e != null) {
               v = e.value;
               if (value == null || value.equals(v)) {
                  oldValue = v;
                  ++this.modCount;
                  CHashIntObjectMap.HashEntry<V> newFirst = e.next;

                  for(CHashIntObjectMap.HashEntry<V> p = first; p != e; p = p.next) {
                     newFirst = new CHashIntObjectMap.HashEntry<>(p.key, newFirst, p.value);
                  }

                  tab[index] = newFirst;
                  this.count = c;
               }
            }

            v = oldValue;
         } finally {
            this.unlock();
         }

         return v;
      }

      void clear() {
         if (this.count != 0) {
            this.lock();

            try {
               CHashIntObjectMap.HashEntry<V>[] tab = this.table;

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

   final class ValueIterator extends CHashIntObjectMap<V>.HashIterator implements Iterator<V> {
      @Override
      public V next() {
         return super.nextEntry().value;
      }
   }

   final class Values extends AbstractCollection<V> {
      @Override
      public Iterator<V> iterator() {
         return CHashIntObjectMap.this.new ValueIterator();
      }

      @Override
      public int size() {
         return CHashIntObjectMap.this.size();
      }

      @Override
      public boolean contains(Object o) {
         return CHashIntObjectMap.this.containsValue(o);
      }

      @Override
      public void clear() {
         CHashIntObjectMap.this.clear();
      }
   }

   final class WriteThroughEntry extends IntObjectPairImpl<V> {
      WriteThroughEntry(int k, V v) {
         super(k, v);
      }

      @Override
      public V setValue(V value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            V v = super.setValue(value);
            CHashIntObjectMap.this.put(this.getKey(), value);
            return v;
         }
      }
   }
}
