package org.napile.primitive.maps.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import org.napile.HashUtils;
import org.napile.primitive.iterators.LongIterator;
import org.napile.primitive.maps.LongObjectMap;
import org.napile.primitive.maps.abstracts.AbstractLongObjectMap;
import org.napile.primitive.pair.LongObjectPair;
import org.napile.primitive.pair.impl.LongObjectPairImpl;
import org.napile.primitive.sets.LongSet;
import org.napile.primitive.sets.abstracts.AbstractLongSet;

public class HashLongObjectMap<V> extends AbstractLongObjectMap<V> implements LongObjectMap<V>, Cloneable, Serializable {
   static final int DEFAULT_INITIAL_CAPACITY = 16;
   static final int MAXIMUM_CAPACITY = 1073741824;
   static final float DEFAULT_LOAD_FACTOR = 0.75F;
   transient HashLongObjectMap.Entry<V>[] table;
   transient int size;
   int threshold;
   final float loadFactor;
   transient volatile int modCount;
   private transient Set<LongObjectPair<V>> entrySet = null;
   private static final long serialVersionUID = 362498820763181265L;

   public HashLongObjectMap(int initialCapacity, float loadFactor) {
      if (initialCapacity < 0) {
         throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
      } else {
         if (initialCapacity > 1073741824) {
            initialCapacity = 1073741824;
         }

         if (!(loadFactor <= 0.0F) && !Float.isNaN(loadFactor)) {
            int capacity = 1;

            while(capacity < initialCapacity) {
               capacity <<= 1;
            }

            this.loadFactor = loadFactor;
            this.threshold = (int)((float)capacity * loadFactor);
            this.table = new HashLongObjectMap.Entry[capacity];
            this.init();
         } else {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
         }
      }
   }

   public HashLongObjectMap(int initialCapacity) {
      this(initialCapacity, 0.75F);
   }

   public HashLongObjectMap() {
      this.loadFactor = 0.75F;
      this.threshold = 12;
      this.table = new HashLongObjectMap.Entry[16];
      this.init();
   }

   public HashLongObjectMap(LongObjectMap<? extends V> m) {
      this(Math.max((int)((float)m.size() / 0.75F) + 1, 16), 0.75F);
      this.putAllForCreate(m);
   }

   void init() {
   }

   static int hash(long value) {
      int h = HashUtils.hashCode(value);
      h ^= h >>> 20 ^ h >>> 12;
      return h ^ h >>> 7 ^ h >>> 4;
   }

   static int indexFor(int h, int length) {
      return h & length - 1;
   }

   @Override
   public int size() {
      return this.size;
   }

   @Override
   public boolean isEmpty() {
      return this.size == 0;
   }

   @Override
   public V get(long key) {
      int hash = hash(key);

      for(HashLongObjectMap.Entry<V> e = this.table[indexFor(hash, this.table.length)]; e != null; e = e.next) {
         if (e.hash == hash && e.getKey() == key) {
            return e.getValue();
         }
      }

      return null;
   }

   @Override
   public boolean containsKey(long key) {
      return this.getEntry(key) != null;
   }

   final HashLongObjectMap.Entry<V> getEntry(long key) {
      int hash = hash(key);

      for(HashLongObjectMap.Entry<V> e = this.table[indexFor(hash, this.table.length)]; e != null; e = e.next) {
         if (e.hash == hash && e.getKey() == key) {
            return e;
         }
      }

      return null;
   }

   @Override
   public V put(long key, V value) {
      int hash = hash(key);
      int i = indexFor(hash, this.table.length);

      for(HashLongObjectMap.Entry<V> e = this.table[i]; e != null; e = e.next) {
         if (e.hash == hash && e.getKey() == key) {
            V oldValue = e.getValue();
            e.setValue(value);
            e.recordAccess(this);
            return oldValue;
         }
      }

      ++this.modCount;
      this.addEntry(hash, key, value, i);
      return null;
   }

   private void putForCreate(long key, V value) {
      int hash = hash(key);
      int i = indexFor(hash, this.table.length);

      for(HashLongObjectMap.Entry<V> e = this.table[i]; e != null; e = e.next) {
         if (e.hash == hash && e.getKey() == key) {
            e.setValue(value);
            return;
         }
      }

      this.createEntry(hash, key, value, i);
   }

   private void putAllForCreate(LongObjectMap<? extends V> m) {
      for(LongObjectPair<? extends V> e : m.entrySet()) {
         this.putForCreate(e.getKey(), e.getValue());
      }
   }

