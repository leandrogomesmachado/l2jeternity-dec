package gnu.trove.decorator;

import gnu.trove.iterator.TCharLongIterator;
import gnu.trove.map.TCharLongMap;
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

public class TCharLongMapDecorator extends AbstractMap<Character, Long> implements Map<Character, Long>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TCharLongMap _map;

   public TCharLongMapDecorator() {
   }

   public TCharLongMapDecorator(TCharLongMap map) {
      this._map = map;
   }

   public TCharLongMap getMap() {
      return this._map;
   }

   public Long put(Character key, Long value) {
      char k;
      if (key == null) {
         k = this._map.getNoEntryKey();
      } else {
         k = this.unwrapKey(key);
      }

      long v;
      if (value == null) {
         v = this._map.getNoEntryValue();
      } else {
         v = this.unwrapValue(value);
      }

      long retval = this._map.put(k, v);
      return retval == this._map.getNoEntryValue() ? null : this.wrapValue(retval);
   }

   public Long get(Object key) {
      char k;
      if (key != null) {
         if (!(key instanceof Character)) {
            return null;
         }

         k = this.unwrapKey(key);
      } else {
         k = this._map.getNoEntryKey();
      }

      long v = this._map.get(k);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public void clear() {
      this._map.clear();
   }

   public Long remove(Object key) {
      char k;
      if (key != null) {
         if (!(key instanceof Character)) {
            return null;
         }

         k = this.unwrapKey(key);
      } else {
         k = this._map.getNoEntryKey();
      }

      long v = this._map.remove(k);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public Set<Entry<Character, Long>> entrySet() {
      return new AbstractSet<Entry<Character, Long>>() {
         @Override
         public int size() {
            return TCharLongMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TCharLongMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TCharLongMapDecorator.this.containsKey(k) && TCharLongMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Character, Long>> iterator() {
            return new Iterator<Entry<Character, Long>>() {
               private final TCharLongIterator it = TCharLongMapDecorator.this._map.iterator();

               public Entry<Character, Long> next() {
                  this.it.advance();
                  char ik = this.it.key();
                  final Character key = ik == TCharLongMapDecorator.this._map.getNoEntryKey() ? null : TCharLongMapDecorator.this.wrapKey(ik);
                  long iv = this.it.value();
                  final Long v = iv == TCharLongMapDecorator.this._map.getNoEntryValue() ? null : TCharLongMapDecorator.this.wrapValue(iv);
                  return new Entry<Character, Long>() {
                     private Long val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Character getKey() {
                        return key;
                     }

                     public Long getValue() {
                        return this.val;
                     }

                     @Override
                     public int hashCode() {
                        return key.hashCode() + this.val.hashCode();
                     }

                     public Long setValue(Long value) {
                        this.val = value;
                        return TCharLongMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Character, Long> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Character key = (Character)((Entry)o).getKey();
               TCharLongMapDecorator.this._map.remove(TCharLongMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Character, Long>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TCharLongMapDecorator.this.clear();
         }
      };
   }

   @Override
   public boolean containsValue(Object val) {
      return val instanceof Long && this._map.containsValue(this.unwrapValue(val));
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
   public void putAll(Map<? extends Character, ? extends Long> map) {
      Iterator<? extends Entry<? extends Character, ? extends Long>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Character, ? extends Long> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Character wrapKey(char k) {
      return k;
   }

   protected char unwrapKey(Object key) {
      return (Character)key;
   }

   protected Long wrapValue(long k) {
      return k;
   }

   protected long unwrapValue(Object value) {
      return (Long)value;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this._map = (TCharLongMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
