package com.mchange.v2.cfg;

import com.mchange.v3.hocon.HoconPropertiesConfigSource;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

final class BasicMultiPropertiesConfig extends MultiPropertiesConfig {
   private static final String HOCON_CFG_CNAME = "com.typesafe.config.Config";
   private static final int HOCON_PFX_LEN = 6;
   static final BasicMultiPropertiesConfig EMPTY = new BasicMultiPropertiesConfig();
   String[] rps;
   Map propsByResourcePaths;
   Map propsByPrefixes;
   List parseMessages;
   Properties propsByKey;

   static boolean isHoconPath(String var0) {
      return var0.length() > 6 && var0.substring(0, 6).toLowerCase().equals("hocon:");
   }

   private static PropertiesConfigSource configSource(String var0) throws Exception {
      boolean var1 = isHoconPath(var0);
      if (!var1 && !var0.startsWith("/")) {
         throw new IllegalArgumentException(
            String.format(
               "Resource identifier '%s' is neither an absolute resource path nor a HOCON path. (Resource paths should be specified beginning with '/' or 'hocon:/')",
               var0
            )
         );
      } else if (var1) {
         try {
            Class.forName("com.typesafe.config.Config");
            return new HoconPropertiesConfigSource();
         } catch (ClassNotFoundException var5) {
            int var3 = var0.lastIndexOf(35);
            String var4 = var3 > 0 ? var0.substring(6, var3) : var0.substring(6);
            if (BasicMultiPropertiesConfig.class.getResource(var4) == null) {
               throw new FileNotFoundException(
                  String.format("HOCON lib (typesafe-config) is not available. Also, no resource available at '%s' for HOCON identifier '%s'.", var4, var0)
               );
            } else {
               throw new Exception(
                  String.format(
                     "Could not decode HOCON resource '%s', even though the resource exists, because HOCON lib (typesafe-config) is not available.", var0
                  ),
                  var5
               );
            }
         }
      } else {
         return (PropertiesConfigSource)("/".equals(var0) ? new BasicMultiPropertiesConfig.SystemPropertiesConfigSource() : new BasicPropertiesConfigSource());
      }
   }

   public BasicMultiPropertiesConfig(String[] var1) {
      this(var1, null);
   }

   BasicMultiPropertiesConfig(String[] var1, List var2) {
      this.firstInit(var1, var2);
      this.finishInit(var2);
   }

   public BasicMultiPropertiesConfig(String var1, Properties var2) {
      this(new String[]{var1}, resourcePathToPropertiesMap(var1, var2), Collections.emptyList());
   }

   private static Map resourcePathToPropertiesMap(String var0, Properties var1) {
      HashMap var2 = new HashMap();
      var2.put(var0, var1);
      return var2;
   }

   BasicMultiPropertiesConfig(String[] var1, Map var2, List var3) {
      this.rps = var1;
      this.propsByResourcePaths = var2;
      ArrayList var4 = new ArrayList();
      var4.addAll(var3);
      this.finishInit(var4);
      this.parseMessages = var4;
   }

   private BasicMultiPropertiesConfig() {
      this.rps = new String[0];
      Map var1 = Collections.emptyMap();
      Map var2 = Collections.emptyMap();
      List var3 = Collections.emptyList();
      new Properties();
   }

   private void firstInit(String[] var1, List var2) {
      boolean var3 = false;
      if (var2 == null) {
         var2 = new ArrayList();
         var3 = true;
      }

      HashMap var4 = new HashMap();
      ArrayList var5 = new ArrayList();
      int var6 = 0;

      for(int var7 = var1.length; var6 < var7; ++var6) {
         String var8 = var1[var6];

         try {
            PropertiesConfigSource var9 = configSource(var8);
            PropertiesConfigSource.Parse var10 = var9.propertiesFromSource(var8);
            var4.put(var8, var10.getProperties());
            var5.add(var8);
            var2.addAll(var10.getDelayedLogItems());
         } catch (FileNotFoundException var11) {
            var2.add(
               new DelayedLogItem(
                  DelayedLogItem.Level.FINE, String.format("The configuration file for resource identifier '%s' could not be found. Skipping.", var8), var11
               )
            );
         } catch (Exception var12) {
            var2.add(
               new DelayedLogItem(
                  DelayedLogItem.Level.WARNING,
                  String.format("An Exception occurred while trying to read configuration data at resource identifier '%s'.", var8),
                  var12
               )
            );
         }
      }

      this.rps = var5.toArray(new String[var5.size()]);
      this.propsByResourcePaths = Collections.unmodifiableMap(var4);
      this.parseMessages = Collections.unmodifiableList((List)var2);
      if (var3) {
         dumpToSysErr((List)var2);
      }
   }

   private void finishInit(List var1) {
      boolean var2 = false;
      if (var1 == null) {
         var1 = new ArrayList();
         var2 = true;
      }

      this.propsByPrefixes = Collections.unmodifiableMap(extractPrefixMapFromRsrcPathMap(this.rps, this.propsByResourcePaths, (List)var1));
      this.propsByKey = extractPropsByKey(this.rps, this.propsByResourcePaths, (List)var1);
      if (var2) {
         dumpToSysErr((List)var1);
      }
   }

