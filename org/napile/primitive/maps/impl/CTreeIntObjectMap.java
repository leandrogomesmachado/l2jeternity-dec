package org.napile.primitive.maps.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.napile.primitive.Comparators;
import org.napile.primitive.collections.IntCollection;
import org.napile.primitive.comparators.IntComparator;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.maps.CNavigableIntObjectMap;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.SortedIntObjectMap;
import org.napile.primitive.maps.abstracts.AbstractIntObjectMap;
import org.napile.primitive.pair.IntObjectPair;
import org.napile.primitive.pair.impl.ImmutableIntObjectPairImpl;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.NavigableIntSet;
import org.napile.primitive.sets.abstracts.AbstractIntSet;
import org.napile.primitive.sets.impl.CTreeIntSet;

public class CTreeIntObjectMap<V> extends AbstractIntObjectMap<V> implements CNavigableIntObjectMap<V>, Cloneable, Serializable {
   private static final Random seedGenerator = new Random();
   private static final Object BASE_HEADER = new Object();
   private transient volatile CTreeIntObjectMap.HeadIndex<V> head;
   private final IntComparator comparator;
   private transient int randomSeed;
   private transient CTreeIntObjectMap.KeySet keySet;
   private transient CTreeIntObjectMap.EntrySet entrySet;
   private transient CTreeIntObjectMap.Values values;
   private transient CNavigableIntObjectMap<V> descendingMap;
   private static final AtomicReferenceFieldUpdater<CTreeIntObjectMap, CTreeIntObjectMap.HeadIndex> headUpdater = AtomicReferenceFieldUpdater.newUpdater(
      CTreeIntObjectMap.class, CTreeIntObjectMap.HeadIndex.class, "head"
   );
   private static final int EQ = 1;
   private static final int LT = 2;
   private static final int GT = 0;

   final void initialize() {
      this.keySet = null;
      this.entrySet = null;
      this.values = null;
      this.descendingMap = null;
      this.randomSeed = seedGenerator.nextInt() | 256;
      this.head = new CTreeIntObjectMap.HeadIndex<>(new CTreeIntObjectMap.Node<>(0, BASE_HEADER, null), null, null, 1);
   }

   private boolean casHead(CTreeIntObjectMap.HeadIndex<V> cmp, CTreeIntObjectMap.HeadIndex<V> val) {
      return headUpdater.compareAndSet(this, cmp, val);
   }

   int compare(int k1, int k2) throws ClassCastException {
      IntComparator cmp = this.comparator;
      return cmp != null ? cmp.compare(k1, k2) : Comparators.DEFAULT_INT_COMPARATOR.compare(k1, k2);
   }

   boolean inHalfOpenRange(int key, int least, int fence) {
      return this.compare(key, least) >= 0 && this.compare(key, fence) < 0;
   }

   boolean inOpenRange(int key, int least, int fence) {
      return this.compare(key, least) >= 0 && this.compare(key, fence) <= 0;
   }

   private CTreeIntObjectMap.Node<V> findPredecessor(int key) {
      while(true) {
         CTreeIntObjectMap.Index<V> q = this.head;
         CTreeIntObjectMap.Index<V> r = q.right;

         while(true) {
            if (r != null) {
               CTreeIntObjectMap.Node<V> n = r.node;
               int k = n.key;
               if (n.value == null) {
                  if (!q.unlink(r)) {
                     break;
                  }

                  r = q.right;
                  continue;
               }

               if (this.compare(key, k) > 0) {
                  q = r;
                  r = r.right;
                  continue;
               }
            }

            CTreeIntObjectMap.Index<V> d = q.down;
            if (d == null) {
               return q.node;
            }

            q = d;
            r = d.right;
         }
      }
   }

   private CTreeIntObjectMap.Node<V> findNode(int key) {
      label37:
      while(true) {
         CTreeIntObjectMap.Node<V> b = this.findPredecessor(key);

         CTreeIntObjectMap.Node<V> f;
         for(CTreeIntObjectMap.Node<V> n = b.next; n != null; n = f) {
            f = n.next;
            if (n != b.next) {
               continue label37;
            }

            Object v = n.value;
            if (v == null) {
               n.helpDelete(b, f);
               continue label37;
            }

            if (v == n || b.value == null) {
               continue label37;
            }

            int c = this.compare(key, n.key);
            if (c == 0) {
               return n;
            }

            if (c < 0) {
               return null;
            }

            b = n;
         }

         return null;
      }
   }

   private V doGet(int key) {
      CTreeIntObjectMap.Node<V> bound = null;
      CTreeIntObjectMap.Index<V> q = this.head;
      CTreeIntObjectMap.Index<V> r = q.right;

      while(true) {
         if (r != null) {
            CTreeIntObjectMap.Node<V> n = r.node;
            if (r.node != bound) {
               int k = n.key;
               int c;
               if ((c = this.compare(key, k)) > 0) {
                  q = r;
                  r = r.right;
                  continue;
               }

               if (c == 0) {
                  Object v = n.value;
                  return (V)(v != null ? v : this.getUsingFindNode(key));
               }

               bound = n;
            }
         }

         CTreeIntObjectMap.Index<V> d = q.down;
         if (q.down == null) {
            for(CTreeIntObjectMap.Node<V> n = q.node.next; n != null; n = n.next) {
               int k = n.key;
               int c;
               if ((c = this.compare(key, k)) == 0) {
                  Object v = n.value;
                  return (V)(v != null ? v : this.getUsingFindNode(key));
               }

               if (c < 0) {
                  break;
               }
            }

            return null;
         }

         q = d;
         r = d.right;
      }
   }

   private V getUsingFindNode(int key) {
      Object v;
      do {
         CTreeIntObjectMap.Node<V> n = this.findNode(key);
         if (n == null) {
            return null;
         }

         v = n.value;
      } while(v == null);

      return (V)v;
   }

   private V doPut(int kkey, V value, boolean onlyIfAbsent) {
      label47:
      while(true) {
         CTreeIntObjectMap.Node<V> b = this.findPredecessor(kkey);

         CTreeIntObjectMap.Node<V> n;
         CTreeIntObjectMap.Node<V> f;
         for(n = b.next; n != null; n = f) {
            f = n.next;
            if (n != b.next) {
               continue label47;
            }

            Object v = n.value;
            if (v == null) {
               n.helpDelete(b, f);
               continue label47;
            }

            if (v == n || b.value == null) {
               continue label47;
            }

            int c = this.compare(kkey, n.key);
            if (c <= 0) {
               if (c == 0) {
                  if (!onlyIfAbsent && !n.casValue(v, value)) {
                     continue label47;
                  }

                  return (V)v;
               }
               break;
            }

            b = n;
         }

         f = new CTreeIntObjectMap.Node<>(kkey, value, n);
         if (b.casNext(n, f)) {
            int level = this.randomLevel();
            if (level > 0) {
               this.insertIndex(f, level);
            }

            return null;
         }
      }
   }

