package gnu.trove.decorator;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.TLongObjectMap;
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

public class TLongObjectMapDecorator<V> extends AbstractMap<Long, V> implements Map<Long, V>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TLongObjectMap<V> _map;

   public TLongObjectMapDecorator() {
   }

   public TLongObjectMapDecorator(TLongObjectMap<V> map) {
      this._map = map;
   }

   public TLongObjectMap<V> getMap() {
      return this._map;
   }

   public V put(Long key, V value) {
      long k;
      if (key == null) {
         k = this._map.getNoEntryKey();
      } else {
         k = this.unwrapKey(key);
      }

      return this._map.put(k, value);
   }

   @Override
   public V get(Object key) {
      long k;
      if (key != null) {
         if (!(key instanceof Long)) {
            return null;
         }

         k = this.unwrapKey((Long)key);
      } else {
         k = this._map.getNoEntryKey();
      }

      return this._map.get(k);
   }

   @Override
   public void clear() {
      this._map.clear();
   }

   @Override
   public V remove(Object key) {
      long k;
      if (key != null) {
         if (!(key instanceof Long)) {
            return null;
         }

         k = this.unwrapKey((Long)key);
      } else {
         k = this._map.getNoEntryKey();
      }

      return this._map.remove(k);
   }

   @Override
   public Set<Entry<Long, V>> entrySet() {
      return new AbstractSet<Entry<Long, V>>() {
         @Override
         public int size() {
            return TLongObjectMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TLongObjectMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TLongObjectMapDecorator.this.containsKey(k) && TLongObjectMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Long, V>> iterator() {
            return new Iterator<Entry<Long, V>>() {
               private final TLongObjectIterator<V> it = TLongObjectMapDecorator.this._map.iterator();

               public Entry<Long, V> next() {
                  this.it.advance();
                  long k = this.it.key();
                  final Long key = k == TLongObjectMapDecorator.this._map.getNoEntryKey() ? null : TLongObjectMapDecorator.this.wrapKey(k);
                  final V v = this.it.value();
                  return new Entry<Long, V>() {
                     private V val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Long getKey() {
                        return key;
                     }

                     @Override
                     public V getValue() {
                        return this.val;
                     }

                     @Override
                     public int hashCode() {
                        return key.hashCode() + this.val.hashCode();
                     }

                     @Override
                     public V setValue(V value) {
                        this.val = value;
                        return (V)TLongObjectMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Long, V> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Long key = (Long)((Entry)o).getKey();
               TLongObjectMapDecorator.this._map.remove(TLongObjectMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Long, V>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TLongObjectMapDecorator.this.clear();
         }
      };
   }

   @Override
   public boolean containsValue(Object val) {
      return this._map.containsValue(val);
   }

   @Override
   public boolean containsKey(Object key) {
      if (key == null) {
         return this._map.containsKey(this._map.getNoEntryKey());
      } else {
         return key instanceof Long && this._map.containsKey((Long)key);
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
   public void putAll(Map<? extends Long, ? extends V> map) {
      Iterator<? extends Entry<? extends Long, ? extends V>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Long, ? extends V> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Long wrapKey(long k) {
      return k;
   }

   protected long unwrapKey(Long key) {
      return key;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this._map = (TLongObjectMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
