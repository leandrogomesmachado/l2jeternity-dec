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
import org.napile.primitive.collections.LongCollection;
import org.napile.primitive.collections.abstracts.AbstractLongCollection;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.iterators.LongIterator;
import org.napile.primitive.maps.IntLongMap;
import org.napile.primitive.maps.abstracts.AbstractIntLongMap;
import org.napile.primitive.pair.IntLongPair;
import org.napile.primitive.pair.impl.IntLongPairImpl;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.abstracts.AbstractIntSet;

public class HashIntLongMap extends AbstractIntLongMap implements IntLongMap, Cloneable, Serializable {
   static final int DEFAULT_INITIAL_CAPACITY = 16;
   static final int MAXIMUM_CAPACITY = 1073741824;
   static final float DEFAULT_LOAD_FACTOR = 0.75F;
   transient HashIntLongMap.Entry[] table;
   transient int size;
   int threshold;
   final float loadFactor;
   transient volatile int modCount;
   private transient Set<IntLongPair> entrySet = null;
   private static final long serialVersionUID = 362498820763181265L;

   public HashIntLongMap(int initialCapacity, float loadFactor) {
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
            this.table = new HashIntLongMap.Entry[capacity];
            this.init();
         } else {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
         }
      }
   }

   public HashIntLongMap(int initialCapacity) {
      this(initialCapacity, 0.75F);
   }

   public HashIntLongMap() {
      this.loadFactor = 0.75F;
      this.threshold = 12;
      this.table = new HashIntLongMap.Entry[16];
      this.init();
   }

   public HashIntLongMap(IntLongMap m) {
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
   public long get(int key) {
      int hash = hash(key);

      for(HashIntLongMap.Entry e = this.table[indexFor(hash, this.table.length)]; e != null; e = e.next) {
         if (e.hash == hash && e.getKey() == key) {
            return e.getValue();
         }
      }

      return (long)Variables.RETURN_LONG_VALUE_IF_NOT_FOUND;
   }

   @Override
   public boolean containsKey(int key) {
      return this.getEntry(key) != null;
   }

   final HashIntLongMap.Entry getEntry(int key) {
      int hash = hash(key);

      for(HashIntLongMap.Entry e = this.table[indexFor(hash, this.table.length)]; e != null; e = e.next) {
         if (e.hash == hash && e.getKey() == key) {
            return e;
         }
      }

      return null;
   }

   @Override
   public long put(int key, long value) {
      int hash = hash(key);
      int i = indexFor(hash, this.table.length);

      for(HashIntLongMap.Entry e = this.table[i]; e != null; e = e.next) {
         if (e.hash == hash && e.getKey() == key) {
            long oldValue = e.setValue(value);
            e.recordAccess(this);
            return oldValue;
         }
      }

      ++this.modCount;
      this.addEntry(hash, key, value, i);
      return (long)Variables.RETURN_LONG_VALUE_IF_NOT_FOUND;
   }

   private void putForCreate(int key, long value) {
      int hash = hash(key);
      int i = indexFor(hash, this.table.length);

      for(HashIntLongMap.Entry e = this.table[i]; e != null; e = e.next) {
         if (e.hash == hash && e.getKey() == key) {
            e.setValue(value);
            return;
         }
      }

      this.createEntry(hash, key, value, i);
   }

   private void putAllForCreate(IntLongMap m) {
      for(IntLongPair e : m.entrySet()) {
         this.putForCreate(e.getKey(), e.getValue());
      }
   }

   void resize(int newCapacity) {
      HashIntLongMap.Entry[] oldTable = this.table;
      int oldCapacity = oldTable.length;
      if (oldCapacity == 1073741824) {
         this.threshold = Integer.MAX_VALUE;
      } else {
         HashIntLongMap.Entry[] newTable = new HashIntLongMap.Entry[newCapacity];
         this.transfer(newTable);
         this.table = newTable;
         this.threshold = (int)((float)newCapacity * this.loadFactor);
      }
   }

   void transfer(HashIntLongMap.Entry[] newTable) {
      HashIntLongMap.Entry[] src = this.table;
      int newCapacity = newTable.length;

      for(int j = 0; j < src.length; ++j) {
         HashIntLongMap.Entry e = src[j];
         if (e != null) {
            src[j] = null;

            while(true) {
               HashIntLongMap.Entry next = e.next;
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
   public void putAll(IntLongMap m) {
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

         for(IntLongPair e : m.entrySet()) {
            this.put(e.getKey(), e.getValue());
         }
      }
   }

   @Override
   public long remove(int key) {
      HashIntLongMap.Entry e = this.removeEntryForKey(key);
      return e == null ? (long)Variables.RETURN_LONG_VALUE_IF_NOT_FOUND : e.getValue();
   }

   final HashIntLongMap.Entry removeEntryForKey(int key) {
      int hash = hash(key);
      int i = indexFor(hash, this.table.length);
      HashIntLongMap.Entry prev = this.table[i];

      HashIntLongMap.Entry e;
      HashIntLongMap.Entry next;
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

   final HashIntLongMap.Entry removeMapping(Object o) {
      if (!(o instanceof IntLongPair)) {
         return null;
      } else {
         IntLongPair entry = (IntLongPair)o;
         Object key = entry.getKey();
         int hash = key == null ? 0 : hash(key.hashCode());
         int i = indexFor(hash, this.table.length);
         HashIntLongMap.Entry prev = this.table[i];

         HashIntLongMap.Entry e;
         HashIntLongMap.Entry next;
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
      HashIntLongMap.Entry[] tab = this.table;

      for(int i = 0; i < tab.length; ++i) {
         tab[i] = null;
      }

      this.size = 0;
   }

   @Override
   public boolean containsValue(long value) {
      HashIntLongMap.Entry[] tab = this.table;

      for(int i = 0; i < tab.length; ++i) {
         for(HashIntLongMap.Entry e = tab[i]; e != null; e = e.next) {
            if (value == e.getValue()) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public Object clone() {
      HashIntLongMap result = null;

      try {
         result = (HashIntLongMap)super.clone();
      } catch (CloneNotSupportedException var3) {
      }

      result.table = new HashIntLongMap.Entry[this.table.length];
      result.entrySet = null;
      result.modCount = 0;
      result.size = 0;
      result.init();
      result.putAllForCreate(this);
      return result;
   }

   void addEntry(int hash, int key, long value, int bucketIndex) {
      HashIntLongMap.Entry e = this.table[bucketIndex];
      this.table[bucketIndex] = new HashIntLongMap.Entry(hash, key, value, e);
      if (this.size++ >= this.threshold) {
         this.resize(2 * this.table.length);
      }
   }

   void createEntry(int hash, int key, long value, int bucketIndex) {
      HashIntLongMap.Entry e = this.table[bucketIndex];
      this.table[bucketIndex] = new HashIntLongMap.Entry(hash, key, value, e);
      ++this.size;
   }

   IntIterator newKeyIterator() {
      return new HashIntLongMap.KeyIterator();
   }

   LongIterator newValueIterator() {
      return new HashIntLongMap.ValueIterator();
   }

   Iterator<IntLongPair> newEntryIterator() {
      return new HashIntLongMap.EntryIterator();
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
      return ks != null ? ks : (this.keySet = new HashIntLongMap.KeySet());
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
      return vs != null ? vs : (this.values = new HashIntLongMap.Values());
   }

   @Override
   public Set<IntLongPair> entrySet() {
      return this.entrySet0();
   }

   private Set<IntLongPair> entrySet0() {
      Set<IntLongPair> es = this.entrySet;
      return es != null ? es : (this.entrySet = new HashIntLongMap.EntrySet());
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      Iterator<IntLongPair> i = this.size > 0 ? this.entrySet0().iterator() : null;
      s.defaultWriteObject();
      s.writeInt(this.table.length);
      s.writeInt(this.size);
      if (i != null) {
         while(i.hasNext()) {
            IntLongPair e = i.next();
            s.writeInt(e.getKey());
            s.writeObject(e.getValue());
         }
      }
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      int numBuckets = s.readInt();
      this.table = new HashIntLongMap.Entry[numBuckets];
      this.init();
      int size = s.readInt();

      for(int i = 0; i < size; ++i) {
         int key = s.readInt();
         long value = s.readLong();
         this.putForCreate(key, value);
      }
   }

   public int capacity() {
      return this.table.length;
   }

   public float loadFactor() {
      return this.loadFactor;
   }

   static class Entry extends IntLongPairImpl {
      HashIntLongMap.Entry next;
      final int hash;

      Entry(int h, int k, long v, HashIntLongMap.Entry n) {
         super(k, v);
         this.next = n;
         this.hash = h;
      }

      void recordAccess(HashIntLongMap m) {
      }

      void recordRemoval(HashIntLongMap m) {
      }
   }

   private final class EntryIterator extends HashIntLongMap.HashIterator<IntLongPair> {
      private EntryIterator() {
      }

      public IntLongPair next() {
         return this.nextEntry();
      }
   }

   private final class EntrySet extends AbstractSet<IntLongPair> {
      private EntrySet() {
      }

      @Override
      public Iterator<IntLongPair> iterator() {
         return HashIntLongMap.this.newEntryIterator();
      }

      @Override
      public boolean contains(Object o) {
         if (!(o instanceof IntLongPair)) {
            return false;
         } else {
            IntLongPair e = (IntLongPair)o;
            HashIntLongMap.Entry candidate = HashIntLongMap.this.getEntry(e.getKey());
            return candidate != null && candidate.equals(e);
         }
      }

      @Override
      public boolean remove(Object o) {
         return HashIntLongMap.this.removeMapping(o) != null;
      }

      @Override
      public int size() {
         return HashIntLongMap.this.size;
      }

      @Override
      public void clear() {
         HashIntLongMap.this.clear();
      }
   }

   private abstract class HashIntIterator implements IntIterator {
      HashIntLongMap.Entry next;
      int expectedModCount = HashIntLongMap.this.modCount;
      int index;
      HashIntLongMap.Entry current;

      HashIntIterator() {
         if (HashIntLongMap.this.size > 0) {
            HashIntLongMap.Entry[] t = HashIntLongMap.this.table;

            while(this.index < t.length && (this.next = t[this.index++]) == null) {
            }
         }
      }

      @Override
      public final boolean hasNext() {
         return this.next != null;
      }

      final HashIntLongMap.Entry nextEntry() {
         if (HashIntLongMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            HashIntLongMap.Entry e = this.next;
            if (e == null) {
               throw new NoSuchElementException();
            } else {
               if ((this.next = e.next) == null) {
                  HashIntLongMap.Entry[] t = HashIntLongMap.this.table;

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
         } else if (HashIntLongMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            int k = this.current.getKey();
            this.current = null;
            HashIntLongMap.this.removeEntryForKey(k);
            this.expectedModCount = HashIntLongMap.this.modCount;
         }
      }
   }

   private abstract class HashIterator<E> implements Iterator<E> {
      HashIntLongMap.Entry next;
      int expectedModCount = HashIntLongMap.this.modCount;
      int index;
      HashIntLongMap.Entry current;

      HashIterator() {
         if (HashIntLongMap.this.size > 0) {
            HashIntLongMap.Entry[] t = HashIntLongMap.this.table;

            while(this.index < t.length && (this.next = t[this.index++]) == null) {
            }
         }
      }

      @Override
      public final boolean hasNext() {
         return this.next != null;
      }

      final HashIntLongMap.Entry nextEntry() {
         if (HashIntLongMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            HashIntLongMap.Entry e = this.next;
            if (e == null) {
               throw new NoSuchElementException();
            } else {
               if ((this.next = e.next) == null) {
                  HashIntLongMap.Entry[] t = HashIntLongMap.this.table;

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
         } else if (HashIntLongMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            int k = this.current.getKey();
            this.current = null;
            HashIntLongMap.this.removeEntryForKey(k);
            this.expectedModCount = HashIntLongMap.this.modCount;
         }
      }
   }

   private abstract class HashLongIterator implements LongIterator {
      HashIntLongMap.Entry next;
      int expectedModCount = HashIntLongMap.this.modCount;
      int index;
      HashIntLongMap.Entry current;

      HashLongIterator() {
         if (HashIntLongMap.this.size > 0) {
            HashIntLongMap.Entry[] t = HashIntLongMap.this.table;

            while(this.index < t.length && (this.next = t[this.index++]) == null) {
            }
         }
      }

      @Override
      public final boolean hasNext() {
         return this.next != null;
      }

      final HashIntLongMap.Entry nextEntry() {
         if (HashIntLongMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            HashIntLongMap.Entry e = this.next;
            if (e == null) {
               throw new NoSuchElementException();
            } else {
               if ((this.next = e.next) == null) {
                  HashIntLongMap.Entry[] t = HashIntLongMap.this.table;

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
         } else if (HashIntLongMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            int k = this.current.getKey();
            this.current = null;
            HashIntLongMap.this.removeEntryForKey(k);
            this.expectedModCount = HashIntLongMap.this.modCount;
         }
      }
   }

   private final class KeyIterator extends HashIntLongMap.HashIntIterator {
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
         return HashIntLongMap.this.newKeyIterator();
      }

      @Override
      public int size() {
         return HashIntLongMap.this.size;
      }

      @Override
      public boolean contains(int o) {
         return HashIntLongMap.this.containsKey(o);
      }

      @Override
      public boolean remove(int o) {
         return HashIntLongMap.this.removeEntryForKey(o) != null;
      }

      @Override
      public void clear() {
         HashIntLongMap.this.clear();
      }
   }

   private final class ValueIterator extends HashIntLongMap.HashLongIterator {
      private ValueIterator() {
      }

      @Override
      public long next() {
         return this.nextEntry().getValue();
      }
   }

   private final class Values extends AbstractLongCollection {
      private Values() {
      }

      @Override
      public LongIterator iterator() {
         return HashIntLongMap.this.newValueIterator();
      }

      @Override
      public int size() {
         return HashIntLongMap.this.size;
      }

      @Override
      public boolean contains(long o) {
         return HashIntLongMap.this.containsValue(o);
      }

      @Override
      public void clear() {
         HashIntLongMap.this.clear();
      }
   }
}
