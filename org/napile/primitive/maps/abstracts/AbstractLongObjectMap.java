package org.napile.primitive.maps.abstracts;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.napile.primitive.iterators.LongIterator;
import org.napile.primitive.maps.LongObjectMap;
import org.napile.primitive.pair.LongObjectPair;
import org.napile.primitive.sets.LongSet;
import org.napile.primitive.sets.abstracts.AbstractLongSet;

public abstract class AbstractLongObjectMap<V> implements LongObjectMap<V> {
   protected transient volatile LongSet keySet = null;
   protected transient volatile Collection<V> values = null;

   protected AbstractLongObjectMap() {
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
      Iterator<LongObjectPair<V>> i = this.entrySet().iterator();
      if (value == null) {
         while(i.hasNext()) {
            LongObjectPair<V> e = i.next();
            if (e.getValue() == null) {
               return true;
            }
         }
      } else {
         while(i.hasNext()) {
            LongObjectPair<V> e = i.next();
            if (value.equals(e.getValue())) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public boolean containsKey(long key) {
      for(LongObjectPair<V> e : this.entrySet()) {
         if (key == e.getKey()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public V get(long key) {
      for(LongObjectPair<V> e : this.entrySet()) {
         if (key == e.getKey()) {
            return e.getValue();
         }
      }

      return null;
   }

   @Override
   public V put(long key, V value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public V remove(long key) {
      Iterator<LongObjectPair<V>> i = this.entrySet().iterator();
      LongObjectPair<V> correctEntry = null;

      while(correctEntry == null && i.hasNext()) {
         LongObjectPair<V> e = i.next();
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
   public void putAll(LongObjectMap<? extends V> m) {
      for(LongObjectPair<? extends V> e : m.entrySet()) {
         this.put(e.getKey(), e.getValue());
      }
   }

   @Override
   public void clear() {
      this.entrySet().clear();
   }

   @Override
   public LongSet keySet() {
      if (this.keySet == null) {
         this.keySet = new AbstractLongSet() {
            @Override
            public LongIterator iterator() {
               return new LongIterator() {
                  private Iterator<LongObjectPair<V>> i = AbstractLongObjectMap.this.entrySet().iterator();

                  @Override
                  public boolean hasNext() {
                     return this.i.hasNext();
                  }

                  @Override
                  public long next() {
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
               return AbstractLongObjectMap.this.size();
            }

            @Override
            public boolean contains(long k) {
               return AbstractLongObjectMap.this.containsKey(k);
            }
         };
      }

      return this.keySet;
   }

   @Override
   public Collection<V> valueCollection() {
      if (this.values == null) {
         this.values = new AbstractCollection<V>() {
            @Override
            public Iterator<V> iterator() {
               return new Iterator<V>() {
                  private Iterator<LongObjectPair<V>> i = AbstractLongObjectMap.this.entrySet().iterator();

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
               return AbstractLongObjectMap.this.size();
            }

            @Override
            public boolean contains(Object v) {
               return AbstractLongObjectMap.this.containsValue(v);
            }
         };
      }

      return this.values;
   }

   @Override
   public abstract Set<LongObjectPair<V>> entrySet();

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof LongObjectMap)) {
         return false;
      } else {
         LongObjectMap<V> m = (LongObjectMap)o;
         if (m.size() != this.size()) {
            return false;
         } else {
            try {
               for(LongObjectPair<V> e : this.entrySet()) {
                  long key = e.getKey();
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
            } catch (ClassCastException var8) {
               return false;
            } catch (NullPointerException var9) {
               return false;
            }
         }
      }
   }

   @Override
   public int hashCode() {
      int h = 0;
      Iterator<LongObjectPair<V>> i = this.entrySet().iterator();

      while(i.hasNext()) {
         h += i.next().hashCode();
      }

      return h;
   }

   @Override
   public String toString() {
      Iterator<LongObjectPair<V>> i = this.entrySet().iterator();
      if (!i.hasNext()) {
         return "{}";
      } else {
         StringBuilder sb = new StringBuilder();
         sb.append('{');

         while(true) {
            LongObjectPair<V> e = i.next();
            long key = e.getKey();
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
      AbstractLongObjectMap<V> result = (AbstractLongObjectMap)super.clone();
      result.keySet = null;
      result.values = null;
      return result;
   }
}
