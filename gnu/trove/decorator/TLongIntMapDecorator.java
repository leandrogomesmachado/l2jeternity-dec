package gnu.trove.decorator;

import gnu.trove.iterator.TLongIntIterator;
import gnu.trove.map.TLongIntMap;
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

public class TLongIntMapDecorator extends AbstractMap<Long, Integer> implements Map<Long, Integer>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TLongIntMap _map;

   public TLongIntMapDecorator() {
   }

   public TLongIntMapDecorator(TLongIntMap map) {
      this._map = map;
   }

   public TLongIntMap getMap() {
      return this._map;
   }

   public Integer put(Long key, Integer value) {
      long k;
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
      long k;
      if (key != null) {
         if (!(key instanceof Long)) {
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
      long k;
      if (key != null) {
         if (!(key instanceof Long)) {
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
   public Set<Entry<Long, Integer>> entrySet() {
      return new AbstractSet<Entry<Long, Integer>>() {
         @Override
         public int size() {
            return TLongIntMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TLongIntMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TLongIntMapDecorator.this.containsKey(k) && TLongIntMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Long, Integer>> iterator() {
            return new Iterator<Entry<Long, Integer>>() {
               private final TLongIntIterator it = TLongIntMapDecorator.this._map.iterator();

               public Entry<Long, Integer> next() {
                  this.it.advance();
                  long ik = this.it.key();
                  final Long key = ik == TLongIntMapDecorator.this._map.getNoEntryKey() ? null : TLongIntMapDecorator.this.wrapKey(ik);
                  int iv = this.it.value();
                  final Integer v = iv == TLongIntMapDecorator.this._map.getNoEntryValue() ? null : TLongIntMapDecorator.this.wrapValue(iv);
                  return new Entry<Long, Integer>() {
                     private Integer val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Long getKey() {
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
                        return TLongIntMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Long, Integer> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Long key = (Long)((Entry)o).getKey();
               TLongIntMapDecorator.this._map.remove(TLongIntMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Long, Integer>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TLongIntMapDecorator.this.clear();
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
         return key instanceof Long && this._map.containsKey(this.unwrapKey(key));
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
   public void putAll(Map<? extends Long, ? extends Integer> map) {
      Iterator<? extends Entry<? extends Long, ? extends Integer>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Long, ? extends Integer> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Long wrapKey(long k) {
      return k;
   }

   protected long unwrapKey(Object key) {
      return (Long)key;
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
      this._map = (TLongIntMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