   private int randomLevel() {
      int x = this.randomSeed;
      x ^= x << 13;
      x ^= x >>> 17;
      int var5;
      this.randomSeed = var5 = x ^ x << 5;
      if ((var5 & 32769) != 0) {
         return 0;
      } else {
         int level = 1;

         while(((var5 >>>= 1) & 1) != 0) {
            ++level;
         }

         return level;
      }
   }

   private void insertIndex(CTreeIntObjectMap.Node<V> z, int level) {
      CTreeIntObjectMap.HeadIndex<V> h = this.head;
      int max = h.level;
      if (level <= max) {
         CTreeIntObjectMap.Index<V> idx = null;

         for(int i = 1; i <= level; ++i) {
            idx = new CTreeIntObjectMap.Index<>(z, idx, null);
         }

         this.addIndex(idx, h, level);
      } else {
         level = max + 1;
         CTreeIntObjectMap.Index<V>[] idxs = new CTreeIntObjectMap.Index[level + 1];
         CTreeIntObjectMap.Index<V> idx = null;

         for(int i = 1; i <= level; ++i) {
            idxs[i] = idx = new CTreeIntObjectMap.Index<>(z, idx, null);
         }

         int k;
         CTreeIntObjectMap.HeadIndex<V> oldh;
         while(true) {
            oldh = this.head;
            int oldLevel = oldh.level;
            if (level <= oldLevel) {
               k = level;
               break;
            }

            CTreeIntObjectMap.HeadIndex<V> newh = oldh;
            CTreeIntObjectMap.Node<V> oldbase = oldh.node;

            for(int j = oldLevel + 1; j <= level; ++j) {
               newh = new CTreeIntObjectMap.HeadIndex<>(oldbase, newh, idxs[j], j);
            }

            if (this.casHead(oldh, newh)) {
               k = oldLevel;
               break;
            }
         }

         this.addIndex(idxs[k], oldh, k);
      }
   }

   private void addIndex(CTreeIntObjectMap.Index<V> idx, CTreeIntObjectMap.HeadIndex<V> h, int indexLevel) {
      int insertionLevel = indexLevel;
      int key = idx.node.key;

      while(true) {
         int j = h.level;
         CTreeIntObjectMap.Index<V> q = h;
         CTreeIntObjectMap.Index<V> r = h.right;
         CTreeIntObjectMap.Index<V> t = idx;

         while(true) {
            if (r != null) {
               CTreeIntObjectMap.Node<V> n = r.node;
               int c = this.compare(key, n.key);
               if (n.value == null) {
                  if (!q.unlink(r)) {
                     break;
                  }

                  r = q.right;
                  continue;
               }

               if (c > 0) {
                  q = r;
                  r = r.right;
                  continue;
               }
            }

            if (j == insertionLevel) {
               if (t.indexesDeletedNode()) {
                  this.findNode(key);
                  return;
               }

               if (!q.link(r, t)) {
                  break;
               }

               if (--insertionLevel == 0) {
                  if (t.indexesDeletedNode()) {
                     this.findNode(key);
                  }

                  return;
               }
            }

            --j;
            if (j >= insertionLevel && j < indexLevel) {
               t = t.down;
            }

            q = q.down;
            r = q.right;
         }
      }
   }

   final V doRemove(int okey, Object value) {
      label58:
      while(true) {
         CTreeIntObjectMap.Node<V> b = this.findPredecessor(okey);

         CTreeIntObjectMap.Node<V> f;
         for(CTreeIntObjectMap.Node<V> n = b.next; n != null; n = f) {
            f = n.next;
            if (n != b.next) {
               continue label58;
            }

            Object v = n.value;
            if (v == null) {
               n.helpDelete(b, f);
               continue label58;
            }

            if (v == n || b.value == null) {
               continue label58;
            }

            int c = this.compare(okey, n.key);
            if (c < 0) {
               return null;
            }

            if (c <= 0) {
               if (value != null && !value.equals(v)) {
                  return null;
               }

               if (!n.casValue(v, null)) {
                  continue label58;
               }

               if (n.appendMarker(f) && b.casNext(n, f)) {
                  this.findPredecessor(okey);
                  if (this.head.right == null) {
                     this.tryReduceLevel();
                  }
               } else {
                  this.findNode(okey);
               }

               return (V)v;
            }

            b = n;
         }

         return null;
      }
   }

   private void tryReduceLevel() {
      CTreeIntObjectMap.HeadIndex<V> h = this.head;
      CTreeIntObjectMap.HeadIndex<V> d;
      CTreeIntObjectMap.HeadIndex<V> e;
      if (h.level > 3
         && (d = (CTreeIntObjectMap.HeadIndex)h.down) != null
         && (e = (CTreeIntObjectMap.HeadIndex)d.down) != null
         && e.right == null
         && d.right == null
         && h.right == null
         && this.casHead(h, d)
         && h.right != null) {
         this.casHead(d, h);
      }
   }

   CTreeIntObjectMap.Node<V> findFirst() {
      while(true) {
         CTreeIntObjectMap.Node<V> b = this.head.node;
         CTreeIntObjectMap.Node<V> n = b.next;
         if (n == null) {
            return null;
         }

         if (n.value != null) {
            return n;
         }

         n.helpDelete(b, n.next);
      }
   }

   IntObjectPair<V> doRemoveFirstEntry() {
      while(true) {
         CTreeIntObjectMap.Node<V> b = this.head.node;
         CTreeIntObjectMap.Node<V> n = b.next;
         if (n == null) {
            return null;
         }

         CTreeIntObjectMap.Node<V> f = n.next;
         if (n == b.next) {
            Object v = n.value;
            if (v == null) {
               n.helpDelete(b, f);
            } else if (n.casValue(v, null)) {
               if (!n.appendMarker(f) || !b.casNext(n, f)) {
                  this.findFirst();
               }

               this.clearIndexToFirst();
               return new ImmutableIntObjectPairImpl<>(n.key, (V)v);
            }
         }
      }
   }

   private void clearIndexToFirst() {
      label24:
      while(true) {
         CTreeIntObjectMap.Index<V> q = this.head;

         do {
            CTreeIntObjectMap.Index<V> r = q.right;
            if (r != null && r.indexesDeletedNode() && !q.unlink(r)) {
               continue label24;
            }
         } while((q = q.down) != null);

         if (this.head.right == null) {
            this.tryReduceLevel();
         }

         return;
      }
   }

   CTreeIntObjectMap.Node<V> findLast() {
      CTreeIntObjectMap.Index<V> q = this.head;

      label46:
      while(true) {
         CTreeIntObjectMap.Index<V> r = q.right;
         if (q.right == null) {
            CTreeIntObjectMap.Index<V> d = q.down;
            if (q.down != null) {
               q = d;
            } else {
               CTreeIntObjectMap.Node<V> b = q.node;
               CTreeIntObjectMap.Node<V> n = b.next;

               while(n != null) {
                  CTreeIntObjectMap.Node<V> f = n.next;
                  if (n == b.next) {
                     Object v = n.value;
                     if (v == null) {
                        n.helpDelete(b, f);
                     } else if (v != n && b.value != null) {
                        b = n;
                        n = f;
                        continue;
                     }
                  }

                  q = this.head;
                  continue label46;
               }

               return b.isBaseHeader() ? null : b;
            }
         } else if (r.indexesDeletedNode()) {
            q.unlink(r);
            q = this.head;
         } else {
            q = r;
         }
      }
   }

