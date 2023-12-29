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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.napile.primitive.Comparators;
import org.napile.primitive.comparators.IntComparator;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.NavigableIntObjectMap;
import org.napile.primitive.maps.SortedIntObjectMap;
import org.napile.primitive.maps.abstracts.AbstractIntObjectMap;
import org.napile.primitive.pair.IntObjectPair;
import org.napile.primitive.pair.impl.ImmutableIntObjectPairImpl;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.NavigableIntSet;
import org.napile.primitive.sets.SortedIntSet;
import org.napile.primitive.sets.abstracts.AbstractIntSet;
import org.napile.primitive.sets.impl.TreeIntSet;

public class TreeIntObjectMap<V> extends AbstractIntObjectMap<V> implements NavigableIntObjectMap<V>, Cloneable, Serializable {
   private final IntComparator comparator;
   private transient TreeIntObjectMap.Entry<V> root = null;
   private transient int size = 0;
   private transient int modCount = 0;
   private transient TreeIntObjectMap<V>.EntrySet entrySet = null;
   private transient TreeIntObjectMap.KeySet navigableKeySet = null;
   private transient NavigableIntObjectMap<V> descendingMap = null;
   private static final boolean RED = false;
   private static final boolean BLACK = true;
   private static final long serialVersionUID = 919286545866124006L;

   public TreeIntObjectMap() {
      this.comparator = null;
   }

   public TreeIntObjectMap(IntComparator comparator) {
      this.comparator = comparator;
   }

   public TreeIntObjectMap(IntObjectMap<? extends V> m) {
      this.comparator = null;
      this.putAll(m);
   }

   public TreeIntObjectMap(SortedIntObjectMap<? extends V> m) {
      this.comparator = m.comparator();

      try {
         this.buildFromSorted(m.size(), m.entrySet().iterator(), null, (V)null);
      } catch (IOException var3) {
      } catch (ClassNotFoundException var4) {
      }
   }

   @Override
   public int size() {
      return this.size;
   }

   @Override
   public boolean containsKey(int key) {
      return this.getEntry(key) != null;
   }

