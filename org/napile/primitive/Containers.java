package org.napile.primitive;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Set;
import org.napile.primitive.collections.IntCollection;
import org.napile.primitive.collections.LongCollection;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.iterators.LongIterator;
import org.napile.primitive.lists.IntList;
import org.napile.primitive.lists.LongList;
import org.napile.primitive.lists.abstracts.AbstractIntList;
import org.napile.primitive.lists.abstracts.AbstractLongList;
import org.napile.primitive.maps.IntIntMap;
import org.napile.primitive.maps.IntLongMap;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.abstracts.AbstractIntIntMap;
import org.napile.primitive.maps.abstracts.AbstractIntLongMap;
import org.napile.primitive.maps.abstracts.AbstractIntObjectMap;
import org.napile.primitive.pair.IntIntPair;
import org.napile.primitive.pair.IntLongPair;
import org.napile.primitive.pair.IntObjectPair;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.abstracts.AbstractIntSet;

public class Containers {
   public static final int[] EMPTY_INT_ARRAY = new int[0];
   public static final long[] EMPTY_LONG_ARRAY = new long[0];
   public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
   public static final IntIterator EMPTY_INT_ITERATOR = new Containers.EmptyIntIterator();
   public static final LongIterator EMPTY_LONG_ITERATOR = new Containers.EmptyLongIterator();
   public static final Container EMPTY_CONTAINER = new Containers.EmptyContainer();
   public static final IntList EMPTY_INT_LIST = new Containers.EmptyIntList();
   public static final LongList EMPTY_LONG_LIST = new Containers.EmptyLongList();
   public static final IntSet EMPTY_INT_SET = new Containers.EmptyIntSet();
   public static final IntObjectMap EMPTY_INT_OBJECT_MAP = new Containers.EmptyIntObjectMap();
   public static final IntIntMap EMPTY_INT_INT_MAP = new Containers.EmptyIntIntMap();
   public static final IntLongMap EMPTY_INT_LONG_MAP = new Containers.EmptyIntLongMap();

   public static <V> IntObjectMap<V> emptyIntObjectMap() {
      return EMPTY_INT_OBJECT_MAP;
   }

   public static IntList singletonIntList(int t) {
      return new Containers.SingletonIntList(t);
   }

   public static LongList singletonLongList(long t) {
      return new Containers.SingletonLongList(t);
   }

   public static IntIterator singletonIntIterator(int e) {
      return new Containers.SingletonIntIterator(e);
   }

   public static LongIterator singletonLongIterator(long e) {
      return new Containers.SingletonLongIterator(e);
   }

   private static class EmptyContainer implements Container {
      private EmptyContainer() {
      }

      @Override
      public int size() {
         return 0;
      }

      @Override
      public void clear() {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean isEmpty() {
         return false;
      }
   }

   private static class EmptyIntIntMap extends AbstractIntIntMap implements Serializable {
      private EmptyIntIntMap() {
      }

      @Override
      public int size() {
         return 0;
      }

      @Override
      public boolean isEmpty() {
         return true;
      }

      @Override
      public boolean containsKey(int key) {
         return false;
      }

      @Override
      public boolean containsValue(int value) {
         return false;
      }

      @Override
      public int get(int key) {
         return Variables.RETURN_INT_VALUE_IF_NOT_FOUND;
      }

      @Override
      public int[] keys() {
         return Containers.EMPTY_INT_ARRAY;
      }

      @Override
      public int[] keys(int[] array) {
         return Containers.EMPTY_INT_ARRAY;
      }

      @Override
      public IntSet keySet() {
         return Containers.EMPTY_INT_SET;
      }

      @Override
      public int[] values() {
         return Containers.EMPTY_INT_ARRAY;
      }

      @Override
      public int[] values(int[] array) {
         return Containers.EMPTY_INT_ARRAY;
      }

      @Override
      public IntCollection valueCollection() {
         return Containers.EMPTY_INT_LIST;
      }

      @Override
      public Set<IntIntPair> entrySet() {
         return Collections.emptySet();
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof IntIntMap && ((IntIntMap)o).size() == 0;
      }

      @Override
      public int hashCode() {
         return 0;
      }

      private Object readResolve() {
         return Containers.EMPTY_INT_OBJECT_MAP;
      }
   }

   private static class EmptyIntIterator implements IntIterator {
      private EmptyIntIterator() {
      }

      @Override
      public boolean hasNext() {
         return false;
      }

