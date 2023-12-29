package gnu.trove.impl.sync;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

class SynchronizedCollection<E> implements Collection<E>, Serializable {
   private static final long serialVersionUID = 3053995032091335093L;
   final Collection<E> c;
   final Object mutex;

   SynchronizedCollection(Collection<E> c, Object mutex) {
      this.c = c;
      this.mutex = mutex;
   }

   @Override
   public int size() {
      synchronized(this.mutex) {
         return this.c.size();
      }
   }

   @Override
   public boolean isEmpty() {
      synchronized(this.mutex) {
         return this.c.isEmpty();
      }
   }

   @Override
   public boolean contains(Object o) {
      synchronized(this.mutex) {
         return this.c.contains(o);
      }
   }

   @Override
   public Object[] toArray() {
      synchronized(this.mutex) {
         return this.c.toArray();
      }
   }

   @Override
   public <T> T[] toArray(T[] a) {
      synchronized(this.mutex) {
         return (T[])this.c.toArray(a);
      }
   }

   @Override
   public Iterator<E> iterator() {
      return this.c.iterator();
   }

   @Override
   public boolean add(E e) {
      synchronized(this.mutex) {
         return this.c.add(e);
      }
   }

   @Override
   public boolean remove(Object o) {
      synchronized(this.mutex) {
         return this.c.remove(o);
      }
   }

   @Override
   public boolean containsAll(Collection<?> coll) {
      synchronized(this.mutex) {
         return this.c.containsAll(coll);
      }
   }

   @Override
   public boolean addAll(Collection<? extends E> coll) {
      synchronized(this.mutex) {
         return this.c.addAll(coll);
      }
   }

   @Override
   public boolean removeAll(Collection<?> coll) {
      synchronized(this.mutex) {
         return this.c.removeAll(coll);
      }
   }

   @Override
   public boolean retainAll(Collection<?> coll) {
      synchronized(this.mutex) {
         return this.c.retainAll(coll);
      }
   }

   @Override
   public void clear() {
      synchronized(this.mutex) {
         this.c.clear();
      }
   }

   @Override
   public String toString() {
      synchronized(this.mutex) {
         return this.c.toString();
      }
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      synchronized(this.mutex) {
         s.defaultWriteObject();
      }
   }
}
