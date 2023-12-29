package gnu.trove.decorator;

import gnu.trove.iterator.TObjectShortIterator;
import gnu.trove.map.TObjectShortMap;
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

public class TObjectShortMapDecorator<K> extends AbstractMap<K, Short> implements Map<K, Short>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TObjectShortMap<K> _map;

   public TObjectShortMapDecorator() {
   }

   public TObjectShortMapDecorator(TObjectShortMap<K> map) {
      this._map = map;
   }

   public TObjectShortMap<K> getMap() {
      return this._map;
   }

   public Short put(K key, Short value) {
      return value == null ? this.wrapValue(this._map.put(key, this._map.getNoEntryValue())) : this.wrapValue(this._map.put(key, this.unwrapValue(value)));
   }

   public Short get(Object key) {
      short v = this._map.get(key);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public void clear() {
      this._map.clear();
   }

   public Short remove(Object key) {
      short v = this._map.remove(key);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public Set<Entry<K, Short>> entrySet() {
      return new AbstractSet<Entry<K, Short>>() {
         @Override
         public int size() {
            return TObjectShortMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TObjectShortMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TObjectShortMapDecorator.this.containsKey(k) && TObjectShortMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<K, Short>> iterator() {
            return new Iterator<Entry<K, Short>>() {
               private final TObjectShortIterator<K> it = TObjectShortMapDecorator.this._map.iterator();

               public Entry<K, Short> next() {
                  this.it.advance();
                  final K key = this.it.key();
                  final Short v = TObjectShortMapDecorator.this.wrapValue(this.it.value());
                  return new Entry<K, Short>() {
                     private Short val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     @Override
                     public K getKey() {
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
                        return TObjectShortMapDecorator.this.put(key, value);
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

         public boolean add(Entry<K, Short> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               K key = (K)((Entry)o).getKey();
               TObjectShortMapDecorator.this._map.remove(key);
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<K, Short>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TObjectShortMapDecorator.this.clear();
         }
      };
   }

   @Override
   public boolean containsValue(Object val) {
      return val instanceof Short && this._map.containsValue(this.unwrapValue(val));
   }

   @Override
   public boolean containsKey(Object key) {
      return this._map.containsKey(key);
   }

   @Override
   public int size() {
      return this._map.size();
   }

   @Override
   public boolean isEmpty() {
      return this._map.size() == 0;
   }

   @Override
   public void putAll(Map<? extends K, ? extends Short> map) {
      Iterator<? extends Entry<? extends K, ? extends Short>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends K, ? extends Short> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
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
      this._map = (TObjectShortMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
