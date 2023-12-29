package gnu.trove.impl.sync;

import gnu.trove.TFloatCollection;
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.procedure.TFloatProcedure;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;

public class TSynchronizedFloatCollection implements TFloatCollection, Serializable {
   private static final long serialVersionUID = 3053995032091335093L;
   final TFloatCollection c;
   final Object mutex;

   public TSynchronizedFloatCollection(TFloatCollection c) {
      if (c == null) {
         throw new NullPointerException();
      } else {
         this.c = c;
         this.mutex = this;
      }
   }

   public TSynchronizedFloatCollection(TFloatCollection c, Object mutex) {
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
   public boolean contains(float o) {
      synchronized(this.mutex) {
         return this.c.contains(o);
      }
   }

   @Override
   public float[] toArray() {
      synchronized(this.mutex) {
         return this.c.toArray();
      }
   }

   @Override
   public float[] toArray(float[] a) {
      synchronized(this.mutex) {
         return this.c.toArray(a);
      }
   }

   @Override
   public TFloatIterator iterator() {
      return this.c.iterator();
   }

   @Override
   public boolean add(float e) {
      synchronized(this.mutex) {
         return this.c.add(e);
      }
   }

   @Override
   public boolean remove(float o) {
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
   public boolean containsAll(TFloatCollection coll) {
      synchronized(this.mutex) {
         return this.c.containsAll(coll);
      }
   }

   @Override
   public boolean containsAll(float[] array) {
      synchronized(this.mutex) {
         return this.c.containsAll(array);
      }
   }

   @Override
   public boolean addAll(Collection<? extends Float> coll) {
      synchronized(this.mutex) {
         return this.c.addAll(coll);
      }
   }

   @Override
   public boolean addAll(TFloatCollection coll) {
      synchronized(this.mutex) {
         return this.c.addAll(coll);
      }
   }

   @Override
   public boolean addAll(float[] array) {
      synchronized(this.mutex) {
         return this.c.addAll(array);
      }
   }

   @Override
   public boolean removeAll(Collection<?> coll) {
      synchronized(this.mutex) {
         return this.c.removeAll(coll);
      }
   }

   @Override
   public boolean removeAll(TFloatCollection coll) {
      synchronized(this.mutex) {
         return this.c.removeAll(coll);
      }
   }

   @Override
   public boolean removeAll(float[] array) {
      synchronized(this.mutex) {
         return this.c.removeAll(array);
      }
   }

   @Override
   public boolean retainAll(Collection<?> coll) {
      synchronized(this.mutex) {
         return this.c.retainAll(coll);
      }
   }

   @Override
   public boolean retainAll(TFloatCollection coll) {
      synchronized(this.mutex) {
         return this.c.retainAll(coll);
      }
   }

   @Override
   public boolean retainAll(float[] array) {
      synchronized(this.mutex) {
         return this.c.retainAll(array);
      }
   }

   @Override
   public float getNoEntryValue() {
      return this.c.getNoEntryValue();
   }

   @Override
   public boolean forEach(TFloatProcedure procedure) {
      synchronized(this.mutex) {
         return this.c.forEach(procedure);
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
