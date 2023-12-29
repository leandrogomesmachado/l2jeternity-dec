package com.mchange.v3.hocon;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException.WrongType;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

public final class HoconUtils {
   public static HoconUtils.PropertiesConversion configToProperties(Config var0) {
      Set var1 = var0.entrySet();
      Properties var2 = new Properties();
      HashSet var3 = new HashSet();

      for(Entry var5 : var1) {
         String var6 = (String)var5.getKey();
         String var7 = null;

         try {
            var7 = var0.getString(var6);
         } catch (WrongType var9) {
            var3.add(var6);
         }

         if (var7 != null) {
            var2.setProperty(var6, var7);
         }
      }

      HoconUtils.PropertiesConversion var10 = new HoconUtils.PropertiesConversion();
      var10.properties = var2;
      var10.unrenderable = var3;
      return var10;
   }

   private HoconUtils() {
   }

   public static class PropertiesConversion {
      Properties properties;
      Set<String> unrenderable;
   }
}
