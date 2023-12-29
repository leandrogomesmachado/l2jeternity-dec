package gnu.trove.decorator;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class TIntIntMapDecorator extends AbstractMap<Integer, Integer> implements Map<Integer, Integer>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TIntIntMap _map;

   public TIntIntMapDecorator() {
   }

   public TIntIntMapDecorator(TIntIntMap map) {
      this._map = map;
   }

   public TIntIntMap getMap() {
      return this._map;
   }

   public Integer put(Integer key, Integer value) {
      int k;
      if (key == null) {
         k = this._map.getNoEntryKey();
      } else {
         k = this.unwrapKey(key);
      }

      int v;
      if (value == null) {
         v = this._map.getNoEntryValue();
      } else {
         v = this.unwrapValue(value);
      }

      int retval = this._map.put(k, v);
      return retval == this._map.getNoEntryValue() ? null : this.wrapValue(retval);
   }

   public Integer get(Object key) {
      int k;
      if (key != null) {
         if (!(key instanceof Integer)) {
            return null;
         }

         k = this.unwrapKey(key);
      } else {
         k = this._map.getNoEntryKey();
      }

      int v = this._map.get(k);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public void clear() {
      this._map.clear();
   }

   public Integer remove(Object key) {
      int k;
      if (key != null) {
         if (!(key instanceof Integer)) {
            return null;
         }

         k = this.unwrapKey(key);
      } else {
         k = this._map.getNoEntryKey();
      }

      int v = this._map.remove(k);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public Set<Entry<Integer, Integer>> entrySet() {
      return new AbstractSet<Entry<Integer, Integer>>() {
         @Override
         public int size() {
            return TIntIntMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TIntIntMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TIntIntMapDecorator.this.containsKey(k) && TIntIntMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Integer, Integer>> iterator() {
            return new Iterator<Entry<Integer, Integer>>() {
               private final TIntIntIterator it = TIntIntMapDecorator.this._map.iterator();

               public Entry<Integer, Integer> next() {
                  this.it.advance();
                  int ik = this.it.key();
                  final Integer key = ik == TIntIntMapDecorator.this._map.getNoEntryKey() ? null : TIntIntMapDecorator.this.wrapKey(ik);
                  int iv = this.it.value();
                  final Integer v = iv == TIntIntMapDecorator.this._map.getNoEntryValue() ? null : TIntIntMapDecorator.this.wrapValue(iv);
                  return new Entry<Integer, Integer>() {
                     private Integer val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Integer getKey() {
                        return key;
                     }

                     public Integer getValue() {
                        return this.val;
                     }

                     @Override
                     public int hashCode() {
                        return key.hashCode() + this.val.hashCode();
                     }

                     public Integer setValue(Integer value) {
                        this.val = value;
                        return TIntIntMapDecorator.this.put(key, value);
                     }
                  };
               }

               @Override
               public boolean hasNext() {
                  return this.it.hasNext();
               }

               @Override
               public void remove() {
                  this.it.remove();
               }
            };
         }

         public boolean add(Entry<Integer, Integer> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Integer key = (Integer)((Entry)o).getKey();
               TIntIntMapDecorator.this._map.remove(TIntIntMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Integer, Integer>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TIntIntMapDecorator.this.clear();
         }
      };
   }

   @Override
   public boolean containsValue(Object val) {
      return val instanceof Integer && this._map.containsValue(this.unwrapValue(val));
   }

   @Override
   public boolean containsKey(Object key) {
      if (key == null) {
         return this._map.containsKey(this._map.getNoEntryKey());
      } else {
         return key instanceof Integer && this._map.containsKey(this.unwrapKey(key));
      }
   }

   @Override
   public int size() {
      return this._map.size();
   }

   @Override
   public boolean isEmpty() {
      return this.size() == 0;
   }

   @Override
   public void putAll(Map<? extends Integer, ? extends Integer> map) {
      Iterator<? extends Entry<? extends Integer, ? extends Integer>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Integer, ? extends Integer> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Integer wrapKey(int k) {
      return k;
   }

   protected int unwrapKey(Object key) {
      return (Integer)key;
   }

   protected Integer wrapValue(int k) {
      return k;
   }

   protected int unwrapValue(Object value) {
      return (Integer)value;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this._map = (TIntIntMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
