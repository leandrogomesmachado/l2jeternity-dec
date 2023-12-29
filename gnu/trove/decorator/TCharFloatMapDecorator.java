package gnu.trove.decorator;

import gnu.trove.iterator.TCharFloatIterator;
import gnu.trove.map.TCharFloatMap;
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

public class TCharFloatMapDecorator extends AbstractMap<Character, Float> implements Map<Character, Float>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TCharFloatMap _map;

   public TCharFloatMapDecorator() {
   }

   public TCharFloatMapDecorator(TCharFloatMap map) {
      this._map = map;
   }

   public TCharFloatMap getMap() {
      return this._map;
   }

   public Float put(Character key, Float value) {
      char k;
      if (key == null) {
         k = this._map.getNoEntryKey();
      } else {
         k = this.unwrapKey(key);
      }

      float v;
      if (value == null) {
         v = this._map.getNoEntryValue();
      } else {
         v = this.unwrapValue(value);
      }

      float retval = this._map.put(k, v);
      return retval == this._map.getNoEntryValue() ? null : this.wrapValue(retval);
   }

   public Float get(Object key) {
      char k;
      if (key != null) {
         if (!(key instanceof Character)) {
            return null;
         }

         k = this.unwrapKey(key);
      } else {
         k = this._map.getNoEntryKey();
      }

      float v = this._map.get(k);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public void clear() {
      this._map.clear();
   }

   public Float remove(Object key) {
      char k;
      if (key != null) {
         if (!(key instanceof Character)) {
            return null;
         }

         k = this.unwrapKey(key);
      } else {
         k = this._map.getNoEntryKey();
      }

      float v = this._map.remove(k);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public Set<Entry<Character, Float>> entrySet() {
      return new AbstractSet<Entry<Character, Float>>() {
         @Override
         public int size() {
            return TCharFloatMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TCharFloatMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TCharFloatMapDecorator.this.containsKey(k) && TCharFloatMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Character, Float>> iterator() {
            return new Iterator<Entry<Character, Float>>() {
               private final TCharFloatIterator it = TCharFloatMapDecorator.this._map.iterator();

               public Entry<Character, Float> next() {
                  this.it.advance();
                  char ik = this.it.key();
                  final Character key = ik == TCharFloatMapDecorator.this._map.getNoEntryKey() ? null : TCharFloatMapDecorator.this.wrapKey(ik);
                  float iv = this.it.value();
                  final Float v = iv == TCharFloatMapDecorator.this._map.getNoEntryValue() ? null : TCharFloatMapDecorator.this.wrapValue(iv);
                  return new Entry<Character, Float>() {
                     private Float val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Character getKey() {
                        return key;
                     }

                     public Float getValue() {
                        return this.val;
                     }

                     @Override
                     public int hashCode() {
                        return key.hashCode() + this.val.hashCode();
                     }

                     public Float setValue(Float value) {
                        this.val = value;
                        return TCharFloatMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Character, Float> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Character key = (Character)((Entry)o).getKey();
               TCharFloatMapDecorator.this._map.remove(TCharFloatMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Character, Float>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TCharFloatMapDecorator.this.clear();
         }
      };
   }

   @Override
   public boolean containsValue(Object val) {
      return val instanceof Float && this._map.containsValue(this.unwrapValue(val));
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
   public void putAll(Map<? extends Character, ? extends Float> map) {
      Iterator<? extends Entry<? extends Character, ? extends Float>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Character, ? extends Float> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Character wrapKey(char k) {
      return k;
   }

   protected char unwrapKey(Object key) {
      return (Character)key;
   }

   protected Float wrapValue(float k) {
      return k;
   }

   protected float unwrapValue(Object value) {
      return (Float)value;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this._map = (TCharFloatMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
