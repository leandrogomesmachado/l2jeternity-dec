package l2e.commons.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

public class ExArrays {
   public static boolean[] add(boolean[] array, boolean value) {
      return push(array, value);
   }

   public static short[] add(short[] array, short value) {
      return push(array, value);
   }

   public static int[] add(int[] array, int value) {
      return push(array, value);
   }

   public static long[] add(long[] array, long value) {
      return push(array, value);
   }

   public static float[] add(float[] array, float value) {
      return push(array, value);
   }

   public static double[] add(double[] array, double value) {
      return push(array, value);
   }

   public static <E> E[] add(E[] array, E value) {
      return (E[])push(array, value);
   }

   public static boolean[] put(boolean[] array, int index, boolean value) {
      if (array == null) {
         if (index != 0) {
            throw new NullPointerException();
         }

         array = push(array, value);
      } else if (index == array.length) {
         array = push(array, value);
      } else {
         array[index] = value;
      }

      return array;
   }

   public static short[] put(short[] array, int index, short value) {
      if (array == null) {
         if (index != 0) {
            throw new NullPointerException();
         }

         array = push(array, value);
      } else if (index == array.length) {
         array = push(array, value);
      } else {
         array[index] = value;
      }

      return array;
   }

   public static int[] put(int[] array, int index, int value) {
      if (array == null) {
         if (index != 0) {
            throw new NullPointerException();
         }

         array = push(array, value);
      } else if (index == array.length) {
         array = push(array, value);
      } else {
         array[index] = value;
      }

      return array;
   }

   public static long[] put(long[] array, int index, long value) {
      if (array == null) {
         if (index != 0) {
            throw new NullPointerException();
         }

         array = push(array, value);
      } else if (index == array.length) {
         array = push(array, value);
      } else {
         array[index] = value;
      }

      return array;
   }

   public static float[] put(float[] array, int index, float value) {
      if (array == null) {
         if (index != 0) {
            throw new NullPointerException();
         }

         array = push(array, value);
      } else if (index == array.length) {
         array = push(array, value);
      } else {
         array[index] = value;
      }

      return array;
   }

   public static double[] put(double[] array, int index, double value) {
      if (array == null) {
         if (index != 0) {
            throw new NullPointerException();
         }

         array = push(array, value);
      } else if (index == array.length) {
         array = push(array, value);
      } else {
         array[index] = value;
      }

      return array;
   }

   public static <E> E[] put(E[] array, int index, E value) {
      if (array == null) {
         if (index != 0) {
            throw new NullPointerException();
         }

         array = (E[])push(array, value);
      } else if (index == array.length) {
         array = (E[])push(array, value);
      } else {
         array[index] = value;
      }

      return array;
   }

   public static boolean get(boolean[] array, int index) {
      return get(array, index, false);
   }

   public static short get(short[] array, int index) {
      return get(array, index, (short)0);
   }

   public static int get(int[] array, int index) {
      return get(array, index, 0);
   }

   public static long get(long[] array, int index) {
      return get(array, index, 0L);
   }

   public static float get(float[] array, int index) {
      return get(array, index, 0.0F);
   }

   public static double get(double[] array, int index) {
      return get(array, index, 0.0);
   }

   public static <E> E get(E[] array, int index) {
      return get(array, index, (E)null);
   }

   public static boolean get(boolean[] array, int index, boolean defaultValue) {
      return array != null && index >= 0 && index < array.length ? array[index] : defaultValue;
   }

   public static short get(short[] array, int index, short defaultValue) {
      return array != null && index >= 0 && index < array.length ? array[index] : defaultValue;
   }

   public static int get(int[] array, int index, int defaultValue) {
      return array != null && index >= 0 && index < array.length ? array[index] : defaultValue;
   }

   public static long get(long[] array, int index, long defaultValue) {
      return array != null && index >= 0 && index < array.length ? array[index] : defaultValue;
   }

   public static float get(float[] array, int index, float defaultValue) {
      return array != null && index >= 0 && index < array.length ? array[index] : defaultValue;
   }

   public static double get(double[] array, int index, double defaultValue) {
      return array != null && index >= 0 && index < array.length ? array[index] : defaultValue;
   }

   public static <E> E get(E[] array, int index, E defaultValue) {
      return (E)(array != null && index >= 0 && index < array.length ? array[index] : defaultValue);
   }

   public static boolean[] push(boolean[] array, boolean value) {
      if (array == null) {
         return new boolean[]{value};
      } else {
         int index = array.length;
         array = Arrays.copyOf(array, index + 1);
         array[index] = value;
         return array;
      }
   }

   public static short[] push(short[] array, short value) {
      if (array == null) {
         return new short[]{value};
      } else {
         int index = array.length;
         array = Arrays.copyOf(array, index + 1);
         array[index] = value;
         return array;
      }
   }

   public static int[] push(int[] array, int value) {
      if (array == null) {
         return new int[]{value};
      } else {
         int index = array.length;
         array = Arrays.copyOf(array, index + 1);
         array[index] = value;
         return array;
      }
   }

   public static long[] push(long[] array, long value) {
      if (array == null) {
         return new long[]{value};
      } else {
         int index = array.length;
         array = Arrays.copyOf(array, index + 1);
         array[index] = value;
         return array;
      }
   }

   public static float[] push(float[] array, float value) {
      if (array == null) {
         return new float[]{value};
      } else {
         int index = array.length;
         array = Arrays.copyOf(array, index + 1);
         array[index] = value;
         return array;
      }
   }

   public static double[] push(double[] array, double value) {
      if (array == null) {
         return new double[]{value};
      } else {
         int index = array.length;
         array = Arrays.copyOf(array, index + 1);
         array[index] = value;
         return array;
      }
   }

   public static <E> E[] push(E[] array, E value) {
      if (array == null) {
         E[] a = (E[])Array.newInstance(value.getClass(), 1);
         a[0] = value;
         return a;
      } else {
         int index = array.length;
         array = Arrays.copyOf(array, index + 1);
         array[index] = value;
         return array;
      }
   }

   public static int[] toPrimitiveArray(Collection<Integer> list) {
      int index = 0;
      int[] array = new int[list.size()];

      for(int s : list) {
         array[index++] = s;
      }

      return array;
   }

   public static int[] toPrimitiveArray(Collection<Integer> list, int nullValue) {
      int index = 0;
      int[] array = new int[list.size()];

      for(Integer v : list) {
         array[index++] = v == null ? nullValue : v;
      }

      return array;
   }

   public static int[] toPrimitiveArrayTrim(Collection<Integer> list) {
      int index = 0;
      int[] array = new int[list.size()];

      for(Integer v : list) {
         if (v != null) {
            array[index++] = v;
         }
      }

      if (index != array.length) {
         int[] tmp = new int[index];
         System.arraycopy(array, 0, tmp, 0, index);
         array = tmp;
      }

      return array;
   }
}
