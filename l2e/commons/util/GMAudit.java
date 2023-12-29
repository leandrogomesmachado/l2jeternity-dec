package l2e.commons.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.lib.Log;
import l2e.gameserver.Config;

public class GMAudit {
   private static final Logger _log = Logger.getLogger(Log.class.getName());

   public static void auditGMAction(String gmName, String action, String target, String params) {
      SimpleDateFormat _formatter = new SimpleDateFormat("dd/MM/yyyy H:mm:ss");
      String date = _formatter.format(new Date());
      String name = Util.replaceIllegalCharacters(gmName);
      if (!Util.isValidFileName(name)) {
         name = "INVALID_GM_NAME_" + date;
      }

      File file = new File("log/GMAudit/" + name + ".txt");

      try (FileWriter save = new FileWriter(file, true)) {
         save.write(date + ">" + gmName + ">" + action + ">" + target + ">" + params + Config.EOL);
      } catch (IOException var21) {
         _log.log(Level.SEVERE, "GMAudit for GM " + gmName + " could not be saved: ", (Throwable)var21);
      }
   }

   public static void auditGMAction(String gmName, String action, String target) {
      auditGMAction(gmName, action, target, "");
   }

   static {
      new File("log/GMAudit").mkdirs();
   }
}