      @Override
      public int next() {
         throw new NoSuchElementException();
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   private static class EmptyIntList extends AbstractIntList implements RandomAccess, Serializable {
      private EmptyIntList() {
      }

      @Override
      public int size() {
         return 0;
      }

      @Override
      public boolean contains(int obj) {
         return false;
      }

      @Override
      public int get(int index) {
         throw new IndexOutOfBoundsException("Index: " + index);
      }

      private Object readResolve() {
         return Containers.EMPTY_INT_LIST;
      }
   }

   private static class EmptyIntLongMap extends AbstractIntLongMap implements Serializable {
      private EmptyIntLongMap() {
      }

      @Override
      public int size() {
         return 0;
      }

      @Override
      public boolean isEmpty() {
         return true;
      }

      @Override
      public boolean containsKey(int key) {
         return false;
      }

      @Override
      public boolean containsValue(long value) {
         return false;
      }

      @Override
      public long get(int key) {
         return (long)Variables.RETURN_LONG_VALUE_IF_NOT_FOUND;
      }

      @Override
      public int[] keys() {
         return Containers.EMPTY_INT_ARRAY;
      }

      @Override
      public int[] keys(int[] array) {
         return Containers.EMPTY_INT_ARRAY;
      }

      @Override
      public IntSet keySet() {
         return Containers.EMPTY_INT_SET;
      }

      @Override
      public long[] values() {
         return Containers.EMPTY_LONG_ARRAY;
      }

      @Override
      public long[] values(long[] array) {
         return Containers.EMPTY_LONG_ARRAY;
      }

      @Override
      public LongCollection valueCollection() {
         return Containers.EMPTY_LONG_LIST;
      }

      @Override
      public Set<IntLongPair> entrySet() {
         return Collections.emptySet();
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof IntLongMap && ((IntLongMap)o).size() == 0;
      }

      @Override
      public int hashCode() {
         return 0;
      }

      private Object readResolve() {
         return Containers.EMPTY_INT_OBJECT_MAP;
      }
   }

   private static class EmptyIntObjectMap extends AbstractIntObjectMap<Object> implements Serializable {
      private EmptyIntObjectMap() {
      }

      @Override
      public int size() {
         return 0;
      }

      @Override
      public boolean isEmpty() {
         return true;
      }

      @Override
      public boolean containsKey(int key) {
         return false;
      }

      @Override
      public boolean containsValue(Object value) {
         return false;
      }

      @Override
      public Object get(int key) {
         return null;
      }

      @Override
      public int[] keys() {
         return Containers.EMPTY_INT_ARRAY;
      }

      @Override
      public int[] keys(int[] array) {
         return Containers.EMPTY_INT_ARRAY;
      }

      @Override
      public IntSet keySet() {
         return Containers.EMPTY_INT_SET;
      }

      @Override
      public Object[] values() {
         return Containers.EMPTY_OBJECT_ARRAY;
      }

      @Override
      public Object[] values(Object[] array) {
         return Containers.EMPTY_OBJECT_ARRAY;
      }

      @Override
      public Collection<Object> valueCollection() {
         return Collections.emptySet();
      }

      @Override
      public Set<IntObjectPair<Object>> entrySet() {
         return Collections.emptySet();
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof IntObjectMap && ((IntObjectMap)o).size() == 0;
      }

      @Override
      public int hashCode() {
         return 0;
      }

      private Object readResolve() {
         return Containers.EMPTY_INT_OBJECT_MAP;
      }
   }

   private static class EmptyIntSet extends AbstractIntSet implements Serializable {
      private EmptyIntSet() {
      }

      @Override
      public IntIterator iterator() {
         return Containers.EMPTY_INT_ITERATOR;
      }

      @Override
      public int size() {
         return 0;
      }

      @Override
      public boolean contains(int obj) {
         return false;
      }

      private Object readResolve() {
         return Containers.EMPTY_INT_SET;
      }
   }

   private static class EmptyLongIterator implements LongIterator {
      private EmptyLongIterator() {
      }

      @Override
      public boolean hasNext() {
         return false;
      }

      @Override
      public long next() {
         throw new NoSuchElementException();
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   private static class EmptyLongList extends AbstractLongList implements RandomAccess, Serializable {
      private EmptyLongList() {
      }

      @Override
      public int size() {
         return 0;
      }

      @Override
      public boolean contains(long obj) {
         return false;
      }

      @Override
      public long get(int index) {
         throw new IndexOutOfBoundsException("Index: " + index);
      }

      private Object readResolve() {
         return Containers.EMPTY_INT_LIST;
      }
   }

   private static class SingletonIntIterator implements IntIterator {
      private boolean _hasNext = true;
      private final int _value;

      public SingletonIntIterator(int value) {
         this._value = value;
      }

      @Override
      public boolean hasNext() {
         return this._hasNext;
      }

      @Override
      public int next() {
         if (this._hasNext) {
            this._hasNext = false;
            return this._value;
         } else {
            throw new NoSuchElementException();
         }
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   private static class SingletonIntList extends AbstractIntList implements RandomAccess, Serializable {
      private final int element;

      SingletonIntList(int obj) {
         this.element = obj;
      }

      @Override
      public IntIterator iterator() {
         return Containers.singletonIntIterator(this.element);
      }

      @Override
      public int size() {
         return 1;
      }

      @Override
      public boolean contains(int obj) {
         return this.element == obj;
      }

      @Override
      public int get(int index) {
         if (index != 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: 1");
         } else {
            return this.element;
         }
      }
   }

   private static class SingletonLongIterator implements LongIterator {
      private boolean _hasNext = true;
      private final long _value;

      public SingletonLongIterator(long value) {
         this._value = value;
      }

      @Override
      public boolean hasNext() {
         return this._hasNext;
      }

      @Override
      public long next() {
         if (this._hasNext) {
            this._hasNext = false;
            return this._value;
         } else {
            throw new NoSuchElementException();
         }
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   private static class SingletonLongList extends AbstractLongList implements RandomAccess, Serializable {
      private final long element;

      SingletonLongList(long obj) {
         this.element = obj;
      }

      @Override
      public LongIterator iterator() {
         return Containers.singletonLongIterator(this.element);
      }

      @Override
      public int size() {
         return 1;
      }

      @Override
      public boolean contains(long obj) {
         return this.element == obj;
      }

      @Override
      public long get(int index) {
         if (index != 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: 1");
         } else {
            return this.element;
         }
      }
   }
}
