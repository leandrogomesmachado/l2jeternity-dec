package com.mchange.v1.jvm;

public final class InternalNameUtils {
   public static String dottifySlashesAndDollarSigns(String var0) {
      return _dottifySlashesAndDollarSigns(var0).toString();
   }

   public static String decodeType(String var0) throws TypeFormatException {
      return _decodeType(var0).toString();
   }

   public static String decodeTypeList(String var0) throws TypeFormatException {
      StringBuffer var1 = new StringBuffer(64);
      _decodeTypeList(var0, 0, var1);
      return var1.toString();
   }

   public static boolean isPrimitive(char var0) {
      return var0 == 'Z' || var0 == 'B' || var0 == 'C' || var0 == 'S' || var0 == 'I' || var0 == 'J' || var0 == 'F' || var0 == 'D' || var0 == 'V';
   }

   private static void _decodeTypeList(String var0, int var1, StringBuffer var2) throws TypeFormatException {
      if (var2.length() != 0) {
         var2.append(' ');
      }

      char var3 = var0.charAt(var1);
      if (isPrimitive(var3)) {
         var2.append(_decodeType(var0.substring(var1, var1 + 1)));
         ++var1;
      } else {
         int var4;
         if (var3 != '[') {
            var4 = var0.indexOf(59, var1);
            if (var4 < 0) {
               throw new TypeFormatException(var0.substring(var1) + " is neither a primitive nor semicolon terminated!");
            }
         } else {
            int var5 = var1 + 1;

            while(var0.charAt(var5) == '[') {
               ++var5;
            }

            if (var0.charAt(var5) == 'L') {
               ++var5;

               while(var0.charAt(var5) != ';') {
                  ++var5;
               }
            }

            var4 = var5;
         }

         var2.append(_decodeType(var0.substring(var1, var1 = var4 + 1)));
      }

      if (var1 < var0.length()) {
         var2.append(',');
         _decodeTypeList(var0, var1, var2);
      }
   }

   private static StringBuffer _decodeType(String var0) throws TypeFormatException {
      int var1 = 0;
      char var3 = var0.charAt(0);
      StringBuffer var2;
      switch(var3) {
         case 'B':
            var2 = new StringBuffer("byte");
            break;
         case 'C':
            var2 = new StringBuffer("char");
            break;
         case 'D':
            var2 = new StringBuffer("double");
            break;
         case 'E':
         case 'G':
         case 'H':
         case 'K':
         case 'M':
         case 'N':
         case 'O':
         case 'P':
         case 'Q':
         case 'R':
         case 'T':
         case 'U':
         case 'W':
         case 'X':
         case 'Y':
         default:
            throw new TypeFormatException(var0 + " is not a valid inernal type name.");
         case 'F':
            var2 = new StringBuffer("float");
            break;
         case 'I':
            var2 = new StringBuffer("int");
            break;
         case 'J':
            var2 = new StringBuffer("long");
            break;
         case 'L':
            var2 = _decodeSimpleClassType(var0);
            break;
         case 'S':
            var2 = new StringBuffer("short");
            break;
         case 'V':
            var2 = new StringBuffer("void");
            break;
         case 'Z':
            var2 = new StringBuffer("boolean");
            break;
         case '[':
            ++var1;
            var2 = _decodeType(var0.substring(1));
      }

      for(int var4 = 0; var4 < var1; ++var4) {
         var2.append("[]");
      }

      return var2;
   }

   private static StringBuffer _decodeSimpleClassType(String var0) throws TypeFormatException {
      int var1 = var0.length();
      if (var0.charAt(0) == 'L' && var0.charAt(var1 - 1) == ';') {
         return _dottifySlashesAndDollarSigns(var0.substring(1, var1 - 1));
      } else {
         throw new TypeFormatException(var0 + " is not a valid representation of a simple class type.");
      }
   }

   private static StringBuffer _dottifySlashesAndDollarSigns(String var0) {
      StringBuffer var1 = new StringBuffer(var0);
      int var2 = 0;

      for(int var3 = var1.length(); var2 < var3; ++var2) {
         char var4 = var1.charAt(var2);
         if (var4 == '/' || var4 == '$') {
            var1.setCharAt(var2, '.');
         }
      }

      return var1;
   }

   private InternalNameUtils() {
   }

   public static void main(String[] var0) {
      try {
         System.out.println(decodeTypeList(var0[0]));
      } catch (Exception var2) {
         var2.printStackTrace();
      }
   }
}
