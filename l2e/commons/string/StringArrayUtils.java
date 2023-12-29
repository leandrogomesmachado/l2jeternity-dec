package l2e.commons.string;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StringArrayUtils {
   private static final Logger _log = Logger.getLogger(StringArrayUtils.class.getName());

   public static int[] stringToIntArray(String text, String separator) {
      if (text != null && !text.isEmpty()) {
         String[] separatedText = text.split(separator);
         int[] result = new int[separatedText.length];

         try {
            for(int i = 0; i < separatedText.length; ++i) {
               result[i] = Integer.parseInt(separatedText[i]);
            }

            return result;
         } catch (NumberFormatException var5) {
            _log.log(Level.SEVERE, "StringArrayUtils: Error while convert string to int array.", (Throwable)var5);
            return new int[0];
         }
      } else {
         return new int[0];
      }
   }

   public static int[][] stringToIntArray2X(String text, String separator1, String separator2) {
      if (text != null && !text.isEmpty()) {
         String[] separatedText = text.split(separator1);
         int[][] result = new int[separatedText.length][];

         for(int i = 0; i < separatedText.length; ++i) {
            result[i] = stringToIntArray(separatedText[i], separator2);
         }

         return result;
      } else {
         return new int[0][];
      }
   }

   public static long[] stringToLongArray(String text, String separator) {
      if (text != null && !text.isEmpty()) {
         String[] separatedText = text.split(separator);
         long[] result = new long[separatedText.length];

         try {
            for(int i = 0; i < separatedText.length; ++i) {
               result[i] = Long.parseLong(separatedText[i]);
            }

            return result;
         } catch (NumberFormatException var5) {
            _log.log(Level.SEVERE, "StringArrayUtils: Error while convert string to long array.", (Throwable)var5);
            return new long[0];
         }
      } else {
         return new long[0];
      }
   }

   public static long[][] stringToLong2X(String text, String separator1, String separator2) {
      if (text != null && !text.isEmpty()) {
         String[] separatedText = text.split(separator1);
         long[][] result = new long[separatedText.length][];

         for(int i = 0; i < separatedText.length; ++i) {
            result[i] = stringToLongArray(separatedText[i], separator2);
         }

         return result;
      } else {
         return new long[0][];
      }
   }
}
