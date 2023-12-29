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
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.abstracts.AbstractIntObjectMap;
import org.napile.primitive.pair.IntObjectPair;
import org.napile.primitive.pair.impl.IntObjectPairImpl;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.abstracts.AbstractIntSet;

public class HashIntObjectMap<V> extends AbstractIntObjectMap<V> implements IntObjectMap<V>, Cloneable, Serializable {
   static final int DEFAULT_INITIAL_CAPACITY = 16;
   static final int MAXIMUM_CAPACITY = 1073741824;
   static final float DEFAULT_LOAD_FACTOR = 0.75F;
   transient HashIntObjectMap.Entry<V>[] table;
   transient int size;
   int threshold;
   final float loadFactor;
   transient volatile int modCount;
   private transient Set<IntObjectPair<V>> entrySet = null;
   private static final long serialVersionUID = 362498820763181265L;

   public HashIntObjectMap(int initialCapacity, float loadFactor) {
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
            this.table = new HashIntObjectMap.Entry[capacity];
            this.init();
         } else {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
         }
      }
   }

   public HashIntObjectMap(int initialCapacity) {
      this(initialCapacity, 0.75F);
   }

   public HashIntObjectMap() {
      this.loadFactor = 0.75F;
      this.threshold = 12;
      this.table = new HashIntObjectMap.Entry[16];
      this.init();
   }

   public HashIntObjectMap(IntObjectMap<? extends V> m) {
      this(Math.max((int)((float)m.size() / 0.75F) + 1, 16), 0.75F);
      this.putAllForCreate(m);
   }

   void init() {
   }

   static int hash(int value) {
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
   public V get(int key) {
      int hash = hash(key);

      for(HashIntObjectMap.Entry<V> e = this.table[indexFor(hash, this.table.length)]; e != null; e = e.next) {
         if (e.hash == hash && e.getKey() == key) {
            return e.getValue();
         }
      }

      return null;
   }

   @Override
   public boolean containsKey(int key) {
      return this.getEntry(key) != null;
   }

   final HashIntObjectMap.Entry<V> getEntry(int key) {
      int hash = hash(key);

      for(HashIntObjectMap.Entry<V> e = this.table[indexFor(hash, this.table.length)]; e != null; e = e.next) {
         if (e.hash == hash && e.getKey() == key) {
            return e;
         }
      }

      return null;
   }

   @Override
   public V put(int key, V value) {
      int hash = hash(key);
      int i = indexFor(hash, this.table.length);

      for(HashIntObjectMap.Entry<V> e = this.table[i]; e != null; e = e.next) {
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

   private void putForCreate(int key, V value) {
      int hash = hash(key);
      int i = indexFor(hash, this.table.length);

      for(HashIntObjectMap.Entry<V> e = this.table[i]; e != null; e = e.next) {
         if (e.hash == hash && e.getKey() == key) {
            e.setValue(value);
            return;
         }
      }

      this.createEntry(hash, key, value, i);
   }

   private void putAllForCreate(IntObjectMap<? extends V> m) {
      for(IntObjectPair<? extends V> e : m.entrySet()) {
         this.putForCreate(e.getKey(), e.getValue());
      }
   }

   void resize(int newCapacity) {
      HashIntObjectMap.Entry<V>[] oldTable = this.table;
      int oldCapacity = oldTable.length;
      if (oldCapacity == 1073741824) {
         this.threshold = Integer.MAX_VALUE;
      } else {
         HashIntObjectMap.Entry<V>[] newTable = new HashIntObjectMap.Entry[newCapacity];
         this.transfer(newTable);
         this.table = newTable;
         this.threshold = (int)((float)newCapacity * this.loadFactor);
      }
   }

   void transfer(HashIntObjectMap.Entry<V>[] newTable) {
      HashIntObjectMap.Entry[] src = this.table;
      int newCapacity = newTable.length;

      for(int j = 0; j < src.length; ++j) {
         HashIntObjectMap.Entry<V> e = src[j];
         if (e != null) {
            src[j] = null;

            while(true) {
               HashIntObjectMap.Entry<V> next = e.next;
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
   public void putAll(IntObjectMap<? extends V> m) {
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

         for(IntObjectPair<? extends V> e : m.entrySet()) {
            this.put(e.getKey(), e.getValue());
         }
      }
   }

   @Override
   public V remove(int key) {
      HashIntObjectMap.Entry<V> e = this.removeEntryForKey(key);
      return e == null ? null : e.getValue();
   }

   final HashIntObjectMap.Entry<V> removeEntryForKey(int key) {
      int hash = hash(key);
      int i = indexFor(hash, this.table.length);
      HashIntObjectMap.Entry<V> prev = this.table[i];

      HashIntObjectMap.Entry<V> e;
      HashIntObjectMap.Entry<V> next;
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

   final HashIntObjectMap.Entry<V> removeMapping(Object o) {
      if (!(o instanceof IntObjectPair)) {
         return null;
      } else {
         IntObjectPair<V> entry = (IntObjectPair)o;
         Object key = entry.getKey();
         int hash = key == null ? 0 : hash(key.hashCode());
         int i = indexFor(hash, this.table.length);
         HashIntObjectMap.Entry<V> prev = this.table[i];

         HashIntObjectMap.Entry<V> e;
         HashIntObjectMap.Entry<V> next;
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
      HashIntObjectMap.Entry[] tab = this.table;

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
         HashIntObjectMap.Entry[] tab = this.table;

         for(int i = 0; i < tab.length; ++i) {
            for(HashIntObjectMap.Entry e = tab[i]; e != null; e = e.next) {
               if (value.equals(e.getValue())) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private boolean containsNullValue() {
      HashIntObjectMap.Entry[] tab = this.table;

      for(int i = 0; i < tab.length; ++i) {
         for(HashIntObjectMap.Entry e = tab[i]; e != null; e = e.next) {
            if (e.getValue() == null) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public Object clone() {
      HashIntObjectMap<V> result = null;

      try {
         result = (HashIntObjectMap)super.clone();
      } catch (CloneNotSupportedException var3) {
      }

      result.table = new HashIntObjectMap.Entry[this.table.length];
      result.entrySet = null;
      result.modCount = 0;
      result.size = 0;
      result.init();
      result.putAllForCreate(this);
      return result;
   }

   void addEntry(int hash, int key, V value, int bucketIndex) {
      HashIntObjectMap.Entry<V> e = this.table[bucketIndex];
      this.table[bucketIndex] = new HashIntObjectMap.Entry<>(hash, key, value, e);
      if (this.size++ >= this.threshold) {
         this.resize(2 * this.table.length);
      }
   }

   void createEntry(int hash, int key, V value, int bucketIndex) {
      HashIntObjectMap.Entry<V> e = this.table[bucketIndex];
      this.table[bucketIndex] = new HashIntObjectMap.Entry<>(hash, key, value, e);
      ++this.size;
   }

   IntIterator newKeyIterator() {
      return new HashIntObjectMap.KeyIterator();
   }

   Iterator<V> newValueIterator() {
      return new HashIntObjectMap.ValueIterator();
   }

   Iterator<IntObjectPair<V>> newEntryIterator() {
      return new HashIntObjectMap.EntryIterator();
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
      return ks != null ? ks : (this.keySet = new HashIntObjectMap.KeySet());
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
      return vs != null ? vs : (this.values = new HashIntObjectMap.Values());
   }

   @Override
   public Set<IntObjectPair<V>> entrySet() {
      return this.entrySet0();
   }

   private Set<IntObjectPair<V>> entrySet0() {
      Set<IntObjectPair<V>> es = this.entrySet;
      return es != null ? es : (this.entrySet = new HashIntObjectMap.EntrySet());
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      Iterator<IntObjectPair<V>> i = this.size > 0 ? this.entrySet0().iterator() : null;
      s.defaultWriteObject();
      s.writeInt(this.table.length);
      s.writeInt(this.size);
      if (i != null) {
         while(i.hasNext()) {
            IntObjectPair<V> e = i.next();
            s.writeInt(e.getKey());
            s.writeObject(e.getValue());
         }
      }
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      int numBuckets = s.readInt();
      this.table = new HashIntObjectMap.Entry[numBuckets];
      this.init();
      int size = s.readInt();

      for(int i = 0; i < size; ++i) {
         int key = s.readInt();
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

   static class Entry<V> extends IntObjectPairImpl<V> {
      HashIntObjectMap.Entry<V> next;
      final int hash;

      Entry(int h, int k, V v, HashIntObjectMap.Entry<V> n) {
         super(k, v);
         this.next = n;
         this.hash = h;
      }

      void recordAccess(HashIntObjectMap<V> m) {
      }

      void recordRemoval(HashIntObjectMap<V> m) {
      }
   }

   private final class EntryIterator extends HashIntObjectMap<V>.HashIterator<IntObjectPair<V>> {
      private EntryIterator() {
      }

      public IntObjectPair<V> next() {
         return this.nextEntry();
      }
   }

   private final class EntrySet extends AbstractSet<IntObjectPair<V>> {
      private EntrySet() {
      }

      @Override
      public Iterator<IntObjectPair<V>> iterator() {
         return HashIntObjectMap.this.newEntryIterator();
      }

      @Override
      public boolean contains(Object o) {
         if (!(o instanceof IntObjectPair)) {
            return false;
         } else {
            IntObjectPair<V> e = (IntObjectPair)o;
            HashIntObjectMap.Entry<V> candidate = HashIntObjectMap.this.getEntry(e.getKey());
            return candidate != null && candidate.equals(e);
         }
      }

      @Override
      public boolean remove(Object o) {
         return HashIntObjectMap.this.removeMapping(o) != null;
      }

      @Override
      public int size() {
         return HashIntObjectMap.this.size;
      }

      @Override
      public void clear() {
         HashIntObjectMap.this.clear();
      }
   }

   private abstract class HashIntIterator implements IntIterator {
      HashIntObjectMap.Entry<V> next;
      int expectedModCount = HashIntObjectMap.this.modCount;
      int index;
      HashIntObjectMap.Entry<V> current;

      HashIntIterator() {
         if (HashIntObjectMap.this.size > 0) {
            HashIntObjectMap.Entry[] t = HashIntObjectMap.this.table;

            while(this.index < t.length && (this.next = t[this.index++]) == null) {
            }
         }
      }

      @Override
      public final boolean hasNext() {
         return this.next != null;
      }

      final HashIntObjectMap.Entry<V> nextEntry() {
         if (HashIntObjectMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            HashIntObjectMap.Entry<V> e = this.next;
            if (e == null) {
               throw new NoSuchElementException();
            } else {
               if ((this.next = e.next) == null) {
                  HashIntObjectMap.Entry[] t = HashIntObjectMap.this.table;

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
         } else if (HashIntObjectMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            int k = this.current.getKey();
            this.current = null;
            HashIntObjectMap.this.removeEntryForKey(k);
            this.expectedModCount = HashIntObjectMap.this.modCount;
         }
      }
   }

   private abstract class HashIterator<E> implements Iterator<E> {
      HashIntObjectMap.Entry<V> next;
      int expectedModCount = HashIntObjectMap.this.modCount;
      int index;
      HashIntObjectMap.Entry<V> current;

      HashIterator() {
         if (HashIntObjectMap.this.size > 0) {
            HashIntObjectMap.Entry[] t = HashIntObjectMap.this.table;

            while(this.index < t.length && (this.next = t[this.index++]) == null) {
            }
         }
      }

      @Override
      public final boolean hasNext() {
         return this.next != null;
      }

      final HashIntObjectMap.Entry<V> nextEntry() {
         if (HashIntObjectMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            HashIntObjectMap.Entry<V> e = this.next;
            if (e == null) {
               throw new NoSuchElementException();
            } else {
               if ((this.next = e.next) == null) {
                  HashIntObjectMap.Entry[] t = HashIntObjectMap.this.table;

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
         } else if (HashIntObjectMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            int k = this.current.getKey();
            this.current = null;
            HashIntObjectMap.this.removeEntryForKey(k);
            this.expectedModCount = HashIntObjectMap.this.modCount;
         }
      }
   }

   private final class KeyIterator extends HashIntObjectMap<V>.HashIntIterator {
      private KeyIterator() {
      }

      @Override
      public int next() {
         return this.nextEntry().getKey();
      }
   }

   private final class KeySet extends AbstractIntSet {
      private KeySet() {
      }

      @Override
      public IntIterator iterator() {
         return HashIntObjectMap.this.newKeyIterator();
      }

      @Override
      public int size() {
         return HashIntObjectMap.this.size;
      }

      @Override
      public boolean contains(int o) {
         return HashIntObjectMap.this.containsKey(o);
      }

      @Override
      public boolean remove(int o) {
         return HashIntObjectMap.this.removeEntryForKey(o) != null;
      }

      @Override
      public void clear() {
         HashIntObjectMap.this.clear();
      }
   }

   private final class ValueIterator extends HashIntObjectMap<V>.HashIterator<V> {
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
         return HashIntObjectMap.this.newValueIterator();
      }

      @Override
      public int size() {
         return HashIntObjectMap.this.size;
      }

      @Override
      public boolean contains(Object o) {
         return HashIntObjectMap.this.containsValue(o);
      }

      @Override
      public void clear() {
         HashIntObjectMap.this.clear();
      }
   }
}
