package gnu.trove.decorator;

import gnu.trove.iterator.TCharIntIterator;
import gnu.trove.map.TCharIntMap;
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

public class TCharIntMapDecorator extends AbstractMap<Character, Integer> implements Map<Character, Integer>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TCharIntMap _map;

   public TCharIntMapDecorator() {
   }

   public TCharIntMapDecorator(TCharIntMap map) {
      this._map = map;
   }

   public TCharIntMap getMap() {
      return this._map;
   }

   public Integer put(Character key, Integer value) {
      char k;
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
      char k;
      if (key != null) {
         if (!(key instanceof Character)) {
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
      char k;
      if (key != null) {
         if (!(key instanceof Character)) {
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
   public Set<Entry<Character, Integer>> entrySet() {
      return new AbstractSet<Entry<Character, Integer>>() {
         @Override
         public int size() {
            return TCharIntMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TCharIntMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TCharIntMapDecorator.this.containsKey(k) && TCharIntMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Character, Integer>> iterator() {
            return new Iterator<Entry<Character, Integer>>() {
               private final TCharIntIterator it = TCharIntMapDecorator.this._map.iterator();

               public Entry<Character, Integer> next() {
                  this.it.advance();
                  char ik = this.it.key();
                  final Character key = ik == TCharIntMapDecorator.this._map.getNoEntryKey() ? null : TCharIntMapDecorator.this.wrapKey(ik);
                  int iv = this.it.value();
                  final Integer v = iv == TCharIntMapDecorator.this._map.getNoEntryValue() ? null : TCharIntMapDecorator.this.wrapValue(iv);
                  return new Entry<Character, Integer>() {
                     private Integer val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Character getKey() {
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
                        return TCharIntMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Character, Integer> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Character key = (Character)((Entry)o).getKey();
               TCharIntMapDecorator.this._map.remove(TCharIntMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Character, Integer>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TCharIntMapDecorator.this.clear();
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
   public void putAll(Map<? extends Character, ? extends Integer> map) {
      Iterator<? extends Entry<? extends Character, ? extends Integer>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Character, ? extends Integer> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Character wrapKey(char k) {
      return k;
   }

   protected char unwrapKey(Object key) {
      return (Character)key;
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
      this._map = (TCharIntMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
