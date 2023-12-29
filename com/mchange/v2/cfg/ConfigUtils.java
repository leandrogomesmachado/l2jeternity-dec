package com.mchange.v2.cfg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map.Entry;

final class ConfigUtils {
   private static final String[] DFLT_VM_RSRC_PATHFILES = new String[]{"/com/mchange/v2/cfg/vmConfigResourcePaths.txt", "/mchange-config-resource-paths.txt"};
   private static final String[] HARDCODED_DFLT_RSRC_PATHS = new String[]{"/mchange-commons.properties", "hocon:/reference,/application,/", "/"};
   static final String[] NO_PATHS = new String[0];
   static MultiPropertiesConfig vmConfig = null;

   static MultiPropertiesConfig read(String[] var0, List var1) {
      return new BasicMultiPropertiesConfig(var0, var1);
   }

   public static MultiPropertiesConfig read(String[] var0) {
      return new BasicMultiPropertiesConfig(var0);
   }

   public static MultiPropertiesConfig combine(MultiPropertiesConfig[] var0) {
      return new CombinedMultiPropertiesConfig(var0).toBasic();
   }

   public static MultiPropertiesConfig readVmConfig(String[] var0, String[] var1) {
      return readVmConfig(var0, var1, (List)null);
   }

   static List vmCondensedPaths(String[] var0, String[] var1, List var2) {
      return condensePaths(new String[][]{var0, vmResourcePaths(var2), var1});
   }

   static String stringFromPathsList(List var0) {
      StringBuffer var1 = new StringBuffer(2048);
      int var2 = 0;

      for(int var3 = var0.size(); var2 < var3; ++var2) {
         if (var2 != 0) {
            var1.append(", ");
         }

         var1.append(var0.get(var2));
      }

      return var1.toString();
   }

   public static MultiPropertiesConfig readVmConfig(String[] var0, String[] var1, List var2) {
      var0 = var0 == null ? NO_PATHS : var0;
      var1 = var1 == null ? NO_PATHS : var1;
      List var3 = vmCondensedPaths(var0, var1, var2);
      if (var2 != null) {
         var2.add(new DelayedLogItem(DelayedLogItem.Level.FINER, "Reading VM config for path list " + stringFromPathsList(var3)));
      }

      return read(var3.toArray(new String[var3.size()]), var2);
   }

   private static List condensePaths(String[][] var0) {
      HashSet var1 = new HashSet();
      ArrayList var2 = new ArrayList();
      int var3 = var0.length;

      while(--var3 >= 0) {
         int var4 = var0[var3].length;

         while(--var4 >= 0) {
            String var5 = var0[var3][var4];
            if (!var1.contains(var5)) {
               var1.add(var5);
               var2.add(var5);
            }
         }
      }

      Collections.reverse(var2);
      return var2;
   }

   private static List readResourcePathsFromResourcePathsTextFile(String var0, List var1) {
      ArrayList var2 = new ArrayList();
      BufferedReader var3 = null;

      try {
         InputStream var4 = MultiPropertiesConfig.class.getResourceAsStream(var0);
         if (var4 != null) {
            var3 = new BufferedReader(new InputStreamReader(var4, "8859_1"));

            String var5;
            while((var5 = var3.readLine()) != null) {
               var5 = var5.trim();
               if (!"".equals(var5) && !var5.startsWith("#")) {
                  var2.add(var5);
               }
            }

            if (var1 != null) {
               var1.add(new DelayedLogItem(DelayedLogItem.Level.FINEST, String.format("Added paths from resource path text file at '%s'", var0)));
            }
         } else if (var1 != null) {
            var1.add(new DelayedLogItem(DelayedLogItem.Level.FINEST, String.format("Could not find resource path text file for path '%s'. Skipping.", var0)));
         }
      } catch (IOException var14) {
         var14.printStackTrace();
      } finally {
         try {
            if (var3 != null) {
               var3.close();
            }
         } catch (IOException var13) {
            var13.printStackTrace();
         }
      }

      return var2;
   }

   private static List readResourcePathsFromResourcePathsTextFiles(String[] var0, List var1) {
      ArrayList var2 = new ArrayList();
      int var3 = 0;

      for(int var4 = var0.length; var3 < var4; ++var3) {
         var2.addAll(readResourcePathsFromResourcePathsTextFile(var0[var3], var1));
      }

      return var2;
   }

   private static String[] vmResourcePaths(List var0) {
      List var1 = vmResourcePathList(var0);
      return var1.toArray(new String[var1.size()]);
   }

   private static List vmResourcePathList(List var0) {
      List var1 = readResourcePathsFromResourcePathsTextFiles(DFLT_VM_RSRC_PATHFILES, var0);
      List var2;
      if (var1.size() > 0) {
         var2 = var1;
      } else {
         var2 = Arrays.asList(HARDCODED_DFLT_RSRC_PATHS);
      }

      return var2;
   }

   public static synchronized MultiPropertiesConfig readVmConfig() {
      return readVmConfig((List)null);
   }

   public static synchronized MultiPropertiesConfig readVmConfig(List var0) {
      if (vmConfig == null) {
         List var1 = vmResourcePathList(var0);
         vmConfig = new BasicMultiPropertiesConfig(var1.toArray(new String[var1.size()]));
      }

      return vmConfig;
   }

   public static synchronized boolean foundVmConfig() {
      return vmConfig != null;
   }

   public static void dumpByPrefix(MultiPropertiesConfig var0, String var1) {
      Properties var2 = var0.getPropertiesByPrefix(var1);
      TreeMap var3 = new TreeMap();
      var3.putAll(var2);

      for(Entry var5 : var3.entrySet()) {
         System.err.println(var5.getKey() + " --> " + var5.getValue());
      }
   }

   private ConfigUtils() {
   }
}
