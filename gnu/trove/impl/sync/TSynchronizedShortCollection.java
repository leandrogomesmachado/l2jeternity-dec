package gnu.trove.impl.sync;

import gnu.trove.TShortCollection;
import gnu.trove.iterator.TShortIterator;
import gnu.trove.procedure.TShortProcedure;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;

public class TSynchronizedShortCollection implements TShortCollection, Serializable {
   private static final long serialVersionUID = 3053995032091335093L;
   final TShortCollection c;
   final Object mutex;

   public TSynchronizedShortCollection(TShortCollection c) {
      if (c == null) {
         throw new NullPointerException();
      } else {
         this.c = c;
         this.mutex = this;
      }
   }

   public TSynchronizedShortCollection(TShortCollection c, Object mutex) {
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
   public boolean contains(short o) {
      synchronized(this.mutex) {
         return this.c.contains(o);
      }
   }

   @Override
   public short[] toArray() {
      synchronized(this.mutex) {
         return this.c.toArray();
      }
   }

   @Override
   public short[] toArray(short[] a) {
      synchronized(this.mutex) {
         return this.c.toArray(a);
      }
   }

   @Override
   public TShortIterator iterator() {
      return this.c.iterator();
   }

   @Override
   public boolean add(short e) {
      synchronized(this.mutex) {
         return this.c.add(e);
      }
   }

   @Override
   public boolean remove(short o) {
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
   public boolean containsAll(TShortCollection coll) {
      synchronized(this.mutex) {
         return this.c.containsAll(coll);
      }
   }

   @Override
   public boolean containsAll(short[] array) {
      synchronized(this.mutex) {
         return this.c.containsAll(array);
      }
   }

   @Override
   public boolean addAll(Collection<? extends Short> coll) {
      synchronized(this.mutex) {
         return this.c.addAll(coll);
      }
   }

   @Override
   public boolean addAll(TShortCollection coll) {
      synchronized(this.mutex) {
         return this.c.addAll(coll);
      }
   }

   @Override
   public boolean addAll(short[] array) {
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
   public boolean removeAll(TShortCollection coll) {
      synchronized(this.mutex) {
         return this.c.removeAll(coll);
      }
   }

   @Override
   public boolean removeAll(short[] array) {
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
   public boolean retainAll(TShortCollection coll) {
      synchronized(this.mutex) {
         return this.c.retainAll(coll);
      }
   }

   @Override
   public boolean retainAll(short[] array) {
      synchronized(this.mutex) {
         return this.c.retainAll(array);
      }
   }

   @Override
   public short getNoEntryValue() {
      return this.c.getNoEntryValue();
   }

   @Override
   public boolean forEach(TShortProcedure procedure) {
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
