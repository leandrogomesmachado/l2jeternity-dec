package gnu.trove.set;

import gnu.trove.TLongCollection;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.procedure.TLongProcedure;
import java.util.Collection;

public interface TLongSet extends TLongCollection {
   @Override
   long getNoEntryValue();

   @Override
   int size();

   @Override
   boolean isEmpty();

   @Override
   boolean contains(long var1);

   @Override
   TLongIterator iterator();

   @Override
   long[] toArray();

   @Override
   long[] toArray(long[] var1);

   @Override
   boolean add(long var1);

   @Override
   boolean remove(long var1);

   @Override
   boolean containsAll(Collection<?> var1);

   @Override
   boolean containsAll(TLongCollection var1);

   @Override
   boolean containsAll(long[] var1);

   @Override
   boolean addAll(Collection<? extends Long> var1);

   @Override
   boolean addAll(TLongCollection var1);

   @Override
   boolean addAll(long[] var1);

   @Override
   boolean retainAll(Collection<?> var1);

   @Override
   boolean retainAll(TLongCollection var1);

   @Override
   boolean retainAll(long[] var1);

   @Override
   boolean removeAll(Collection<?> var1);

   @Override
   boolean removeAll(TLongCollection var1);

   @Override
   boolean removeAll(long[] var1);

   @Override
   void clear();

   @Override
   boolean forEach(TLongProcedure var1);

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();
}
