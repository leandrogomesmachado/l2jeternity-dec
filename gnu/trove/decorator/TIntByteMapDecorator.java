package gnu.trove.decorator;

import gnu.trove.iterator.TIntByteIterator;
import gnu.trove.map.TIntByteMap;
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

public class TIntByteMapDecorator extends AbstractMap<Integer, Byte> implements Map<Integer, Byte>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TIntByteMap _map;

   public TIntByteMapDecorator() {
   }

   public TIntByteMapDecorator(TIntByteMap map) {
      this._map = map;
   }

   public TIntByteMap getMap() {
      return this._map;
   }

   public Byte put(Integer key, Byte value) {
      int k;
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
      int k;
      if (key != null) {
         if (!(key instanceof Integer)) {
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
      int k;
      if (key != null) {
         if (!(key instanceof Integer)) {
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
   public Set<Entry<Integer, Byte>> entrySet() {
      return new AbstractSet<Entry<Integer, Byte>>() {
         @Override
         public int size() {
            return TIntByteMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TIntByteMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TIntByteMapDecorator.this.containsKey(k) && TIntByteMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Integer, Byte>> iterator() {
            return new Iterator<Entry<Integer, Byte>>() {
               private final TIntByteIterator it = TIntByteMapDecorator.this._map.iterator();

               public Entry<Integer, Byte> next() {
                  this.it.advance();
                  int ik = this.it.key();
                  final Integer key = ik == TIntByteMapDecorator.this._map.getNoEntryKey() ? null : TIntByteMapDecorator.this.wrapKey(ik);
                  byte iv = this.it.value();
                  final Byte v = iv == TIntByteMapDecorator.this._map.getNoEntryValue() ? null : TIntByteMapDecorator.this.wrapValue(iv);
                  return new Entry<Integer, Byte>() {
                     private Byte val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Integer getKey() {
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
                        return TIntByteMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Integer, Byte> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Integer key = (Integer)((Entry)o).getKey();
               TIntByteMapDecorator.this._map.remove(TIntByteMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Integer, Byte>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TIntByteMapDecorator.this.clear();
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
   public void putAll(Map<? extends Integer, ? extends Byte> map) {
      Iterator<? extends Entry<? extends Integer, ? extends Byte>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Integer, ? extends Byte> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Integer wrapKey(int k) {
      return k;
   }

   protected int unwrapKey(Object key) {
      return (Integer)key;
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
      this._map = (TIntByteMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
