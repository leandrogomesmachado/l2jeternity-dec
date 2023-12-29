package gnu.trove.decorator;

import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.TObjectDoubleMap;
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

public class TObjectDoubleMapDecorator<K> extends AbstractMap<K, Double> implements Map<K, Double>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TObjectDoubleMap<K> _map;

   public TObjectDoubleMapDecorator() {
   }

   public TObjectDoubleMapDecorator(TObjectDoubleMap<K> map) {
      this._map = map;
   }

   public TObjectDoubleMap<K> getMap() {
      return this._map;
   }

   public Double put(K key, Double value) {
      return value == null ? this.wrapValue(this._map.put(key, this._map.getNoEntryValue())) : this.wrapValue(this._map.put(key, this.unwrapValue(value)));
   }

   public Double get(Object key) {
      double v = this._map.get(key);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public void clear() {
      this._map.clear();
   }

   public Double remove(Object key) {
      double v = this._map.remove(key);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public Set<Entry<K, Double>> entrySet() {
      return new AbstractSet<Entry<K, Double>>() {
         @Override
         public int size() {
            return TObjectDoubleMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TObjectDoubleMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TObjectDoubleMapDecorator.this.containsKey(k) && TObjectDoubleMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<K, Double>> iterator() {
            return new Iterator<Entry<K, Double>>() {
               private final TObjectDoubleIterator<K> it = TObjectDoubleMapDecorator.this._map.iterator();

               public Entry<K, Double> next() {
                  this.it.advance();
                  final K key = this.it.key();
                  final Double v = TObjectDoubleMapDecorator.this.wrapValue(this.it.value());
                  return new Entry<K, Double>() {
                     private Double val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     @Override
                     public K getKey() {
                        return key;
                     }

                     public Double getValue() {
                        return this.val;
                     }

                     @Override
                     public int hashCode() {
                        return key.hashCode() + this.val.hashCode();
                     }

                     public Double setValue(Double value) {
                        this.val = value;
                        return TObjectDoubleMapDecorator.this.put(key, value);
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

         public boolean add(Entry<K, Double> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               K key = (K)((Entry)o).getKey();
               TObjectDoubleMapDecorator.this._map.remove(key);
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<K, Double>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TObjectDoubleMapDecorator.this.clear();
         }
      };
   }

   @Override
   public boolean containsValue(Object val) {
      return val instanceof Double && this._map.containsValue(this.unwrapValue(val));
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
   public void putAll(Map<? extends K, ? extends Double> map) {
      Iterator<? extends Entry<? extends K, ? extends Double>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends K, ? extends Double> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Double wrapValue(double k) {
      return k;
   }

   protected double unwrapValue(Object value) {
      return (Double)value;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this._map = (TObjectDoubleMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
