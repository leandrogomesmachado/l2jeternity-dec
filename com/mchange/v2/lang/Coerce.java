package com.mchange.v2.lang;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Coerce {
   static final Set CAN_COERCE;

   public static boolean canCoerce(Class var0) {
      return CAN_COERCE.contains(var0);
   }

   public static boolean canCoerce(Object var0) {
      return canCoerce(var0.getClass());
   }

   public static int toInt(String var0) {
      try {
         return Integer.parseInt(var0);
      } catch (NumberFormatException var2) {
         return (int)Double.parseDouble(var0);
      }
   }

   public static long toLong(String var0) {
      try {
         return Long.parseLong(var0);
      } catch (NumberFormatException var2) {
         return (long)Double.parseDouble(var0);
      }
   }

   public static float toFloat(String var0) {
      return Float.parseFloat(var0);
   }

   public static double toDouble(String var0) {
      return Double.parseDouble(var0);
   }

   public static byte toByte(String var0) {
      return (byte)toInt(var0);
   }

   public static short toShort(String var0) {
      return (short)toInt(var0);
   }

   public static boolean toBoolean(String var0) {
      return Boolean.valueOf(var0);
   }

   public static char toChar(String var0) {
      var0 = var0.trim();
      return var0.length() == 1 ? var0.charAt(0) : (char)toInt(var0);
   }

   public static Object toObject(String var0, Class var1) {
      if (var1 == Byte.TYPE) {
         var1 = Byte.class;
      } else if (var1 == Boolean.TYPE) {
         var1 = Boolean.class;
      } else if (var1 == Character.TYPE) {
         var1 = Character.class;
      } else if (var1 == Short.TYPE) {
         var1 = Short.class;
      } else if (var1 == Integer.TYPE) {
         var1 = Integer.class;
      } else if (var1 == Long.TYPE) {
         var1 = Long.class;
      } else if (var1 == Float.TYPE) {
         var1 = Float.class;
      } else if (var1 == Double.TYPE) {
         var1 = Double.class;
      }

      if (var1 == String.class) {
         return var0;
      } else if (var1 == Byte.class) {
         return new Byte(toByte(var0));
      } else if (var1 == Boolean.class) {
         return Boolean.valueOf(var0);
      } else if (var1 == Character.class) {
         return new Character(toChar(var0));
      } else if (var1 == Short.class) {
         return new Short(toShort(var0));
      } else if (var1 == Integer.class) {
         return new Integer(var0);
      } else if (var1 == Long.class) {
         return new Long(var0);
      } else if (var1 == Float.class) {
         return new Float(var0);
      } else if (var1 == Double.class) {
         return new Double(var0);
      } else {
         throw new IllegalArgumentException("Cannot coerce to type: " + var1.getName());
      }
   }

   private Coerce() {
   }

   static {
      Class[] var0 = new Class[]{
         Byte.TYPE,
         Boolean.TYPE,
         Character.TYPE,
         Short.TYPE,
         Integer.TYPE,
         Long.TYPE,
         Float.TYPE,
         Double.TYPE,
         String.class,
         Byte.class,
         Boolean.class,
         Character.class,
         Short.class,
         Integer.class,
         Long.class,
         Float.class,
         Double.class
      };
      HashSet var1 = new HashSet();
      var1.addAll(Arrays.asList(var0));
      CAN_COERCE = Collections.unmodifiableSet(var1);
   }
}
