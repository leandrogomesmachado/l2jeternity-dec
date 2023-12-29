package gnu.trove.decorator;

import gnu.trove.iterator.TFloatObjectIterator;
import gnu.trove.map.TFloatObjectMap;
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

public class TFloatObjectMapDecorator<V> extends AbstractMap<Float, V> implements Map<Float, V>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TFloatObjectMap<V> _map;

   public TFloatObjectMapDecorator() {
   }

   public TFloatObjectMapDecorator(TFloatObjectMap<V> map) {
      this._map = map;
   }

   public TFloatObjectMap<V> getMap() {
      return this._map;
   }

   public V put(Float key, V value) {
      float k;
      if (key == null) {
         k = this._map.getNoEntryKey();
      } else {
         k = this.unwrapKey(key);
      }

      return this._map.put(k, value);
   }

   @Override
   public V get(Object key) {
      float k;
      if (key != null) {
         if (!(key instanceof Float)) {
            return null;
         }

         k = this.unwrapKey((Float)key);
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
      float k;
      if (key != null) {
         if (!(key instanceof Float)) {
            return null;
         }

         k = this.unwrapKey((Float)key);
      } else {
         k = this._map.getNoEntryKey();
      }

      return this._map.remove(k);
   }

   @Override
   public Set<Entry<Float, V>> entrySet() {
      return new AbstractSet<Entry<Float, V>>() {
         @Override
         public int size() {
            return TFloatObjectMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TFloatObjectMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TFloatObjectMapDecorator.this.containsKey(k) && TFloatObjectMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Float, V>> iterator() {
            return new Iterator<Entry<Float, V>>() {
               private final TFloatObjectIterator<V> it = TFloatObjectMapDecorator.this._map.iterator();

               public Entry<Float, V> next() {
                  this.it.advance();
                  float k = this.it.key();
                  final Float key = k == TFloatObjectMapDecorator.this._map.getNoEntryKey() ? null : TFloatObjectMapDecorator.this.wrapKey(k);
                  final V v = this.it.value();
                  return new Entry<Float, V>() {
                     private V val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Float getKey() {
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
                        return (V)TFloatObjectMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Float, V> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Float key = (Float)((Entry)o).getKey();
               TFloatObjectMapDecorator.this._map.remove(TFloatObjectMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Float, V>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TFloatObjectMapDecorator.this.clear();
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
         return key instanceof Float && this._map.containsKey((Float)key);
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
   public void putAll(Map<? extends Float, ? extends V> map) {
      Iterator<? extends Entry<? extends Float, ? extends V>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Float, ? extends V> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Float wrapKey(float k) {
      return k;
   }

   protected float unwrapKey(Float key) {
      return key;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this._map = (TFloatObjectMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
