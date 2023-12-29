package org.napile.primitive.sets.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.napile.primitive.collections.IntCollection;
import org.napile.primitive.comparators.IntComparator;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.maps.NavigableIntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;
import org.napile.primitive.pair.IntObjectPair;
import org.napile.primitive.sets.NavigableIntSet;
import org.napile.primitive.sets.SortedIntSet;
import org.napile.primitive.sets.abstracts.AbstractIntSet;

public class TreeIntSet extends AbstractIntSet implements NavigableIntSet, Cloneable, Serializable {
   private transient NavigableIntObjectMap<Object> m;
   private static final Object PRESENT = new Object();

   public TreeIntSet(NavigableIntObjectMap<?> m) {
      this.m = m;
   }

   public TreeIntSet() {
      this(new TreeIntObjectMap());
   }

   public TreeIntSet(IntComparator comparator) {
      this(new TreeIntObjectMap(comparator));
   }

   public TreeIntSet(IntCollection c) {
      this();
      this.addAll(c);
   }

   public TreeIntSet(SortedIntSet s) {
      this(s.comparator());
      this.addAll(s);
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
   public NavigableIntSet descendingSet() {
      return new TreeIntSet(this.m.descendingMap());
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
      return this.m.put(e, PRESENT) == null;
   }

   @Override
   public boolean remove(int o) {
      return this.m.remove(o) == PRESENT;
   }

   @Override
   public void clear() {
      this.m.clear();
   }

   @Override
   public boolean addAll(IntCollection c) {
      if (this.m.size() == 0 && c.size() > 0 && c instanceof SortedIntSet && this.m instanceof TreeIntObjectMap) {
         SortedIntSet set = (SortedIntSet)c;
         TreeIntObjectMap<Object> map = (TreeIntObjectMap)this.m;
         IntComparator cc = set.comparator();
         IntComparator mc = map.comparator();
         if (cc == mc || cc != null && cc.equals(mc)) {
            map.addAllForTreeSet(set, PRESENT);
            return true;
         }
      }

      return super.addAll(c);
   }

   @Override
   public NavigableIntSet subSet(int fromElement, boolean fromInclusive, int toElement, boolean toInclusive) {
      return new TreeIntSet(this.m.subMap(fromElement, fromInclusive, toElement, toInclusive));
   }

   @Override
   public NavigableIntSet headSet(int toElement, boolean inclusive) {
      return new TreeIntSet(this.m.headMap(toElement, inclusive));
   }

   @Override
   public NavigableIntSet tailSet(int fromElement, boolean inclusive) {
      return new TreeIntSet(this.m.tailMap(fromElement, inclusive));
   }

   @Override
   public SortedIntSet subSet(int fromElement, int toElement) {
      return this.subSet(fromElement, true, toElement, false);
   }

   @Override
   public SortedIntSet headSet(int toElement) {
      return this.headSet(toElement, false);
   }

   @Override
   public SortedIntSet tailSet(int fromElement) {
      return this.tailSet(fromElement, true);
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
      IntObjectPair<?> e = this.m.pollFirstEntry();
      return e == null ? null : e.getKey();
   }

   @Override
   public int pollLast() {
      IntObjectPair<?> e = this.m.pollLastEntry();
      return e == null ? null : e.getKey();
   }

   @Override
   public Object clone() {
      TreeIntSet clone = null;

      try {
         clone = (TreeIntSet)super.clone();
      } catch (CloneNotSupportedException var3) {
         throw new InternalError();
      }

      clone.m = new TreeIntObjectMap<>(this.m);
      return clone;
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      s.defaultWriteObject();
      s.writeObject(this.m.comparator());
      s.writeInt(this.m.size());
      IntIterator i = this.m.keySet().iterator();

      while(i.hasNext()) {
         s.writeInt(i.next());
      }
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      IntComparator c = (IntComparator)s.readObject();
      TreeIntObjectMap<Object> tm;
      if (c == null) {
         tm = new TreeIntObjectMap<>();
      } else {
         tm = new TreeIntObjectMap<>(c);
      }

      this.m = tm;
      int size = s.readInt();
      tm.readTreeSet(size, s, PRESENT);
   }
}
