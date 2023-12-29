package org.napile.primitive.maps.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import org.napile.HashUtils;
import org.napile.primitive.Variables;
import org.napile.primitive.collections.IntCollection;
import org.napile.primitive.collections.abstracts.AbstractIntCollection;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.maps.IntIntMap;
import org.napile.primitive.maps.abstracts.AbstractIntIntMap;
import org.napile.primitive.pair.IntIntPair;
import org.napile.primitive.pair.impl.IntIntPairImpl;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.abstracts.AbstractIntSet;

public class HashIntIntMap extends AbstractIntIntMap implements IntIntMap, Cloneable, Serializable {
   static final int DEFAULT_INITIAL_CAPACITY = 16;
   static final int MAXIMUM_CAPACITY = 1073741824;
   static final float DEFAULT_LOAD_FACTOR = 0.75F;
   transient HashIntIntMap.Entry[] table;
   transient int size;
   int threshold;
   final float loadFactor;
   transient volatile int modCount;
   private transient Set<IntIntPair> entrySet = null;
   private static final long serialVersionUID = 362498820763181265L;

   public HashIntIntMap(int initialCapacity, float loadFactor) {
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
            this.table = new HashIntIntMap.Entry[capacity];
            this.init();
         } else {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
         }
      }
   }

   public HashIntIntMap(int initialCapacity) {
      this(initialCapacity, 0.75F);
   }

   public HashIntIntMap() {
      this.loadFactor = 0.75F;
      this.threshold = 12;
      this.table = new HashIntIntMap.Entry[16];
      this.init();
   }

   public HashIntIntMap(IntIntMap m) {
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
   public int get(int key) {
      int hash = hash(key);

      for(HashIntIntMap.Entry e = this.table[indexFor(hash, this.table.length)]; e != null; e = e.next) {
         if (e.hash == hash && e.getKey() == key) {
            return e.getValue();
         }
      }

      return Variables.RETURN_LONG_VALUE_IF_NOT_FOUND;
   }

   @Override
   public boolean containsKey(int key) {
      return this.getEntry(key) != null;
   }

   final HashIntIntMap.Entry getEntry(int key) {
      int hash = hash(key);

      for(HashIntIntMap.Entry e = this.table[indexFor(hash, this.table.length)]; e != null; e = e.next) {
         if (e.hash == hash && e.getKey() == key) {
            return e;
         }
      }

      return null;
   }

   @Override
   public int put(int key, int value) {
      int hash = hash(key);
      int i = indexFor(hash, this.table.length);

      for(HashIntIntMap.Entry e = this.table[i]; e != null; e = e.next) {
         if (e.hash == hash && e.getKey() == key) {
            int oldValue = e.setValue(value);
            e.recordAccess(this);
            return oldValue;
         }
      }

      ++this.modCount;
      this.addEntry(hash, key, value, i);
      return Variables.RETURN_LONG_VALUE_IF_NOT_FOUND;
   }

   private void putForCreate(int key, int value) {
      int hash = hash(key);
      int i = indexFor(hash, this.table.length);

      for(HashIntIntMap.Entry e = this.table[i]; e != null; e = e.next) {
         if (e.hash == hash && e.getKey() == key) {
            e.setValue(value);
            return;
         }
      }

      this.createEntry(hash, key, value, i);
   }

   private void putAllForCreate(IntIntMap m) {
      for(IntIntPair e : m.entrySet()) {
         this.putForCreate(e.getKey(), e.getValue());
      }
   }

   void resize(int newCapacity) {
      HashIntIntMap.Entry[] oldTable = this.table;
      int oldCapacity = oldTable.length;
      if (oldCapacity == 1073741824) {
         this.threshold = Integer.MAX_VALUE;
      } else {
         HashIntIntMap.Entry[] newTable = new HashIntIntMap.Entry[newCapacity];
         this.transfer(newTable);
         this.table = newTable;
         this.threshold = (int)((float)newCapacity * this.loadFactor);
      }
   }

   void transfer(HashIntIntMap.Entry[] newTable) {
      HashIntIntMap.Entry[] src = this.table;
      int newCapacity = newTable.length;

      for(int j = 0; j < src.length; ++j) {
         HashIntIntMap.Entry e = src[j];
         if (e != null) {
            src[j] = null;

            while(true) {
               HashIntIntMap.Entry next = e.next;
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
   public void putAll(IntIntMap m) {
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

         for(IntIntPair e : m.entrySet()) {
            this.put(e.getKey(), e.getValue());
         }
      }
   }

   @Override
   public int remove(int key) {
      HashIntIntMap.Entry e = this.removeEntryForKey(key);
      return e == null ? Variables.RETURN_LONG_VALUE_IF_NOT_FOUND : e.getValue();
   }

   final HashIntIntMap.Entry removeEntryForKey(int key) {
      int hash = hash(key);
      int i = indexFor(hash, this.table.length);
      HashIntIntMap.Entry prev = this.table[i];

      HashIntIntMap.Entry e;
      HashIntIntMap.Entry next;
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

   final HashIntIntMap.Entry removeMapping(Object o) {
      if (!(o instanceof IntIntPair)) {
         return null;
      } else {
         IntIntPair entry = (IntIntPair)o;
         Object key = entry.getKey();
         int hash = key == null ? 0 : hash(key.hashCode());
         int i = indexFor(hash, this.table.length);
         HashIntIntMap.Entry prev = this.table[i];

         HashIntIntMap.Entry e;
         HashIntIntMap.Entry next;
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
      HashIntIntMap.Entry[] tab = this.table;

      for(int i = 0; i < tab.length; ++i) {
         tab[i] = null;
      }

      this.size = 0;
   }

   @Override
   public boolean containsValue(int value) {
      HashIntIntMap.Entry[] tab = this.table;

      for(int i = 0; i < tab.length; ++i) {
         for(HashIntIntMap.Entry e = tab[i]; e != null; e = e.next) {
            if (value == e.getValue()) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public Object clone() {
      HashIntIntMap result = null;

      try {
         result = (HashIntIntMap)super.clone();
      } catch (CloneNotSupportedException var3) {
      }

      result.table = new HashIntIntMap.Entry[this.table.length];
      result.entrySet = null;
      result.modCount = 0;
      result.size = 0;
      result.init();
      result.putAllForCreate(this);
      return result;
   }

   void addEntry(int hash, int key, int value, int bucketIndex) {
      HashIntIntMap.Entry e = this.table[bucketIndex];
      this.table[bucketIndex] = new HashIntIntMap.Entry(hash, key, value, e);
      if (this.size++ >= this.threshold) {
         this.resize(2 * this.table.length);
      }
   }

   void createEntry(int hash, int key, int value, int bucketIndex) {
      HashIntIntMap.Entry e = this.table[bucketIndex];
      this.table[bucketIndex] = new HashIntIntMap.Entry(hash, key, value, e);
      ++this.size;
   }

   IntIterator newKeyIterator() {
      return new HashIntIntMap.KeyIterator();
   }

   IntIterator newValueIterator() {
      return new HashIntIntMap.ValueIterator();
   }

   Iterator<IntIntPair> newEntryIterator() {
      return new HashIntIntMap.EntryIterator();
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
      return ks != null ? ks : (this.keySet = new HashIntIntMap.KeySet());
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
      return vs != null ? vs : (this.values = new HashIntIntMap.Values());
   }

   @Override
   public Set<IntIntPair> entrySet() {
      return this.entrySet0();
   }

   private Set<IntIntPair> entrySet0() {
      Set<IntIntPair> es = this.entrySet;
      return es != null ? es : (this.entrySet = new HashIntIntMap.EntrySet());
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      Iterator<IntIntPair> i = this.size > 0 ? this.entrySet0().iterator() : null;
      s.defaultWriteObject();
      s.writeInt(this.table.length);
      s.writeInt(this.size);
      if (i != null) {
         while(i.hasNext()) {
            IntIntPair e = i.next();
            s.writeInt(e.getKey());
            s.writeObject(e.getValue());
         }
      }
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      int numBuckets = s.readInt();
      this.table = new HashIntIntMap.Entry[numBuckets];
      this.init();
      int size = s.readInt();

      for(int i = 0; i < size; ++i) {
         int key = s.readInt();
         int value = s.readInt();
         this.putForCreate(key, value);
      }
   }

   public int capacity() {
      return this.table.length;
   }

   public float loadFactor() {
      return this.loadFactor;
   }

   static class Entry extends IntIntPairImpl {
      HashIntIntMap.Entry next;
      final int hash;

      Entry(int h, int k, int v, HashIntIntMap.Entry n) {
         super(k, v);
         this.next = n;
         this.hash = h;
      }

      void recordAccess(HashIntIntMap m) {
      }

      void recordRemoval(HashIntIntMap m) {
      }
   }

   private final class EntryIterator extends HashIntIntMap.HashIterator<IntIntPair> {
      private EntryIterator() {
      }

      public IntIntPair next() {
         return this.nextEntry();
      }
   }

   private final class EntrySet extends AbstractSet<IntIntPair> {
      private EntrySet() {
      }

      @Override
      public Iterator<IntIntPair> iterator() {
         return HashIntIntMap.this.newEntryIterator();
      }

      @Override
      public boolean contains(Object o) {
         if (!(o instanceof IntIntPair)) {
            return false;
         } else {
            IntIntPair e = (IntIntPair)o;
            HashIntIntMap.Entry candidate = HashIntIntMap.this.getEntry(e.getKey());
            return candidate != null && candidate.equals(e);
         }
      }

      @Override
      public boolean remove(Object o) {
         return HashIntIntMap.this.removeMapping(o) != null;
      }

      @Override
      public int size() {
         return HashIntIntMap.this.size;
      }

      @Override
      public void clear() {
         HashIntIntMap.this.clear();
      }
   }

   private abstract class HashIntIterator implements IntIterator {
      HashIntIntMap.Entry next;
      int expectedModCount = HashIntIntMap.this.modCount;
      int index;
      HashIntIntMap.Entry current;

      HashIntIterator() {
         if (HashIntIntMap.this.size > 0) {
            HashIntIntMap.Entry[] t = HashIntIntMap.this.table;

            while(this.index < t.length && (this.next = t[this.index++]) == null) {
            }
         }
      }

      @Override
      public final boolean hasNext() {
         return this.next != null;
      }

      final HashIntIntMap.Entry nextEntry() {
         if (HashIntIntMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            HashIntIntMap.Entry e = this.next;
            if (e == null) {
               throw new NoSuchElementException();
            } else {
               if ((this.next = e.next) == null) {
                  HashIntIntMap.Entry[] t = HashIntIntMap.this.table;

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
         } else if (HashIntIntMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            int k = this.current.getKey();
            this.current = null;
            HashIntIntMap.this.removeEntryForKey(k);
            this.expectedModCount = HashIntIntMap.this.modCount;
         }
      }
   }

   private abstract class HashIterator<E> implements Iterator<E> {
      HashIntIntMap.Entry next;
      int expectedModCount = HashIntIntMap.this.modCount;
      int index;
      HashIntIntMap.Entry current;

      HashIterator() {
         if (HashIntIntMap.this.size > 0) {
            HashIntIntMap.Entry[] t = HashIntIntMap.this.table;

            while(this.index < t.length && (this.next = t[this.index++]) == null) {
            }
         }
      }

      @Override
      public final boolean hasNext() {
         return this.next != null;
      }

      final HashIntIntMap.Entry nextEntry() {
         if (HashIntIntMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            HashIntIntMap.Entry e = this.next;
            if (e == null) {
               throw new NoSuchElementException();
            } else {
               if ((this.next = e.next) == null) {
                  HashIntIntMap.Entry[] t = HashIntIntMap.this.table;

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
         } else if (HashIntIntMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            int k = this.current.getKey();
            this.current = null;
            HashIntIntMap.this.removeEntryForKey(k);
            this.expectedModCount = HashIntIntMap.this.modCount;
         }
      }
   }

   private final class KeyIterator extends HashIntIntMap.HashIntIterator {
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
         return HashIntIntMap.this.newKeyIterator();
      }

      @Override
      public int size() {
         return HashIntIntMap.this.size;
      }

      @Override
      public boolean contains(int o) {
         return HashIntIntMap.this.containsKey(o);
      }

      @Override
      public boolean remove(int o) {
         return HashIntIntMap.this.removeEntryForKey(o) != null;
      }

      @Override
      public void clear() {
         HashIntIntMap.this.clear();
      }
   }

   private final class ValueIterator extends HashIntIntMap.HashIntIterator {
      private ValueIterator() {
      }

      @Override
      public int next() {
         return this.nextEntry().getValue();
      }
   }

   private final class Values extends AbstractIntCollection {
      private Values() {
      }

      @Override
      public IntIterator iterator() {
         return HashIntIntMap.this.newValueIterator();
      }

      @Override
      public int size() {
         return HashIntIntMap.this.size;
      }

      @Override
      public boolean contains(int o) {
         return HashIntIntMap.this.containsValue(o);
      }

      @Override
      public void clear() {
         HashIntIntMap.this.clear();
      }
   }
}
