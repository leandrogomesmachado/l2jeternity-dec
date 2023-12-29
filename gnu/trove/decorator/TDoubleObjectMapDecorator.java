package gnu.trove.decorator;

import gnu.trove.iterator.TDoubleObjectIterator;
import gnu.trove.map.TDoubleObjectMap;
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

public class TDoubleObjectMapDecorator<V> extends AbstractMap<Double, V> implements Map<Double, V>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TDoubleObjectMap<V> _map;

   public TDoubleObjectMapDecorator() {
   }

   public TDoubleObjectMapDecorator(TDoubleObjectMap<V> map) {
      this._map = map;
   }

   public TDoubleObjectMap<V> getMap() {
      return this._map;
   }

   public V put(Double key, V value) {
      double k;
      if (key == null) {
         k = this._map.getNoEntryKey();
      } else {
         k = this.unwrapKey(key);
      }

      return this._map.put(k, value);
   }

   @Override
   public V get(Object key) {
      double k;
      if (key != null) {
         if (!(key instanceof Double)) {
            return null;
         }

         k = this.unwrapKey((Double)key);
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
      double k;
      if (key != null) {
         if (!(key instanceof Double)) {
            return null;
         }

         k = this.unwrapKey((Double)key);
      } else {
         k = this._map.getNoEntryKey();
      }

      return this._map.remove(k);
   }

   @Override
   public Set<Entry<Double, V>> entrySet() {
      return new AbstractSet<Entry<Double, V>>() {
         @Override
         public int size() {
            return TDoubleObjectMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TDoubleObjectMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TDoubleObjectMapDecorator.this.containsKey(k) && TDoubleObjectMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Double, V>> iterator() {
            return new Iterator<Entry<Double, V>>() {
               private final TDoubleObjectIterator<V> it = TDoubleObjectMapDecorator.this._map.iterator();

               public Entry<Double, V> next() {
                  this.it.advance();
                  double k = this.it.key();
                  final Double key = k == TDoubleObjectMapDecorator.this._map.getNoEntryKey() ? null : TDoubleObjectMapDecorator.this.wrapKey(k);
                  final V v = this.it.value();
                  return new Entry<Double, V>() {
                     private V val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Double getKey() {
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
                        return (V)TDoubleObjectMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Double, V> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Double key = (Double)((Entry)o).getKey();
               TDoubleObjectMapDecorator.this._map.remove(TDoubleObjectMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Double, V>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TDoubleObjectMapDecorator.this.clear();
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
         return key instanceof Double && this._map.containsKey((Double)key);
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
   public void putAll(Map<? extends Double, ? extends V> map) {
      Iterator<? extends Entry<? extends Double, ? extends V>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Double, ? extends V> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Double wrapKey(double k) {
      return k;
   }

   protected double unwrapKey(Double key) {
      return key;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this._map = (TDoubleObjectMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
