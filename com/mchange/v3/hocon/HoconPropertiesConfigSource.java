package com.mchange.v3.hocon;

import com.mchange.v2.cfg.DelayedLogItem;
import com.mchange.v2.cfg.PropertiesConfigSource;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigMergeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class HoconPropertiesConfigSource implements PropertiesConfigSource {
   private static Config extractConfig(ClassLoader var0, String var1, List<DelayedLogItem> var2) throws FileNotFoundException, Exception {
      int var3 = var1.indexOf(58);
      ArrayList var4 = new ArrayList();
      if (var3 >= 0 && "hocon".equals(var1.substring(0, var3).toLowerCase())) {
         String var5 = var1.substring(var3 + 1).trim();
         String[] var6 = var5.split("\\s*,\\s*");

         for(String var10 : var6) {
            int var13 = var10.lastIndexOf(35);
            String var11;
            String var12;
            if (var13 > 0) {
               var11 = var10.substring(0, var13);
               var12 = var10.substring(var13 + 1).replace('/', '.').trim();
            } else {
               var11 = var10;
               var12 = null;
            }

            Config var14 = null;
            if ("/".equals(var11)) {
               var14 = ConfigFactory.systemProperties();
            } else {
               Config var15 = null;
               if ("application".equals(var11) || "/application".equals(var11)) {
                  String var16;
                  if ((var16 = System.getProperty("config.resource")) != null) {
                     var11 = var16;
                  } else if ((var16 = System.getProperty("config.file")) != null) {
                     File var17 = new File(var16);
                     if (var17.exists()) {
                        if (var17.canRead()) {
                           var15 = ConfigFactory.parseFile(var17);
                        } else {
                           var2.add(
                              new DelayedLogItem(
                                 DelayedLogItem.Level.WARNING,
                                 String.format(
                                    "Specified config.file '%s' is not readable. Falling back to standard application.(conf|json|properties).}",
                                    var17.getAbsolutePath()
                                 )
                              )
                           );
                        }
                     } else {
                        var2.add(
                           new DelayedLogItem(
                              DelayedLogItem.Level.WARNING,
                              String.format(
                                 "Specified config.file '%s' does not exist. Falling back to standard application.(conf|json|properties).}",
                                 var17.getAbsolutePath()
                              )
                           )
                        );
                     }
                  } else if ((var16 = System.getProperty("config.url")) != null) {
                     var15 = ConfigFactory.parseURL(new URL(var16));
                  }
               }

               if (var15 == null) {
                  if (var11.charAt(0) == '/') {
                     var11 = var11.substring(1);
                  }

                  boolean var22 = var11.indexOf(46) >= 0;
                  if (var22) {
                     var15 = ConfigFactory.parseResources(var0, var11);
                  } else {
                     var15 = ConfigFactory.parseResourcesAnySyntax(var0, var11);
                  }
               }

               if (var15.isEmpty()) {
                  var2.add(new DelayedLogItem(DelayedLogItem.Level.FINE, String.format("Missing or empty HOCON configuration for resource path '%s'.", var11)));
               } else {
                  var14 = var15;
               }
            }

            if (var14 != null) {
               if (var12 != null) {
                  var14 = var14.getConfig(var12);
               }

               var4.add(var14);
            }
         }

         if (var4.size() == 0) {
            throw new FileNotFoundException(String.format("Could not find HOCON configuration at any of the listed resources in '%s'", var1));
         } else {
            Config var18 = ConfigFactory.empty();
            int var19 = var4.size();

            while(--var19 >= 0) {
               var18 = var18.withFallback((ConfigMergeable)var4.get(var19));
            }

            return var18.resolve();
         }
      } else {
         throw new IllegalArgumentException(String.format("Invalid resource identifier for hocon config file: '%s'", var1));
      }
   }

   public PropertiesConfigSource.Parse propertiesFromSource(ClassLoader var1, String var2) throws FileNotFoundException, Exception {
      LinkedList var3 = new LinkedList();
      Config var4 = extractConfig(var1, var2, var3);
      HoconUtils.PropertiesConversion var5 = HoconUtils.configToProperties(var4);

      for(String var7 : var5.unrenderable) {
         var3.add(new DelayedLogItem(DelayedLogItem.Level.FINE, String.format("Value at path '%s' could not be converted to a String. Skipping.", var7)));
      }

      return new PropertiesConfigSource.Parse(var5.properties, var3);
   }

   @Override
   public PropertiesConfigSource.Parse propertiesFromSource(String var1) throws FileNotFoundException, Exception {
      return this.propertiesFromSource(HoconPropertiesConfigSource.class.getClassLoader(), var1);
   }
}
