package gnu.trove.decorator;

import gnu.trove.iterator.TObjectLongIterator;
import gnu.trove.map.TObjectLongMap;
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

public class TObjectLongMapDecorator<K> extends AbstractMap<K, Long> implements Map<K, Long>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TObjectLongMap<K> _map;

   public TObjectLongMapDecorator() {
   }

   public TObjectLongMapDecorator(TObjectLongMap<K> map) {
      this._map = map;
   }

   public TObjectLongMap<K> getMap() {
      return this._map;
   }

   public Long put(K key, Long value) {
      return value == null ? this.wrapValue(this._map.put(key, this._map.getNoEntryValue())) : this.wrapValue(this._map.put(key, this.unwrapValue(value)));
   }

   public Long get(Object key) {
      long v = this._map.get(key);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public void clear() {
      this._map.clear();
   }

   public Long remove(Object key) {
      long v = this._map.remove(key);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public Set<Entry<K, Long>> entrySet() {
      return new AbstractSet<Entry<K, Long>>() {
         @Override
         public int size() {
            return TObjectLongMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TObjectLongMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TObjectLongMapDecorator.this.containsKey(k) && TObjectLongMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<K, Long>> iterator() {
            return new Iterator<Entry<K, Long>>() {
               private final TObjectLongIterator<K> it = TObjectLongMapDecorator.this._map.iterator();

               public Entry<K, Long> next() {
                  this.it.advance();
                  final K key = this.it.key();
                  final Long v = TObjectLongMapDecorator.this.wrapValue(this.it.value());
                  return new Entry<K, Long>() {
                     private Long val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     @Override
                     public K getKey() {
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
                        return TObjectLongMapDecorator.this.put(key, value);
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

         public boolean add(Entry<K, Long> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               K key = (K)((Entry)o).getKey();
               TObjectLongMapDecorator.this._map.remove(key);
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<K, Long>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TObjectLongMapDecorator.this.clear();
         }
      };
   }

   @Override
   public boolean containsValue(Object val) {
      return val instanceof Long && this._map.containsValue(this.unwrapValue(val));
   }

   @Override
   public boolean containsKey(Object key) {
      return this._map.containsKey(key);
   }

   @Override
   public int size() {
      return this._map.size();
   }

   @Override
   public boolean isEmpty() {
      return this._map.size() == 0;
   }

   @Override
   public void putAll(Map<? extends K, ? extends Long> map) {
      Iterator<? extends Entry<? extends K, ? extends Long>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends K, ? extends Long> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
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
      this._map = (TObjectLongMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
