package gnu.trove.decorator;

import gnu.trove.iterator.TFloatByteIterator;
import gnu.trove.map.TFloatByteMap;
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

public class TFloatByteMapDecorator extends AbstractMap<Float, Byte> implements Map<Float, Byte>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TFloatByteMap _map;

   public TFloatByteMapDecorator() {
   }

   public TFloatByteMapDecorator(TFloatByteMap map) {
      this._map = map;
   }

   public TFloatByteMap getMap() {
      return this._map;
   }

   public Byte put(Float key, Byte value) {
      float k;
      if (key == null) {
         k = this._map.getNoEntryKey();
      } else {
         k = this.unwrapKey(key);
      }

      byte v;
      if (value == null) {
         v = this._map.getNoEntryValue();
      } else {
         v = this.unwrapValue(value);
      }

      byte retval = this._map.put(k, v);
      return retval == this._map.getNoEntryValue() ? null : this.wrapValue(retval);
   }

   public Byte get(Object key) {
      float k;
      if (key != null) {
         if (!(key instanceof Float)) {
            return null;
         }

         k = this.unwrapKey(key);
      } else {
         k = this._map.getNoEntryKey();
      }

      byte v = this._map.get(k);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public void clear() {
      this._map.clear();
   }

   public Byte remove(Object key) {
      float k;
      if (key != null) {
         if (!(key instanceof Float)) {
            return null;
         }

         k = this.unwrapKey(key);
      } else {
         k = this._map.getNoEntryKey();
      }

      byte v = this._map.remove(k);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public Set<Entry<Float, Byte>> entrySet() {
      return new AbstractSet<Entry<Float, Byte>>() {
         @Override
         public int size() {
            return TFloatByteMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TFloatByteMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TFloatByteMapDecorator.this.containsKey(k) && TFloatByteMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Float, Byte>> iterator() {
            return new Iterator<Entry<Float, Byte>>() {
               private final TFloatByteIterator it = TFloatByteMapDecorator.this._map.iterator();

               public Entry<Float, Byte> next() {
                  this.it.advance();
                  float ik = this.it.key();
                  final Float key = ik == TFloatByteMapDecorator.this._map.getNoEntryKey() ? null : TFloatByteMapDecorator.this.wrapKey(ik);
                  byte iv = this.it.value();
                  final Byte v = iv == TFloatByteMapDecorator.this._map.getNoEntryValue() ? null : TFloatByteMapDecorator.this.wrapValue(iv);
                  return new Entry<Float, Byte>() {
                     private Byte val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Float getKey() {
                        return key;
                     }

                     public Byte getValue() {
                        return this.val;
                     }

                     @Override
                     public int hashCode() {
                        return key.hashCode() + this.val.hashCode();
                     }

                     public Byte setValue(Byte value) {
                        this.val = value;
                        return TFloatByteMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Float, Byte> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Float key = (Float)((Entry)o).getKey();
               TFloatByteMapDecorator.this._map.remove(TFloatByteMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Float, Byte>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TFloatByteMapDecorator.this.clear();
         }
      };
   }

   @Override
   public boolean containsValue(Object val) {
      return val instanceof Byte && this._map.containsValue(this.unwrapValue(val));
   }

   @Override
   public boolean containsKey(Object key) {
      if (key == null) {
         return this._map.containsKey(this._map.getNoEntryKey());
      } else {
         return key instanceof Float && this._map.containsKey(this.unwrapKey(key));
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
   public void putAll(Map<? extends Float, ? extends Byte> map) {
      Iterator<? extends Entry<? extends Float, ? extends Byte>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Float, ? extends Byte> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Float wrapKey(float k) {
      return k;
   }

   protected float unwrapKey(Object key) {
      return (Float)key;
   }

   protected Byte wrapValue(byte k) {
      return k;
   }

   protected byte unwrapValue(Object value) {
      return (Byte)value;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this._map = (TFloatByteMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