   @Override
   public boolean containsValue(Object value) {
      for(TreeIntObjectMap.Entry<V> e = this.getFirstEntry(); e != null; e = successor(e)) {
         if (valEquals(value, e.value)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public V get(int key) {
      TreeIntObjectMap.Entry<V> p = this.getEntry(key);
      return p == null ? null : p.value;
   }

   @Override
   public IntComparator comparator() {
      return this.comparator;
   }

   @Override
   public int firstKey() {
      return key(this.getFirstEntry());
   }

   @Override
   public int lastKey() {
      return key(this.getLastEntry());
   }

   @Override
   public void putAll(IntObjectMap<? extends V> map) {
      int mapSize = map.size();
      if (this.size == 0 && mapSize != 0 && map instanceof SortedIntObjectMap) {
         IntComparator c = ((SortedIntObjectMap)map).comparator();
         if (c == this.comparator || c != null && c.equals(this.comparator)) {
            ++this.modCount;

            try {
               this.buildFromSorted(mapSize, map.entrySet().iterator(), null, (V)null);
            } catch (IOException var5) {
            } catch (ClassNotFoundException var6) {
            }

            return;
         }
      }

      super.putAll(map);
   }

   final TreeIntObjectMap.Entry<V> getEntry(int key) {
      if (this.comparator != null) {
         return this.getEntryUsingComparator(key);
      } else {
         TreeIntObjectMap.Entry<V> p = this.root;

         while(p != null) {
            int cmp = Comparators.DEFAULT_INT_COMPARATOR.compare(key, p.key);
            if (cmp < 0) {
               p = p.left;
            } else {
               if (cmp <= 0) {
                  return p;
               }

               p = p.right;
            }
         }

         return null;
      }
   }

   final TreeIntObjectMap.Entry<V> getEntryUsingComparator(int key) {
      IntComparator cpr = this.comparator;
      if (cpr != null) {
         TreeIntObjectMap.Entry<V> p = this.root;

         while(p != null) {
            int cmp = cpr.compare(key, p.key);
            if (cmp < 0) {
               p = p.left;
            } else {
               if (cmp <= 0) {
                  return p;
               }

               p = p.right;
            }
         }
      }

      return null;
   }

   final TreeIntObjectMap.Entry<V> getCeilingEntry(int key) {
      TreeIntObjectMap.Entry<V> p = this.root;

      while(p != null) {
         int cmp = this.compare(key, p.key);
         if (cmp < 0) {
            if (p.left == null) {
               return p;
            }

            p = p.left;
         } else {
            if (cmp <= 0) {
               return p;
            }

            if (p.right == null) {
               TreeIntObjectMap.Entry<V> parent = p.parent;

               for(TreeIntObjectMap.Entry<V> ch = p; parent != null && ch == parent.right; parent = parent.parent) {
                  ch = parent;
               }

               return parent;
            }

            p = p.right;
         }
      }

      return null;
   }

   final TreeIntObjectMap.Entry<V> getFloorEntry(int key) {
      TreeIntObjectMap.Entry<V> p = this.root;

      while(p != null) {
         int cmp = this.compare(key, p.key);
         if (cmp > 0) {
            if (p.right == null) {
               return p;
            }

            p = p.right;
         } else {
            if (cmp >= 0) {
               return p;
            }

            if (p.left == null) {
               TreeIntObjectMap.Entry<V> parent = p.parent;

               for(TreeIntObjectMap.Entry<V> ch = p; parent != null && ch == parent.left; parent = parent.parent) {
                  ch = parent;
               }

               return parent;
            }

            p = p.left;
         }
      }

      return null;
   }

   final TreeIntObjectMap.Entry<V> getHigherEntry(int key) {
      TreeIntObjectMap.Entry<V> p = this.root;

      while(p != null) {
         int cmp = this.compare(key, p.key);
         if (cmp < 0) {
            if (p.left == null) {
               return p;
            }

            p = p.left;
         } else {
            if (p.right == null) {
               TreeIntObjectMap.Entry<V> parent = p.parent;

               for(TreeIntObjectMap.Entry<V> ch = p; parent != null && ch == parent.right; parent = parent.parent) {
                  ch = parent;
               }

               return parent;
            }

            p = p.right;
         }
      }

      return null;
   }

   final TreeIntObjectMap.Entry<V> getLowerEntry(int key) {
      TreeIntObjectMap.Entry<V> p = this.root;

      while(p != null) {
         int cmp = this.compare(key, p.key);
         if (cmp > 0) {
            if (p.right == null) {
               return p;
            }

            p = p.right;
         } else {
            if (p.left == null) {
               TreeIntObjectMap.Entry<V> parent = p.parent;

               for(TreeIntObjectMap.Entry<V> ch = p; parent != null && ch == parent.left; parent = parent.parent) {
                  ch = parent;
               }

               return parent;
            }

            p = p.left;
         }
      }

      return null;
   }

   @Override
   public V put(int key, V value) {
      TreeIntObjectMap.Entry<V> t = this.root;
      if (t == null) {
         this.root = new TreeIntObjectMap.Entry<>(key, value, null);
         this.size = 1;
         ++this.modCount;
         return null;
      } else {
         IntComparator cpr = this.comparator;
         int cmp;
         TreeIntObjectMap.Entry<V> parent;
         if (cpr != null) {
            do {
               parent = t;
               cmp = cpr.compare(key, t.key);
               if (cmp < 0) {
                  t = t.left;
               } else {
                  if (cmp <= 0) {
                     return t.setValue(value);
                  }

                  t = t.right;
               }
            } while(t != null);
         } else {
            do {
               parent = t;
               cmp = Comparators.DEFAULT_INT_COMPARATOR.compare(key, t.key);
               if (cmp < 0) {
                  t = t.left;
               } else {
                  if (cmp <= 0) {
                     return t.setValue(value);
                  }

                  t = t.right;
               }
            } while(t != null);
         }

         TreeIntObjectMap.Entry<V> e = new TreeIntObjectMap.Entry<>(key, value, parent);
         if (cmp < 0) {
            parent.left = e;
         } else {
            parent.right = e;
         }

         this.fixAfterInsertion(e);
         ++this.size;
         ++this.modCount;
         return null;
      }
   }

   @Override
   public V remove(int key) {
      TreeIntObjectMap.Entry<V> p = this.getEntry(key);
      if (p == null) {
         return null;
      } else {
         V oldValue = p.value;
         this.deleteEntry(p);
         return oldValue;
      }
   }

   @Override
   public void clear() {
      ++this.modCount;
      this.size = 0;
      this.root = null;
   }

   @Override
   public Object clone() {
      TreeIntObjectMap<V> clone = null;

      try {
         clone = (TreeIntObjectMap)super.clone();
      } catch (CloneNotSupportedException var5) {
         throw new InternalError();
      }

      clone.root = null;
      clone.size = 0;
      clone.modCount = 0;
      clone.entrySet = null;
      clone.navigableKeySet = null;
      clone.descendingMap = null;

      try {
         clone.buildFromSorted(this.size, this.entrySet().iterator(), null, (V)null);
      } catch (IOException var3) {
      } catch (ClassNotFoundException var4) {
      }

      return clone;
   }

   @Override
   public IntObjectPair<V> firstEntry() {
      return exportEntry(this.getFirstEntry());
   }

   @Override
   public IntObjectPair<V> lastEntry() {
      return exportEntry(this.getLastEntry());
   }

   @Override
   public IntObjectPair<V> pollFirstEntry() {
      TreeIntObjectMap.Entry<V> p = this.getFirstEntry();
      IntObjectPair<V> result = exportEntry(p);
      if (p != null) {
         this.deleteEntry(p);
      }

      return result;
   }

   @Override
   public IntObjectPair<V> pollLastEntry() {
      TreeIntObjectMap.Entry<V> p = this.getLastEntry();
      IntObjectPair<V> result = exportEntry(p);
      if (p != null) {
         this.deleteEntry(p);
      }

      return result;
   }

   @Override
   public IntObjectPair<V> lowerEntry(int key) {
      return exportEntry(this.getLowerEntry(key));
   }

   @Override
   public int lowerKey(int key) {
      return keyOrNull(this.getLowerEntry(key));
   }

   @Override
   public IntObjectPair<V> floorEntry(int key) {
      return exportEntry(this.getFloorEntry(key));
   }

   @Override
   public int floorKey(int key) {
      return keyOrNull(this.getFloorEntry(key));
   }

   @Override
   public IntObjectPair<V> ceilingEntry(int key) {
      return exportEntry(this.getCeilingEntry(key));
   }

   @Override
   public int ceilingKey(int key) {
      return keyOrNull(this.getCeilingEntry(key));
   }

   @Override
   public IntObjectPair<V> higherEntry(int key) {
      return exportEntry(this.getHigherEntry(key));
   }

   @Override
   public int higherKey(int key) {
      return keyOrNull(this.getHigherEntry(key));
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
      return this.navigableKeySet();
   }

   @Override
   public NavigableIntSet navigableKeySet() {
      TreeIntObjectMap.KeySet nks = this.navigableKeySet;
      return nks != null ? nks : (this.navigableKeySet = new TreeIntObjectMap.KeySet(this));
   }

   @Override
   public NavigableIntSet descendingKeySet() {
      return this.descendingMap().navigableKeySet();
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
      return vs != null ? vs : (this.values = new TreeIntObjectMap.Values());
   }

   @Override
   public Set<IntObjectPair<V>> entrySet() {
      TreeIntObjectMap<V>.EntrySet es = this.entrySet;
      return es != null ? es : (this.entrySet = new TreeIntObjectMap.EntrySet());
   }

   @Override
   public NavigableIntObjectMap<V> descendingMap() {
      NavigableIntObjectMap<V> km = this.descendingMap;
      return km != null ? km : (this.descendingMap = new TreeIntObjectMap.DescendingSubMap<>(this, true, 0, true, true, 0, true));
   }

   @Override
   public NavigableIntObjectMap<V> subMap(int fromKey, boolean fromInclusive, int toKey, boolean toInclusive) {
      return new TreeIntObjectMap.AscendingSubMap<>(this, false, fromKey, fromInclusive, false, toKey, toInclusive);
   }

   @Override
   public NavigableIntObjectMap<V> headMap(int toKey, boolean inclusive) {
      return new TreeIntObjectMap.AscendingSubMap<>(this, true, 0, true, false, toKey, inclusive);
   }

   @Override
   public NavigableIntObjectMap<V> tailMap(int fromKey, boolean inclusive) {
      return new TreeIntObjectMap.AscendingSubMap<>(this, false, fromKey, inclusive, true, 0, true);
   }

   @Override
   public SortedIntObjectMap<V> subMap(int fromKey, int toKey) {
      return this.subMap(fromKey, true, toKey, false);
   }

   @Override
   public SortedIntObjectMap<V> headMap(int toKey) {
      return this.headMap(toKey, false);
   }

   @Override
   public SortedIntObjectMap<V> tailMap(int fromKey) {
      return this.tailMap(fromKey, true);
   }

   IntIterator keyIterator() {
      return new TreeIntObjectMap.KeyIterator(this.getFirstEntry());
   }

   IntIterator descendingKeyIterator() {
      return new TreeIntObjectMap.DescendingKeyIterator(this.getLastEntry());
   }

   final int compare(int k1, int k2) {
      return this.comparator == null ? Comparators.DEFAULT_INT_COMPARATOR.compare(k1, k2) : this.comparator.compare(k1, k2);
   }

   static final boolean valEquals(Object o1, Object o2) {
      return o1 == null ? o2 == null : o1.equals(o2);
   }

   static <V> IntObjectPair<V> exportEntry(TreeIntObjectMap.Entry<V> e) {
      return e == null ? null : new ImmutableIntObjectPairImpl<>(e.getKey(), e.getValue());
   }

   static <V> int keyOrNull(TreeIntObjectMap.Entry<V> e) {
      return e == null ? null : e.key;
   }

   static int key(TreeIntObjectMap.Entry<?> e) {
      if (e == null) {
         throw new NoSuchElementException();
      } else {
         return e.key;
      }
   }

   final TreeIntObjectMap.Entry<V> getFirstEntry() {
      TreeIntObjectMap.Entry<V> p = this.root;
      if (p != null) {
         while(p.left != null) {
            p = p.left;
         }
      }

      return p;
   }

   final TreeIntObjectMap.Entry<V> getLastEntry() {
      TreeIntObjectMap.Entry<V> p = this.root;
      if (p != null) {
         while(p.right != null) {
            p = p.right;
         }
      }

      return p;
   }

   static <V> TreeIntObjectMap.Entry<V> successor(TreeIntObjectMap.Entry<V> t) {
      if (t == null) {
         return null;
      } else if (t.right != null) {
         TreeIntObjectMap.Entry<V> p = t.right;

         while(p.left != null) {
            p = p.left;
         }

         return p;
      } else {
         TreeIntObjectMap.Entry<V> p = t.parent;

         for(TreeIntObjectMap.Entry<V> ch = t; p != null && ch == p.right; p = p.parent) {
            ch = p;
         }

         return p;
      }
   }

   static <V> TreeIntObjectMap.Entry<V> predecessor(TreeIntObjectMap.Entry<V> t) {
      if (t == null) {
         return null;
      } else if (t.left != null) {
         TreeIntObjectMap.Entry<V> p = t.left;

         while(p.right != null) {
            p = p.right;
         }

         return p;
      } else {
         TreeIntObjectMap.Entry<V> p = t.parent;

         for(TreeIntObjectMap.Entry<V> ch = t; p != null && ch == p.left; p = p.parent) {
            ch = p;
         }

         return p;
      }
   }

   private static <V> boolean colorOf(TreeIntObjectMap.Entry<V> p) {
      return p == null ? true : p.color;
   }

   private static <V> TreeIntObjectMap.Entry<V> parentOf(TreeIntObjectMap.Entry<V> p) {
      return p == null ? null : p.parent;
   }

   private static <V> void setColor(TreeIntObjectMap.Entry<V> p, boolean c) {
      if (p != null) {
         p.color = c;
      }
   }

   private static <V> TreeIntObjectMap.Entry<V> leftOf(TreeIntObjectMap.Entry<V> p) {
      return p == null ? null : p.left;
   }

   private static <V> TreeIntObjectMap.Entry<V> rightOf(TreeIntObjectMap.Entry<V> p) {
      return p == null ? null : p.right;
   }

   private void rotateLeft(TreeIntObjectMap.Entry<V> p) {
      if (p != null) {
         TreeIntObjectMap.Entry<V> r = p.right;
         p.right = r.left;
         if (r.left != null) {
            r.left.parent = p;
         }

         r.parent = p.parent;
         if (p.parent == null) {
            this.root = r;
         } else if (p.parent.left == p) {
            p.parent.left = r;
         } else {
            p.parent.right = r;
         }

         r.left = p;
         p.parent = r;
      }
   }

   private void rotateRight(TreeIntObjectMap.Entry<V> p) {
      if (p != null) {
         TreeIntObjectMap.Entry<V> l = p.left;
         p.left = l.right;
         if (l.right != null) {
            l.right.parent = p;
         }

         l.parent = p.parent;
         if (p.parent == null) {
            this.root = l;
         } else if (p.parent.right == p) {
            p.parent.right = l;
         } else {
            p.parent.left = l;
         }

         l.right = p;
         p.parent = l;
      }
   }

   private void fixAfterInsertion(TreeIntObjectMap.Entry<V> x) {
      x.color = false;

      while(x != null && x != this.root && !x.parent.color) {
         if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
            TreeIntObjectMap.Entry<V> y = rightOf(parentOf(parentOf(x)));
            if (!colorOf(y)) {
               setColor(parentOf(x), true);
               setColor(y, true);
               setColor(parentOf(parentOf(x)), false);
               x = parentOf(parentOf(x));
            } else {
               if (x == rightOf(parentOf(x))) {
                  x = parentOf(x);
                  this.rotateLeft(x);
               }

               setColor(parentOf(x), true);
               setColor(parentOf(parentOf(x)), false);
               this.rotateRight(parentOf(parentOf(x)));
            }
         } else {
            TreeIntObjectMap.Entry<V> y = leftOf(parentOf(parentOf(x)));
            if (!colorOf(y)) {
               setColor(parentOf(x), true);
               setColor(y, true);
               setColor(parentOf(parentOf(x)), false);
               x = parentOf(parentOf(x));
            } else {
               if (x == leftOf(parentOf(x))) {
                  x = parentOf(x);
                  this.rotateRight(x);
               }

               setColor(parentOf(x), true);
               setColor(parentOf(parentOf(x)), false);
               this.rotateLeft(parentOf(parentOf(x)));
            }
         }
      }

      this.root.color = true;
   }

   private void deleteEntry(TreeIntObjectMap.Entry<V> p) {
      ++this.modCount;
      --this.size;
      if (p.left != null && p.right != null) {
         TreeIntObjectMap.Entry<V> s = successor(p);
         p.key = s.key;
         p.value = s.value;
         p = s;
      }

      TreeIntObjectMap.Entry<V> replacement = p.left != null ? p.left : p.right;
      if (replacement != null) {
         replacement.parent = p.parent;
         if (p.parent == null) {
            this.root = replacement;
         } else if (p == p.parent.left) {
            p.parent.left = replacement;
         } else {
            p.parent.right = replacement;
         }

         p.left = p.right = p.parent = null;
         if (p.color) {
            this.fixAfterDeletion(replacement);
         }
      } else if (p.parent == null) {
         this.root = null;
      } else {
         if (p.color) {
            this.fixAfterDeletion(p);
         }

         if (p.parent != null) {
            if (p == p.parent.left) {
               p.parent.left = null;
            } else if (p == p.parent.right) {
               p.parent.right = null;
            }

            p.parent = null;
         }
      }
   }

   private void fixAfterDeletion(TreeIntObjectMap.Entry<V> x) {
      while(x != this.root && colorOf(x)) {
         if (x == leftOf(parentOf(x))) {
            TreeIntObjectMap.Entry<V> sib = rightOf(parentOf(x));
            if (!colorOf(sib)) {
               setColor(sib, true);
               setColor(parentOf(x), false);
               this.rotateLeft(parentOf(x));
               sib = rightOf(parentOf(x));
            }

            if (colorOf(leftOf(sib)) && colorOf(rightOf(sib))) {
               setColor(sib, false);
               x = parentOf(x);
            } else {
               if (colorOf(rightOf(sib))) {
                  setColor(leftOf(sib), true);
                  setColor(sib, false);
                  this.rotateRight(sib);
                  sib = rightOf(parentOf(x));
               }

               setColor(sib, colorOf(parentOf(x)));
               setColor(parentOf(x), true);
               setColor(rightOf(sib), true);
               this.rotateLeft(parentOf(x));
               x = this.root;
            }
         } else {
            TreeIntObjectMap.Entry<V> sib = leftOf(parentOf(x));
            if (!colorOf(sib)) {
               setColor(sib, true);
               setColor(parentOf(x), false);
               this.rotateRight(parentOf(x));
               sib = leftOf(parentOf(x));
            }

            if (colorOf(rightOf(sib)) && colorOf(leftOf(sib))) {
               setColor(sib, false);
               x = parentOf(x);
            } else {
               if (colorOf(leftOf(sib))) {
                  setColor(rightOf(sib), true);
                  setColor(sib, false);
                  this.rotateLeft(sib);
                  sib = leftOf(parentOf(x));
               }

               setColor(sib, colorOf(parentOf(x)));
               setColor(parentOf(x), true);
               setColor(leftOf(sib), true);
               this.rotateRight(parentOf(x));
               x = this.root;
            }
         }
      }

      setColor(x, true);
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      s.defaultWriteObject();
      s.writeInt(this.size);

      for(IntObjectPair<V> e : this.entrySet()) {
         s.writeInt(e.getKey());
         s.writeObject(e.getValue());
      }
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      int size = s.readInt();
      this.buildFromSorted(size, null, s, (V)null);
   }

   public void readTreeSet(int size, ObjectInputStream s, V defaultVal) throws IOException, ClassNotFoundException {
      this.buildFromSorted(size, null, s, defaultVal);
   }

   public void addAllForTreeSet(SortedIntSet set, V defaultVal) {
      try {
         this.buildFromSorted(set.size(), set.iterator(), null, defaultVal);
      } catch (IOException var4) {
      } catch (ClassNotFoundException var5) {
      }
   }

   private void buildFromSorted(int size, Object it, ObjectInputStream str, V defaultVal) throws IOException, ClassNotFoundException {
      this.size = size;
      this.root = this.buildFromSorted(0, 0, size - 1, computeRedLevel(size), it, str, defaultVal);
   }

   private final TreeIntObjectMap.Entry<V> buildFromSorted(int level, int lo, int hi, int redLevel, Object it, ObjectInputStream str, V defaultVal) throws IOException, ClassNotFoundException {
      if (hi < lo) {
         return null;
      } else {
         int mid = (lo + hi) / 2;
         TreeIntObjectMap.Entry<V> left = null;
         if (lo < mid) {
            left = this.buildFromSorted(level + 1, lo, mid - 1, redLevel, it, str, defaultVal);
         }

         int key;
         V value;
         if (it != null) {
            if (defaultVal == null) {
               Iterator<IntObjectPair<V>> iterator = (Iterator)it;
               IntObjectPair<V> entry = iterator.next();
               key = entry.getKey();
               value = entry.getValue();
            } else {
               IntIterator iterator = (IntIterator)it;
               key = iterator.next();
               value = defaultVal;
            }
         } else {
            key = str.readInt();
            value = (V)(defaultVal != null ? defaultVal : str.readObject());
         }

         TreeIntObjectMap.Entry<V> middle = new TreeIntObjectMap.Entry<>(key, value, null);
         if (level == redLevel) {
            middle.color = false;
         }

         if (left != null) {
            middle.left = left;
            left.parent = middle;
         }

         if (mid < hi) {
            TreeIntObjectMap.Entry<V> right = this.buildFromSorted(level + 1, mid + 1, hi, redLevel, it, str, defaultVal);
            middle.right = right;
            right.parent = middle;
         }

         return middle;
      }
   }

   private static int computeRedLevel(int sz) {
      int level = 0;

      for(int m = sz - 1; m >= 0; m = m / 2 - 1) {
         ++level;
      }

      return level;
   }

   static final class AscendingSubMap<V> extends TreeIntObjectMap.NavigableSubMap<V> {
      private static final long serialVersionUID = 912986545866124060L;

      AscendingSubMap(TreeIntObjectMap<V> m, boolean fromStart, int lo, boolean loInclusive, boolean toEnd, int hi, boolean hiInclusive) {
         super(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive);
      }

      @Override
      public IntComparator comparator() {
         return this.m.comparator();
      }

      @Override
      public NavigableIntObjectMap<V> subMap(int fromKey, boolean fromInclusive, int toKey, boolean toInclusive) {
         if (!this.inRange(fromKey, fromInclusive)) {
            throw new IllegalArgumentException("fromKey out of range");
         } else if (!this.inRange(toKey, toInclusive)) {
            throw new IllegalArgumentException("toKey out of range");
         } else {
            return new TreeIntObjectMap.AscendingSubMap<>(this.m, false, fromKey, fromInclusive, false, toKey, toInclusive);
         }
      }

      @Override
      public NavigableIntObjectMap<V> headMap(int toKey, boolean inclusive) {
         if (!this.inRange(toKey, inclusive)) {
            throw new IllegalArgumentException("toKey out of range");
         } else {
            return new TreeIntObjectMap.AscendingSubMap<>(this.m, this.fromStart, this.lo, this.loInclusive, false, toKey, inclusive);
         }
      }

      @Override
      public NavigableIntObjectMap<V> tailMap(int fromKey, boolean inclusive) {
         if (!this.inRange(fromKey, inclusive)) {
            throw new IllegalArgumentException("fromKey out of range");
         } else {
            return new TreeIntObjectMap.AscendingSubMap<>(this.m, false, fromKey, inclusive, this.toEnd, this.hi, this.hiInclusive);
         }
      }

      @Override
      public NavigableIntObjectMap<V> descendingMap() {
         NavigableIntObjectMap<V> mv = this.descendingMapView;
         return mv != null
            ? mv
            : (
               this.descendingMapView = new TreeIntObjectMap.DescendingSubMap<>(
                  this.m, this.fromStart, this.lo, this.loInclusive, this.toEnd, this.hi, this.hiInclusive
               )
            );
      }

      @Override
      IntIterator keyIterator() {
         return new TreeIntObjectMap.NavigableSubMap.SubMapKeyIterator(this.absLowest(), this.absHighFence());
      }

      @Override
      IntIterator descendingKeyIterator() {
         return new TreeIntObjectMap.NavigableSubMap.DescendingSubMapKeyIterator(this.absHighest(), this.absLowFence());
      }

      @Override
      public Set<IntObjectPair<V>> entrySet() {
         TreeIntObjectMap.NavigableSubMap<V>.EntrySetView es = this.entrySetView;
         return (Set<IntObjectPair<V>>)(es != null ? es : new TreeIntObjectMap.AscendingSubMap.AscendingEntrySetView());
      }

      @Override
      TreeIntObjectMap.Entry<V> subLowest() {
         return this.absLowest();
      }

      @Override
      TreeIntObjectMap.Entry<V> subHighest() {
         return this.absHighest();
      }

      @Override
      TreeIntObjectMap.Entry<V> subCeiling(int key) {
         return this.absCeiling(key);
      }

      @Override
      TreeIntObjectMap.Entry<V> subHigher(int key) {
         return this.absHigher(key);
      }

      @Override
      TreeIntObjectMap.Entry<V> subFloor(int key) {
         return this.absFloor(key);
      }

      @Override
      TreeIntObjectMap.Entry<V> subLower(int key) {
         return this.absLower(key);
      }

      final class AscendingEntrySetView extends TreeIntObjectMap.NavigableSubMap<V>.EntrySetView {
         @Override
         public Iterator<IntObjectPair<V>> iterator() {
            return AscendingSubMap.this.new SubMapEntryIterator(AscendingSubMap.this.absLowest(), AscendingSubMap.this.absHighFence());
         }
      }
   }

   final class DescendingKeyIterator extends TreeIntObjectMap<V>.PrivateEntryIterator implements IntIterator {
      DescendingKeyIterator(TreeIntObjectMap.Entry<V> first) {
         super(first);
      }

      @Override
      public int next() {
         return this.prevEntry().key;
      }
   }

   static final class DescendingSubMap<V> extends TreeIntObjectMap.NavigableSubMap<V> {
      private static final long serialVersionUID = 912986545866120460L;
      private final IntComparator reverseComparator = Comparators.reverseOrder(this.m.comparator);

      DescendingSubMap(TreeIntObjectMap<V> m, boolean fromStart, int lo, boolean loInclusive, boolean toEnd, int hi, boolean hiInclusive) {
         super(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive);
      }

      @Override
      public IntComparator comparator() {
         return this.reverseComparator;
      }

      @Override
      public NavigableIntObjectMap<V> subMap(int fromKey, boolean fromInclusive, int toKey, boolean toInclusive) {
         if (!this.inRange(fromKey, fromInclusive)) {
            throw new IllegalArgumentException("fromKey out of range");
         } else if (!this.inRange(toKey, toInclusive)) {
            throw new IllegalArgumentException("toKey out of range");
         } else {
            return new TreeIntObjectMap.DescendingSubMap<>(this.m, false, toKey, toInclusive, false, fromKey, fromInclusive);
         }
      }

      @Override
      public NavigableIntObjectMap<V> headMap(int toKey, boolean inclusive) {
         if (!this.inRange(toKey, inclusive)) {
            throw new IllegalArgumentException("toKey out of range");
         } else {
            return new TreeIntObjectMap.DescendingSubMap<>(this.m, false, toKey, inclusive, this.toEnd, this.hi, this.hiInclusive);
         }
      }

      @Override
      public NavigableIntObjectMap<V> tailMap(int fromKey, boolean inclusive) {
         if (!this.inRange(fromKey, inclusive)) {
            throw new IllegalArgumentException("fromKey out of range");
         } else {
            return new TreeIntObjectMap.DescendingSubMap<>(this.m, this.fromStart, this.lo, this.loInclusive, false, fromKey, inclusive);
         }
      }

      @Override
      public NavigableIntObjectMap<V> descendingMap() {
         NavigableIntObjectMap<V> mv = this.descendingMapView;
         return mv != null
            ? mv
            : (
               this.descendingMapView = new TreeIntObjectMap.AscendingSubMap<>(
                  this.m, this.fromStart, this.lo, this.loInclusive, this.toEnd, this.hi, this.hiInclusive
               )
            );
      }

      @Override
      IntIterator keyIterator() {
         return new TreeIntObjectMap.NavigableSubMap.DescendingSubMapKeyIterator(this.absHighest(), this.absLowFence());
      }

      @Override
      IntIterator descendingKeyIterator() {
         return new TreeIntObjectMap.NavigableSubMap.SubMapKeyIterator(this.absLowest(), this.absHighFence());
      }

      @Override
      public Set<IntObjectPair<V>> entrySet() {
         TreeIntObjectMap.NavigableSubMap<V>.EntrySetView es = this.entrySetView;
         return (Set<IntObjectPair<V>>)(es != null ? es : new TreeIntObjectMap.DescendingSubMap.DescendingEntrySetView());
      }

      @Override
      TreeIntObjectMap.Entry<V> subLowest() {
         return this.absHighest();
      }

      @Override
      TreeIntObjectMap.Entry<V> subHighest() {
         return this.absLowest();
      }

      @Override
      TreeIntObjectMap.Entry<V> subCeiling(int key) {
         return this.absFloor(key);
      }

      @Override
      TreeIntObjectMap.Entry<V> subHigher(int key) {
         return this.absLower(key);
      }

      @Override
      TreeIntObjectMap.Entry<V> subFloor(int key) {
         return this.absCeiling(key);
      }

      @Override
      TreeIntObjectMap.Entry<V> subLower(int key) {
         return this.absHigher(key);
      }

      final class DescendingEntrySetView extends TreeIntObjectMap.NavigableSubMap<V>.EntrySetView {
         @Override
         public Iterator<IntObjectPair<V>> iterator() {
            return DescendingSubMap.this.new DescendingSubMapEntryIterator(DescendingSubMap.this.absHighest(), DescendingSubMap.this.absLowFence());
         }
      }
   }

   static final class Entry<V> implements IntObjectPair<V> {
      int key;
      V value;
      TreeIntObjectMap.Entry<V> left = null;
      TreeIntObjectMap.Entry<V> right = null;
      TreeIntObjectMap.Entry<V> parent;
      boolean color = true;

      Entry(int key, V value, TreeIntObjectMap.Entry<V> parent) {
         this.key = key;
         this.value = value;
         this.parent = parent;
      }

      @Override
      public int getKey() {
         return this.key;
      }

      @Override
      public V getValue() {
         return this.value;
      }

      @Override
      public V setValue(V value) {
         V oldValue = this.value;
         this.value = value;
         return oldValue;
      }

      @Override
      public boolean equals(Object o) {
         if (!(o instanceof IntObjectPair)) {
            return false;
         } else {
            IntObjectPair<?> e = (IntObjectPair)o;
            return TreeIntObjectMap.valEquals(this.key, e.getKey()) && TreeIntObjectMap.valEquals(this.value, e.getValue());
         }
      }

      @Override
      public int hashCode() {
         int valueHash = this.value == null ? 0 : this.value.hashCode();
         return this.key ^ valueHash;
      }

      @Override
      public String toString() {
         return this.key + "=" + this.value;
      }
   }

   final class EntryIterator extends TreeIntObjectMap<V>.PrivateEntryIterator implements Iterator<IntObjectPair<V>> {
      EntryIterator(TreeIntObjectMap.Entry<V> first) {
         super(first);
      }

      public IntObjectPair<V> next() {
         return this.nextEntry();
      }
   }

   class EntrySet extends AbstractSet<IntObjectPair<V>> {
      @Override
      public Iterator<IntObjectPair<V>> iterator() {
         return TreeIntObjectMap.this.new EntryIterator(TreeIntObjectMap.this.getFirstEntry());
      }

      @Override
      public boolean contains(Object o) {
         if (!(o instanceof IntObjectPair)) {
            return false;
         } else {
            IntObjectPair<V> entry = (IntObjectPair)o;
            V value = entry.getValue();
            TreeIntObjectMap.Entry<V> p = TreeIntObjectMap.this.getEntry(entry.getKey());
            return p != null && TreeIntObjectMap.valEquals(p.getValue(), value);
         }
      }

      @Override
      public boolean remove(Object o) {
         if (!(o instanceof IntObjectPair)) {
            return false;
         } else {
            IntObjectPair<V> entry = (IntObjectPair)o;
            V value = entry.getValue();
            TreeIntObjectMap.Entry<V> p = TreeIntObjectMap.this.getEntry(entry.getKey());
            if (p != null && TreeIntObjectMap.valEquals(p.getValue(), value)) {
               TreeIntObjectMap.this.deleteEntry(p);
               return true;
            } else {
               return false;
            }
         }
      }

      @Override
      public int size() {
         return TreeIntObjectMap.this.size();
      }

      @Override
      public void clear() {
         TreeIntObjectMap.this.clear();
      }
   }

   final class KeyIterator extends TreeIntObjectMap<V>.PrivateEntryIterator implements IntIterator {
      KeyIterator(TreeIntObjectMap.Entry<V> first) {
         super(first);
      }

      @Override
      public int next() {
         return this.nextEntry().key;
      }
   }

   static final class KeySet extends AbstractIntSet implements NavigableIntSet {
      private final NavigableIntObjectMap<?> m;

      KeySet(NavigableIntObjectMap<?> map) {
         this.m = map;
      }

      @Override
      public IntIterator iterator() {
         return this.m instanceof TreeIntObjectMap ? ((TreeIntObjectMap)this.m).keyIterator() : ((TreeIntObjectMap.NavigableSubMap)this.m).keyIterator();
      }

      @Override
      public IntIterator descendingIterator() {
         return this.m instanceof TreeIntObjectMap
            ? ((TreeIntObjectMap)this.m).descendingKeyIterator()
            : ((TreeIntObjectMap.NavigableSubMap)this.m).descendingKeyIterator();
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
      public void clear() {
         this.m.clear();
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
      public int first() {
         return this.m.firstKey();
      }

      @Override
      public int last() {
         return this.m.lastKey();
      }

      @Override
      public IntComparator comparator() {
         return this.m.comparator();
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
      public boolean remove(int o) {
         int oldSize = this.size();
         this.m.remove(o);
         return this.size() != oldSize;
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
      public NavigableIntSet descendingSet() {
         return new TreeIntSet(this.m.descendingMap());
      }
   }

   abstract static class NavigableSubMap<V> extends AbstractIntObjectMap<V> implements NavigableIntObjectMap<V>, Serializable {
      final TreeIntObjectMap<V> m;
      final int lo;
      final int hi;
      final boolean fromStart;
      final boolean toEnd;
      final boolean loInclusive;
      final boolean hiInclusive;
      transient NavigableIntObjectMap<V> descendingMapView = null;
      transient TreeIntObjectMap.NavigableSubMap<V>.EntrySetView entrySetView = null;
      transient TreeIntObjectMap.KeySet navigableKeySetView = null;

      NavigableSubMap(TreeIntObjectMap<V> m, boolean fromStart, int lo, boolean loInclusive, boolean toEnd, int hi, boolean hiInclusive) {
         if (!fromStart && !toEnd) {
            if (m.compare(lo, hi) > 0) {
               throw new IllegalArgumentException("fromKey > toKey");
            }
         } else {
            if (!fromStart) {
               m.compare(lo, lo);
            }

            if (!toEnd) {
               m.compare(hi, hi);
            }
         }

         this.m = m;
         this.fromStart = fromStart;
         this.lo = lo;
         this.loInclusive = loInclusive;
         this.toEnd = toEnd;
         this.hi = hi;
         this.hiInclusive = hiInclusive;
      }

      final boolean tooLow(int key) {
         if (!this.fromStart) {
            int c = this.m.compare(key, this.lo);
            if (c < 0 || c == 0 && !this.loInclusive) {
               return true;
            }
         }

         return false;
      }

      final boolean tooHigh(int key) {
         if (!this.toEnd) {
            int c = this.m.compare(key, this.hi);
            if (c > 0 || c == 0 && !this.hiInclusive) {
               return true;
            }
         }

         return false;
      }

      final boolean inRange(int key) {
         return !this.tooLow(key) && !this.tooHigh(key);
      }

      final boolean inClosedRange(int key) {
         return (this.fromStart || this.m.compare(key, this.lo) >= 0) && (this.toEnd || this.m.compare(this.hi, key) >= 0);
      }

      final boolean inRange(int key, boolean inclusive) {
         return inclusive ? this.inRange(key) : this.inClosedRange(key);
      }

      final TreeIntObjectMap.Entry<V> absLowest() {
         TreeIntObjectMap.Entry<V> e = this.fromStart
            ? this.m.getFirstEntry()
            : (this.loInclusive ? this.m.getCeilingEntry(this.lo) : this.m.getHigherEntry(this.lo));
         return e != null && !this.tooHigh(e.key) ? e : null;
      }

      final TreeIntObjectMap.Entry<V> absHighest() {
         TreeIntObjectMap.Entry<V> e = this.toEnd ? this.m.getLastEntry() : (this.hiInclusive ? this.m.getFloorEntry(this.hi) : this.m.getLowerEntry(this.hi));
         return e != null && !this.tooLow(e.key) ? e : null;
      }

      final TreeIntObjectMap.Entry<V> absCeiling(int key) {
         if (this.tooLow(key)) {
            return this.absLowest();
         } else {
            TreeIntObjectMap.Entry<V> e = this.m.getCeilingEntry(key);
            return e != null && !this.tooHigh(e.key) ? e : null;
         }
      }

      final TreeIntObjectMap.Entry<V> absHigher(int key) {
         if (this.tooLow(key)) {
            return this.absLowest();
         } else {
            TreeIntObjectMap.Entry<V> e = this.m.getHigherEntry(key);
            return e != null && !this.tooHigh(e.key) ? e : null;
         }
      }

      final TreeIntObjectMap.Entry<V> absFloor(int key) {
         if (this.tooHigh(key)) {
            return this.absHighest();
         } else {
            TreeIntObjectMap.Entry<V> e = this.m.getFloorEntry(key);
            return e != null && !this.tooLow(e.key) ? e : null;
         }
      }

      final TreeIntObjectMap.Entry<V> absLower(int key) {
         if (this.tooHigh(key)) {
            return this.absHighest();
         } else {
            TreeIntObjectMap.Entry<V> e = this.m.getLowerEntry(key);
            return e != null && !this.tooLow(e.key) ? e : null;
         }
      }

      final TreeIntObjectMap.Entry<V> absHighFence() {
         return this.toEnd ? null : (this.hiInclusive ? this.m.getHigherEntry(this.hi) : this.m.getCeilingEntry(this.hi));
      }

      final TreeIntObjectMap.Entry<V> absLowFence() {
         return this.fromStart ? null : (this.loInclusive ? this.m.getLowerEntry(this.lo) : this.m.getFloorEntry(this.lo));
      }

      abstract TreeIntObjectMap.Entry<V> subLowest();

      abstract TreeIntObjectMap.Entry<V> subHighest();

      abstract TreeIntObjectMap.Entry<V> subCeiling(int var1);

      abstract TreeIntObjectMap.Entry<V> subHigher(int var1);

      abstract TreeIntObjectMap.Entry<V> subFloor(int var1);

      abstract TreeIntObjectMap.Entry<V> subLower(int var1);

      abstract IntIterator keyIterator();

      abstract IntIterator descendingKeyIterator();

      @Override
      public boolean isEmpty() {
         return this.fromStart && this.toEnd ? this.m.isEmpty() : this.entrySet().isEmpty();
      }

      @Override
      public int size() {
         return this.fromStart && this.toEnd ? this.m.size() : this.entrySet().size();
      }

      @Override
      public final boolean containsKey(int key) {
         return this.inRange(key) && this.m.containsKey(key);
      }

      @Override
      public final V put(int key, V value) {
         if (!this.inRange(key)) {
            throw new IllegalArgumentException("key out of range");
         } else {
            return this.m.put(key, value);
         }
      }

      @Override
      public final V get(int key) {
         return !this.inRange(key) ? null : this.m.get(key);
      }

      @Override
      public final V remove(int key) {
         return !this.inRange(key) ? null : this.m.remove(key);
      }

      @Override
      public final IntObjectPair<V> ceilingEntry(int key) {
         return TreeIntObjectMap.exportEntry(this.subCeiling(key));
      }

      @Override
      public final int ceilingKey(int key) {
         return TreeIntObjectMap.keyOrNull(this.subCeiling(key));
      }

      @Override
      public final IntObjectPair<V> higherEntry(int key) {
         return TreeIntObjectMap.exportEntry(this.subHigher(key));
      }

      @Override
      public final int higherKey(int key) {
         return TreeIntObjectMap.keyOrNull(this.subHigher(key));
      }

      @Override
      public final IntObjectPair<V> floorEntry(int key) {
         return TreeIntObjectMap.exportEntry(this.subFloor(key));
      }

      @Override
      public final int floorKey(int key) {
         return TreeIntObjectMap.keyOrNull(this.subFloor(key));
      }

      @Override
      public final IntObjectPair<V> lowerEntry(int key) {
         return TreeIntObjectMap.exportEntry(this.subLower(key));
      }

      @Override
      public final int lowerKey(int key) {
         return TreeIntObjectMap.keyOrNull(this.subLower(key));
      }

      @Override
      public final int firstKey() {
         return TreeIntObjectMap.key(this.subLowest());
      }

      @Override
      public final int lastKey() {
         return TreeIntObjectMap.key(this.subHighest());
      }

      @Override
      public final IntObjectPair<V> firstEntry() {
         return TreeIntObjectMap.exportEntry(this.subLowest());
      }

      @Override
      public final IntObjectPair<V> lastEntry() {
         return TreeIntObjectMap.exportEntry(this.subHighest());
      }

      @Override
      public final IntObjectPair<V> pollFirstEntry() {
         TreeIntObjectMap.Entry<V> e = this.subLowest();
         IntObjectPair<V> result = TreeIntObjectMap.exportEntry(e);
         if (e != null) {
            this.m.deleteEntry(e);
         }

         return result;
      }

      @Override
      public final IntObjectPair<V> pollLastEntry() {
         TreeIntObjectMap.Entry<V> e = this.subHighest();
         IntObjectPair<V> result = TreeIntObjectMap.exportEntry(e);
         if (e != null) {
            this.m.deleteEntry(e);
         }

         return result;
      }

      @Override
      public final NavigableIntSet navigableKeySet() {
         TreeIntObjectMap.KeySet nksv = this.navigableKeySetView;
         return nksv != null ? nksv : (this.navigableKeySetView = new TreeIntObjectMap.KeySet(this));
      }

      @Override
      public final IntSet keySet() {
         return this.navigableKeySet();
      }

      @Override
      public NavigableIntSet descendingKeySet() {
         return this.descendingMap().navigableKeySet();
      }

      @Override
      public final SortedIntObjectMap<V> subMap(int fromKey, int toKey) {
         return this.subMap(fromKey, true, toKey, false);
      }

      @Override
      public final SortedIntObjectMap<V> headMap(int toKey) {
         return this.headMap(toKey, false);
      }

      @Override
      public final SortedIntObjectMap<V> tailMap(int fromKey) {
         return this.tailMap(fromKey, true);
      }

      final class DescendingSubMapEntryIterator extends TreeIntObjectMap.NavigableSubMap<V>.SubMapIterator implements Iterator<IntObjectPair<V>> {
         DescendingSubMapEntryIterator(TreeIntObjectMap.Entry<V> last, TreeIntObjectMap.Entry<V> fence) {
            super(last, fence);
         }

         public IntObjectPair<V> next() {
            return this.prevEntry();
         }

         @Override
         public void remove() {
            this.removeDescending();
         }
      }

      final class DescendingSubMapKeyIterator extends TreeIntObjectMap.NavigableSubMap<V>.SubMapIterator implements IntIterator {
         DescendingSubMapKeyIterator(TreeIntObjectMap.Entry<V> last, TreeIntObjectMap.Entry<V> fence) {
            super(last, fence);
         }

         @Override
         public int next() {
            return this.prevEntry().key;
         }

         @Override
         public void remove() {
            this.removeDescending();
         }
      }

      abstract class EntrySetView extends AbstractSet<IntObjectPair<V>> {
         private transient int size = -1;
         private transient int sizeModCount;

         @Override
         public int size() {
            if (NavigableSubMap.this.fromStart && NavigableSubMap.this.toEnd) {
               return NavigableSubMap.this.m.size();
            } else {
               if (this.size == -1 || this.sizeModCount != NavigableSubMap.this.m.modCount) {
                  this.sizeModCount = NavigableSubMap.this.m.modCount;
                  this.size = 0;
                  Iterator i = this.iterator();

                  while(i.hasNext()) {
                     ++this.size;
                     i.next();
                  }
               }

               return this.size;
            }
         }

         @Override
         public boolean isEmpty() {
            TreeIntObjectMap.Entry<V> n = NavigableSubMap.this.absLowest();
            return n == null || NavigableSubMap.this.tooHigh(n.key);
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof IntObjectPair)) {
               return false;
            } else {
               IntObjectPair<V> entry = (IntObjectPair)o;
               int key = entry.getKey();
               if (!NavigableSubMap.this.inRange(key)) {
                  return false;
               } else {
                  TreeIntObjectMap.Entry node = NavigableSubMap.this.m.getEntry(key);
                  return node != null && TreeIntObjectMap.valEquals(node.getValue(), entry.getValue());
               }
            }
         }

         @Override
         public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
               return false;
            } else {
               IntObjectPair<V> entry = (IntObjectPair)o;
               int key = entry.getKey();
               if (!NavigableSubMap.this.inRange(key)) {
                  return false;
               } else {
                  TreeIntObjectMap.Entry<V> node = NavigableSubMap.this.m.getEntry(key);
                  if (node != null && TreeIntObjectMap.valEquals(node.getValue(), entry.getValue())) {
                     NavigableSubMap.this.m.deleteEntry(node);
                     return true;
                  } else {
                     return false;
                  }
               }
            }
         }
      }

      final class SubMapEntryIterator extends TreeIntObjectMap.NavigableSubMap<V>.SubMapIterator implements Iterator<IntObjectPair<V>> {
         SubMapEntryIterator(TreeIntObjectMap.Entry<V> first, TreeIntObjectMap.Entry<V> fence) {
            super(first, fence);
         }

         public IntObjectPair<V> next() {
            return this.nextEntry();
         }

         @Override
         public void remove() {
            this.removeAscending();
         }
      }

      abstract class SubMapIterator {
         TreeIntObjectMap.Entry<V> lastReturned;
         TreeIntObjectMap.Entry<V> next;
         final int fenceKey;
         int expectedModCount = NavigableSubMap.this.m.modCount;

         SubMapIterator(TreeIntObjectMap.Entry<V> first, TreeIntObjectMap.Entry<V> fence) {
            this.lastReturned = null;
            this.next = first;
            this.fenceKey = fence == null ? 0 : fence.key;
         }

         public final boolean hasNext() {
            return this.next != null && this.next.key != this.fenceKey;
         }

         final TreeIntObjectMap.Entry<V> nextEntry() {
            TreeIntObjectMap.Entry<V> e = this.next;
            if (e != null && e.key != this.fenceKey) {
               if (NavigableSubMap.this.m.modCount != this.expectedModCount) {
                  throw new ConcurrentModificationException();
               } else {
                  this.next = TreeIntObjectMap.successor(e);
                  this.lastReturned = e;
                  return e;
               }
            } else {
               throw new NoSuchElementException();
            }
         }

         final TreeIntObjectMap.Entry<V> prevEntry() {
            TreeIntObjectMap.Entry<V> e = this.next;
            if (e != null && e.key != this.fenceKey) {
               if (NavigableSubMap.this.m.modCount != this.expectedModCount) {
                  throw new ConcurrentModificationException();
               } else {
                  this.next = TreeIntObjectMap.predecessor(e);
                  this.lastReturned = e;
                  return e;
               }
            } else {
               throw new NoSuchElementException();
            }
         }

         final void removeAscending() {
            if (this.lastReturned == null) {
               throw new IllegalStateException();
            } else if (NavigableSubMap.this.m.modCount != this.expectedModCount) {
               throw new ConcurrentModificationException();
            } else {
               if (this.lastReturned.left != null && this.lastReturned.right != null) {
                  this.next = this.lastReturned;
               }

               NavigableSubMap.this.m.deleteEntry(this.lastReturned);
               this.lastReturned = null;
               this.expectedModCount = NavigableSubMap.this.m.modCount;
            }
         }

         final void removeDescending() {
            if (this.lastReturned == null) {
               throw new IllegalStateException();
            } else if (NavigableSubMap.this.m.modCount != this.expectedModCount) {
               throw new ConcurrentModificationException();
            } else {
               NavigableSubMap.this.m.deleteEntry(this.lastReturned);
               this.lastReturned = null;
               this.expectedModCount = NavigableSubMap.this.m.modCount;
            }
         }
      }

      final class SubMapKeyIterator extends TreeIntObjectMap.NavigableSubMap<V>.SubMapIterator implements IntIterator {
         SubMapKeyIterator(TreeIntObjectMap.Entry<V> first, TreeIntObjectMap.Entry<V> fence) {
            super(first, fence);
         }

         @Override
         public int next() {
            return this.nextEntry().key;
         }

         @Override
         public void remove() {
            this.removeAscending();
         }
      }
   }

   abstract class PrivateEntryIterator {
      TreeIntObjectMap.Entry<V> next;
      TreeIntObjectMap.Entry<V> lastReturned;
      int expectedModCount = TreeIntObjectMap.this.modCount;

      PrivateEntryIterator(TreeIntObjectMap.Entry<V> first) {
         this.lastReturned = null;
         this.next = first;
      }

      public final boolean hasNext() {
         return this.next != null;
      }

      final TreeIntObjectMap.Entry<V> nextEntry() {
         TreeIntObjectMap.Entry<V> e = this.next;
         if (e == null) {
            throw new NoSuchElementException();
         } else if (TreeIntObjectMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            this.next = TreeIntObjectMap.successor(e);
            this.lastReturned = e;
            return e;
         }
      }

      final TreeIntObjectMap.Entry<V> prevEntry() {
         TreeIntObjectMap.Entry<V> e = this.next;
         if (e == null) {
            throw new NoSuchElementException();
         } else if (TreeIntObjectMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            this.next = TreeIntObjectMap.predecessor(e);
            this.lastReturned = e;
            return e;
         }
      }

      public void remove() {
         if (this.lastReturned == null) {
            throw new IllegalStateException();
         } else if (TreeIntObjectMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            if (this.lastReturned.left != null && this.lastReturned.right != null) {
               this.next = this.lastReturned;
            }

            TreeIntObjectMap.this.deleteEntry(this.lastReturned);
            this.expectedModCount = TreeIntObjectMap.this.modCount;
            this.lastReturned = null;
         }
      }
   }

   private class SubMap extends AbstractIntObjectMap<V> implements SortedIntObjectMap<V>, Serializable {
      private static final long serialVersionUID = -6520786458950516097L;
      private boolean fromStart = false;
      private boolean toEnd = false;
      private int fromKey;
      private int toKey;

      private Object readResolve() {
         return new TreeIntObjectMap.AscendingSubMap<>(TreeIntObjectMap.this, this.fromStart, this.fromKey, true, this.toEnd, this.toKey, false);
      }

      @Override
      public Set<IntObjectPair<V>> entrySet() {
         throw new InternalError();
      }

      @Override
      public int lastKey() {
         throw new InternalError();
      }

      @Override
      public int firstKey() {
         throw new InternalError();
      }

      @Override
      public SortedIntObjectMap<V> subMap(int fromKey, int toKey) {
         throw new InternalError();
      }

      @Override
      public SortedIntObjectMap<V> headMap(int toKey) {
         throw new InternalError();
      }

      @Override
      public SortedIntObjectMap<V> tailMap(int fromKey) {
         throw new InternalError();
      }

      @Override
      public IntComparator comparator() {
         throw new InternalError();
      }
   }

   final class ValueIterator extends TreeIntObjectMap<V>.PrivateEntryIterator implements Iterator<V> {
      ValueIterator(TreeIntObjectMap.Entry<V> first) {
         super(first);
      }

      @Override
      public V next() {
         return this.nextEntry().value;
      }
   }

   class Values extends AbstractCollection<V> {
      @Override
      public Iterator<V> iterator() {
         return TreeIntObjectMap.this.new ValueIterator(TreeIntObjectMap.this.getFirstEntry());
      }

      @Override
      public int size() {
         return TreeIntObjectMap.this.size();
      }

      @Override
      public boolean contains(Object o) {
         return TreeIntObjectMap.this.containsValue(o);
      }

      @Override
      public boolean remove(Object o) {
         for(TreeIntObjectMap.Entry<V> e = TreeIntObjectMap.this.getFirstEntry(); e != null; e = TreeIntObjectMap.successor(e)) {
            if (TreeIntObjectMap.valEquals(e.getValue(), o)) {
               TreeIntObjectMap.this.deleteEntry(e);
               return true;
            }
         }

         return false;
      }

      @Override
      public void clear() {
         TreeIntObjectMap.this.clear();
      }
   }
}
