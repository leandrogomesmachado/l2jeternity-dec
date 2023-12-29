package gnu.trove.decorator;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.TObjectIntMap;
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

public class TObjectIntMapDecorator<K> extends AbstractMap<K, Integer> implements Map<K, Integer>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TObjectIntMap<K> _map;

   public TObjectIntMapDecorator() {
   }

   public TObjectIntMapDecorator(TObjectIntMap<K> map) {
      this._map = map;
   }

   public TObjectIntMap<K> getMap() {
      return this._map;
   }

   public Integer put(K key, Integer value) {
      return value == null ? this.wrapValue(this._map.put(key, this._map.getNoEntryValue())) : this.wrapValue(this._map.put(key, this.unwrapValue(value)));
   }

   public Integer get(Object key) {
      int v = this._map.get(key);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public void clear() {
      this._map.clear();
   }

   public Integer remove(Object key) {
      int v = this._map.remove(key);
      return v == this._map.getNoEntryValue() ? null : this.wrapValue(v);
   }

   @Override
   public Set<Entry<K, Integer>> entrySet() {
      return new AbstractSet<Entry<K, Integer>>() {
         @Override
         public int size() {
            return TObjectIntMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TObjectIntMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TObjectIntMapDecorator.this.containsKey(k) && TObjectIntMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<K, Integer>> iterator() {
            return new Iterator<Entry<K, Integer>>() {
               private final TObjectIntIterator<K> it = TObjectIntMapDecorator.this._map.iterator();

               public Entry<K, Integer> next() {
                  this.it.advance();
                  final K key = this.it.key();
                  final Integer v = TObjectIntMapDecorator.this.wrapValue(this.it.value());
                  return new Entry<K, Integer>() {
                     private Integer val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     @Override
                     public K getKey() {
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
                        return TObjectIntMapDecorator.this.put(key, value);
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

         public boolean add(Entry<K, Integer> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               K key = (K)((Entry)o).getKey();
               TObjectIntMapDecorator.this._map.remove(key);
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<K, Integer>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TObjectIntMapDecorator.this.clear();
         }
      };
   }

   @Override
   public boolean containsValue(Object val) {
      return val instanceof Integer && this._map.containsValue(this.unwrapValue(val));
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
   public void putAll(Map<? extends K, ? extends Integer> map) {
      Iterator<? extends Entry<? extends K, ? extends Integer>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends K, ? extends Integer> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
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
      this._map = (TObjectIntMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
