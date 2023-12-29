package gnu.trove.impl.sync;

import gnu.trove.TDoubleCollection;
import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.procedure.TDoubleProcedure;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;

public class TSynchronizedDoubleCollection implements TDoubleCollection, Serializable {
   private static final long serialVersionUID = 3053995032091335093L;
   final TDoubleCollection c;
   final Object mutex;

   public TSynchronizedDoubleCollection(TDoubleCollection c) {
      if (c == null) {
         throw new NullPointerException();
      } else {
         this.c = c;
         this.mutex = this;
      }
   }

   public TSynchronizedDoubleCollection(TDoubleCollection c, Object mutex) {
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
   public boolean contains(double o) {
      synchronized(this.mutex) {
         return this.c.contains(o);
      }
   }

   @Override
   public double[] toArray() {
      synchronized(this.mutex) {
         return this.c.toArray();
      }
   }

   @Override
   public double[] toArray(double[] a) {
      synchronized(this.mutex) {
         return this.c.toArray(a);
      }
   }

   @Override
   public TDoubleIterator iterator() {
      return this.c.iterator();
   }

   @Override
   public boolean add(double e) {
      synchronized(this.mutex) {
         return this.c.add(e);
      }
   }

   @Override
   public boolean remove(double o) {
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
   public boolean containsAll(TDoubleCollection coll) {
      synchronized(this.mutex) {
         return this.c.containsAll(coll);
      }
   }

   @Override
   public boolean containsAll(double[] array) {
      synchronized(this.mutex) {
         return this.c.containsAll(array);
      }
   }

   @Override
   public boolean addAll(Collection<? extends Double> coll) {
      synchronized(this.mutex) {
         return this.c.addAll(coll);
      }
   }

   @Override
   public boolean addAll(TDoubleCollection coll) {
      synchronized(this.mutex) {
         return this.c.addAll(coll);
      }
   }

   @Override
   public boolean addAll(double[] array) {
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
   public boolean removeAll(TDoubleCollection coll) {
      synchronized(this.mutex) {
         return this.c.removeAll(coll);
      }
   }

   @Override
   public boolean removeAll(double[] array) {
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
   public boolean retainAll(TDoubleCollection coll) {
      synchronized(this.mutex) {
         return this.c.retainAll(coll);
      }
   }

   @Override
   public boolean retainAll(double[] array) {
      synchronized(this.mutex) {
         return this.c.retainAll(array);
      }
   }

   @Override
   public double getNoEntryValue() {
      return this.c.getNoEntryValue();
   }

   @Override
   public boolean forEach(TDoubleProcedure procedure) {
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
