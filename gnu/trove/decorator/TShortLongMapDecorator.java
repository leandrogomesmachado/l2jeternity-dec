package gnu.trove.decorator;

import gnu.trove.iterator.TShortLongIterator;
import gnu.trove.map.TShortLongMap;
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

public class TShortLongMapDecorator extends AbstractMap<Short, Long> implements Map<Short, Long>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TShortLongMap _map;

   public TShortLongMapDecorator() {
   }

   public TShortLongMapDecorator(TShortLongMap map) {
      this._map = map;
   }

   public TShortLongMap getMap() {
      return this._map;
   }

   public Long put(Short key, Long value) {
      short k;
      if (key == null) {
         k = this._map.getNoEntryKey();
      } else {
         k = this.unwrapKey(key);
      }

      long v;
      if (value == null) {
         v = this._map.getNoEntryValue();
      } else {
         v = this.unwrapValue(value);
      }

      long retval = this._map.put(k, v);
      return retval == this._map.getNoEntryValue() ? null : this.wrapValue(retval);
   }

   public Long get(Object key) {
      short k;
      if (key != null) {
         if (!(key instanceof Short)) {
            return null;
         }

         k = this.unwrapKey(key);
      } else {
         k = this._map.getNoEntryKey();
      }

      long v = this._map.get(k);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public void clear() {
      this._map.clear();
   }

   public Long remove(Object key) {
      short k;
      if (key != null) {
         if (!(key instanceof Short)) {
            return null;
         }

         k = this.unwrapKey(key);
      } else {
         k = this._map.getNoEntryKey();
      }

      long v = this._map.remove(k);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public Set<Entry<Short, Long>> entrySet() {
      return new AbstractSet<Entry<Short, Long>>() {
         @Override
         public int size() {
            return TShortLongMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TShortLongMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TShortLongMapDecorator.this.containsKey(k) && TShortLongMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Short, Long>> iterator() {
            return new Iterator<Entry<Short, Long>>() {
               private final TShortLongIterator it = TShortLongMapDecorator.this._map.iterator();

               public Entry<Short, Long> next() {
                  this.it.advance();
                  short ik = this.it.key();
                  final Short key = ik == TShortLongMapDecorator.this._map.getNoEntryKey() ? null : TShortLongMapDecorator.this.wrapKey(ik);
                  long iv = this.it.value();
                  final Long v = iv == TShortLongMapDecorator.this._map.getNoEntryValue() ? null : TShortLongMapDecorator.this.wrapValue(iv);
                  return new Entry<Short, Long>() {
                     private Long val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Short getKey() {
                        return key;
                     }

                     public Long getValue() {
                        return this.val;
                     }

                     @Override
                     public int hashCode() {
                        return key.hashCode() + this.val.hashCode();
                     }

                     public Long setValue(Long value) {
                        this.val = value;
                        return TShortLongMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Short, Long> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Short key = (Short)((Entry)o).getKey();
               TShortLongMapDecorator.this._map.remove(TShortLongMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Short, Long>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TShortLongMapDecorator.this.clear();
         }
      };
   }

   @Override
   public boolean containsValue(Object val) {
      return val instanceof Long && this._map.containsValue(this.unwrapValue(val));
   }

   @Override
   public boolean containsKey(Object key) {
      if (key == null) {
         return this._map.containsKey(this._map.getNoEntryKey());
      } else {
         return key instanceof Short && this._map.containsKey(this.unwrapKey(key));
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
   public void putAll(Map<? extends Short, ? extends Long> map) {
      Iterator<? extends Entry<? extends Short, ? extends Long>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Short, ? extends Long> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Short wrapKey(short k) {
      return k;
   }

   protected short unwrapKey(Object key) {
      return (Short)key;
   }

   protected Long wrapValue(long k) {
      return k;
   }

   protected long unwrapValue(Object value) {
      return (Long)value;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this._map = (TShortLongMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
