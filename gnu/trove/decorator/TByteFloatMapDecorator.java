package gnu.trove.decorator;

import gnu.trove.iterator.TByteFloatIterator;
import gnu.trove.map.TByteFloatMap;
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

public class TByteFloatMapDecorator extends AbstractMap<Byte, Float> implements Map<Byte, Float>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TByteFloatMap _map;

   public TByteFloatMapDecorator() {
   }

   public TByteFloatMapDecorator(TByteFloatMap map) {
      this._map = map;
   }

   public TByteFloatMap getMap() {
      return this._map;
   }

   public Float put(Byte key, Float value) {
      byte k;
      if (key == null) {
         k = this._map.getNoEntryKey();
      } else {
         k = this.unwrapKey(key);
      }

      float v;
      if (value == null) {
         v = this._map.getNoEntryValue();
      } else {
         v = this.unwrapValue(value);
      }

      float retval = this._map.put(k, v);
      return retval == this._map.getNoEntryValue() ? null : this.wrapValue(retval);
   }

   public Float get(Object key) {
      byte k;
      if (key != null) {
         if (!(key instanceof Byte)) {
            return null;
         }

         k = this.unwrapKey(key);
      } else {
         k = this._map.getNoEntryKey();
      }

      float v = this._map.get(k);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public void clear() {
      this._map.clear();
   }

   public Float remove(Object key) {
      byte k;
      if (key != null) {
         if (!(key instanceof Byte)) {
            return null;
         }

         k = this.unwrapKey(key);
      } else {
         k = this._map.getNoEntryKey();
      }

      float v = this._map.remove(k);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public Set<Entry<Byte, Float>> entrySet() {
      return new AbstractSet<Entry<Byte, Float>>() {
         @Override
         public int size() {
            return TByteFloatMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TByteFloatMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TByteFloatMapDecorator.this.containsKey(k) && TByteFloatMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Byte, Float>> iterator() {
            return new Iterator<Entry<Byte, Float>>() {
               private final TByteFloatIterator it = TByteFloatMapDecorator.this._map.iterator();

               public Entry<Byte, Float> next() {
                  this.it.advance();
                  byte ik = this.it.key();
                  final Byte key = ik == TByteFloatMapDecorator.this._map.getNoEntryKey() ? null : TByteFloatMapDecorator.this.wrapKey(ik);
                  float iv = this.it.value();
                  final Float v = iv == TByteFloatMapDecorator.this._map.getNoEntryValue() ? null : TByteFloatMapDecorator.this.wrapValue(iv);
                  return new Entry<Byte, Float>() {
                     private Float val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Byte getKey() {
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
                        return TByteFloatMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Byte, Float> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Byte key = (Byte)((Entry)o).getKey();
               TByteFloatMapDecorator.this._map.remove(TByteFloatMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Byte, Float>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TByteFloatMapDecorator.this.clear();
         }
      };
   }

   @Override
   public boolean containsValue(Object val) {
      return val instanceof Float && this._map.containsValue(this.unwrapValue(val));
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
   public void putAll(Map<? extends Byte, ? extends Float> map) {
      Iterator<? extends Entry<? extends Byte, ? extends Float>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Byte, ? extends Float> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Byte wrapKey(byte k) {
      return k;
   }

   protected byte unwrapKey(Object key) {
      return (Byte)key;
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
      this._map = (TByteFloatMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
