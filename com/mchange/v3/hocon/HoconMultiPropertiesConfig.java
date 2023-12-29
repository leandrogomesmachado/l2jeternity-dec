package com.mchange.v3.hocon;

import com.mchange.v2.cfg.DelayedLogItem;
import com.mchange.v2.cfg.MultiPropertiesConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

public class HoconMultiPropertiesConfig extends MultiPropertiesConfig {
   String quasiResourcePath;
   Properties props;
   List<DelayedLogItem> delayedLogItems = new LinkedList<>();
   Map<String, Properties> propsByPrefix = new HashMap<>();

   public HoconMultiPropertiesConfig(String var1, Config var2) {
      this.quasiResourcePath = var1;
      this.props = this.propsForConfig(var2);
   }

   private Properties propsForConfig(Config var1) {
      Properties var2 = new Properties();

      for(Entry var4 : var1.entrySet()) {
         try {
            var2.put(var4.getKey(), asSimpleString((ConfigValue)var4.getValue()));
         } catch (IllegalArgumentException var6) {
            this.delayedLogItems.add(new DelayedLogItem(DelayedLogItem.Level.FINE, "For property '" + (String)var4.getKey() + "', " + var6.getMessage()));
         }
      }

      return var2;
   }

   private static String asSimpleString(ConfigValue var0) throws IllegalArgumentException {
      ConfigValueType var1 = var0.valueType();
      switch(var1) {
         case BOOLEAN:
         case NUMBER:
         case STRING:
            return String.valueOf(var0.unwrapped());
         case LIST:
            ConfigList var2 = (ConfigList)var0;

            for(ConfigValue var4 : var2) {
               if (!isSimple(var4)) {
                  throw new IllegalArgumentException("value is a complex list, could not be rendered as a simple property: " + var0);
               }
            }

            StringBuilder var6 = new StringBuilder();
            int var7 = 0;

            for(int var5 = var2.size(); var7 < var5; ++var7) {
               if (var7 != 0) {
                  var6.append(',');
               }

               var6.append(asSimpleString((ConfigValue)var2.get(var7)));
            }

            return var6.toString();
         case OBJECT:
            throw new IllegalArgumentException("value is a ConfigValue object rather than an atom or list of atoms: " + var0);
         case NULL:
            throw new IllegalArgumentException("value is a null; will be excluded from the MultiPropertiesConfig: " + var0);
         default:
            throw new IllegalArgumentException("value of an unexpected type: (value->" + var0 + ", type->" + var1 + ")");
      }
   }

   private static boolean isSimple(ConfigValue var0) {
      ConfigValueType var1 = var0.valueType();
      switch(var1) {
         case BOOLEAN:
         case NUMBER:
         case STRING:
            return true;
         default:
            return false;
      }
   }

   @Override
   public String[] getPropertiesResourcePaths() {
      return new String[]{this.quasiResourcePath};
   }

   @Override
   public Properties getPropertiesByResourcePath(String var1) {
      if (var1.equals(this.quasiResourcePath)) {
         Properties var2 = new Properties();
         var2.putAll(this.props);
         return var2;
      } else {
         return null;
      }
   }

   @Override
   public synchronized Properties getPropertiesByPrefix(String var1) {
      Properties var2 = this.propsByPrefix.get(var1);
      if (var2 == null) {
         var2 = new Properties();
         if ("".equals(var1)) {
            var2.putAll(this.props);
         } else {
            String var3 = var1 + '.';

            for(Entry var5 : this.props.entrySet()) {
               String var6 = (String)var5.getKey();
               if (var6.startsWith(var3)) {
                  var2.put(var6, var5.getValue());
               }
            }
         }

         this.propsByPrefix.put(var1, var2);
      }

      return var2;
   }

   @Override
   public String getProperty(String var1) {
      return (String)this.props.get(var1);
   }

   @Override
   public List getDelayedLogItems() {
      return this.delayedLogItems;
   }
}
