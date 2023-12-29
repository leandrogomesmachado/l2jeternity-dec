package gnu.trove;

import gnu.trove.iterator.TShortIterator;
import gnu.trove.procedure.TShortProcedure;
import java.util.Collection;

public interface TShortCollection {
   long serialVersionUID = 1L;

   short getNoEntryValue();

   int size();

   boolean isEmpty();

   boolean contains(short var1);

   TShortIterator iterator();

   short[] toArray();

   short[] toArray(short[] var1);

   boolean add(short var1);

   boolean remove(short var1);

   boolean containsAll(Collection<?> var1);

   boolean containsAll(TShortCollection var1);

   boolean containsAll(short[] var1);

   boolean addAll(Collection<? extends Short> var1);

   boolean addAll(TShortCollection var1);

   boolean addAll(short[] var1);

   boolean retainAll(Collection<?> var1);

   boolean retainAll(TShortCollection var1);

   boolean retainAll(short[] var1);

   boolean removeAll(Collection<?> var1);

   boolean removeAll(TShortCollection var1);

   boolean removeAll(short[] var1);

   void clear();

   boolean forEach(TShortProcedure var1);

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();
}