   private CTreeIntObjectMap.Node<V> findPredecessorOfLast() {
      while(true) {
         CTreeIntObjectMap.Index<V> q = this.head;

         while(true) {
            CTreeIntObjectMap.Index<V> r = q.right;
            if (q.right != null) {
               if (r.indexesDeletedNode()) {
                  q.unlink(r);
                  break;
               }

               if (r.node.next != null) {
                  q = r;
                  continue;
               }
            }

            CTreeIntObjectMap.Index<V> d = q.down;
            if (q.down == null) {
               return q.node;
            }

            q = d;
         }
      }
   }

   IntObjectPair<V> doRemoveLastEntry() {
      while(true) {
         CTreeIntObjectMap.Node<V> b = this.findPredecessorOfLast();
         CTreeIntObjectMap.Node<V> n = b.next;
         if (n == null) {
            if (b.isBaseHeader()) {
               return null;
            }
         } else {
            while(true) {
               CTreeIntObjectMap.Node<V> f = n.next;
               if (n != b.next) {
                  break;
               }

               Object v = n.value;
               if (v == null) {
                  n.helpDelete(b, f);
                  break;
               }

               if (v == n || b.value == null) {
                  break;
               }

               if (f == null) {
                  if (!n.casValue(v, null)) {
                     break;
                  }

                  int key = n.key;
                  if (n.appendMarker(f) && b.casNext(n, f)) {
                     this.findPredecessor(key);
                     if (this.head.right == null) {
                        this.tryReduceLevel();
                     }
                  } else {
                     this.findNode(key);
                  }

                  return new ImmutableIntObjectPairImpl<>(key, (V)v);
               }

               b = n;
               n = f;
            }
         }
      }
   }

   CTreeIntObjectMap.Node<V> findNear(int kkey, int rel) {
      label62:
      while(true) {
         CTreeIntObjectMap.Node<V> b = this.findPredecessor(kkey);

         CTreeIntObjectMap.Node<V> f;
         for(CTreeIntObjectMap.Node<V> n = b.next; n != null; n = f) {
            f = n.next;
            if (n != b.next) {
               continue label62;
            }

            Object v = n.value;
            if (v == null) {
               n.helpDelete(b, f);
               continue label62;
            }

            if (v == n || b.value == null) {
               continue label62;
            }

            int c = this.compare(kkey, n.key);
            if (c == 0 && (rel & 1) != 0 || c < 0 && (rel & 2) == 0) {
               return n;
            }

            if (c <= 0 && (rel & 2) != 0) {
               return b.isBaseHeader() ? null : b;
            }

            b = n;
         }

         return (rel & 2) != 0 && !b.isBaseHeader() ? b : null;
      }
   }

   IntObjectPair<V> getNear(int key, int rel) {
      IntObjectPair<V> e;
      do {
         CTreeIntObjectMap.Node<V> n = this.findNear(key, rel);
         if (n == null) {
            return null;
         }

         e = n.createSnapshot();
      } while(e == null);

      return e;
   }

   public CTreeIntObjectMap() {
      this.comparator = null;
      this.initialize();
   }

   public CTreeIntObjectMap(IntComparator comparator) {
      this.comparator = comparator;
      this.initialize();
   }

   public CTreeIntObjectMap(IntObjectMap<? extends V> m) {
      this.comparator = null;
      this.initialize();
      this.putAll(m);
   }

   public CTreeIntObjectMap(SortedIntObjectMap<? extends V> m) {
      this.comparator = m.comparator();
      this.initialize();
      this.buildFromSorted(m);
   }

   public CTreeIntObjectMap<V> clone() {
      CTreeIntObjectMap<V> clone = null;

      try {
         clone = (CTreeIntObjectMap)super.clone();
      } catch (CloneNotSupportedException var3) {
         throw new InternalError();
      }

      clone.initialize();
      clone.buildFromSorted(this);
      return clone;
   }

   private void buildFromSorted(SortedIntObjectMap<? extends V> map) {
      if (map == null) {
         throw new NullPointerException();
      } else {
         CTreeIntObjectMap.HeadIndex<V> h = this.head;
         CTreeIntObjectMap.Node<V> basepred = h.node;
         ArrayList<CTreeIntObjectMap.Index<V>> preds = new ArrayList<>();

         for(int i = 0; i <= h.level; ++i) {
            preds.add(null);
         }

         CTreeIntObjectMap.Index<V> q = h;

         for(int i = h.level; i > 0; --i) {
            preds.set(i, q);
            q = q.down;
         }

         for(IntObjectPair<? extends V> e : map.entrySet()) {
            int j = this.randomLevel();
            if (j > h.level) {
               j = h.level + 1;
            }

            int k = e.getKey();
            V v = e.getValue();
            if (v == null) {
               throw new NullPointerException();
            }

            CTreeIntObjectMap.Node<V> z = new CTreeIntObjectMap.Node<>(k, v, null);
            basepred.next = z;
            basepred = z;
            if (j > 0) {
               CTreeIntObjectMap.Index<V> idx = null;

               for(int i = 1; i <= j; ++i) {
                  idx = new CTreeIntObjectMap.Index<>(z, idx, null);
                  if (i > h.level) {
                     h = new CTreeIntObjectMap.HeadIndex<>(h.node, h, idx, i);
                  }

                  if (i < preds.size()) {
                     preds.get(i).right = idx;
                     preds.set(i, idx);
                  } else {
                     preds.add(idx);
                  }
               }
            }
         }

         this.head = h;
      }
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      s.defaultWriteObject();

      for(CTreeIntObjectMap.Node<V> n = this.findFirst(); n != null; n = n.next) {
         V v = n.getValidValue();
         if (v != null) {
            s.writeObject(n.key);
            s.writeObject(v);
         }
      }

      s.writeObject(null);
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      this.initialize();
      CTreeIntObjectMap.HeadIndex<V> h = this.head;
      CTreeIntObjectMap.Node<V> basepred = h.node;
      ArrayList<CTreeIntObjectMap.Index<V>> preds = new ArrayList<>();

      for(int i = 0; i <= h.level; ++i) {
         preds.add(null);
      }

      CTreeIntObjectMap.Index<V> q = h;

      for(int i = h.level; i > 0; --i) {
         preds.set(i, q);
         q = q.down;
      }

      while(true) {
         int key = 0;

         try {
            key = s.readInt();
         } catch (IOException var13) {
            this.head = h;
            return;
         }

         Object v = s.readObject();
         if (v == null) {
            throw new NullPointerException();
         }

         int j = this.randomLevel();
         if (j > h.level) {
            j = h.level + 1;
         }

         CTreeIntObjectMap.Node<V> z = new CTreeIntObjectMap.Node<>(key, v, null);
         basepred.next = z;
         basepred = z;
         if (j > 0) {
            CTreeIntObjectMap.Index<V> idx = null;

            for(int i = 1; i <= j; ++i) {
               idx = new CTreeIntObjectMap.Index<>(z, idx, null);
               if (i > h.level) {
                  h = new CTreeIntObjectMap.HeadIndex<>(h.node, h, idx, i);
               }

               if (i < preds.size()) {
                  preds.get(i).right = idx;
                  preds.set(i, idx);
               } else {
                  preds.add(idx);
               }
            }
         }
      }
   }

