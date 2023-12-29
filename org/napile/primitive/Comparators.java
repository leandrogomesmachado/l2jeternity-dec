package org.napile.primitive;

import org.napile.primitive.comparators.IntComparator;
import org.napile.primitive.comparators.LongComparator;

public class Comparators {
   public static final IntComparator DEFAULT_INT_COMPARATOR = new IntComparator() {
      @Override
      public int compare(int x, int y) {
         return x < y ? -1 : (x == y ? 0 : 1);
      }
   };
   public static final LongComparator DEFAULT_LONG_COMPARATOR = new LongComparator() {
      @Override
      public int compare(long x, long y) {
         return x < y ? -1 : (x == y ? 0 : 1);
      }
   };
   public static final IntComparator REVERSE_INT_COMPARATOR = reverseOrder(DEFAULT_INT_COMPARATOR);
   public static final LongComparator REVERSE_LONG_COMPARATOR = reverseOrder(DEFAULT_LONG_COMPARATOR);

   public static IntComparator reverseOrder(final IntComparator comparator) {
      return comparator == null ? REVERSE_INT_COMPARATOR : new IntComparator() {
         @Override
         public int compare(int o1, int o2) {
            return comparator.compare(o2, o1);
         }
      };
   }

   public static LongComparator reverseOrder(final LongComparator comparator) {
      return comparator == null ? REVERSE_LONG_COMPARATOR : new LongComparator() {
         @Override
         public int compare(long o1, long o2) {
            return comparator.compare(o2, o1);
         }
      };
   }
}