   @Override
   public List getDelayedLogItems() {
      return this.parseMessages;
   }

   private static void dumpToSysErr(List var0) {
      for(Object var2 : var0) {
         System.err.println(var2);
      }
   }

   private static String extractPrefix(String var0) {
      int var1 = var0.lastIndexOf(46);
      if (var1 < 0) {
         return "".equals(var0) ? null : "";
      } else {
         return var0.substring(0, var1);
      }
   }

   private static Properties findProps(String var0, Map var1) {
      return (Properties)var1.get(var0);
   }

   private static Properties extractPropsByKey(String[] var0, Map var1, List var2) {
      Properties var3 = new Properties();
      int var4 = 0;

      for(int var5 = var0.length; var4 < var5; ++var4) {
         String var6 = var0[var4];
         Properties var7 = findProps(var6, var1);
         if (var7 == null) {
            var2.add(
               new DelayedLogItem(
                  DelayedLogItem.Level.WARNING,
                  BasicMultiPropertiesConfig.class.getName() + ".extractPropsByKey(): Could not find loaded properties for resource path: " + var6
               )
            );
         } else {
            for(Object var9 : var7.keySet()) {
               if (!(var9 instanceof String)) {
                  String var13 = BasicMultiPropertiesConfig.class.getName()
                     + ": "
                     + "Properties object found at resource path "
                     + ("/".equals(var6) ? "[system properties]" : "'" + var6 + "'")
                     + "' contains a key that is not a String: "
                     + var9
                     + "; Skipping...";
                  var2.add(new DelayedLogItem(DelayedLogItem.Level.WARNING, var13));
               } else {
                  Object var10 = var7.get(var9);
                  if (var10 != null && !(var10 instanceof String)) {
                     String var14 = BasicMultiPropertiesConfig.class.getName()
                        + ": "
                        + "Properties object found at resource path "
                        + ("/".equals(var6) ? "[system properties]" : "'" + var6 + "'")
                        + " contains a value that is not a String: "
                        + var10
                        + "; Skipping...";
                     var2.add(new DelayedLogItem(DelayedLogItem.Level.WARNING, var14));
                  } else {
                     String var11 = (String)var9;
                     String var12 = (String)var10;
                     var3.put(var11, var12);
                  }
               }
            }
         }
      }

      return var3;
   }

   private static Map extractPrefixMapFromRsrcPathMap(String[] var0, Map var1, List var2) {
      HashMap var3 = new HashMap();
      int var4 = 0;

      for(int var5 = var0.length; var4 < var5; ++var4) {
         String var6 = var0[var4];
         Properties var7 = findProps(var6, var1);
         if (var7 == null) {
            String var13 = BasicMultiPropertiesConfig.class.getName()
               + ".extractPrefixMapFromRsrcPathMap(): Could not find loaded properties for resource path: "
               + var6;
            var2.add(new DelayedLogItem(DelayedLogItem.Level.WARNING, var13));
         } else {
            for(Object var9 : var7.keySet()) {
               if (!(var9 instanceof String)) {
                  String var14 = BasicMultiPropertiesConfig.class.getName()
                     + ": "
                     + "Properties object found at resource path "
                     + ("/".equals(var6) ? "[system properties]" : "'" + var6 + "'")
                     + "' contains a key that is not a String: "
                     + var9
                     + "; Skipping...";
                  var2.add(new DelayedLogItem(DelayedLogItem.Level.WARNING, var14));
               } else {
                  String var10 = (String)var9;

                  for(String var11 = extractPrefix(var10); var11 != null; var11 = extractPrefix(var11)) {
                     Properties var12 = (Properties)var3.get(var11);
                     if (var12 == null) {
                        var12 = new Properties();
                        var3.put(var11, var12);
                     }

                     var12.put(var10, var7.get(var10));
                  }
               }
            }
         }
      }

      return var3;
   }

   @Override
   public String[] getPropertiesResourcePaths() {
      return (String[])this.rps.clone();
   }

   @Override
   public Properties getPropertiesByResourcePath(String var1) {
      Properties var2 = (Properties)this.propsByResourcePaths.get(var1);
      return var2 == null ? new Properties() : var2;
   }

   @Override
   public Properties getPropertiesByPrefix(String var1) {
      Properties var2 = (Properties)this.propsByPrefixes.get(var1);
      return var2 == null ? new Properties() : var2;
   }

   @Override
   public String getProperty(String var1) {
      return this.propsByKey.getProperty(var1);
   }

   public String dump() {
      return String.format("[ propertiesByResourcePaths -> %s, propertiesByPrefixes -> %s ]", this.propsByResourcePaths, this.propsByPrefixes);
   }

   @Override
   public String toString() {
      return super.toString() + " " + this.dump();
   }

   static final class SystemPropertiesConfigSource implements PropertiesConfigSource {
      @Override
      public PropertiesConfigSource.Parse propertiesFromSource(String var1) throws FileNotFoundException, Exception {
         if ("/".equals(var1)) {
            return new PropertiesConfigSource.Parse((Properties)System.getProperties().clone(), Collections.emptyList());
         } else {
            throw new Exception(String.format("Unexpected identifier for System properties: '%s'", var1));
         }
      }
   }
}
