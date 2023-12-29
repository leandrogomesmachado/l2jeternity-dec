package org.napile.primitive.sets.impl;

import java.io.Serializable;
import java.util.concurrent.ConcurrentSkipListSet;
import org.napile.primitive.collections.IntCollection;
import org.napile.primitive.comparators.IntComparator;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.maps.CNavigableIntObjectMap;
import org.napile.primitive.maps.impl.CTreeIntObjectMap;
import org.napile.primitive.pair.IntObjectPair;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.NavigableIntSet;
import org.napile.primitive.sets.SortedIntSet;
import org.napile.primitive.sets.abstracts.AbstractIntSet;
import sun.misc.Unsafe;

public class CTreeIntSet extends AbstractIntSet implements NavigableIntSet, Cloneable, Serializable {
   private final CNavigableIntObjectMap<Object> m;
   private static final Unsafe unsafe = Unsafe.getUnsafe();
   private static final long mapOffset;

   public CTreeIntSet() {
      this.m = new CTreeIntObjectMap<>();
   }

   public CTreeIntSet(IntComparator comparator) {
      this.m = new CTreeIntObjectMap<>(comparator);
   }

   public CTreeIntSet(IntCollection c) {
      this();
      this.addAll(c);
   }

   public CTreeIntSet(SortedIntSet s) {
      this.m = new CTreeIntObjectMap<>(s.comparator());
      this.addAll(s);
   }

   public CTreeIntSet(CNavigableIntObjectMap<Object> m) {
      this.m = m;
   }

   public CTreeIntSet clone() {
      CTreeIntSet clone = null;

      try {
         clone = (CTreeIntSet)super.clone();
         clone.setMap(new CTreeIntObjectMap<>(this.m));
         return clone;
      } catch (CloneNotSupportedException var3) {
         throw new InternalError();
      }
   }

   @Override
   public int size() {
      return this.m.size();
   }

   @Override
   public boolean isEmpty() {
      return this.m.isEmpty();
   }

   @Override
   public boolean contains(int o) {
      return this.m.containsKey(o);
   }

   @Override
   public boolean add(int e) {
      return this.m.putIfAbsent(e, Boolean.TRUE) == null;
   }

   @Override
   public boolean remove(int o) {
      return this.m.remove(o, Boolean.TRUE);
   }

   @Override
   public void clear() {
      this.m.clear();
   }

   @Override
   public IntIterator iterator() {
      return this.m.navigableKeySet().iterator();
   }

   @Override
   public IntIterator descendingIterator() {
      return this.m.descendingKeySet().iterator();
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof IntSet)) {
         return false;
      } else {
         IntCollection c = (IntCollection)o;

         try {
            return this.containsAll(c) && c.containsAll(this);
         } catch (ClassCastException var4) {
            return false;
         } catch (NullPointerException var5) {
            return false;
         }
      }
   }

   @Override
   public boolean removeAll(IntCollection c) {
      boolean modified = false;
      IntIterator i = c.iterator();

      while(i.hasNext()) {
         if (this.remove(i.next())) {
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public int lower(int e) {
      return this.m.lowerKey(e);
   }

   @Override
   public int floor(int e) {
      return this.m.floorKey(e);
   }

   @Override
   public int ceiling(int e) {
      return this.m.ceilingKey(e);
   }

   @Override
   public int higher(int e) {
      return this.m.higherKey(e);
   }

   @Override
   public int pollFirst() {
      IntObjectPair<Object> e = this.m.pollFirstEntry();
      return e == null ? null : e.getKey();
   }

   @Override
   public int pollLast() {
      IntObjectPair<Object> e = this.m.pollLastEntry();
      return e == null ? null : e.getKey();
   }

   @Override
   public IntComparator comparator() {
      return this.m.comparator();
   }

   @Override
   public int first() {
      return this.m.firstKey();
   }

   @Override
   public int last() {
      return this.m.lastKey();
   }

   @Override
   public NavigableIntSet subSet(int fromElement, boolean fromInclusive, int toElement, boolean toInclusive) {
      return new CTreeIntSet(this.m.subMap(fromElement, fromInclusive, toElement, toInclusive));
   }

   @Override
   public NavigableIntSet headSet(int toElement, boolean inclusive) {
      return new CTreeIntSet(this.m.headMap(toElement, inclusive));
   }

   @Override
   public NavigableIntSet tailSet(int fromElement, boolean inclusive) {
      return new CTreeIntSet(this.m.tailMap(fromElement, inclusive));
   }

   public NavigableIntSet subSet(int fromElement, int toElement) {
      return this.subSet(fromElement, true, toElement, false);
   }

   public NavigableIntSet headSet(int toElement) {
      return this.headSet(toElement, false);
   }

   public NavigableIntSet tailSet(int fromElement) {
      return this.tailSet(fromElement, true);
   }

   @Override
   public NavigableIntSet descendingSet() {
      return new CTreeIntSet(this.m.descendingMap());
   }

   private void setMap(CNavigableIntObjectMap<Object> map) {
      unsafe.putObjectVolatile(this, mapOffset, map);
   }

   static {
      try {
         mapOffset = unsafe.objectFieldOffset(ConcurrentSkipListSet.class.getDeclaredField("m"));
      } catch (Exception var1) {
         throw new Error(var1);
      }
   }
}
