package gnu.trove.decorator;

import gnu.trove.iterator.TIntCharIterator;
import gnu.trove.map.TIntCharMap;
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

public class TIntCharMapDecorator extends AbstractMap<Integer, Character> implements Map<Integer, Character>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TIntCharMap _map;

   public TIntCharMapDecorator() {
   }

   public TIntCharMapDecorator(TIntCharMap map) {
      this._map = map;
   }

   public TIntCharMap getMap() {
      return this._map;
   }

   public Character put(Integer key, Character value) {
      int k;
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
      int k;
      if (key != null) {
         if (!(key instanceof Integer)) {
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
      int k;
      if (key != null) {
         if (!(key instanceof Integer)) {
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
   public Set<Entry<Integer, Character>> entrySet() {
      return new AbstractSet<Entry<Integer, Character>>() {
         @Override
         public int size() {
            return TIntCharMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TIntCharMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TIntCharMapDecorator.this.containsKey(k) && TIntCharMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Integer, Character>> iterator() {
            return new Iterator<Entry<Integer, Character>>() {
               private final TIntCharIterator it = TIntCharMapDecorator.this._map.iterator();

               public Entry<Integer, Character> next() {
                  this.it.advance();
                  int ik = this.it.key();
                  final Integer key = ik == TIntCharMapDecorator.this._map.getNoEntryKey() ? null : TIntCharMapDecorator.this.wrapKey(ik);
                  char iv = this.it.value();
                  final Character v = iv == TIntCharMapDecorator.this._map.getNoEntryValue() ? null : TIntCharMapDecorator.this.wrapValue(iv);
                  return new Entry<Integer, Character>() {
                     private Character val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Integer getKey() {
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
                        return TIntCharMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Integer, Character> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Integer key = (Integer)((Entry)o).getKey();
               TIntCharMapDecorator.this._map.remove(TIntCharMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Integer, Character>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TIntCharMapDecorator.this.clear();
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
   public void putAll(Map<? extends Integer, ? extends Character> map) {
      Iterator<? extends Entry<? extends Integer, ? extends Character>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Integer, ? extends Character> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Integer wrapKey(int k) {
      return k;
   }

   protected int unwrapKey(Object key) {
      return (Integer)key;
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
      this._map = (TIntCharMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
