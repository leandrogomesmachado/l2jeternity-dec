package gnu.trove;

import gnu.trove.iterator.TFloatIterator;
import gnu.trove.procedure.TFloatProcedure;
import java.util.Collection;

public interface TFloatCollection {
   long serialVersionUID = 1L;

   float getNoEntryValue();

   int size();

   boolean isEmpty();

   boolean contains(float var1);

   TFloatIterator iterator();

   float[] toArray();

   float[] toArray(float[] var1);

   boolean add(float var1);

   boolean remove(float var1);

   boolean containsAll(Collection<?> var1);

   boolean containsAll(TFloatCollection var1);

   boolean containsAll(float[] var1);

   boolean addAll(Collection<? extends Float> var1);

   boolean addAll(TFloatCollection var1);

   boolean addAll(float[] var1);

   boolean retainAll(Collection<?> var1);

   boolean retainAll(TFloatCollection var1);

   boolean retainAll(float[] var1);

   boolean removeAll(Collection<?> var1);

   boolean removeAll(TFloatCollection var1);

   boolean removeAll(float[] var1);

   void clear();

   boolean forEach(TFloatProcedure var1);

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();
}
