package com.mchange.v1.io;

import java.io.IOException;
import java.io.Writer;

public final class WriterUtils {
   public static void attemptClose(Writer var0) {
      try {
         if (var0 != null) {
            var0.close();
         }
      } catch (IOException var2) {
         var2.printStackTrace();
      }
   }

   private WriterUtils() {
   }
}
