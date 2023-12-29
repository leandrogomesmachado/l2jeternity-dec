package org.napile.primitive.sets.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.napile.primitive.collections.IntCollection;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.abstracts.AbstractIntSet;

public class HashIntSet extends AbstractIntSet implements IntSet, Cloneable, Serializable {
   private transient HashIntObjectMap<Object> map;
   private static final Object PRESENT = new Object();

   public HashIntSet() {
      this.map = new HashIntObjectMap<>();
   }

   public HashIntSet(IntCollection c) {
      this.map = new HashIntObjectMap<>(Math.max((int)((float)c.size() / 0.75F) + 1, 16));
      this.addAll(c);
   }

   public HashIntSet(int initialCapacity, float loadFactor) {
      this.map = new HashIntObjectMap<>(initialCapacity, loadFactor);
   }

   public HashIntSet(int initialCapacity) {
      this.map = new HashIntObjectMap<>(initialCapacity);
   }

   @Override
   public IntIterator iterator() {
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
   public boolean contains(int o) {
      return this.map.containsKey(o);
   }

   @Override
   public boolean add(int e) {
      return this.map.put(e, PRESENT) == null;
   }

   @Override
   public boolean remove(int o) {
      return this.map.remove(o) == PRESENT;
   }

   @Override
   public void clear() {
      this.map.clear();
   }

   @Override
   public Object clone() {
      try {
         HashIntSet newSet = (HashIntSet)super.clone();
         newSet.map = (HashIntObjectMap)this.map.clone();
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
      IntIterator i = this.map.keySet().iterator();

      while(i.hasNext()) {
         s.writeObject(i.next());
      }
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      int capacity = s.readInt();
      float loadFactor = s.readFloat();
      this.map = new HashIntObjectMap<>(capacity, loadFactor);
      int size = s.readInt();

      for(int i = 0; i < size; ++i) {
         int e = s.readInt();
         this.map.put(e, PRESENT);
      }
   }
}
