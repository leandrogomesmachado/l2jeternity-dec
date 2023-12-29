package org.napile.primitive.maps.abstracts;

import java.util.Iterator;
import java.util.Set;
import org.napile.primitive.Variables;
import org.napile.primitive.collections.LongCollection;
import org.napile.primitive.collections.abstracts.AbstractLongCollection;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.iterators.LongIterator;
import org.napile.primitive.maps.IntLongMap;
import org.napile.primitive.pair.IntLongPair;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.abstracts.AbstractIntSet;

public abstract class AbstractIntLongMap implements IntLongMap {
   protected transient volatile IntSet keySet = null;
   protected transient volatile LongCollection values = null;

   protected AbstractIntLongMap() {
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
   public boolean containsValue(long value) {
      for(IntLongPair e : this.entrySet()) {
         if (value == e.getValue()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean containsKey(int key) {
      for(IntLongPair e : this.entrySet()) {
         if (key == e.getKey()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public long get(int key) {
      for(IntLongPair e : this.entrySet()) {
         if (key == e.getKey()) {
            return e.getValue();
         }
      }

      return (long)Variables.RETURN_LONG_VALUE_IF_NOT_FOUND;
   }

   @Override
   public long put(int key, long value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public long remove(int key) {
      Iterator<IntLongPair> i = this.entrySet().iterator();
      IntLongPair correctEntry = null;

      while(correctEntry == null && i.hasNext()) {
         IntLongPair e = i.next();
         if (key == e.getKey()) {
            correctEntry = e;
         }
      }

      long oldValue = (long)Variables.RETURN_LONG_VALUE_IF_NOT_FOUND;
      if (correctEntry != null) {
         oldValue = correctEntry.getValue();
         i.remove();
      }

      return oldValue;
   }

   @Override
   public void putAll(IntLongMap m) {
      for(IntLongPair e : m.entrySet()) {
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
                  private Iterator<IntLongPair> i = AbstractIntLongMap.this.entrySet().iterator();

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
               return AbstractIntLongMap.this.size();
            }

            @Override
            public boolean contains(int k) {
               return AbstractIntLongMap.this.containsKey(k);
            }
         };
      }

      return this.keySet;
   }

   @Override
   public LongCollection valueCollection() {
      if (this.values == null) {
         this.values = new AbstractLongCollection() {
            @Override
            public LongIterator iterator() {
               return new LongIterator() {
                  private Iterator<IntLongPair> i = AbstractIntLongMap.this.entrySet().iterator();

                  @Override
                  public boolean hasNext() {
                     return this.i.hasNext();
                  }

                  @Override
                  public long next() {
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
               return AbstractIntLongMap.this.size();
            }

            @Override
            public boolean contains(long v) {
               return AbstractIntLongMap.this.containsValue(v);
            }
         };
      }

      return this.values;
   }

   @Override
   public abstract Set<IntLongPair> entrySet();

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof IntLongMap)) {
         return false;
      } else {
         IntLongMap m = (IntLongMap)o;
         if (m.size() != this.size()) {
            return false;
         } else {
            try {
               for(IntLongPair e : this.entrySet()) {
                  int key = e.getKey();
                  long value = e.getValue();
                  if (value != m.get(key)) {
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

      for(IntLongPair intLongPair : this.entrySet()) {
         h += intLongPair.hashCode();
      }

      return h;
   }

   @Override
   public String toString() {
      Iterator<IntLongPair> i = this.entrySet().iterator();
      if (!i.hasNext()) {
         return "{}";
      } else {
         StringBuilder sb = new StringBuilder();
         sb.append('{');

         while(true) {
            IntLongPair e = i.next();
            int key = e.getKey();
            long value = e.getValue();
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
      AbstractIntLongMap result = (AbstractIntLongMap)super.clone();
      result.keySet = null;
      result.values = null;
      return result;
   }
}
