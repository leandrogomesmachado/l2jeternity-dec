package org.napile.primitive.lists.abstracts;

import java.util.RandomAccess;
import org.napile.primitive.lists.LongList;

class RandomAccessSubLongList extends SubLongList implements RandomAccess {
   RandomAccessSubLongList(AbstractLongList list, int fromIndex, int toIndex) {
      super(list, fromIndex, toIndex);
   }

   @Override
   public LongList subList(int fromIndex, int toIndex) {
      return new RandomAccessSubLongList(this, fromIndex, toIndex);
   }
}
