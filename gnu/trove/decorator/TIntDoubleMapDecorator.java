package gnu.trove.decorator;

import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.TIntDoubleMap;
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

public class TIntDoubleMapDecorator extends AbstractMap<Integer, Double> implements Map<Integer, Double>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TIntDoubleMap _map;

   public TIntDoubleMapDecorator() {
   }

   public TIntDoubleMapDecorator(TIntDoubleMap map) {
      this._map = map;
   }

   public TIntDoubleMap getMap() {
      return this._map;
   }

   public Double put(Integer key, Double value) {
      int k;
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
      int k;
      if (key != null) {
         if (!(key instanceof Integer)) {
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
      int k;
      if (key != null) {
         if (!(key instanceof Integer)) {
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
   public Set<Entry<Integer, Double>> entrySet() {
      return new AbstractSet<Entry<Integer, Double>>() {
         @Override
         public int size() {
            return TIntDoubleMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TIntDoubleMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TIntDoubleMapDecorator.this.containsKey(k) && TIntDoubleMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Integer, Double>> iterator() {
            return new Iterator<Entry<Integer, Double>>() {
               private final TIntDoubleIterator it = TIntDoubleMapDecorator.this._map.iterator();

               public Entry<Integer, Double> next() {
                  this.it.advance();
                  int ik = this.it.key();
                  final Integer key = ik == TIntDoubleMapDecorator.this._map.getNoEntryKey() ? null : TIntDoubleMapDecorator.this.wrapKey(ik);
                  double iv = this.it.value();
                  final Double v = iv == TIntDoubleMapDecorator.this._map.getNoEntryValue() ? null : TIntDoubleMapDecorator.this.wrapValue(iv);
                  return new Entry<Integer, Double>() {
                     private Double val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Integer getKey() {
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
                        return TIntDoubleMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Integer, Double> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Integer key = (Integer)((Entry)o).getKey();
               TIntDoubleMapDecorator.this._map.remove(TIntDoubleMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Integer, Double>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TIntDoubleMapDecorator.this.clear();
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
         return key instanceof Integer && this._map.containsKey(this.unwrapKey(key));
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
   public void putAll(Map<? extends Integer, ? extends Double> map) {
      Iterator<? extends Entry<? extends Integer, ? extends Double>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Integer, ? extends Double> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Integer wrapKey(int k) {
      return k;
   }

   protected int unwrapKey(Object key) {
      return (Integer)key;
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
      this._map = (TIntDoubleMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
