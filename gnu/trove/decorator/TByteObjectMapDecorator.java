package gnu.trove.decorator;

import gnu.trove.iterator.TByteObjectIterator;
import gnu.trove.map.TByteObjectMap;
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

public class TByteObjectMapDecorator<V> extends AbstractMap<Byte, V> implements Map<Byte, V>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TByteObjectMap<V> _map;

   public TByteObjectMapDecorator() {
   }

   public TByteObjectMapDecorator(TByteObjectMap<V> map) {
      this._map = map;
   }

   public TByteObjectMap<V> getMap() {
      return this._map;
   }

   public V put(Byte key, V value) {
      byte k;
      if (key == null) {
         k = this._map.getNoEntryKey();
      } else {
         k = this.unwrapKey(key);
      }

      return this._map.put(k, value);
   }

   @Override
   public V get(Object key) {
      byte k;
      if (key != null) {
         if (!(key instanceof Byte)) {
            return null;
         }

         k = this.unwrapKey((Byte)key);
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
      byte k;
      if (key != null) {
         if (!(key instanceof Byte)) {
            return null;
         }

         k = this.unwrapKey((Byte)key);
      } else {
         k = this._map.getNoEntryKey();
      }

      return this._map.remove(k);
   }

   @Override
   public Set<Entry<Byte, V>> entrySet() {
      return new AbstractSet<Entry<Byte, V>>() {
         @Override
         public int size() {
            return TByteObjectMapDecorator.this._map.size();
         }

         @Override
         public boolean isEmpty() {
            return TByteObjectMapDecorator.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Object k = ((Entry)o).getKey();
               Object v = ((Entry)o).getValue();
               return TByteObjectMapDecorator.this.containsKey(k) && TByteObjectMapDecorator.this.get(k).equals(v);
            }
         }

         @Override
         public Iterator<Entry<Byte, V>> iterator() {
            return new Iterator<Entry<Byte, V>>() {
               private final TByteObjectIterator<V> it = TByteObjectMapDecorator.this._map.iterator();

               public Entry<Byte, V> next() {
                  this.it.advance();
                  byte k = this.it.key();
                  final Byte key = k == TByteObjectMapDecorator.this._map.getNoEntryKey() ? null : TByteObjectMapDecorator.this.wrapKey(k);
                  final V v = this.it.value();
                  return new Entry<Byte, V>() {
                     private V val = v;

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof Entry && ((Entry)o).getKey().equals(key) && ((Entry)o).getValue().equals(this.val);
                     }

                     public Byte getKey() {
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
                        return (V)TByteObjectMapDecorator.this.put(key, value);
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

         public boolean add(Entry<Byte, V> o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean remove(Object o) {
            boolean modified = false;
            if (this.contains(o)) {
               Byte key = (Byte)((Entry)o).getKey();
               TByteObjectMapDecorator.this._map.remove(TByteObjectMapDecorator.this.unwrapKey(key));
               modified = true;
            }

            return modified;
         }

         @Override
         public boolean addAll(Collection<? extends Entry<Byte, V>> c) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void clear() {
            TByteObjectMapDecorator.this.clear();
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
         return key instanceof Byte && this._map.containsKey((Byte)key);
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
   public void putAll(Map<? extends Byte, ? extends V> map) {
      Iterator<? extends Entry<? extends Byte, ? extends V>> it = map.entrySet().iterator();
      int i = map.size();

      while(i-- > 0) {
         Entry<? extends Byte, ? extends V> e = it.next();
         this.put(e.getKey(), e.getValue());
      }
   }

   protected Byte wrapKey(byte k) {
      return k;
   }

   protected byte unwrapKey(Byte key) {
      return key;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this._map = (TByteObjectMap)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._map);
   }
}
