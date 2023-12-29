package gnu.trove.decorator;

import gnu.trove.iterator.TShortCharIterator;
import gnu.trove.map.TShortCharMap;
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

public class TShortCharMapDecorator extends AbstractMap<Short, Character> implements Map<Short, Character>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TShortCharMap _map;

   public TShortCharMapDecorator() {
   }

   public TShortCharMapDecorator(TShortCharMap map) {
      this._map = map;
   }

   public TShortCharMap getMap() {
      return this._map;
   }

   public Character put(Short key, Character value) {
      short k;
      if (key == null) {
         k = this._map.getNoEntryKey();
      } else {
         k = this.unwrapKey(key);
      }

      char v;
      if (value == null) {
         v = this._map.getNoEntryValue();
      } else {
         v = this.unwrapValue(value);
      }

      char retval = this._map.put(k, v);
      return retval == this._map.getNoEntryValue() ? null : this.wrapValue(retval);
   }

   public Character get(Object key) {
      short k;
      if (key != null) {
         if (!(key instanceof Short)) {
            return null;
         }

         k = this.unwrapKey(key);
      } else {
         k = this._map.getNoEntryKey();
      }

      char v = this._map.get(k);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public void clear() {
      this._map.clear();
   }

   public Character remove(Object key) {
      short k;
      if (key != null) {
         if (!(key instanceof Short)) {
            return null;
         }

         k = this.unwrapKey(key);
      } else {
         k = this._map.getNoEntryKey();
      }

      char v = this._map.remove(k);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public Set<Entry<Short, Character>> entrySet() {
      return new AbstractSet<Entry<Short, Character>>() {
         @Override
         public int size() {
            return TShortCharMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TShortCharMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TShortCharMapDecorator.this.containsKey(k) && TShortCharMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Short, Character>> iterator() {
            return new Iterator<Entry<Short, Character>>() {
               private final TShortCharIterator it = TShortCharMapDecorator.this._map.iterator();

               public Entry<Short, Character> next() {
                  this.it.advance();
                  short ik = this.it.key();
                  final Short key = ik == TShortCharMapDecorator.this._map.getNoEntryKey() ? null : TShortCharMapDecorator.this.wrapKey(ik);
                  char iv = this.it.value();
                  final Character v = iv == TShortCharMapDecorator.this._map.getNoEntryValue() ? null : TShortCharMapDecorator.this.wrapValue(iv);
                  return new Entry<Short, Character>() {
                     private Character val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Short getKey() {
                        return key;
                     }

                     public Character getValue() {
                        return this.val;
                     }

                     @Override
                     public int hashCode() {
                        return key.hashCode() + this.val.hashCode();
                     }

                     public Character setValue(Character value) {
                        this.val = value;
                        return TShortCharMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Short, Character> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Short key = (Short)((Entry)o).getKey();
               TShortCharMapDecorator.this._map.remove(TShortCharMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Short, Character>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TShortCharMapDecorator.this.clear();
         }
      };
   }

   @Override
   public boolean containsValue(Object val) {
      return val instanceof Character && this._map.containsValue(this.unwrapValue(val));
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
   public void putAll(Map<? extends Short, ? extends Character> map) {
      Iterator<? extends Entry<? extends Short, ? extends Character>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Short, ? extends Character> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Short wrapKey(short k) {
      return k;
   }

   protected short unwrapKey(Object key) {
      return (Short)key;
   }

   protected Character wrapValue(char k) {
      return k;
   }

   protected char unwrapValue(Object value) {
      return (Character)value;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this._map = (TShortCharMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