   void resize(int newCapacity) {
      HashLongObjectMap.Entry<V>[] oldTable = this.table;
      int oldCapacity = oldTable.length;
      if (oldCapacity == 1073741824) {
         this.threshold = Integer.MAX_VALUE;
      } else {
         HashLongObjectMap.Entry<V>[] newTable = new HashLongObjectMap.Entry[newCapacity];
         this.transfer(newTable);
         this.table = newTable;
         this.threshold = (int)((float)newCapacity * this.loadFactor);
      }
   }

   void transfer(HashLongObjectMap.Entry<V>[] newTable) {
      HashLongObjectMap.Entry[] src = this.table;
      int newCapacity = newTable.length;

      for(int j = 0; j < src.length; ++j) {
         HashLongObjectMap.Entry<V> e = src[j];
         if (e != null) {
            src[j] = null;

            while(true) {
               HashLongObjectMap.Entry<V> next = e.next;
               int i = indexFor(e.hash, newCapacity);
               e.next = newTable[i];
               newTable[i] = e;
               e = next;
               if (next == null) {
                  break;
               }
            }
         }
      }
   }

   @Override
   public void putAll(LongObjectMap<? extends V> m) {
      int numKeysToBeAdded = m.size();
      if (numKeysToBeAdded != 0) {
         if (numKeysToBeAdded > this.threshold) {
            int targetCapacity = (int)((float)numKeysToBeAdded / this.loadFactor + 1.0F);
            if (targetCapacity > 1073741824) {
               targetCapacity = 1073741824;
            }

            int newCapacity = this.table.length;

            while(newCapacity < targetCapacity) {
               newCapacity <<= 1;
            }

            if (newCapacity > this.table.length) {
               this.resize(newCapacity);
            }
         }

         for(LongObjectPair<? extends V> e : m.entrySet()) {
            this.put(e.getKey(), e.getValue());
         }
      }
   }

   @Override
   public V remove(long key) {
      HashLongObjectMap.Entry<V> e = this.removeEntryForKey(key);
      return e == null ? null : e.getValue();
   }

   final HashLongObjectMap.Entry<V> removeEntryForKey(long key) {
      int hash = hash(key);
      int i = indexFor(hash, this.table.length);
      HashLongObjectMap.Entry<V> prev = this.table[i];

      HashLongObjectMap.Entry<V> e;
      HashLongObjectMap.Entry<V> next;
      for(e = prev; e != null; e = next) {
         next = e.next;
         if (e.hash == hash && e.getKey() == key) {
            ++this.modCount;
            --this.size;
            if (prev == e) {
               this.table[i] = next;
            } else {
               prev.next = next;
            }

            e.recordRemoval(this);
            return e;
         }

         prev = e;
      }

      return e;
   }

   final HashLongObjectMap.Entry<V> removeMapping(Object o) {
      if (!(o instanceof LongObjectPair)) {
         return null;
      } else {
         LongObjectPair<V> entry = (LongObjectPair)o;
         long key = entry.getKey();
         int hash = hash(key);
         int i = indexFor(hash, this.table.length);
         HashLongObjectMap.Entry<V> prev = this.table[i];

         HashLongObjectMap.Entry<V> e;
         HashLongObjectMap.Entry<V> next;
         for(e = prev; e != null; e = next) {
            next = e.next;
            if (e.hash == hash && e.equals(entry)) {
               ++this.modCount;
               --this.size;
               if (prev == e) {
                  this.table[i] = next;
               } else {
                  prev.next = next;
               }

               e.recordRemoval(this);
               return e;
            }

            prev = e;
         }

         return e;
      }
   }

   @Override
   public void clear() {
      ++this.modCount;
      HashLongObjectMap.Entry[] tab = this.table;

      for(int i = 0; i < tab.length; ++i) {
         tab[i] = null;
      }

      this.size = 0;
   }

