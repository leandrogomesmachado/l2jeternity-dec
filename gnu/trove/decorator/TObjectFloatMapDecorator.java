package gnu.trove.decorator;

import gnu.trove.iterator.TObjectFloatIterator;
import gnu.trove.map.TObjectFloatMap;
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

public class TObjectFloatMapDecorator<K> extends AbstractMap<K, Float> implements Map<K, Float>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TObjectFloatMap<K> _map;

   public TObjectFloatMapDecorator() {
   }

   public TObjectFloatMapDecorator(TObjectFloatMap<K> map) {
      this._map = map;
   }

   public TObjectFloatMap<K> getMap() {
      return this._map;
   }

   public Float put(K key, Float value) {
      return value == null ? this.wrapValue(this._map.put(key, this._map.getNoEntryValue())) : this.wrapValue(this._map.put(key, this.unwrapValue(value)));
   }

   public Float get(Object key) {
      float v = this._map.get(key);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public void clear() {
      this._map.clear();
   }

   public Float remove(Object key) {
      float v = this._map.remove(key);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public Set<Entry<K, Float>> entrySet() {
      return new AbstractSet<Entry<K, Float>>() {
         @Override
         public int size() {
            return TObjectFloatMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TObjectFloatMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TObjectFloatMapDecorator.this.containsKey(k) && TObjectFloatMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<K, Float>> iterator() {
            return new Iterator<Entry<K, Float>>() {
               private final TObjectFloatIterator<K> it = TObjectFloatMapDecorator.this._map.iterator();

               public Entry<K, Float> next() {
                  this.it.advance();
                  final K key = this.it.key();
                  final Float v = TObjectFloatMapDecorator.this.wrapValue(this.it.value());
                  return new Entry<K, Float>() {
                     private Float val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     @Override
                     public K getKey() {
                        return key;
                     }

                     public Float getValue() {
                        return this.val;
                     }

                     @Override
                     public int hashCode() {
                        return key.hashCode() + this.val.hashCode();
                     }

                     public Float setValue(Float value) {
                        this.val = value;
                        return TObjectFloatMapDecorator.this.put(key, value);
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

         public boolean add(Entry<K, Float> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               K key = (K)((Entry)o).getKey();
               TObjectFloatMapDecorator.this._map.remove(key);
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<K, Float>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TObjectFloatMapDecorator.this.clear();
         }
      };
   }

   @Override
   public boolean containsValue(Object val) {
      return val instanceof Float && this._map.containsValue(this.unwrapValue(val));
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
   public void putAll(Map<? extends K, ? extends Float> map) {
      Iterator<? extends Entry<? extends K, ? extends Float>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends K, ? extends Float> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Float wrapValue(float k) {
      return k;
   }

   protected float unwrapValue(Object value) {
      return (Float)value;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this._map = (TObjectFloatMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
