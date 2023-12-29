package l2e.gameserver.model.strings.server;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import l2e.commons.util.Files;
import l2e.gameserver.Config;

public class ServerStorage {
   private static final Logger _log = Logger.getLogger(ServerStorage.class.getName());
   private static final Pattern LINE_PATTERN = Pattern.compile("^(((?!=).)+)=(.*?)$");
   private final HashMap<String, HashMap<String, String>> _sysmessages = new HashMap<>();

   protected ServerStorage() {
      this._sysmessages.clear();
      this.reload();
   }

   public String getString(String lang, String name) {
      if (lang == null) {
         lang = "en";
      }

      if (this._sysmessages.get(lang) == null) {
         return "";
      } else {
         return this._sysmessages.get(lang).get(name) == null ? "" : this._sysmessages.get(lang).get(name);
      }
   }

   private void reload() {
      File dir = new File(Config.DATAPACK_ROOT, "data/localization/");

      for(File file : dir.listFiles()) {
         if (file.isDirectory() && !file.isHidden()) {
            String lang = file.getName();
            HashMap<String, String> map = new HashMap<>();
            this.readFromDisk(map, lang);
            this._sysmessages.put(lang, map);
            _log.info("ServerStorage: Loading " + map.size() + " server messages for [" + lang + "] lang.");
         }
      }
   }

   private void readFromDisk(HashMap<String, String> map, String lang) {
      Scanner scanner = null;

      try {
         File file = new File(Config.DATAPACK_ROOT, "data/localization/" + lang + "/messages.txt");
         String content = Files.readFile(file);
         scanner = new Scanner(content);
         int i = 0;

         while(scanner.hasNextLine()) {
            ++i;
            String line = scanner.nextLine();
            if (!line.startsWith("#")) {
               Matcher m = LINE_PATTERN.matcher(line);
               if (m.find()) {
                  String name = m.group(1);
                  String value = m.group(3);
                  map.put(name, value);
               } else {
                  _log.warning("Error on line #: " + i + "; file: " + file.getName());
               }
            }
         }
      } catch (IOException var19) {
         _log.log(Level.SEVERE, "Error loading \"" + lang + "\" language pack: ", (Throwable)var19);
      } finally {
         try {
            scanner.close();
         } catch (Exception var18) {
         }
      }
   }

   public static ServerStorage getInstance() {
      return ServerStorage.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final ServerStorage _instance = new ServerStorage();
   }
}
