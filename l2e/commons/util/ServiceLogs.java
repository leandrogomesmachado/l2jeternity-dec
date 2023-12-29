package l2e.commons.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.lib.Log;
import l2e.gameserver.Config;

public final class ServiceLogs {
   private static final Logger _log = Logger.getLogger(Log.class.getName());

   public static void addServiceLogs(String message) {
      if (message != null && !message.isEmpty()) {
         message = message + Config.EOL;
         File file = new File("log/game/serviceLogs.txt");

         try (FileWriter save = new FileWriter(file, true)) {
            save.write(message);
         } catch (IOException var15) {
            _log.log(Level.SEVERE, "ServiceLogs could not be saved: ", (Throwable)var15);
         }
      }
   }
}
