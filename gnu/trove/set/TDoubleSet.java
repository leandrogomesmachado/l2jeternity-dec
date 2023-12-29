package gnu.trove.set;

import gnu.trove.TDoubleCollection;
import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.procedure.TDoubleProcedure;
import java.util.Collection;

public interface TDoubleSet extends TDoubleCollection {
   @Override
   double getNoEntryValue();

   @Override
   int size();

   @Override
   boolean isEmpty();

   @Override
   boolean contains(double var1);

   @Override
   TDoubleIterator iterator();

   @Override
   double[] toArray();

   @Override
   double[] toArray(double[] var1);

   @Override
   boolean add(double var1);

   @Override
   boolean remove(double var1);

   @Override
   boolean containsAll(Collection<?> var1);

   @Override
   boolean containsAll(TDoubleCollection var1);

   @Override
   boolean containsAll(double[] var1);

   @Override
   boolean addAll(Collection<? extends Double> var1);

   @Override
   boolean addAll(TDoubleCollection var1);

   @Override
   boolean addAll(double[] var1);

   @Override
   boolean retainAll(Collection<?> var1);

   @Override
   boolean retainAll(TDoubleCollection var1);

   @Override
   boolean retainAll(double[] var1);

   @Override
   boolean removeAll(Collection<?> var1);

   @Override
   boolean removeAll(TDoubleCollection var1);

   @Override
   boolean removeAll(double[] var1);

   @Override
   void clear();

   @Override
   boolean forEach(TDoubleProcedure var1);

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();
}
