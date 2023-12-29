package com.mchange.v2.cfg;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

class CombinedMultiPropertiesConfig extends MultiPropertiesConfig {
   MultiPropertiesConfig[] configs;
   String[] resourcePaths;
   List parseMessages;

   CombinedMultiPropertiesConfig(MultiPropertiesConfig[] var1) {
      this.configs = var1;
      LinkedList var2 = new LinkedList();

      for(int var3 = var1.length - 1; var3 >= 0; --var3) {
         String[] var4 = var1[var3].getPropertiesResourcePaths();

         for(int var5 = var4.length - 1; var5 >= 0; --var5) {
            String var6 = var4[var5];
            if (!var2.contains(var6)) {
               var2.add(0, var6);
            }
         }
      }

      this.resourcePaths = var2.toArray(new String[var2.size()]);
      LinkedList var7 = new LinkedList();
      int var8 = 0;

      for(int var9 = var1.length; var8 < var9; ++var8) {
         var7.addAll(var1[var8].getDelayedLogItems());
      }

      this.parseMessages = Collections.unmodifiableList(var7);
   }

   private Map getPropsByResourcePaths() {
      HashMap var1 = new HashMap();
      int var2 = 0;

      for(int var3 = this.resourcePaths.length; var2 < var3; ++var2) {
         String var4 = this.resourcePaths[var2];
         var1.put(var4, this.getPropertiesByResourcePath(var4));
      }

      return Collections.unmodifiableMap(var1);
   }

   public BasicMultiPropertiesConfig toBasic() {
      String[] var1 = this.getPropertiesResourcePaths();
      Map var2 = this.getPropsByResourcePaths();
      List var3 = this.getDelayedLogItems();
      return new BasicMultiPropertiesConfig(var1, var2, var3);
   }

   @Override
   public String[] getPropertiesResourcePaths() {
      return (String[])this.resourcePaths.clone();
   }

   @Override
   public Properties getPropertiesByResourcePath(String var1) {
      Properties var2 = new Properties();
      int var3 = 0;

      for(int var4 = this.configs.length; var3 < var4; ++var3) {
         MultiPropertiesConfig var5 = this.configs[var3];
         Properties var6 = var5.getPropertiesByResourcePath(var1);
         if (var6 != null) {
            var2.putAll(var6);
         }
      }

      return var2.size() > 0 ? var2 : null;
   }

   @Override
   public Properties getPropertiesByPrefix(String var1) {
      LinkedList var2 = new LinkedList();

      for(int var3 = this.configs.length - 1; var3 >= 0; --var3) {
         MultiPropertiesConfig var4 = this.configs[var3];
         Properties var5 = var4.getPropertiesByPrefix(var1);
         if (var5 != null) {
            var2.addAll(0, var5.entrySet());
         }
      }

      if (var2.size() == 0) {
         return null;
      } else {
         Properties var6 = new Properties();

         for(Entry var8 : var2) {
            var6.put(var8.getKey(), var8.getValue());
         }

         return var6;
      }
   }

   @Override
   public String getProperty(String var1) {
      for(int var2 = this.configs.length - 1; var2 >= 0; --var2) {
         MultiPropertiesConfig var3 = this.configs[var2];
         String var4 = var3.getProperty(var1);
         if (var4 != null) {
            return var4;
         }
      }

      return null;
   }

   @Override
   public List getDelayedLogItems() {
      return this.parseMessages;
   }
}
