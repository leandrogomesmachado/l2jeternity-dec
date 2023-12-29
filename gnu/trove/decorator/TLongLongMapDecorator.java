package gnu.trove.decorator;

import gnu.trove.iterator.TLongLongIterator;
import gnu.trove.map.TLongLongMap;
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

public class TLongLongMapDecorator extends AbstractMap<Long, Long> implements Map<Long, Long>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TLongLongMap _map;

   public TLongLongMapDecorator() {
   }

   public TLongLongMapDecorator(TLongLongMap map) {
      this._map = map;
   }

   public TLongLongMap getMap() {
      return this._map;
   }

   public Long put(Long key, Long value) {
      long k;
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
      long k;
      if (key != null) {
         if (!(key instanceof Long)) {
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
      long k;
      if (key != null) {
         if (!(key instanceof Long)) {
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
   public Set<Entry<Long, Long>> entrySet() {
      return new AbstractSet<Entry<Long, Long>>() {
         @Override
         public int size() {
            return TLongLongMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TLongLongMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TLongLongMapDecorator.this.containsKey(k) && TLongLongMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Long, Long>> iterator() {
            return new Iterator<Entry<Long, Long>>() {
               private final TLongLongIterator it = TLongLongMapDecorator.this._map.iterator();

               public Entry<Long, Long> next() {
                  this.it.advance();
                  long ik = this.it.key();
                  final Long key = ik == TLongLongMapDecorator.this._map.getNoEntryKey() ? null : TLongLongMapDecorator.this.wrapKey(ik);
                  long iv = this.it.value();
                  final Long v = iv == TLongLongMapDecorator.this._map.getNoEntryValue() ? null : TLongLongMapDecorator.this.wrapValue(iv);
                  return new Entry<Long, Long>() {
                     private Long val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Long getKey() {
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
                        return TLongLongMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Long, Long> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Long key = (Long)((Entry)o).getKey();
               TLongLongMapDecorator.this._map.remove(TLongLongMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Long, Long>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TLongLongMapDecorator.this.clear();
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
   public void putAll(Map<? extends Long, ? extends Long> map) {
      Iterator<? extends Entry<? extends Long, ? extends Long>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Long, ? extends Long> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Long wrapKey(long k) {
      return k;
   }

   protected long unwrapKey(Object key) {
      return (Long)key;
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
      this._map = (TLongLongMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
