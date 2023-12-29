package gnu.trove.decorator;

import gnu.trove.iterator.TByteIntIterator;
import gnu.trove.map.TByteIntMap;
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

public class TByteIntMapDecorator extends AbstractMap<Byte, Integer> implements Map<Byte, Integer>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TByteIntMap _map;

   public TByteIntMapDecorator() {
   }

   public TByteIntMapDecorator(TByteIntMap map) {
      this._map = map;
   }

   public TByteIntMap getMap() {
      return this._map;
   }

   public Integer put(Byte key, Integer value) {
      byte k;
      if (key == null) {
         k = this._map.getNoEntryKey();
      } else {
         k = this.unwrapKey(key);
      }

      int v;
      if (value == null) {
         v = this._map.getNoEntryValue();
      } else {
         v = this.unwrapValue(value);
      }

      int retval = this._map.put(k, v);
      return retval == this._map.getNoEntryValue() ? null : this.wrapValue(retval);
   }

   public Integer get(Object key) {
      byte k;
      if (key != null) {
         if (!(key instanceof Byte)) {
            return null;
         }

         k = this.unwrapKey(key);
      } else {
         k = this._map.getNoEntryKey();
      }

      int v = this._map.get(k);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public void clear() {
      this._map.clear();
   }

   public Integer remove(Object key) {
      byte k;
      if (key != null) {
         if (!(key instanceof Byte)) {
            return null;
         }

         k = this.unwrapKey(key);
      } else {
         k = this._map.getNoEntryKey();
      }

      int v = this._map.remove(k);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public Set<Entry<Byte, Integer>> entrySet() {
      return new AbstractSet<Entry<Byte, Integer>>() {
         @Override
         public int size() {
            return TByteIntMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TByteIntMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TByteIntMapDecorator.this.containsKey(k) && TByteIntMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Byte, Integer>> iterator() {
            return new Iterator<Entry<Byte, Integer>>() {
               private final TByteIntIterator it = TByteIntMapDecorator.this._map.iterator();

               public Entry<Byte, Integer> next() {
                  this.it.advance();
                  byte ik = this.it.key();
                  final Byte key = ik == TByteIntMapDecorator.this._map.getNoEntryKey() ? null : TByteIntMapDecorator.this.wrapKey(ik);
                  int iv = this.it.value();
                  final Integer v = iv == TByteIntMapDecorator.this._map.getNoEntryValue() ? null : TByteIntMapDecorator.this.wrapValue(iv);
                  return new Entry<Byte, Integer>() {
                     private Integer val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Byte getKey() {
                        return key;
                     }

                     public Integer getValue() {
                        return this.val;
                     }

                     @Override
                     public int hashCode() {
                        return key.hashCode() + this.val.hashCode();
                     }

                     public Integer setValue(Integer value) {
                        this.val = value;
                        return TByteIntMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Byte, Integer> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Byte key = (Byte)((Entry)o).getKey();
               TByteIntMapDecorator.this._map.remove(TByteIntMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Byte, Integer>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TByteIntMapDecorator.this.clear();
         }
      };
   }

   @Override
   public boolean containsValue(Object val) {
      return val instanceof Integer && this._map.containsValue(this.unwrapValue(val));
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
   public void putAll(Map<? extends Byte, ? extends Integer> map) {
      Iterator<? extends Entry<? extends Byte, ? extends Integer>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Byte, ? extends Integer> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Byte wrapKey(byte k) {
      return k;
   }

   protected byte unwrapKey(Object key) {
      return (Byte)key;
   }

   protected Integer wrapValue(int k) {
      return k;
   }

   protected int unwrapValue(Object value) {
      return (Integer)value;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this._map = (TByteIntMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
