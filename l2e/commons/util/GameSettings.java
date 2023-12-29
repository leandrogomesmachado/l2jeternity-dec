package l2e.commons.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.logging.Logger;
import l2e.gameserver.Config;

public final class GameSettings extends Properties {
   private static final long serialVersionUID = 1L;
   private static Logger _log = Logger.getLogger(GameSettings.class.getName());

   public GameSettings() {
   }

   public GameSettings(String name) throws IOException {
      try (FileInputStream fis = new FileInputStream(name)) {
         this.load(fis);
      }
   }

   public GameSettings(File file) throws IOException {
      try (FileInputStream fis = new FileInputStream(file)) {
         this.load(fis);
      }
   }

   public GameSettings(InputStream inStream) throws IOException {
      this.load(inStream);
   }

   public GameSettings(Reader reader) throws IOException {
      this.load(reader);
   }

   public void load(String name) throws IOException {
      try (FileInputStream fis = new FileInputStream(name)) {
         this.load(fis);
      }
   }

   public void load(File file) throws IOException {
      try (FileInputStream fis = new FileInputStream(file)) {
         this.load(fis);
      }
   }

   @Override
   public void load(InputStream inStream) throws IOException {
      try (InputStreamReader isr = new InputStreamReader(inStream, Charset.defaultCharset())) {
         super.load(isr);
      } finally {
         inStream.close();
      }
   }

   @Override
   public void load(Reader reader) throws IOException {
      try {
         super.load(reader);
      } finally {
         reader.close();
      }
   }

   @Override
   public String getProperty(String key) {
      String property = super.getProperty(key);
      if (property == null) {
         String str = key.replaceAll("\\d", "");
         if (!str.equalsIgnoreCase("RateXpByLevel")
            && str.indexOf("RateSpByLevel") == -1
            && str.indexOf("_PUNISHMENT_LIMIT") == -1
            && str.indexOf("_LOG_FLOODING") == -1
            && str.indexOf("_PUNISHMENT_TYPE") == -1
            && str.indexOf("_PUNISHMENT_TIME") == -1) {
            _log.info("Missing config - " + key);
         }

         return null;
      } else {
         return property.trim();
      }
   }

   @Override
   public String getProperty(String key, String defaultValue) {
      if (Config.getPersonalConfigs().containsKey(key)) {
         return Config.getPersonalConfigs().get(key);
      } else {
         String property = super.getProperty(key, defaultValue);
         if (property == null) {
            _log.warning("Missing parameter for config - " + key);
            return null;
         } else {
            return property.trim();
         }
      }
   }
}
