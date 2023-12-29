package com.mchange.io;

import com.mchange.util.RobustMessageLogger;
import java.io.IOException;
import java.io.Reader;

public final class ReaderUtils {
   public static void attemptClose(Reader var0) {
      attemptClose(var0, null);
   }

   public static void attemptClose(Reader var0, RobustMessageLogger var1) {
      try {
         var0.close();
      } catch (IOException var3) {
         if (var1 != null) {
            var1.log(var3, "IOException trying to close Reader");
         }
      } catch (NullPointerException var4) {
         if (var1 != null) {
            var1.log(var4, "NullPointerException trying to close Reader");
         }
      }
   }

   private ReaderUtils() {
   }
}
