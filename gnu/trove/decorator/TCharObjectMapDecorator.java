package gnu.trove.decorator;

import gnu.trove.iterator.TCharObjectIterator;
import gnu.trove.map.TCharObjectMap;
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

public class TCharObjectMapDecorator<V> extends AbstractMap<Character, V> implements Map<Character, V>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TCharObjectMap<V> _map;

   public TCharObjectMapDecorator() {
   }

   public TCharObjectMapDecorator(TCharObjectMap<V> map) {
      this._map = map;
   }

   public TCharObjectMap<V> getMap() {
      return this._map;
   }

   public V put(Character key, V value) {
      char k;
      if (key == null) {
         k = this._map.getNoEntryKey();
      } else {
         k = this.unwrapKey(key);
      }

      return this._map.put(k, value);
   }

   @Override
   public V get(Object key) {
      char k;
      if (key != null) {
         if (!(key instanceof Character)) {
            return null;
         }

         k = this.unwrapKey((Character)key);
      } else {
         k = this._map.getNoEntryKey();
      }

      return this._map.get(k);
   }

   @Override
   public void clear() {
      this._map.clear();
   }

   @Override
   public V remove(Object key) {
      char k;
      if (key != null) {
         if (!(key instanceof Character)) {
            return null;
         }

         k = this.unwrapKey((Character)key);
      } else {
         k = this._map.getNoEntryKey();
      }

      return this._map.remove(k);
   }

   @Override
   public Set<Entry<Character, V>> entrySet() {
      return new AbstractSet<Entry<Character, V>>() {
         @Override
         public int size() {
            return TCharObjectMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TCharObjectMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TCharObjectMapDecorator.this.containsKey(k) && TCharObjectMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Character, V>> iterator() {
            return new Iterator<Entry<Character, V>>() {
               private final TCharObjectIterator<V> it = TCharObjectMapDecorator.this._map.iterator();

               public Entry<Character, V> next() {
                  this.it.advance();
                  char k = this.it.key();
                  final Character key = k == TCharObjectMapDecorator.this._map.getNoEntryKey() ? null : TCharObjectMapDecorator.this.wrapKey(k);
                  final V v = this.it.value();
                  return new Entry<Character, V>() {
                     private V val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Character getKey() {
                        return key;
                     }

                     @Override
                     public V getValue() {
                        return this.val;
                     }

                     @Override
                     public int hashCode() {
                        return key.hashCode() + this.val.hashCode();
                     }

                     @Override
                     public V setValue(V value) {
                        this.val = value;
                        return (V)TCharObjectMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Character, V> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Character key = (Character)((Entry)o).getKey();
               TCharObjectMapDecorator.this._map.remove(TCharObjectMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Character, V>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TCharObjectMapDecorator.this.clear();
         }
      };
   }

   @Override
   public boolean containsValue(Object val) {
      return this._map.containsValue(val);
   }

   @Override
   public boolean containsKey(Object key) {
      if (key == null) {
         return this._map.containsKey(this._map.getNoEntryKey());
      } else {
         return key instanceof Character && this._map.containsKey((Character)key);
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
   public void putAll(Map<? extends Character, ? extends V> map) {
      Iterator<? extends Entry<? extends Character, ? extends V>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Character, ? extends V> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Character wrapKey(char k) {
      return k;
   }

   protected char unwrapKey(Character key) {
      return key;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this._map = (TCharObjectMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
