package org.napile.primitive.maps.abstracts;

import java.util.Iterator;
import java.util.Set;
import org.napile.primitive.Variables;
import org.napile.primitive.collections.IntCollection;
import org.napile.primitive.collections.abstracts.AbstractIntCollection;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.maps.IntIntMap;
import org.napile.primitive.pair.IntIntPair;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.abstracts.AbstractIntSet;

public abstract class AbstractIntIntMap implements IntIntMap {
   protected transient volatile IntSet keySet = null;
   protected transient volatile IntCollection values = null;

   protected AbstractIntIntMap() {
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
   public boolean containsValue(int value) {
      for(IntIntPair e : this.entrySet()) {
         if (value == e.getValue()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean containsKey(int key) {
      for(IntIntPair e : this.entrySet()) {
         if (key == e.getKey()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public int get(int key) {
      for(IntIntPair e : this.entrySet()) {
         if (key == e.getKey()) {
            return e.getValue();
         }
      }

      return Variables.RETURN_INT_VALUE_IF_NOT_FOUND;
   }

   @Override
   public int put(int key, int value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int remove(int key) {
      Iterator<IntIntPair> i = this.entrySet().iterator();
      IntIntPair correctEntry = null;

      while(correctEntry == null && i.hasNext()) {
         IntIntPair e = i.next();
         if (key == e.getKey()) {
            correctEntry = e;
         }
      }

      int oldValue = Variables.RETURN_INT_VALUE_IF_NOT_FOUND;
      if (correctEntry != null) {
         oldValue = correctEntry.getValue();
         i.remove();
      }

      return oldValue;
   }

   @Override
   public void putAll(IntIntMap m) {
      for(IntIntPair e : m.entrySet()) {
         this.put(e.getKey(), e.getValue());
      }
   }

   @Override
   public void clear() {
      this.entrySet().clear();
   }

   @Override
   public IntSet keySet() {
      if (this.keySet == null) {
         this.keySet = new AbstractIntSet() {
            @Override
            public IntIterator iterator() {
               return new IntIterator() {
                  private Iterator<IntIntPair> i = AbstractIntIntMap.this.entrySet().iterator();

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
               return AbstractIntIntMap.this.size();
            }

            @Override
            public boolean contains(int k) {
               return AbstractIntIntMap.this.containsKey(k);
            }
         };
      }

      return this.keySet;
   }

   @Override
   public IntCollection valueCollection() {
      if (this.values == null) {
         this.values = new AbstractIntCollection() {
            @Override
            public IntIterator iterator() {
               return new IntIterator() {
                  private Iterator<IntIntPair> i = AbstractIntIntMap.this.entrySet().iterator();

                  @Override
                  public boolean hasNext() {
                     return this.i.hasNext();
                  }

                  @Override
                  public int next() {
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
               return AbstractIntIntMap.this.size();
            }

            @Override
            public boolean contains(int v) {
               return AbstractIntIntMap.this.containsValue(v);
            }
         };
      }

      return this.values;
   }

   @Override
   public abstract Set<IntIntPair> entrySet();

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof IntIntMap)) {
         return false;
      } else {
         IntIntMap m = (IntIntMap)o;
         if (m.size() != this.size()) {
            return false;
         } else {
            try {
               for(IntIntPair e : this.entrySet()) {
                  int key = e.getKey();
                  int value = e.getValue();
                  if (value != m.get(key)) {
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

      for(IntIntPair intIntPair : this.entrySet()) {
         h += intIntPair.hashCode();
      }

      return h;
   }

   @Override
   public String toString() {
      Iterator<IntIntPair> i = this.entrySet().iterator();
      if (!i.hasNext()) {
         return "{}";
      } else {
         StringBuilder sb = new StringBuilder();
         sb.append('{');

         while(true) {
            IntIntPair e = i.next();
            int key = e.getKey();
            int value = e.getValue();
            sb.append(key);
            sb.append('=');
            sb.append(value);
            if (!i.hasNext()) {
               return sb.append('}').toString();
            }

            sb.append(", ");
         }
      }
   }

   @Override
   protected Object clone() throws CloneNotSupportedException {
      AbstractIntIntMap result = (AbstractIntIntMap)super.clone();
      result.keySet = null;
      result.values = null;
      return result;
   }
}
