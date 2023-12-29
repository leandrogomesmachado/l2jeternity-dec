package gnu.trove.decorator;

import gnu.trove.iterator.TShortByteIterator;
import gnu.trove.map.TShortByteMap;
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

public class TShortByteMapDecorator extends AbstractMap<Short, Byte> implements Map<Short, Byte>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TShortByteMap _map;

   public TShortByteMapDecorator() {
   }

   public TShortByteMapDecorator(TShortByteMap map) {
      this._map = map;
   }

   public TShortByteMap getMap() {
      return this._map;
   }

   public Byte put(Short key, Byte value) {
      short k;
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
      short k;
      if (key != null) {
         if (!(key instanceof Short)) {
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
      short k;
      if (key != null) {
         if (!(key instanceof Short)) {
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
   public Set<Entry<Short, Byte>> entrySet() {
      return new AbstractSet<Entry<Short, Byte>>() {
         @Override
         public int size() {
            return TShortByteMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TShortByteMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TShortByteMapDecorator.this.containsKey(k) && TShortByteMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Short, Byte>> iterator() {
            return new Iterator<Entry<Short, Byte>>() {
               private final TShortByteIterator it = TShortByteMapDecorator.this._map.iterator();

               public Entry<Short, Byte> next() {
                  this.it.advance();
                  short ik = this.it.key();
                  final Short key = ik == TShortByteMapDecorator.this._map.getNoEntryKey() ? null : TShortByteMapDecorator.this.wrapKey(ik);
                  byte iv = this.it.value();
                  final Byte v = iv == TShortByteMapDecorator.this._map.getNoEntryValue() ? null : TShortByteMapDecorator.this.wrapValue(iv);
                  return new Entry<Short, Byte>() {
                     private Byte val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Short getKey() {
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
                        return TShortByteMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Short, Byte> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Short key = (Short)((Entry)o).getKey();
               TShortByteMapDecorator.this._map.remove(TShortByteMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Short, Byte>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TShortByteMapDecorator.this.clear();
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
   public void putAll(Map<? extends Short, ? extends Byte> map) {
      Iterator<? extends Entry<? extends Short, ? extends Byte>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Short, ? extends Byte> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Short wrapKey(short k) {
      return k;
   }

   protected short unwrapKey(Object key) {
      return (Short)key;
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
      this._map = (TShortByteMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
