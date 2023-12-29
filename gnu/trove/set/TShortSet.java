package gnu.trove.set;

import gnu.trove.TShortCollection;
import gnu.trove.iterator.TShortIterator;
import gnu.trove.procedure.TShortProcedure;
import java.util.Collection;

public interface TShortSet extends TShortCollection {
   @Override
   short getNoEntryValue();

   @Override
   int size();

   @Override
   boolean isEmpty();

   @Override
   boolean contains(short var1);

   @Override
   TShortIterator iterator();

   @Override
   short[] toArray();

   @Override
   short[] toArray(short[] var1);

   @Override
   boolean add(short var1);

   @Override
   boolean remove(short var1);

   @Override
   boolean containsAll(Collection<?> var1);

   @Override
   boolean containsAll(TShortCollection var1);

   @Override
   boolean containsAll(short[] var1);

   @Override
   boolean addAll(Collection<? extends Short> var1);

   @Override
   boolean addAll(TShortCollection var1);

   @Override
   boolean addAll(short[] var1);

   @Override
   boolean retainAll(Collection<?> var1);

   @Override
   boolean retainAll(TShortCollection var1);

   @Override
   boolean retainAll(short[] var1);

   @Override
   boolean removeAll(Collection<?> var1);

   @Override
   boolean removeAll(TShortCollection var1);

   @Override
   boolean removeAll(short[] var1);

   @Override
   void clear();

   @Override
   boolean forEach(TShortProcedure var1);

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();
}
