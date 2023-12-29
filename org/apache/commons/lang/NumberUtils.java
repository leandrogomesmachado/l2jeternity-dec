package org.apache.commons.lang;

import java.math.BigDecimal;
import java.math.BigInteger;

/** @deprecated */
public final class NumberUtils {
   public static int stringToInt(String str) {
      return stringToInt(str, 0);
   }

   public static int stringToInt(String str, int defaultValue) {
      try {
         return Integer.parseInt(str);
      } catch (NumberFormatException var3) {
         return defaultValue;
      }
   }

   public static Number createNumber(String val) throws NumberFormatException {
      if (val == null) {
         return null;
      } else if (val.length() == 0) {
         throw new NumberFormatException("\"\" is not a valid number.");
      } else if (val.startsWith("--")) {
         return null;
      } else if (!val.startsWith("0x") && !val.startsWith("-0x")) {
         char lastChar = val.charAt(val.length() - 1);
         int decPos = val.indexOf(46);
         int expPos = val.indexOf(101) + val.indexOf(69) + 1;
         String mant;
         String dec;
         if (decPos > -1) {
            if (expPos > -1) {
               if (expPos < decPos) {
                  throw new NumberFormatException(val + " is not a valid number.");
               }

               dec = val.substring(decPos + 1, expPos);
            } else {
               dec = val.substring(decPos + 1);
            }

            mant = val.substring(0, decPos);
         } else {
            if (expPos > -1) {
               mant = val.substring(0, expPos);
            } else {
               mant = val;
            }

            dec = null;
         }

         if (!Character.isDigit(lastChar)) {
            String exp;
            if (expPos > -1 && expPos < val.length() - 1) {
               exp = val.substring(expPos + 1, val.length() - 1);
            } else {
               exp = null;
            }

            String numeric = val.substring(0, val.length() - 1);
            boolean allZeros = isAllZeros(mant) && isAllZeros(exp);
            switch(lastChar) {
               case 'D':
               case 'd':
                  break;
               case 'F':
               case 'f':
                  try {
                     Float f = createFloat(numeric);
                     if (f.isInfinite() || f == 0.0F && !allZeros) {
                        break;
                     }

                     return f;
                  } catch (NumberFormatException var15) {
                     break;
                  }
               case 'L':
               case 'l':
                  if (dec == null && exp == null && (numeric.charAt(0) == '-' && isDigits(numeric.substring(1)) || isDigits(numeric))) {
                     try {
                        return createLong(numeric);
                     } catch (NumberFormatException var11) {
                        return createBigInteger(numeric);
                     }
                  }

                  throw new NumberFormatException(val + " is not a valid number.");
               default:
                  throw new NumberFormatException(val + " is not a valid number.");
            }

            try {
               Double d = createDouble(numeric);
               if (!d.isInfinite() && ((double)d.floatValue() != 0.0 || allZeros)) {
                  return d;
               }
            } catch (NumberFormatException var14) {
            }

            try {
               return createBigDecimal(numeric);
            } catch (NumberFormatException var13) {
               throw new NumberFormatException(val + " is not a valid number.");
            }
         } else {
            String exp;
            if (expPos > -1 && expPos < val.length() - 1) {
               exp = val.substring(expPos + 1, val.length());
            } else {
               exp = null;
            }

            if (dec == null && exp == null) {
               try {
                  return createInteger(val);
               } catch (NumberFormatException var12) {
                  try {
                     return createLong(val);
                  } catch (NumberFormatException var10) {
                     return createBigInteger(val);
                  }
               }
            } else {
               boolean allZeros = isAllZeros(mant) && isAllZeros(exp);

               try {
                  Float f = createFloat(val);
                  if (!f.isInfinite() && (f != 0.0F || allZeros)) {
                     return f;
                  }
               } catch (NumberFormatException var17) {
               }

               try {
                  Double d = createDouble(val);
                  if (!d.isInfinite() && (d != 0.0 || allZeros)) {
                     return d;
                  }
               } catch (NumberFormatException var16) {
               }

               return createBigDecimal(val);
            }
         }
      } else {
         return createInteger(val);
      }
   }

   private static boolean isAllZeros(String s) {
      if (s == null) {
         return true;
      } else {
         for(int i = s.length() - 1; i >= 0; --i) {
            if (s.charAt(i) != '0') {
               return false;
            }
         }

         return s.length() > 0;
      }
   }

   public static Float createFloat(String val) {
      return Float.valueOf(val);
   }

