package com.mchange.v1.io;

import java.io.IOException;
import java.io.Reader;

public final class ReaderUtils {
   public static void attemptClose(Reader var0) {
      try {
         if (var0 != null) {
            var0.close();
         }
      } catch (IOException var2) {
         var2.printStackTrace();
      }
   }

   private ReaderUtils() {
   }
}
