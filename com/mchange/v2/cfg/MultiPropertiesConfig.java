package com.mchange.v2.cfg;

import java.util.List;
import java.util.Properties;

public abstract class MultiPropertiesConfig implements PropertiesConfig {
   /** @deprecated */
   public static MultiPropertiesConfig readVmConfig(String[] var0, String[] var1) {
      return ConfigUtils.readVmConfig(var0, var1);
   }

   /** @deprecated */
   public static MultiPropertiesConfig readVmConfig() {
      return ConfigUtils.readVmConfig();
   }

   public abstract String[] getPropertiesResourcePaths();

   public abstract Properties getPropertiesByResourcePath(String var1);

   @Override
   public abstract Properties getPropertiesByPrefix(String var1);

   @Override
   public abstract String getProperty(String var1);

   public abstract List getDelayedLogItems();
}
