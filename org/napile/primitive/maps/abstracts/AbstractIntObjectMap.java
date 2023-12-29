package org.napile.primitive.maps.abstracts;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.pair.IntObjectPair;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.abstracts.AbstractIntSet;

public abstract class AbstractIntObjectMap<V> implements IntObjectMap<V> {
   protected transient volatile IntSet keySet = null;
   protected transient volatile Collection<V> values = null;

   protected AbstractIntObjectMap() {
   }

   @Override
   public int size() {
      return this.entrySet().size();
   }

   @Override
   public boolean isEmpty() {
      return this.size() == 0;
   }

   @Override
   public boolean containsValue(Object value) {
      Iterator<IntObjectPair<V>> i = this.entrySet().iterator();
      if (value == null) {
         while(i.hasNext()) {
            IntObjectPair<V> e = i.next();
            if (e.getValue() == null) {
               return true;
            }
         }
      } else {
         while(i.hasNext()) {
            IntObjectPair<V> e = i.next();
            if (value.equals(e.getValue())) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public boolean containsKey(int key) {
      for(IntObjectPair<V> e : this.entrySet()) {
         if (key == e.getKey()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public V get(int key) {
      for(IntObjectPair<V> e : this.entrySet()) {
         if (key == e.getKey()) {
            return e.getValue();
         }
      }

      return null;
   }

   @Override
   public V put(int key, V value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public V remove(int key) {
      Iterator<IntObjectPair<V>> i = this.entrySet().iterator();
      IntObjectPair<V> correctEntry = null;

      while(correctEntry == null && i.hasNext()) {
         IntObjectPair<V> e = i.next();
         if (key == e.getKey()) {
            correctEntry = e;
         }
      }

      V oldValue = null;
      if (correctEntry != null) {
         oldValue = correctEntry.getValue();
         i.remove();
      }

      return oldValue;
   }

   @Override
   public void putAll(IntObjectMap<? extends V> m) {
      for(IntObjectPair<? extends V> e : m.entrySet()) {
         this.put(e.getKey(), e.getValue());
      }
   }

   @Override
   public void clear() {
      this.entrySet().clear();
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
      if (this.keySet == null) {
         this.keySet = new AbstractIntSet() {
            @Override
            public IntIterator iterator() {
               return new IntIterator() {
                  private Iterator<IntObjectPair<V>> i = AbstractIntObjectMap.this.entrySet().iterator();

                  @Override
                  public boolean hasNext() {
                     return this.i.hasNext();
                  }

                  @Override
                  public int next() {
                     return this.i.next().getKey();
                  }

                  @Override
                  public void remove() {
                     this.i.remove();
                  }
               };
            }

            @Override
            public int size() {
               return AbstractIntObjectMap.this.size();
            }

            @Override
            public boolean contains(int k) {
               return AbstractIntObjectMap.this.containsKey(k);
            }
         };
      }

      return this.keySet;
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
      if (this.values == null) {
         this.values = new AbstractCollection<V>() {
            @Override
            public Iterator<V> iterator() {
               return new Iterator<V>() {
                  private Iterator<IntObjectPair<V>> i = AbstractIntObjectMap.this.entrySet().iterator();

                  @Override
                  public boolean hasNext() {
                     return this.i.hasNext();
                  }

                  @Override
                  public V next() {
                     return this.i.next().getValue();
                  }

                  @Override
                  public void remove() {
                     this.i.remove();
                  }
               };
            }

            @Override
            public int size() {
               return AbstractIntObjectMap.this.size();
            }

            @Override
            public boolean contains(Object v) {
               return AbstractIntObjectMap.this.containsValue(v);
            }
         };
      }

      return this.values;
   }

   @Override
   public abstract Set<IntObjectPair<V>> entrySet();

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof IntObjectMap)) {
         return false;
      } else {
         IntObjectMap<V> m = (IntObjectMap)o;
         if (m.size() != this.size()) {
            return false;
         } else {
            try {
               for(IntObjectPair<V> e : this.entrySet()) {
                  int key = e.getKey();
                  V value = e.getValue();
                  if (value == null) {
                     if (m.get(key) != null || !m.containsKey(key)) {
                        return false;
                     }
                  } else if (!value.equals(m.get(key))) {
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
   }

   @Override
   public int hashCode() {
      int h = 0;
      Iterator<IntObjectPair<V>> i = this.entrySet().iterator();

      while(i.hasNext()) {
         h += i.next().hashCode();
      }

      return h;
   }

   @Override
   public String toString() {
      Iterator<IntObjectPair<V>> i = this.entrySet().iterator();
      if (!i.hasNext()) {
         return "{}";
      } else {
         StringBuilder sb = new StringBuilder();
         sb.append('{');

         while(true) {
            IntObjectPair<V> e = i.next();
            int key = e.getKey();
            V value = e.getValue();
            sb.append(key);
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value);
            if (!i.hasNext()) {
               return sb.append('}').toString();
            }

            sb.append(", ");
         }
      }
   }

   @Override
   protected Object clone() throws CloneNotSupportedException {
      AbstractIntObjectMap<V> result = (AbstractIntObjectMap)super.clone();
      result.keySet = null;
      result.values = null;
      return result;
   }

   private static boolean eq(int o1, int o2) {
      return o1 == o2;
   }

   private static boolean eq(Object o1, Object o2) {
      return o1 == null ? o2 == null : o1.equals(o2);
   }
}
