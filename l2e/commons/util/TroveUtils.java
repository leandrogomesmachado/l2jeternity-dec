package l2e.commons.util;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;

public class TroveUtils {
   private static final TIntObjectHashMap EMPTY_INT_OBJECT_MAP = new TroveUtils.TIntObjectHashMapEmpty();
   public static final TIntArrayList EMPTY_INT_ARRAY_LIST = new TroveUtils.TIntArrayListEmpty();

   public static <V> TIntObjectHashMap<V> emptyIntObjectMap() {
      return EMPTY_INT_OBJECT_MAP;
   }

   private static class TIntArrayListEmpty extends TIntArrayList {
      TIntArrayListEmpty() {
         super(0);
      }

      @Override
      public boolean add(int val) {
         throw new UnsupportedOperationException();
      }
   }

   private static class TIntObjectHashMapEmpty<V> extends TIntObjectHashMap<V> {
      TIntObjectHashMapEmpty() {
         super(0);
      }

      @Override
      public V put(int key, V value) {
         throw new UnsupportedOperationException();
      }

      @Override
      public V putIfAbsent(int key, V value) {
         throw new UnsupportedOperationException();
      }
   }
}
