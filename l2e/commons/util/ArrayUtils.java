package l2e.commons.util;

import java.lang.reflect.Array;

public class ArrayUtils {
   public static final int INDEX_NOT_FOUND = -1;

   public static boolean contains(Object[] array, Object objectToFind) {
      return indexOf(array, objectToFind) != -1;
   }

   public static int indexOf(Object[] array, Object objectToFind) {
      return indexOf(array, objectToFind, 0);
   }

   public static int indexOf(Object[] array, Object objectToFind, int startIndex) {
      if (array == null) {
         return -1;
      } else {
         if (startIndex < 0) {
            startIndex = 0;
         }

         if (objectToFind == null) {
            for(int i = startIndex; i < array.length; ++i) {
               if (array[i] == null) {
                  return i;
               }
            }
         } else {
            for(int i = startIndex; i < array.length; ++i) {
               if (objectToFind.equals(array[i])) {
                  return i;
               }
            }
         }

         return -1;
      }
   }

   public static boolean isIntInArray(int val, int[] array) {
      for(int elem : array) {
         if (val == elem) {
            return true;
         }
      }

      return false;
   }

   public static Object[] add(Object[] array, Object element) {
      Class<?> type;
      if (array != null) {
         type = array.getClass();
      } else if (element != null) {
         type = element.getClass();
      } else {
         type = Object.class;
      }

      Object[] newArray = (Object[])copyArrayGrow1(array, type);
      newArray[newArray.length - 1] = element;
      return newArray;
   }

   private static Object copyArrayGrow1(Object array, Class<?> newArrayComponentType) {
      if (array != null) {
         int arrayLength = Array.getLength(array);
         Object newArray = Array.newInstance(array.getClass().getComponentType(), arrayLength + 1);
         System.arraycopy(array, 0, newArray, 0, arrayLength);
         return newArray;
      } else {
         return Array.newInstance(newArrayComponentType, 1);
      }
   }

   public static Object[] remove(Object[] array, int index) {
      return remove(array, index);
   }

   private static Object remove(Object array, int index) {
      int length = getLength(array);
      if (index >= 0 && index < length) {
         Object result = Array.newInstance(array.getClass().getComponentType(), length - 1);
         System.arraycopy(array, 0, result, 0, index);
         if (index < length - 1) {
            System.arraycopy(array, index + 1, result, index, length - index - 1);
         }

         return result;
      } else {
         throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
      }
   }

   public static int getLength(Object array) {
      return array == null ? 0 : Array.getLength(array);
   }
}
