package gnu.trove.set;

import gnu.trove.TCharCollection;
import gnu.trove.iterator.TCharIterator;
import gnu.trove.procedure.TCharProcedure;
import java.util.Collection;

public interface TCharSet extends TCharCollection {
   @Override
   char getNoEntryValue();

   @Override
   int size();

   @Override
   boolean isEmpty();

   @Override
   boolean contains(char var1);

   @Override
   TCharIterator iterator();

   @Override
   char[] toArray();

   @Override
   char[] toArray(char[] var1);

   @Override
   boolean add(char var1);

   @Override
   boolean remove(char var1);

   @Override
   boolean containsAll(Collection<?> var1);

   @Override
   boolean containsAll(TCharCollection var1);

   @Override
   boolean containsAll(char[] var1);

   @Override
   boolean addAll(Collection<? extends Character> var1);

   @Override
   boolean addAll(TCharCollection var1);

   @Override
   boolean addAll(char[] var1);

   @Override
   boolean retainAll(Collection<?> var1);

   @Override
   boolean retainAll(TCharCollection var1);

   @Override
   boolean retainAll(char[] var1);

   @Override
   boolean removeAll(Collection<?> var1);

   @Override
   boolean removeAll(TCharCollection var1);

   @Override
   boolean removeAll(char[] var1);

   @Override
   void clear();

   @Override
   boolean forEach(TCharProcedure var1);

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();
}
