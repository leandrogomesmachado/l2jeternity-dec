package gnu.trove.decorator;

import gnu.trove.iterator.TCharByteIterator;
import gnu.trove.map.TCharByteMap;
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

public class TCharByteMapDecorator extends AbstractMap<Character, Byte> implements Map<Character, Byte>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TCharByteMap _map;

   public TCharByteMapDecorator() {
   }

   public TCharByteMapDecorator(TCharByteMap map) {
      this._map = map;
   }

   public TCharByteMap getMap() {
      return this._map;
   }

   public Byte put(Character key, Byte value) {
      char k;
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
      char k;
      if (key != null) {
         if (!(key instanceof Character)) {
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
      char k;
      if (key != null) {
         if (!(key instanceof Character)) {
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
   public Set<Entry<Character, Byte>> entrySet() {
      return new AbstractSet<Entry<Character, Byte>>() {
         @Override
         public int size() {
            return TCharByteMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TCharByteMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TCharByteMapDecorator.this.containsKey(k) && TCharByteMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Character, Byte>> iterator() {
            return new Iterator<Entry<Character, Byte>>() {
               private final TCharByteIterator it = TCharByteMapDecorator.this._map.iterator();

               public Entry<Character, Byte> next() {
                  this.it.advance();
                  char ik = this.it.key();
                  final Character key = ik == TCharByteMapDecorator.this._map.getNoEntryKey() ? null : TCharByteMapDecorator.this.wrapKey(ik);
                  byte iv = this.it.value();
                  final Byte v = iv == TCharByteMapDecorator.this._map.getNoEntryValue() ? null : TCharByteMapDecorator.this.wrapValue(iv);
                  return new Entry<Character, Byte>() {
                     private Byte val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Character getKey() {
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
                        return TCharByteMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Character, Byte> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Character key = (Character)((Entry)o).getKey();
               TCharByteMapDecorator.this._map.remove(TCharByteMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Character, Byte>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TCharByteMapDecorator.this.clear();
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
         return key instanceof Character && this._map.containsKey(this.unwrapKey(key));
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
   public void putAll(Map<? extends Character, ? extends Byte> map) {
      Iterator<? extends Entry<? extends Character, ? extends Byte>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Character, ? extends Byte> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Character wrapKey(char k) {
      return k;
   }

   protected char unwrapKey(Object key) {
      return (Character)key;
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
      this._map = (TCharByteMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