   @Override
   public boolean containsKey(int key) {
      return this.doGet(key) != null;
   }

   @Override
   public V get(int key) {
      return this.doGet(key);
   }

   @Override
   public V put(int key, V value) {
      if (value == null) {
         throw new NullPointerException();
      } else {
         return this.doPut(key, value, false);
      }
   }

   @Override
   public V remove(int key) {
      return this.doRemove(key, null);
   }

   @Override
   public boolean containsValue(Object value) {
      if (value == null) {
         throw new NullPointerException();
      } else {
         for(CTreeIntObjectMap.Node<V> n = this.findFirst(); n != null; n = n.next) {
            V v = n.getValidValue();
            if (v != null && value.equals(v)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public int size() {
      long count = 0L;

      for(CTreeIntObjectMap.Node<V> n = this.findFirst(); n != null; n = n.next) {
         if (n.getValidValue() != null) {
            ++count;
         }
      }

      return count >= 2147483647L ? Integer.MAX_VALUE : (int)count;
   }

   @Override
   public boolean isEmpty() {
      return this.findFirst() == null;
   }

   @Override
   public void clear() {
      this.initialize();
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
   public NavigableIntSet keySet() {
      CTreeIntObjectMap.KeySet ks = this.keySet;
      return ks != null ? ks : (this.keySet = new CTreeIntObjectMap.KeySet(this));
   }

   @Override
   public NavigableIntSet navigableKeySet() {
      CTreeIntObjectMap.KeySet ks = this.keySet;
      return ks != null ? ks : (this.keySet = new CTreeIntObjectMap.KeySet(this));
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
      CTreeIntObjectMap.Values vs = this.values;
      return vs != null ? vs : (this.values = new CTreeIntObjectMap.Values<>(this));
   }

   @Override
   public Set<IntObjectPair<V>> entrySet() {
      CTreeIntObjectMap.EntrySet es = this.entrySet;
      return es != null ? es : (this.entrySet = new CTreeIntObjectMap.EntrySet<>(this));
   }

   @Override
   public CNavigableIntObjectMap<V> descendingMap() {
      CNavigableIntObjectMap<V> dm = this.descendingMap;
      return dm != null ? dm : (this.descendingMap = new CTreeIntObjectMap.SubMap<>(this, 0, false, 0, false, true));
   }

   @Override
   public NavigableIntSet descendingKeySet() {
      return this.descendingMap().navigableKeySet();
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof IntObjectMap)) {
         return false;
      } else {
         IntObjectMap<?> m = (IntObjectMap)o;

         try {
            for(IntObjectPair<V> e : this.entrySet()) {
               if (!e.getValue().equals(m.get(e.getKey()))) {
                  return false;
               }
            }

            for(IntObjectPair<?> e : m.entrySet()) {
               int k = e.getKey();
               Object v = e.getValue();
               if (v == null || !v.equals(this.get(k))) {
                  return false;
               }
            }

            return true;
         } catch (ClassCastException var7) {
            return false;
         } catch (NullPointerException var8) {
            return false;
         }
      }
   }

   @Override
   public V putIfAbsent(int key, V value) {
      if (value == null) {
         throw new NullPointerException();
      } else {
         return this.doPut(key, value, true);
      }
   }

   @Override
   public boolean remove(int key, Object value) {
      if (value == null) {
         return false;
      } else {
         return this.doRemove(key, value) != null;
      }
   }

   @Override
   public boolean replace(int key, V oldValue, V newValue) {
      if (oldValue != null && newValue != null) {
         while(true) {
            CTreeIntObjectMap.Node<V> n = this.findNode(key);
            if (n == null) {
               return false;
            }

            Object v = n.value;
            if (v != null) {
               if (!oldValue.equals(v)) {
                  return false;
               }

               if (n.casValue(v, newValue)) {
                  return true;
               }
            }
         }
      } else {
         throw new NullPointerException();
      }
   }

   @Override
   public V replace(int key, V value) {
      if (value == null) {
         throw new NullPointerException();
      } else {
         CTreeIntObjectMap.Node<V> n;
         Object v;
         do {
            n = this.findNode(key);
            if (n == null) {
               return null;
            }

            v = n.value;
         } while(v == null || !n.casValue(v, value));

         return (V)v;
      }
   }

   @Override
   public IntComparator comparator() {
      return this.comparator;
   }

   @Override
   public int firstKey() {
      CTreeIntObjectMap.Node<V> n = this.findFirst();
      if (n == null) {
         throw new NoSuchElementException();
      } else {
         return n.key;
      }
   }

   @Override
   public int lastKey() {
      CTreeIntObjectMap.Node<V> n = this.findLast();
      if (n == null) {
         throw new NoSuchElementException();
      } else {
         return n.key;
      }
   }

   @Override
   public CNavigableIntObjectMap<V> subMap(int fromKey, boolean fromInclusive, int toKey, boolean toInclusive) {
      return new CTreeIntObjectMap.SubMap<>(this, fromKey, fromInclusive, toKey, toInclusive, false);
   }

   @Override
   public CNavigableIntObjectMap<V> headMap(int toKey, boolean inclusive) {
      return new CTreeIntObjectMap.SubMap<>(this, 0, false, toKey, inclusive, false);
   }

   @Override
   public CNavigableIntObjectMap<V> tailMap(int fromKey, boolean inclusive) {
      return new CTreeIntObjectMap.SubMap<>(this, fromKey, inclusive, 0, false, false);
   }

   @Override
   public CNavigableIntObjectMap<V> subMap(int fromKey, int toKey) {
      return this.subMap(fromKey, true, toKey, false);
   }

   @Override
   public CNavigableIntObjectMap<V> headMap(int toKey) {
      return this.headMap(toKey, false);
   }

   @Override
   public CNavigableIntObjectMap<V> tailMap(int fromKey) {
      return this.tailMap(fromKey, true);
   }

   @Override
   public IntObjectPair<V> lowerEntry(int key) {
      return this.getNear(key, 2);
   }

   @Override
   public int lowerKey(int key) {
      CTreeIntObjectMap.Node<V> n = this.findNear(key, 2);
      return n == null ? null : n.key;
   }

   @Override
   public IntObjectPair<V> floorEntry(int key) {
      return this.getNear(key, 3);
   }

   @Override
   public int floorKey(int key) {
      CTreeIntObjectMap.Node<V> n = this.findNear(key, 3);
      return n == null ? null : n.key;
   }

   @Override
   public IntObjectPair<V> ceilingEntry(int key) {
      return this.getNear(key, 1);
   }

   @Override
   public int ceilingKey(int key) {
      CTreeIntObjectMap.Node<V> n = this.findNear(key, 1);
      return n == null ? null : n.key;
   }

   @Override
   public IntObjectPair<V> higherEntry(int key) {
      return this.getNear(key, 0);
   }

   @Override
   public int higherKey(int key) {
      CTreeIntObjectMap.Node<V> n = this.findNear(key, 0);
      return n == null ? null : n.key;
   }

   @Override
   public IntObjectPair<V> firstEntry() {
      IntObjectPair<V> e;
      do {
         CTreeIntObjectMap.Node<V> n = this.findFirst();
         if (n == null) {
            return null;
         }

         e = n.createSnapshot();
      } while(e == null);

      return e;
   }

   @Override
   public IntObjectPair<V> lastEntry() {
      IntObjectPair<V> e;
      do {
         CTreeIntObjectMap.Node<V> n = this.findLast();
         if (n == null) {
            return null;
         }

         e = n.createSnapshot();
      } while(e == null);

      return e;
   }

   @Override
   public IntObjectPair<V> pollFirstEntry() {
      return this.doRemoveFirstEntry();
   }

   @Override
   public IntObjectPair<V> pollLastEntry() {
      return this.doRemoveLastEntry();
   }

   IntIterator keyIterator() {
      return new CTreeIntObjectMap.KeyIterator();
   }

   Iterator<V> valueIterator() {
      return new CTreeIntObjectMap.ValueIterator();
   }

   Iterator<IntObjectPair<V>> entryIterator() {
      return new CTreeIntObjectMap.EntryIterator();
   }

   static final <E> List<E> toList(Collection<E> c) {
      List<E> list = new ArrayList<>(c.size());

      for(E e : c) {
         list.add(e);
      }

      return list;
   }

   final class EntryIterator extends CTreeIntObjectMap<V>.Iter<IntObjectPair<V>> implements Iterator<IntObjectPair<V>> {
      public IntObjectPair<V> next() {
         CTreeIntObjectMap.Node<V> n = this.next;
         V v = this.nextValue;
         this.advance();
         return new ImmutableIntObjectPairImpl<>(n.key, v);
      }
   }

   static final class EntrySet<V1> extends AbstractSet<IntObjectPair<V1>> {
      private final CNavigableIntObjectMap<V1> m;

      EntrySet(CNavigableIntObjectMap<V1> map) {
         this.m = map;
      }

      @Override
      public Iterator<IntObjectPair<V1>> iterator() {
         return this.m instanceof CTreeIntObjectMap ? ((CTreeIntObjectMap)this.m).entryIterator() : ((CTreeIntObjectMap.SubMap)this.m).entryIterator();
      }

      @Override
      public boolean contains(Object o) {
         if (!(o instanceof IntObjectPair)) {
            return false;
         } else {
            IntObjectPair<V1> e = (IntObjectPair)o;
            V1 v = this.m.get(e.getKey());
            return v != null && v.equals(e.getValue());
         }
      }

      @Override
      public boolean remove(Object o) {
         if (!(o instanceof IntObjectPair)) {
            return false;
         } else {
            IntObjectPair<V1> e = (IntObjectPair)o;
            return this.m.remove(e.getKey(), e.getValue());
         }
      }

      @Override
      public boolean isEmpty() {
         return this.m.isEmpty();
      }

      @Override
      public int size() {
         return this.m.size();
      }

      @Override
      public void clear() {
         this.m.clear();
      }

      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof Set)) {
            return false;
         } else {
            Collection<?> c = (Collection)o;

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
      public Object[] toArray() {
         return CTreeIntObjectMap.toList(this).toArray();
      }

      @Override
      public <T> T[] toArray(T[] a) {
         return (T[])CTreeIntObjectMap.toList(this).toArray(a);
      }
   }

   static final class HeadIndex<V> extends CTreeIntObjectMap.Index<V> {
      final int level;

      HeadIndex(CTreeIntObjectMap.Node<V> node, CTreeIntObjectMap.Index<V> down, CTreeIntObjectMap.Index<V> right, int level) {
         super(node, down, right);
         this.level = level;
      }
   }

   static class Index<V> {
      final CTreeIntObjectMap.Node<V> node;
      final CTreeIntObjectMap.Index<V> down;
      volatile CTreeIntObjectMap.Index<V> right;
      static final AtomicReferenceFieldUpdater<CTreeIntObjectMap.Index, CTreeIntObjectMap.Index> rightUpdater = AtomicReferenceFieldUpdater.newUpdater(
         CTreeIntObjectMap.Index.class, CTreeIntObjectMap.Index.class, "right"
      );

      Index(CTreeIntObjectMap.Node<V> node, CTreeIntObjectMap.Index<V> down, CTreeIntObjectMap.Index<V> right) {
         this.node = node;
         this.down = down;
         this.right = right;
      }

      final boolean casRight(CTreeIntObjectMap.Index<V> cmp, CTreeIntObjectMap.Index<V> val) {
         return rightUpdater.compareAndSet(this, cmp, val);
      }

      final boolean indexesDeletedNode() {
         return this.node.value == null;
      }

      final boolean link(CTreeIntObjectMap.Index<V> succ, CTreeIntObjectMap.Index<V> newSucc) {
         CTreeIntObjectMap.Node<V> n = this.node;
         newSucc.right = succ;
         return n.value != null && this.casRight(succ, newSucc);
      }

      final boolean unlink(CTreeIntObjectMap.Index<V> succ) {
         return !this.indexesDeletedNode() && this.casRight(succ, succ.right);
      }
   }

   abstract class Iter<T> {
      CTreeIntObjectMap.Node<V> lastReturned;
      CTreeIntObjectMap.Node<V> next;
      V nextValue;

      Iter() {
         while(true) {
            this.next = CTreeIntObjectMap.this.findFirst();
            if (this.next == null) {
               break;
            }

            Object x = this.next.value;
            if (x != null && x != this.next) {
               this.nextValue = (V)x;
               break;
            }
         }
      }

      public final boolean hasNext() {
         return this.next != null;
      }

      final void advance() {
         if (this.next == null) {
            throw new NoSuchElementException();
         } else {
            this.lastReturned = this.next;

            while(true) {
               this.next = this.next.next;
               if (this.next == null) {
                  break;
               }

               Object x = this.next.value;
               if (x != null && x != this.next) {
                  this.nextValue = (V)x;
                  break;
               }
            }
         }
      }

      public void remove() {
         CTreeIntObjectMap.Node<V> l = this.lastReturned;
         if (l == null) {
            throw new IllegalStateException();
         } else {
            CTreeIntObjectMap.this.remove(l.key);
            this.lastReturned = null;
         }
      }
   }

   final class KeyIterator extends CTreeIntObjectMap<V>.Iter<V> implements IntIterator {
      @Override
      public int next() {
         CTreeIntObjectMap.Node<V> n = this.next;
         this.advance();
         return n.key;
      }
   }

   static final class KeySet extends AbstractIntSet implements NavigableIntSet {
      private final CNavigableIntObjectMap<Object> m;

      KeySet(CNavigableIntObjectMap<?> map) {
         this.m = map;
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
      public boolean remove(int o) {
         return this.m.remove(o) != null;
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
      public IntIterator iterator() {
         return this.m instanceof CTreeIntObjectMap ? ((CTreeIntObjectMap)this.m).keyIterator() : ((CTreeIntObjectMap.SubMap)this.m).keyIterator();
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
      public IntIterator descendingIterator() {
         return this.descendingSet().iterator();
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
   }

   static final class Node<V> {
      final int key;
      volatile Object value;
      volatile CTreeIntObjectMap.Node<V> next;
      static final AtomicReferenceFieldUpdater<CTreeIntObjectMap.Node, CTreeIntObjectMap.Node> nextUpdater = AtomicReferenceFieldUpdater.newUpdater(
         CTreeIntObjectMap.Node.class, CTreeIntObjectMap.Node.class, "next"
      );
      static final AtomicReferenceFieldUpdater<CTreeIntObjectMap.Node, Object> valueUpdater = AtomicReferenceFieldUpdater.newUpdater(
         CTreeIntObjectMap.Node.class, Object.class, "value"
      );

      Node(int key, Object value, CTreeIntObjectMap.Node<V> next) {
         this.key = key;
         this.value = value;
         this.next = next;
      }

      Node(CTreeIntObjectMap.Node<V> next) {
         this.key = 0;
         this.value = this;
         this.next = next;
      }

      boolean casValue(Object cmp, Object val) {
         return valueUpdater.compareAndSet(this, cmp, val);
      }

      boolean casNext(CTreeIntObjectMap.Node<V> cmp, CTreeIntObjectMap.Node<V> val) {
         return nextUpdater.compareAndSet(this, cmp, val);
      }

      boolean isMarker() {
         return this.value == this;
      }

      boolean isBaseHeader() {
         return this.value == CTreeIntObjectMap.BASE_HEADER;
      }

      boolean appendMarker(CTreeIntObjectMap.Node<V> f) {
         return this.casNext(f, new CTreeIntObjectMap.Node<>(f));
      }

      void helpDelete(CTreeIntObjectMap.Node<V> b, CTreeIntObjectMap.Node<V> f) {
         if (f == this.next && this == b.next) {
            if (f != null && f.value == f) {
               b.casNext(this, f.next);
            } else {
               this.appendMarker(f);
            }
         }
      }

      V getValidValue() {
         Object v = this.value;
         return (V)(v != this && v != CTreeIntObjectMap.BASE_HEADER ? v : null);
      }

      IntObjectPair<V> createSnapshot() {
         V v = this.getValidValue();
         return v == null ? null : new ImmutableIntObjectPairImpl<>(this.key, v);
      }
   }

   static final class SubMap<V> extends AbstractIntObjectMap<V> implements CNavigableIntObjectMap<V>, Cloneable, Serializable {
      private static final long serialVersionUID = -7647078645895051609L;
      private final CTreeIntObjectMap<V> m;
      private final int lo;
      private final int hi;
      private final boolean loInclusive;
      private final boolean hiInclusive;
      private final boolean isDescending;
      private transient CTreeIntObjectMap.KeySet keySetView;
      private transient Set<IntObjectPair<V>> entrySetView;
      private transient Collection<V> valuesView;

      SubMap(CTreeIntObjectMap<V> map, int fromKey, boolean fromInclusive, int toKey, boolean toInclusive, boolean isDescending) {
         if (map.compare(fromKey, toKey) > 0) {
            throw new IllegalArgumentException("inconsistent range");
         } else {
            this.m = map;
            this.lo = fromKey;
            this.hi = toKey;
            this.loInclusive = fromInclusive;
            this.hiInclusive = toInclusive;
            this.isDescending = isDescending;
         }
      }

      private boolean tooLow(int key) {
         int c = this.m.compare(key, this.lo);
         return c < 0 || c == 0 && !this.loInclusive;
      }

      private boolean tooHigh(int key) {
         int c = this.m.compare(key, this.hi);
         return c > 0 || c == 0 && !this.hiInclusive;
      }

      private boolean inBounds(int key) {
         return !this.tooLow(key) && !this.tooHigh(key);
      }

      private void checkKeyBounds(int key) throws IllegalArgumentException {
         if (!this.inBounds(key)) {
            throw new IllegalArgumentException("key out of range");
         }
      }

      private boolean isBeforeEnd(CTreeIntObjectMap.Node<V> n) {
         if (n == null) {
            return false;
         } else {
            int k = n.key;
            int c = this.m.compare(k, this.hi);
            return c <= 0 && (c != 0 || this.hiInclusive);
         }
      }

      private CTreeIntObjectMap.Node<V> loNode() {
         return this.loInclusive ? this.m.findNear(this.lo, 0 | 1) : this.m.findNear(this.lo, 0);
      }

      private CTreeIntObjectMap.Node<V> hiNode() {
         return this.hiInclusive ? this.m.findNear(this.hi, 2 | 1) : this.m.findNear(this.hi, 2);
      }

      private int lowestKey() {
         CTreeIntObjectMap.Node<V> n = this.loNode();
         if (this.isBeforeEnd(n)) {
            return n.key;
         } else {
            throw new NoSuchElementException();
         }
      }

      private int highestKey() {
         CTreeIntObjectMap.Node<V> n = this.hiNode();
         if (n != null) {
            int last = n.key;
            if (this.inBounds(last)) {
               return last;
            }
         }

         throw new NoSuchElementException();
      }

      private IntObjectPair<V> lowestEntry() {
         IntObjectPair<V> e;
         do {
            CTreeIntObjectMap.Node<V> n = this.loNode();
            if (!this.isBeforeEnd(n)) {
               return null;
            }

            e = n.createSnapshot();
         } while(e == null);

         return e;
      }

      private IntObjectPair<V> highestEntry() {
         while(true) {
            CTreeIntObjectMap.Node<V> n = this.hiNode();
            if (n != null && this.inBounds(n.key)) {
               IntObjectPair<V> e = n.createSnapshot();
               if (e == null) {
                  continue;
               }

               return e;
            }

            return null;
         }
      }

      private IntObjectPair<V> removeLowest() {
         int k;
         V v;
         do {
            CTreeIntObjectMap.Node<V> n = this.loNode();
            if (n == null) {
               return null;
            }

            k = n.key;
            if (!this.inBounds(k)) {
               return null;
            }

            v = this.m.doRemove(k, null);
         } while(v == null);

         return new ImmutableIntObjectPairImpl<>(k, v);
      }

      private IntObjectPair<V> removeHighest() {
         int k;
         V v;
         do {
            CTreeIntObjectMap.Node<V> n = this.hiNode();
            if (n == null) {
               return null;
            }

            k = n.key;
            if (!this.inBounds(k)) {
               return null;
            }

            v = this.m.doRemove(k, null);
         } while(v == null);

         return new ImmutableIntObjectPairImpl<>(k, v);
      }

      private IntObjectPair<V> getNearEntry(int key, int rel) {
         if (this.isDescending) {
            if ((rel & 2) == 0) {
               rel |= 2;
            } else {
               rel &= ~2;
            }
         }

         if (this.tooLow(key)) {
            return (rel & 2) != 0 ? null : this.lowestEntry();
         } else if (this.tooHigh(key)) {
            return (rel & 2) != 0 ? this.highestEntry() : null;
         } else {
            int k;
            V v;
            do {
               CTreeIntObjectMap.Node<V> n = this.m.findNear(key, rel);
               if (n == null || !this.inBounds(n.key)) {
                  return null;
               }

               k = n.key;
               v = n.getValidValue();
            } while(v == null);

            return new ImmutableIntObjectPairImpl<>(k, v);
         }
      }

      private int getNearKey(int key, int rel) {
         if (this.isDescending) {
            if ((rel & 2) == 0) {
               rel |= 2;
            } else {
               rel &= ~2;
            }
         }

         if (this.tooLow(key)) {
            if ((rel & 2) == 0) {
               CTreeIntObjectMap.Node<V> n = this.loNode();
               if (this.isBeforeEnd(n)) {
                  return n.key;
               }
            }

            return 0;
         } else if (this.tooHigh(key)) {
            if ((rel & 2) != 0) {
               CTreeIntObjectMap.Node<V> n = this.hiNode();
               if (n != null) {
                  int last = n.key;
                  if (this.inBounds(last)) {
                     return last;
                  }
               }
            }

            return 0;
         } else {
            int k;
            V v;
            do {
               CTreeIntObjectMap.Node<V> n = this.m.findNear(key, rel);
               if (n == null || !this.inBounds(n.key)) {
                  return 0;
               }

               k = n.key;
               v = n.getValidValue();
            } while(v == null);

            return k;
         }
      }

      @Override
      public boolean containsKey(int key) {
         return this.inBounds(key) && this.m.containsKey(key);
      }

      @Override
      public V get(int key) {
         return !this.inBounds(key) ? null : this.m.get(key);
      }

      @Override
      public V put(int key, V value) {
         this.checkKeyBounds(key);
         return this.m.put(key, value);
      }

      @Override
      public V remove(int key) {
         return !this.inBounds(key) ? null : this.m.remove(key);
      }

      @Override
      public int size() {
         long count = 0L;

         for(CTreeIntObjectMap.Node<V> n = this.loNode(); this.isBeforeEnd(n); n = n.next) {
            if (n.getValidValue() != null) {
               ++count;
            }
         }

         return count >= 2147483647L ? Integer.MAX_VALUE : (int)count;
      }

      @Override
      public boolean isEmpty() {
         return !this.isBeforeEnd(this.loNode());
      }

      @Override
      public boolean containsValue(Object value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            for(CTreeIntObjectMap.Node<V> n = this.loNode(); this.isBeforeEnd(n); n = n.next) {
               V v = n.getValidValue();
               if (v != null && value.equals(v)) {
                  return true;
               }
            }

            return false;
         }
      }

      @Override
      public void clear() {
         for(CTreeIntObjectMap.Node<V> n = this.loNode(); this.isBeforeEnd(n); n = n.next) {
            if (n.getValidValue() != null) {
               this.m.remove(n.key);
            }
         }
      }

      @Override
      public V putIfAbsent(int key, V value) {
         this.checkKeyBounds(key);
         return this.m.putIfAbsent(key, value);
      }

      @Override
      public boolean remove(int key, Object value) {
         return this.inBounds(key) && this.m.remove(key, value);
      }

      @Override
      public boolean replace(int key, V oldValue, V newValue) {
         this.checkKeyBounds(key);
         return this.m.replace(key, oldValue, newValue);
      }

      @Override
      public V replace(int key, V value) {
         this.checkKeyBounds(key);
         return this.m.replace(key, value);
      }

      @Override
      public IntComparator comparator() {
         IntComparator cmp = this.m.comparator();
         return this.isDescending ? Comparators.reverseOrder(cmp) : cmp;
      }

      private CTreeIntObjectMap.SubMap<V> newSubMap(int fromKey, boolean fromInclusive, int toKey, boolean toInclusive) {
         if (this.isDescending) {
            int tk = fromKey;
            fromKey = toKey;
            toKey = tk;
            boolean ti = fromInclusive;
            fromInclusive = toInclusive;
            toInclusive = ti;
         }

         int c = this.m.compare(fromKey, this.lo);
         if (c >= 0 && (c != 0 || this.loInclusive || !fromInclusive)) {
            c = this.m.compare(toKey, this.hi);
            if (c <= 0 && (c != 0 || this.hiInclusive || !toInclusive)) {
               return new CTreeIntObjectMap.SubMap<>(this.m, fromKey, fromInclusive, toKey, toInclusive, this.isDescending);
            } else {
               throw new IllegalArgumentException("key out of range");
            }
         } else {
            throw new IllegalArgumentException("key out of range");
         }
      }

      public CTreeIntObjectMap.SubMap<V> subMap(int fromKey, boolean fromInclusive, int toKey, boolean toInclusive) {
         return this.newSubMap(fromKey, fromInclusive, toKey, toInclusive);
      }

      public CTreeIntObjectMap.SubMap<V> headMap(int toKey, boolean inclusive) {
         return this.newSubMap(0, false, toKey, inclusive);
      }

      public CTreeIntObjectMap.SubMap<V> tailMap(int fromKey, boolean inclusive) {
         return this.newSubMap(fromKey, inclusive, 0, false);
      }

      public CTreeIntObjectMap.SubMap<V> subMap(int fromKey, int toKey) {
         return this.subMap(fromKey, true, toKey, false);
      }

      public CTreeIntObjectMap.SubMap<V> headMap(int toKey) {
         return this.headMap(toKey, false);
      }

      public CTreeIntObjectMap.SubMap<V> tailMap(int fromKey) {
         return this.tailMap(fromKey, true);
      }

      public CTreeIntObjectMap.SubMap<V> descendingMap() {
         return new CTreeIntObjectMap.SubMap<>(this.m, this.lo, this.loInclusive, this.hi, this.hiInclusive, !this.isDescending);
      }

      @Override
      public IntObjectPair<V> ceilingEntry(int key) {
         return this.getNearEntry(key, 0 | 1);
      }

      @Override
      public int ceilingKey(int key) {
         return this.getNearKey(key, 0 | 1);
      }

      @Override
      public IntObjectPair<V> lowerEntry(int key) {
         return this.getNearEntry(key, 2);
      }

      @Override
      public int lowerKey(int key) {
         return this.getNearKey(key, 2);
      }

      @Override
      public IntObjectPair<V> floorEntry(int key) {
         return this.getNearEntry(key, 2 | 1);
      }

      @Override
      public int floorKey(int key) {
         return this.getNearKey(key, 2 | 1);
      }

      @Override
      public IntObjectPair<V> higherEntry(int key) {
         return this.getNearEntry(key, 0);
      }

      @Override
      public int higherKey(int key) {
         return this.getNearKey(key, 0);
      }

      @Override
      public int firstKey() {
         return this.isDescending ? this.highestKey() : this.lowestKey();
      }

      @Override
      public int lastKey() {
         return this.isDescending ? this.lowestKey() : this.highestKey();
      }

      @Override
      public IntObjectPair<V> firstEntry() {
         return this.isDescending ? this.highestEntry() : this.lowestEntry();
      }

      @Override
      public IntObjectPair<V> lastEntry() {
         return this.isDescending ? this.lowestEntry() : this.highestEntry();
      }

      @Override
      public IntObjectPair<V> pollFirstEntry() {
         return this.isDescending ? this.removeHighest() : this.removeLowest();
      }

      @Override
      public IntObjectPair<V> pollLastEntry() {
         return this.isDescending ? this.removeLowest() : this.removeHighest();
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
      public NavigableIntSet keySet() {
         CTreeIntObjectMap.KeySet ks = this.keySetView;
         return ks != null ? ks : (this.keySetView = new CTreeIntObjectMap.KeySet(this));
      }

      @Override
      public NavigableIntSet navigableKeySet() {
         CTreeIntObjectMap.KeySet ks = this.keySetView;
         return ks != null ? ks : (this.keySetView = new CTreeIntObjectMap.KeySet(this));
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
         Collection<V> vs = this.valuesView;
         return vs != null ? vs : (this.valuesView = new CTreeIntObjectMap.Values<>(this));
      }

      @Override
      public Set<IntObjectPair<V>> entrySet() {
         Set<IntObjectPair<V>> es = this.entrySetView;
         return es != null ? es : (this.entrySetView = new CTreeIntObjectMap.EntrySet<>(this));
      }

      @Override
      public NavigableIntSet descendingKeySet() {
         return this.descendingMap().navigableKeySet();
      }

      IntIterator keyIterator() {
         return new CTreeIntObjectMap.SubMap.SubMapKeyIterator();
      }

      Iterator<V> valueIterator() {
         return new CTreeIntObjectMap.SubMap.SubMapValueIterator();
      }

      Iterator<IntObjectPair<V>> entryIterator() {
         return new CTreeIntObjectMap.SubMap.SubMapEntryIterator();
      }

      final class SubMapEntryIterator extends CTreeIntObjectMap.SubMap<V>.SubMapIter<IntObjectPair<V>> implements Iterator<IntObjectPair<V>> {
         public IntObjectPair<V> next() {
            CTreeIntObjectMap.Node<V> n = this.next;
            V v = this.nextValue;
            this.advance();
            return new ImmutableIntObjectPairImpl<>(n.key, v);
         }
      }

      abstract class SubMapIter<T> {
         CTreeIntObjectMap.Node<V> lastReturned;
         CTreeIntObjectMap.Node<V> next;
         V nextValue;

         SubMapIter() {
            while(true) {
               this.next = SubMap.this.isDescending ? SubMap.this.hiNode() : SubMap.this.loNode();
               if (this.next == null) {
                  break;
               }

               Object x = this.next.value;
               if (x != null && x != this.next) {
                  if (!SubMap.this.inBounds(this.next.key)) {
                     this.next = null;
                  } else {
                     this.nextValue = (V)x;
                  }
                  break;
               }
            }
         }

         public final boolean hasNext() {
            return this.next != null;
         }

         final void advance() {
            if (this.next == null) {
               throw new NoSuchElementException();
            } else {
               this.lastReturned = this.next;
               if (SubMap.this.isDescending) {
                  this.descend();
               } else {
                  this.ascend();
               }
            }
         }

         private void ascend() {
            while(true) {
               this.next = this.next.next;
               if (this.next != null) {
                  Object x = this.next.value;
                  if (x == null || x == this.next) {
                     continue;
                  }

                  if (SubMap.this.tooHigh(this.next.key)) {
                     this.next = null;
                  } else {
                     this.nextValue = (V)x;
                  }
               }

               return;
            }
         }

         private void descend() {
            while(true) {
               this.next = SubMap.this.m.findNear(this.lastReturned.key, 2);
               if (this.next != null) {
                  Object x = this.next.value;
                  if (x == null || x == this.next) {
                     continue;
                  }

                  if (SubMap.this.tooLow(this.next.key)) {
                     this.next = null;
                  } else {
                     this.nextValue = (V)x;
                  }
               }

               return;
            }
         }

         public void remove() {
            CTreeIntObjectMap.Node<V> l = this.lastReturned;
            if (l == null) {
               throw new IllegalStateException();
            } else {
               SubMap.this.m.remove(l.key);
               this.lastReturned = null;
            }
         }
      }

      final class SubMapKeyIterator extends CTreeIntObjectMap.SubMap.SubMapIter implements IntIterator {
         @Override
         public int next() {
            CTreeIntObjectMap.Node<V> n = this.next;
            this.advance();
            return n.key;
         }
      }

      final class SubMapValueIterator extends CTreeIntObjectMap.SubMap<V>.SubMapIter<V> implements Iterator<V> {
         @Override
         public V next() {
            V v = this.nextValue;
            this.advance();
            return v;
         }
      }
   }

   final class ValueIterator extends CTreeIntObjectMap<V>.Iter<V> implements Iterator<V> {
      @Override
      public V next() {
         V v = this.nextValue;
         this.advance();
         return v;
      }
   }

   static final class Values<E> extends AbstractCollection<E> {
      private final CNavigableIntObjectMap<E> m;

      Values(CNavigableIntObjectMap<E> map) {
         this.m = map;
      }

      @Override
      public Iterator<E> iterator() {
         return this.m instanceof CTreeIntObjectMap ? ((CTreeIntObjectMap)this.m).valueIterator() : ((CTreeIntObjectMap.SubMap)this.m).valueIterator();
      }

      @Override
      public boolean isEmpty() {
         return this.m.isEmpty();
      }

      @Override
      public int size() {
         return this.m.size();
      }

      @Override
      public boolean contains(Object o) {
         return this.m.containsValue(o);
      }

      @Override
      public void clear() {
         this.m.clear();
      }

      @Override
      public Object[] toArray() {
         return CTreeIntObjectMap.toList(this).toArray();
      }

      @Override
      public <T> T[] toArray(T[] a) {
         return (T[])CTreeIntObjectMap.toList(this).toArray(a);
      }
   }
}
