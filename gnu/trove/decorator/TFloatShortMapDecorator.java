package gnu.trove.decorator;

import gnu.trove.iterator.TFloatShortIterator;
import gnu.trove.map.TFloatShortMap;
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

public class TFloatShortMapDecorator extends AbstractMap<Float, Short> implements Map<Float, Short>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TFloatShortMap _map;

   public TFloatShortMapDecorator() {
   }

   public TFloatShortMapDecorator(TFloatShortMap map) {
      this._map = map;
   }

   public TFloatShortMap getMap() {
      return this._map;
   }

   public Short put(Float key, Short value) {
      float k;
      if (key == null) {
         k = this._map.getNoEntryKey();
      } else {
         k = this.unwrapKey(key);
      }

      short v;
      if (value == null) {
         v = this._map.getNoEntryValue();
      } else {
         v = this.unwrapValue(value);
      }

      short retval = this._map.put(k, v);
      return retval == this._map.getNoEntryValue() ? null : this.wrapValue(retval);
   }

   public Short get(Object key) {
      float k;
      if (key != null) {
         if (!(key instanceof Float)) {
            return null;
         }

         k = this.unwrapKey(key);
      } else {
         k = this._map.getNoEntryKey();
      }

      short v = this._map.get(k);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public void clear() {
      this._map.clear();
   }

   public Short remove(Object key) {
      float k;
      if (key != null) {
         if (!(key instanceof Float)) {
            return null;
         }

         k = this.unwrapKey(key);
      } else {
         k = this._map.getNoEntryKey();
      }

      short v = this._map.remove(k);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public Set<Entry<Float, Short>> entrySet() {
      return new AbstractSet<Entry<Float, Short>>() {
         @Override
         public int size() {
            return TFloatShortMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TFloatShortMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TFloatShortMapDecorator.this.containsKey(k) && TFloatShortMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Float, Short>> iterator() {
            return new Iterator<Entry<Float, Short>>() {
               private final TFloatShortIterator it = TFloatShortMapDecorator.this._map.iterator();

               public Entry<Float, Short> next() {
                  this.it.advance();
                  float ik = this.it.key();
                  final Float key = ik == TFloatShortMapDecorator.this._map.getNoEntryKey() ? null : TFloatShortMapDecorator.this.wrapKey(ik);
                  short iv = this.it.value();
                  final Short v = iv == TFloatShortMapDecorator.this._map.getNoEntryValue() ? null : TFloatShortMapDecorator.this.wrapValue(iv);
                  return new Entry<Float, Short>() {
                     private Short val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Float getKey() {
                        return key;
                     }

                     public Short getValue() {
                        return this.val;
                     }

                     @Override
                     public int hashCode() {
                        return key.hashCode() + this.val.hashCode();
                     }

                     public Short setValue(Short value) {
                        this.val = value;
                        return TFloatShortMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Float, Short> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Float key = (Float)((Entry)o).getKey();
               TFloatShortMapDecorator.this._map.remove(TFloatShortMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Float, Short>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TFloatShortMapDecorator.this.clear();
         }
      };
   }

   @Override
   public boolean containsValue(Object val) {
      return val instanceof Short && this._map.containsValue(this.unwrapValue(val));
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
   public void putAll(Map<? extends Float, ? extends Short> map) {
      Iterator<? extends Entry<? extends Float, ? extends Short>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Float, ? extends Short> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Float wrapKey(float k) {
      return k;
   }

   protected float unwrapKey(Object key) {
      return (Float)key;
   }

   protected Short wrapValue(short k) {
      return k;
   }

   protected short unwrapValue(Object value) {
      return (Short)value;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this._map = (TFloatShortMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
