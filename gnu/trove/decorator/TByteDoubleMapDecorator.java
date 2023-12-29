package gnu.trove.decorator;

import gnu.trove.iterator.TByteDoubleIterator;
import gnu.trove.map.TByteDoubleMap;
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

public class TByteDoubleMapDecorator extends AbstractMap<Byte, Double> implements Map<Byte, Double>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TByteDoubleMap _map;

   public TByteDoubleMapDecorator() {
   }

   public TByteDoubleMapDecorator(TByteDoubleMap map) {
      this._map = map;
   }

   public TByteDoubleMap getMap() {
      return this._map;
   }

   public Double put(Byte key, Double value) {
      byte k;
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
      byte k;
      if (key != null) {
         if (!(key instanceof Byte)) {
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
      byte k;
      if (key != null) {
         if (!(key instanceof Byte)) {
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
   public Set<Entry<Byte, Double>> entrySet() {
      return new AbstractSet<Entry<Byte, Double>>() {
         @Override
         public int size() {
            return TByteDoubleMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TByteDoubleMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TByteDoubleMapDecorator.this.containsKey(k) && TByteDoubleMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Byte, Double>> iterator() {
            return new Iterator<Entry<Byte, Double>>() {
               private final TByteDoubleIterator it = TByteDoubleMapDecorator.this._map.iterator();

               public Entry<Byte, Double> next() {
                  this.it.advance();
                  byte ik = this.it.key();
                  final Byte key = ik == TByteDoubleMapDecorator.this._map.getNoEntryKey() ? null : TByteDoubleMapDecorator.this.wrapKey(ik);
                  double iv = this.it.value();
                  final Double v = iv == TByteDoubleMapDecorator.this._map.getNoEntryValue() ? null : TByteDoubleMapDecorator.this.wrapValue(iv);
                  return new Entry<Byte, Double>() {
                     private Double val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Byte getKey() {
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
                        return TByteDoubleMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Byte, Double> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Byte key = (Byte)((Entry)o).getKey();
               TByteDoubleMapDecorator.this._map.remove(TByteDoubleMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Byte, Double>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TByteDoubleMapDecorator.this.clear();
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
         return key instanceof Byte && this._map.containsKey(this.unwrapKey(key));
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
   public void putAll(Map<? extends Byte, ? extends Double> map) {
      Iterator<? extends Entry<? extends Byte, ? extends Double>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Byte, ? extends Double> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Byte wrapKey(byte k) {
      return k;
   }

   protected byte unwrapKey(Object key) {
      return (Byte)key;
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
      this._map = (TByteDoubleMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
