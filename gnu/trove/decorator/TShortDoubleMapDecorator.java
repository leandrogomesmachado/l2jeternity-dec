package gnu.trove.decorator;

import gnu.trove.iterator.TShortDoubleIterator;
import gnu.trove.map.TShortDoubleMap;
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

public class TShortDoubleMapDecorator extends AbstractMap<Short, Double> implements Map<Short, Double>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TShortDoubleMap _map;

   public TShortDoubleMapDecorator() {
   }

   public TShortDoubleMapDecorator(TShortDoubleMap map) {
      this._map = map;
   }

   public TShortDoubleMap getMap() {
      return this._map;
   }

   public Double put(Short key, Double value) {
      short k;
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
      short k;
      if (key != null) {
         if (!(key instanceof Short)) {
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
      short k;
      if (key != null) {
         if (!(key instanceof Short)) {
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
   public Set<Entry<Short, Double>> entrySet() {
      return new AbstractSet<Entry<Short, Double>>() {
         @Override
         public int size() {
            return TShortDoubleMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TShortDoubleMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TShortDoubleMapDecorator.this.containsKey(k) && TShortDoubleMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Short, Double>> iterator() {
            return new Iterator<Entry<Short, Double>>() {
               private final TShortDoubleIterator it = TShortDoubleMapDecorator.this._map.iterator();

               public Entry<Short, Double> next() {
                  this.it.advance();
                  short ik = this.it.key();
                  final Short key = ik == TShortDoubleMapDecorator.this._map.getNoEntryKey() ? null : TShortDoubleMapDecorator.this.wrapKey(ik);
                  double iv = this.it.value();
                  final Double v = iv == TShortDoubleMapDecorator.this._map.getNoEntryValue() ? null : TShortDoubleMapDecorator.this.wrapValue(iv);
                  return new Entry<Short, Double>() {
                     private Double val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Short getKey() {
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
                        return TShortDoubleMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Short, Double> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Short key = (Short)((Entry)o).getKey();
               TShortDoubleMapDecorator.this._map.remove(TShortDoubleMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Short, Double>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TShortDoubleMapDecorator.this.clear();
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
   public void putAll(Map<? extends Short, ? extends Double> map) {
      Iterator<? extends Entry<? extends Short, ? extends Double>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Short, ? extends Double> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Short wrapKey(short k) {
      return k;
   }

   protected short unwrapKey(Object key) {
      return (Short)key;
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
      this._map = (TShortDoubleMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
