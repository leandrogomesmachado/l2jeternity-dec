package gnu.trove.set;

import gnu.trove.TFloatCollection;
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.procedure.TFloatProcedure;
import java.util.Collection;

public interface TFloatSet extends TFloatCollection {
   @Override
   float getNoEntryValue();

   @Override
   int size();

   @Override
   boolean isEmpty();

   @Override
   boolean contains(float var1);

   @Override
   TFloatIterator iterator();

   @Override
   float[] toArray();

   @Override
   float[] toArray(float[] var1);

   @Override
   boolean add(float var1);

   @Override
   boolean remove(float var1);

   @Override
   boolean containsAll(Collection<?> var1);

   @Override
   boolean containsAll(TFloatCollection var1);

   @Override
   boolean containsAll(float[] var1);

   @Override
   boolean addAll(Collection<? extends Float> var1);

   @Override
   boolean addAll(TFloatCollection var1);

   @Override
   boolean addAll(float[] var1);

   @Override
   boolean retainAll(Collection<?> var1);

   @Override
   boolean retainAll(TFloatCollection var1);

   @Override
   boolean retainAll(float[] var1);

   @Override
   boolean removeAll(Collection<?> var1);

   @Override
   boolean removeAll(TFloatCollection var1);

   @Override
   boolean removeAll(float[] var1);

   @Override
   void clear();

   @Override
   boolean forEach(TFloatProcedure var1);

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();
}
