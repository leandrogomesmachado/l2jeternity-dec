package l2e.commons.util.lib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;

public class Log {
   private static final Logger _log = Logger.getLogger(Log.class.getName());

   public static final void add(String text, String cat) {
      String date = new SimpleDateFormat("yy.MM.dd H:mm:ss").format(new Date());
      String curr = new SimpleDateFormat("yyyy-MM-dd-").format(new Date());
      new File("log/game").mkdirs();
      File file = new File("log/game/" + (curr != null ? curr : "") + (cat != null ? cat : "unk") + ".txt");

      try (FileWriter save = new FileWriter(file, true)) {
         save.write("[" + date + "] " + text + Config.EOL);
      } catch (IOException var18) {
         _log.log(Level.WARNING, "Error saving logfile: ", (Throwable)var18);
      }
   }
}
