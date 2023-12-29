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
import l2e.loginserver.Config;

public final class LoginSettings extends Properties {
   private static final long serialVersionUID = 1L;
   private static Logger _log = Logger.getLogger(LoginSettings.class.getName());

   public LoginSettings() {
   }

   public LoginSettings(String name) throws IOException {
      try (FileInputStream fis = new FileInputStream(name)) {
         this.load(fis);
      }
   }

   public LoginSettings(File file) throws IOException {
      try (FileInputStream fis = new FileInputStream(file)) {
         this.load(fis);
      }
   }

   public LoginSettings(InputStream inStream) throws IOException {
      this.load(inStream);
   }

   public LoginSettings(Reader reader) throws IOException {
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
         if (!str.equalsIgnoreCase("RateXpByLevel") && !str.equalsIgnoreCase("RateSpByLevel")) {
            _log.info("Missing config - " + key);
         }

         return null;
      } else {
         return property.trim();
      }
   }

   @Override
   public String getProperty(String key, String defaultValue) {
      String str = key.replaceAll("\\d", "");
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
