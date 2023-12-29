package org.napile.primitive.sets.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.napile.primitive.collections.LongCollection;
import org.napile.primitive.iterators.LongIterator;
import org.napile.primitive.maps.impl.HashLongObjectMap;
import org.napile.primitive.sets.LongSet;
import org.napile.primitive.sets.abstracts.AbstractLongSet;

public class HashLongSet extends AbstractLongSet implements LongSet, Cloneable, Serializable {
   private transient HashLongObjectMap<Object> map;
   private static final Object PRESENT = new Object();

   public HashLongSet() {
      this.map = new HashLongObjectMap<>();
   }

   public HashLongSet(LongCollection c) {
      this.map = new HashLongObjectMap<>(Math.max((int)((float)c.size() / 0.75F) + 1, 16));
      this.addAll(c);
   }

   public HashLongSet(int initialCapacity, float loadFactor) {
      this.map = new HashLongObjectMap<>(initialCapacity, loadFactor);
   }

   public HashLongSet(int initialCapacity) {
      this.map = new HashLongObjectMap<>(initialCapacity);
   }

   @Override
   public LongIterator iterator() {
      return this.map.keySet().iterator();
   }

   @Override
   public int size() {
      return this.map.size();
   }

   @Override
   public boolean isEmpty() {
      return this.map.isEmpty();
   }

   @Override
   public boolean contains(long o) {
      return this.map.containsKey(o);
   }

   @Override
   public boolean add(long e) {
      return this.map.put(e, PRESENT) == null;
   }

   @Override
   public boolean remove(long o) {
      return this.map.remove(o) == PRESENT;
   }

   @Override
   public void clear() {
      this.map.clear();
   }

   @Override
   public Object clone() {
      try {
         HashLongSet newSet = (HashLongSet)super.clone();
         newSet.map = (HashLongObjectMap)this.map.clone();
         return newSet;
      } catch (CloneNotSupportedException var2) {
         throw new InternalError();
      }
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      s.defaultWriteObject();
      s.writeInt(this.map.capacity());
      s.writeFloat(this.map.loadFactor());
      s.writeInt(this.map.size());
      LongIterator i = this.map.keySet().iterator();

      while(i.hasNext()) {
         s.writeLong(i.next());
      }
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      int capacity = s.readInt();
      float loadFactor = s.readFloat();
      this.map = new HashLongObjectMap<>(capacity, loadFactor);
      int size = s.readInt();

      for(int i = 0; i < size; ++i) {
         long e = s.readLong();
         this.map.put(e, PRESENT);
      }
   }
}
