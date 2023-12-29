package com.mchange.net;

import com.mchange.util.RobustMessageLogger;
import java.io.IOException;
import java.net.Socket;

/** @deprecated */
public final class SocketUtils {
   public static void attemptClose(Socket var0) {
      attemptClose(var0, null);
   }

   public static void attemptClose(Socket var0, RobustMessageLogger var1) {
      try {
         var0.close();
      } catch (IOException var3) {
         if (var1 != null) {
            var1.log(var3, "IOException trying to close Socket");
         }
      } catch (NullPointerException var4) {
         if (var1 != null) {
            var1.log(var4, "NullPointerException trying to close Socket");
         }
      }
   }

   private SocketUtils() {
   }
}
