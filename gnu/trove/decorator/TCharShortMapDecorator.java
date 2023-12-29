package gnu.trove.decorator;

import gnu.trove.iterator.TCharShortIterator;
import gnu.trove.map.TCharShortMap;
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

public class TCharShortMapDecorator extends AbstractMap<Character, Short> implements Map<Character, Short>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TCharShortMap _map;

   public TCharShortMapDecorator() {
   }

   public TCharShortMapDecorator(TCharShortMap map) {
      this._map = map;
   }

   public TCharShortMap getMap() {
      return this._map;
   }

   public Short put(Character key, Short value) {
      char k;
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
      char k;
      if (key != null) {
         if (!(key instanceof Character)) {
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
      char k;
      if (key != null) {
         if (!(key instanceof Character)) {
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
   public Set<Entry<Character, Short>> entrySet() {
      return new AbstractSet<Entry<Character, Short>>() {
         @Override
         public int size() {
            return TCharShortMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TCharShortMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TCharShortMapDecorator.this.containsKey(k) && TCharShortMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Character, Short>> iterator() {
            return new Iterator<Entry<Character, Short>>() {
               private final TCharShortIterator it = TCharShortMapDecorator.this._map.iterator();

               public Entry<Character, Short> next() {
                  this.it.advance();
                  char ik = this.it.key();
                  final Character key = ik == TCharShortMapDecorator.this._map.getNoEntryKey() ? null : TCharShortMapDecorator.this.wrapKey(ik);
                  short iv = this.it.value();
                  final Short v = iv == TCharShortMapDecorator.this._map.getNoEntryValue() ? null : TCharShortMapDecorator.this.wrapValue(iv);
                  return new Entry<Character, Short>() {
                     private Short val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Character getKey() {
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
                        return TCharShortMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Character, Short> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Character key = (Character)((Entry)o).getKey();
               TCharShortMapDecorator.this._map.remove(TCharShortMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Character, Short>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TCharShortMapDecorator.this.clear();
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
   public void putAll(Map<? extends Character, ? extends Short> map) {
      Iterator<? extends Entry<? extends Character, ? extends Short>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Character, ? extends Short> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Character wrapKey(char k) {
      return k;
   }

   protected char unwrapKey(Object key) {
      return (Character)key;
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
      this._map = (TCharShortMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
