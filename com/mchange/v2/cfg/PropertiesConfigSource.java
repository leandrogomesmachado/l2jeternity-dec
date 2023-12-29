package com.mchange.v2.cfg;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Properties;

public interface PropertiesConfigSource {
   PropertiesConfigSource.Parse propertiesFromSource(String var1) throws FileNotFoundException, Exception;

   public static class Parse {
      private Properties properties;
      private List<DelayedLogItem> parseMessages;

      public Properties getProperties() {
         return this.properties;
      }

      public List<DelayedLogItem> getDelayedLogItems() {
         return this.parseMessages;
      }

      public Parse(Properties var1, List<DelayedLogItem> var2) {
         this.properties = var1;
         this.parseMessages = var2;
      }
   }
}
