package gnu.trove.set;

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.procedure.TIntProcedure;
import java.util.Collection;

public interface TIntSet extends TIntCollection {
   @Override
   int getNoEntryValue();

   @Override
   int size();

   @Override
   boolean isEmpty();

   @Override
   boolean contains(int var1);

   @Override
   TIntIterator iterator();

   @Override
   int[] toArray();

   @Override
   int[] toArray(int[] var1);

   @Override
   boolean add(int var1);

   @Override
   boolean remove(int var1);

   @Override
   boolean containsAll(Collection<?> var1);

   @Override
   boolean containsAll(TIntCollection var1);

   @Override
   boolean containsAll(int[] var1);

   @Override
   boolean addAll(Collection<? extends Integer> var1);

   @Override
   boolean addAll(TIntCollection var1);

   @Override
   boolean addAll(int[] var1);

   @Override
   boolean retainAll(Collection<?> var1);

   @Override
   boolean retainAll(TIntCollection var1);

   @Override
   boolean retainAll(int[] var1);

   @Override
   boolean removeAll(Collection<?> var1);

   @Override
   boolean removeAll(TIntCollection var1);

   @Override
   boolean removeAll(int[] var1);

   @Override
   void clear();

   @Override
   boolean forEach(TIntProcedure var1);

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();
}
