package gnu.trove.decorator;

import gnu.trove.iterator.TByteShortIterator;
import gnu.trove.map.TByteShortMap;
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

public class TByteShortMapDecorator extends AbstractMap<Byte, Short> implements Map<Byte, Short>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TByteShortMap _map;

   public TByteShortMapDecorator() {
   }

   public TByteShortMapDecorator(TByteShortMap map) {
      this._map = map;
   }

   public TByteShortMap getMap() {
      return this._map;
   }

   public Short put(Byte key, Short value) {
      byte k;
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
      byte k;
      if (key != null) {
         if (!(key instanceof Byte)) {
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
      byte k;
      if (key != null) {
         if (!(key instanceof Byte)) {
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
   public Set<Entry<Byte, Short>> entrySet() {
      return new AbstractSet<Entry<Byte, Short>>() {
         @Override
         public int size() {
            return TByteShortMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TByteShortMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TByteShortMapDecorator.this.containsKey(k) && TByteShortMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Byte, Short>> iterator() {
            return new Iterator<Entry<Byte, Short>>() {
               private final TByteShortIterator it = TByteShortMapDecorator.this._map.iterator();

               public Entry<Byte, Short> next() {
                  this.it.advance();
                  byte ik = this.it.key();
                  final Byte key = ik == TByteShortMapDecorator.this._map.getNoEntryKey() ? null : TByteShortMapDecorator.this.wrapKey(ik);
                  short iv = this.it.value();
                  final Short v = iv == TByteShortMapDecorator.this._map.getNoEntryValue() ? null : TByteShortMapDecorator.this.wrapValue(iv);
                  return new Entry<Byte, Short>() {
                     private Short val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Byte getKey() {
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
                        return TByteShortMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Byte, Short> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Byte key = (Byte)((Entry)o).getKey();
               TByteShortMapDecorator.this._map.remove(TByteShortMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Byte, Short>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TByteShortMapDecorator.this.clear();
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
   public void putAll(Map<? extends Byte, ? extends Short> map) {
      Iterator<? extends Entry<? extends Byte, ? extends Short>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Byte, ? extends Short> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Byte wrapKey(byte k) {
      return k;
   }

   protected byte unwrapKey(Object key) {
      return (Byte)key;
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
      this._map = (TByteShortMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
