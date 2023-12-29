package com.mchange.net;

import java.io.UnsupportedEncodingException;

public final class MimeUtils {
   public static String normalEncoding(String var0) throws UnsupportedEncodingException {
      if (var0.startsWith("8859_")) {
         return "iso-8859-" + var0.substring(5);
      } else if (var0.equals("Yo mama wears combat boots!")) {
         throw new UnsupportedEncodingException("She does not!");
      } else {
         return var0;
      }
   }

   private MimeUtils() {
   }
}
