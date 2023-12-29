package org.napile.primitive.lists.abstracts;

import java.util.RandomAccess;
import org.napile.primitive.lists.IntList;

class RandomAccessSubIntList extends SubIntList implements RandomAccess {
   RandomAccessSubIntList(AbstractIntList list, int fromIndex, int toIndex) {
      super(list, fromIndex, toIndex);
   }

   @Override
   public IntList subList(int fromIndex, int toIndex) {
      return new RandomAccessSubIntList(this, fromIndex, toIndex);
   }
}