   @Override
   public boolean containsValue(Object value) {
      if (value == null) {
         return this.containsNullValue();
      } else {
         HashLongObjectMap.Entry[] tab = this.table;

         for(int i = 0; i < tab.length; ++i) {
            for(HashLongObjectMap.Entry e = tab[i]; e != null; e = e.next) {
               if (value.equals(e.getValue())) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private boolean containsNullValue() {
      HashLongObjectMap.Entry[] tab = this.table;

      for(int i = 0; i < tab.length; ++i) {
         for(HashLongObjectMap.Entry e = tab[i]; e != null; e = e.next) {
            if (e.getValue() == null) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public Object clone() {
      HashLongObjectMap<V> result = null;

      try {
         result = (HashLongObjectMap)super.clone();
      } catch (CloneNotSupportedException var3) {
      }

      result.table = new HashLongObjectMap.Entry[this.table.length];
      result.entrySet = null;
      result.modCount = 0;
      result.size = 0;
      result.init();
      result.putAllForCreate(this);
      return result;
   }

   void addEntry(int hash, long key, V value, int bucketIndex) {
      HashLongObjectMap.Entry<V> e = this.table[bucketIndex];
      this.table[bucketIndex] = new HashLongObjectMap.Entry<>(hash, key, value, e);
      if (this.size++ >= this.threshold) {
         this.resize(2 * this.table.length);
      }
   }

   void createEntry(int hash, long key, V value, int bucketIndex) {
      HashLongObjectMap.Entry<V> e = this.table[bucketIndex];
      this.table[bucketIndex] = new HashLongObjectMap.Entry<>(hash, key, value, e);
      ++this.size;
   }

   LongIterator newKeyIterator() {
      return new HashLongObjectMap.KeyIterator();
   }

   Iterator<V> newValueIterator() {
      return new HashLongObjectMap.ValueIterator();
   }

   Iterator<LongObjectPair<V>> newEntryIterator() {
      return new HashLongObjectMap.EntryIterator();
   }

   @Override
   public long[] keys() {
      return this.keySet().toArray();
   }

   @Override
   public long[] keys(long[] array) {
      return this.keySet().toArray(array);
   }

   @Override
   public LongSet keySet() {
      LongSet ks = this.keySet;
      return ks != null ? ks : (this.keySet = new HashLongObjectMap.KeySet());
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
      return vs != null ? vs : (this.values = new HashLongObjectMap.Values());
   }

   @Override
   public Set<LongObjectPair<V>> entrySet() {
      return this.entrySet0();
   }

   private Set<LongObjectPair<V>> entrySet0() {
      Set<LongObjectPair<V>> es = this.entrySet;
      return es != null ? es : (this.entrySet = new HashLongObjectMap.EntrySet());
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      Iterator<LongObjectPair<V>> i = this.size > 0 ? this.entrySet0().iterator() : null;
      s.defaultWriteObject();
      s.writeInt(this.table.length);
      s.writeInt(this.size);
      if (i != null) {
         while(i.hasNext()) {
            LongObjectPair<V> e = i.next();
            s.writeLong(e.getKey());
            s.writeObject(e.getValue());
         }
      }
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      int numBuckets = s.readInt();
      this.table = new HashLongObjectMap.Entry[numBuckets];
      this.init();
      int size = s.readInt();

      for(int i = 0; i < size; ++i) {
         long key = s.readLong();
         V value = (V)s.readObject();
         this.putForCreate(key, value);
      }
   }

   public int capacity() {
      return this.table.length;
   }

   public float loadFactor() {
      return this.loadFactor;
   }

   static class Entry<V> extends LongObjectPairImpl<V> {
      HashLongObjectMap.Entry<V> next;
      final int hash;

      Entry(int h, long k, V v, HashLongObjectMap.Entry<V> n) {
         super(k, v);
         this.next = n;
         this.hash = h;
      }

      void recordAccess(HashLongObjectMap<V> m) {
      }

      void recordRemoval(HashLongObjectMap<V> m) {
      }
   }

   private final class EntryIterator extends HashLongObjectMap<V>.HashIterator<LongObjectPair<V>> {
      private EntryIterator() {
      }

      public LongObjectPair<V> next() {
         return this.nextEntry();
      }
   }

   private final class EntrySet extends AbstractSet<LongObjectPair<V>> {
      private EntrySet() {
      }

      @Override
      public Iterator<LongObjectPair<V>> iterator() {
         return HashLongObjectMap.this.newEntryIterator();
      }

      @Override
      public boolean contains(Object o) {
         if (!(o instanceof LongObjectPair)) {
            return false;
         } else {
            LongObjectPair<V> e = (LongObjectPair)o;
            HashLongObjectMap.Entry<V> candidate = HashLongObjectMap.this.getEntry(e.getKey());
            return candidate != null && candidate.equals(e);
         }
      }

      @Override
      public boolean remove(Object o) {
         return HashLongObjectMap.this.removeMapping(o) != null;
      }

      @Override
      public int size() {
         return HashLongObjectMap.this.size;
      }

      @Override
      public void clear() {
         HashLongObjectMap.this.clear();
      }
   }

   private abstract class HashIterator<E> implements Iterator<E> {
      HashLongObjectMap.Entry<V> next;
      int expectedModCount = HashLongObjectMap.this.modCount;
      int index;
      HashLongObjectMap.Entry<V> current;

      HashIterator() {
         if (HashLongObjectMap.this.size > 0) {
            HashLongObjectMap.Entry[] t = HashLongObjectMap.this.table;

            while(this.index < t.length && (this.next = t[this.index++]) == null) {
            }
         }
      }

      @Override
      public final boolean hasNext() {
         return this.next != null;
      }

      final LongObjectPair<V> nextEntry() {
         if (HashLongObjectMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            HashLongObjectMap.Entry<V> e = this.next;
            if (e == null) {
               throw new NoSuchElementException();
            } else {
               if ((this.next = e.next) == null) {
                  HashLongObjectMap.Entry[] t = HashLongObjectMap.this.table;

                  while(this.index < t.length && (this.next = t[this.index++]) == null) {
                  }
               }

               this.current = e;
               return e;
            }
         }
      }

      @Override
      public void remove() {
         if (this.current == null) {
            throw new IllegalStateException();
         } else if (HashLongObjectMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            long k = this.current.getKey();
            this.current = null;
            HashLongObjectMap.this.removeEntryForKey(k);
            this.expectedModCount = HashLongObjectMap.this.modCount;
         }
      }
   }

   private abstract class HashLongIterator implements LongIterator {
      HashLongObjectMap.Entry<V> next;
      int expectedModCount = HashLongObjectMap.this.modCount;
      int index;
      HashLongObjectMap.Entry<V> current;

      HashLongIterator() {
         if (HashLongObjectMap.this.size > 0) {
            HashLongObjectMap.Entry[] t = HashLongObjectMap.this.table;

            while(this.index < t.length && (this.next = t[this.index++]) == null) {
            }
         }
      }

      @Override
      public final boolean hasNext() {
         return this.next != null;
      }

      final HashLongObjectMap.Entry<V> nextEntry() {
         if (HashLongObjectMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            HashLongObjectMap.Entry<V> e = this.next;
            if (e == null) {
               throw new NoSuchElementException();
            } else {
               if ((this.next = e.next) == null) {
                  HashLongObjectMap.Entry[] t = HashLongObjectMap.this.table;

                  while(this.index < t.length && (this.next = t[this.index++]) == null) {
                  }
               }

               this.current = e;
               return e;
            }
         }
      }

      @Override
      public void remove() {
         if (this.current == null) {
            throw new IllegalStateException();
         } else if (HashLongObjectMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            long k = this.current.getKey();
            this.current = null;
            HashLongObjectMap.this.removeEntryForKey(k);
            this.expectedModCount = HashLongObjectMap.this.modCount;
         }
      }
   }

   private final class KeyIterator extends HashLongObjectMap<V>.HashLongIterator {
      private KeyIterator() {
      }

      @Override
      public long next() {
         return this.nextEntry().getKey();
      }
   }

   private final class KeySet extends AbstractLongSet {
      private KeySet() {
      }

      @Override
      public LongIterator iterator() {
         return HashLongObjectMap.this.newKeyIterator();
      }

      @Override
      public int size() {
         return HashLongObjectMap.this.size;
      }

      public boolean contains(int o) {
         return HashLongObjectMap.this.containsKey((long)o);
      }

      public boolean remove(int o) {
         return HashLongObjectMap.this.removeEntryForKey((long)o) != null;
      }

      @Override
      public void clear() {
         HashLongObjectMap.this.clear();
      }
   }

   private final class ValueIterator extends HashLongObjectMap<V>.HashIterator<V> {
      private ValueIterator() {
      }

      @Override
      public V next() {
         return (V)this.nextEntry().getValue();
      }
   }

   private final class Values extends AbstractCollection<V> {
      private Values() {
      }

      @Override
      public Iterator<V> iterator() {
         return HashLongObjectMap.this.newValueIterator();
      }

      @Override
      public int size() {
         return HashLongObjectMap.this.size;
      }

      @Override
      public boolean contains(Object o) {
         return HashLongObjectMap.this.containsValue(o);
      }

      @Override
      public void clear() {
         HashLongObjectMap.this.clear();
      }
   }
}