   public static Double createDouble(String val) {
      return Double.valueOf(val);
   }

   public static Integer createInteger(String val) {
      return Integer.decode(val);
   }

   public static Long createLong(String val) {
      return Long.valueOf(val);
   }

   public static BigInteger createBigInteger(String val) {
      return new BigInteger(val);
   }

   public static BigDecimal createBigDecimal(String val) {
      return new BigDecimal(val);
   }

   public static long minimum(long a, long b, long c) {
      if (b < a) {
         a = b;
      }

      if (c < a) {
         a = c;
      }

      return a;
   }

   public static int minimum(int a, int b, int c) {
      if (b < a) {
         a = b;
      }

      if (c < a) {
         a = c;
      }

      return a;
   }

   public static long maximum(long a, long b, long c) {
      if (b > a) {
         a = b;
      }

      if (c > a) {
         a = c;
      }

      return a;
   }

   public static int maximum(int a, int b, int c) {
      if (b > a) {
         a = b;
      }

      if (c > a) {
         a = c;
      }

      return a;
   }

   public static int compare(double lhs, double rhs) {
      if (lhs < rhs) {
         return -1;
      } else if (lhs > rhs) {
         return 1;
      } else {
         long lhsBits = Double.doubleToLongBits(lhs);
         long rhsBits = Double.doubleToLongBits(rhs);
         if (lhsBits == rhsBits) {
            return 0;
         } else {
            return lhsBits < rhsBits ? -1 : 1;
         }
      }
   }

   public static int compare(float lhs, float rhs) {
      if (lhs < rhs) {
         return -1;
      } else if (lhs > rhs) {
         return 1;
      } else {
         int lhsBits = Float.floatToIntBits(lhs);
         int rhsBits = Float.floatToIntBits(rhs);
         if (lhsBits == rhsBits) {
            return 0;
         } else {
            return lhsBits < rhsBits ? -1 : 1;
         }
      }
   }

   public static boolean isDigits(String str) {
      if (str != null && str.length() != 0) {
         for(int i = 0; i < str.length(); ++i) {
            if (!Character.isDigit(str.charAt(i))) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean isNumber(String str) {
      if (StringUtils.isEmpty(str)) {
         return false;
      } else {
         char[] chars = str.toCharArray();
         int sz = chars.length;
         boolean hasExp = false;
         boolean hasDecPoint = false;
         boolean allowSigns = false;
         boolean foundDigit = false;
         int start = chars[0] == '-' ? 1 : 0;
         if (sz > start + 1 && chars[start] == '0' && chars[start + 1] == 'x') {
            int i = start + 2;
            if (i == sz) {
               return false;
            } else {
               while(i < chars.length) {
                  if ((chars[i] < '0' || chars[i] > '9') && (chars[i] < 'a' || chars[i] > 'f') && (chars[i] < 'A' || chars[i] > 'F')) {
                     return false;
                  }

                  ++i;
               }

               return true;
            }
         } else {
            --sz;

            int i;
            for(i = start; i < sz || i < sz + 1 && allowSigns && !foundDigit; ++i) {
               if (chars[i] >= '0' && chars[i] <= '9') {
                  foundDigit = true;
                  allowSigns = false;
               } else if (chars[i] == '.') {
                  if (hasDecPoint || hasExp) {
                     return false;
                  }

                  hasDecPoint = true;
               } else if (chars[i] != 'e' && chars[i] != 'E') {
                  if (chars[i] != '+' && chars[i] != '-') {
                     return false;
                  }

                  if (!allowSigns) {
                     return false;
                  }

                  allowSigns = false;
                  foundDigit = false;
               } else {
                  if (hasExp) {
                     return false;
                  }

                  if (!foundDigit) {
                     return false;
                  }

                  hasExp = true;
                  allowSigns = true;
               }
            }

            if (i < chars.length) {
               if (chars[i] >= '0' && chars[i] <= '9') {
                  return true;
               } else if (chars[i] != 'e' && chars[i] != 'E') {
                  if (allowSigns || chars[i] != 'd' && chars[i] != 'D' && chars[i] != 'f' && chars[i] != 'F') {
                     if (chars[i] != 'l' && chars[i] != 'L') {
                        return false;
                     } else {
                        return foundDigit && !hasExp;
                     }
                  } else {
                     return foundDigit;
                  }
               } else {
                  return false;
               }
            } else {
               return !allowSigns && foundDigit;
            }
         }
      }
   }
}
