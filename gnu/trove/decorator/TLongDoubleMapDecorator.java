package gnu.trove.decorator;

import gnu.trove.iterator.TLongDoubleIterator;
import gnu.trove.map.TLongDoubleMap;
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

public class TLongDoubleMapDecorator extends AbstractMap<Long, Double> implements Map<Long, Double>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TLongDoubleMap _map;

   public TLongDoubleMapDecorator() {
   }

   public TLongDoubleMapDecorator(TLongDoubleMap map) {
      this._map = map;
   }

   public TLongDoubleMap getMap() {
      return this._map;
   }

   public Double put(Long key, Double value) {
      long k;
      if (key == null) {
         k = this._map.getNoEntryKey();
      } else {
         k = this.unwrapKey(key);
      }

      double v;
      if (value == null) {
         v = this._map.getNoEntryValue();
      } else {
         v = this.unwrapValue(value);
      }

      double retval = this._map.put(k, v);
      return retval == this._map.getNoEntryValue() ? null : this.wrapValue(retval);
   }

   public Double get(Object key) {
      long k;
      if (key != null) {
         if (!(key instanceof Long)) {
            return null;
         }

         k = this.unwrapKey(key);
      } else {
         k = this._map.getNoEntryKey();
      }

      double v = this._map.get(k);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public void clear() {
      this._map.clear();
   }

   public Double remove(Object key) {
      long k;
      if (key != null) {
         if (!(key instanceof Long)) {
            return null;
         }

         k = this.unwrapKey(key);
      } else {
         k = this._map.getNoEntryKey();
      }

      double v = this._map.remove(k);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public Set<Entry<Long, Double>> entrySet() {
      return new AbstractSet<Entry<Long, Double>>() {
         @Override
         public int size() {
            return TLongDoubleMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TLongDoubleMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TLongDoubleMapDecorator.this.containsKey(k) && TLongDoubleMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Long, Double>> iterator() {
            return new Iterator<Entry<Long, Double>>() {
               private final TLongDoubleIterator it = TLongDoubleMapDecorator.this._map.iterator();

               public Entry<Long, Double> next() {
                  this.it.advance();
                  long ik = this.it.key();
                  final Long key = ik == TLongDoubleMapDecorator.this._map.getNoEntryKey() ? null : TLongDoubleMapDecorator.this.wrapKey(ik);
                  double iv = this.it.value();
                  final Double v = iv == TLongDoubleMapDecorator.this._map.getNoEntryValue() ? null : TLongDoubleMapDecorator.this.wrapValue(iv);
                  return new Entry<Long, Double>() {
                     private Double val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Long getKey() {
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
                        return TLongDoubleMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Long, Double> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Long key = (Long)((Entry)o).getKey();
               TLongDoubleMapDecorator.this._map.remove(TLongDoubleMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Long, Double>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TLongDoubleMapDecorator.this.clear();
         }
      };
   }

   @Override
   public boolean containsValue(Object val) {
      return val instanceof Double && this._map.containsValue(this.unwrapValue(val));
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
   public void putAll(Map<? extends Long, ? extends Double> map) {
      Iterator<? extends Entry<? extends Long, ? extends Double>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Long, ? extends Double> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Long wrapKey(long k) {
      return k;
   }

   protected long unwrapKey(Object key) {
      return (Long)key;
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
      this._map = (TLongDoubleMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
