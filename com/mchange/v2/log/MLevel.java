package com.mchange.v2.log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class MLevel {
   public static final MLevel ALL;
   public static final MLevel CONFIG;
   public static final MLevel FINE;
   public static final MLevel FINER;
   public static final MLevel FINEST;
   public static final MLevel INFO;
   public static final MLevel OFF;
   public static final MLevel SEVERE;
   public static final MLevel WARNING;
   private static final Map integersToMLevels;
   private static final Map namesToMLevels;
   private static final int ALL_INTVAL = Integer.MIN_VALUE;
   private static final int CONFIG_INTVAL = 700;
   private static final int FINE_INTVAL = 500;
   private static final int FINER_INTVAL = 400;
   private static final int FINEST_INTVAL = 300;
   private static final int INFO_INTVAL = 800;
   private static final int OFF_INTVAL = Integer.MAX_VALUE;
   private static final int SEVERE_INTVAL = 1000;
   private static final int WARNING_INTVAL = 900;
   Object level;
   int intval;
   String lvlstring;

   public static MLevel fromIntValue(int var0) {
      return (MLevel)integersToMLevels.get(new Integer(var0));
   }

   public static MLevel fromSeverity(String var0) {
      return (MLevel)namesToMLevels.get(var0);
   }

   public int intValue() {
      return this.intval;
   }

   public Object asJdk14Level() {
      return this.level;
   }

   public String getSeverity() {
      return this.lvlstring;
   }

   @Override
   public String toString() {
      return this.getClass().getName() + this.getLineHeader();
   }

   public String getLineHeader() {
      return "[" + this.lvlstring + ']';
   }

   public boolean isLoggable(MLevel var1) {
      return this.intval >= var1.intval;
   }

   private MLevel(Object var1, int var2, String var3) {
      this.level = var1;
      this.intval = var2;
      this.lvlstring = var3;
   }

   static {
      Class var0;
      boolean var1;
      try {
         var0 = Class.forName("java.util.logging.Level");
         var1 = true;
      } catch (ClassNotFoundException var13) {
         var0 = null;
         var1 = false;
      }

      MLevel var2;
      MLevel var3;
      MLevel var4;
      MLevel var5;
      MLevel var6;
      MLevel var7;
      MLevel var8;
      MLevel var9;
      MLevel var10;
      try {
         var2 = new MLevel(var1 ? var0.getField("ALL").get(null) : null, Integer.MIN_VALUE, "ALL");
         var3 = new MLevel(var1 ? var0.getField("CONFIG").get(null) : null, 700, "CONFIG");
         var4 = new MLevel(var1 ? var0.getField("FINE").get(null) : null, 500, "FINE");
         var5 = new MLevel(var1 ? var0.getField("FINER").get(null) : null, 400, "FINER");
         var6 = new MLevel(var1 ? var0.getField("FINEST").get(null) : null, 300, "FINEST");
         var7 = new MLevel(var1 ? var0.getField("INFO").get(null) : null, 800, "INFO");
         var8 = new MLevel(var1 ? var0.getField("OFF").get(null) : null, Integer.MAX_VALUE, "OFF");
         var9 = new MLevel(var1 ? var0.getField("SEVERE").get(null) : null, 1000, "SEVERE");
         var10 = new MLevel(var1 ? var0.getField("WARNING").get(null) : null, 900, "WARNING");
      } catch (Exception var12) {
         var12.printStackTrace();
         throw new InternalError("Huh? java.util.logging.Level is here, but not its expected public fields?");
      }

      ALL = var2;
      CONFIG = var3;
      FINE = var4;
      FINER = var5;
      FINEST = var6;
      INFO = var7;
      OFF = var8;
      SEVERE = var9;
      WARNING = var10;
      HashMap var11 = new HashMap();
      var11.put(new Integer(var2.intValue()), var2);
      var11.put(new Integer(var3.intValue()), var3);
      var11.put(new Integer(var4.intValue()), var4);
      var11.put(new Integer(var5.intValue()), var5);
      var11.put(new Integer(var6.intValue()), var6);
      var11.put(new Integer(var7.intValue()), var7);
      var11.put(new Integer(var8.intValue()), var8);
      var11.put(new Integer(var9.intValue()), var9);
      var11.put(new Integer(var10.intValue()), var10);
      integersToMLevels = Collections.unmodifiableMap(var11);
      var11 = new HashMap();
      var11.put(var2.getSeverity(), var2);
      var11.put(var3.getSeverity(), var3);
      var11.put(var4.getSeverity(), var4);
      var11.put(var5.getSeverity(), var5);
      var11.put(var6.getSeverity(), var6);
      var11.put(var7.getSeverity(), var7);
      var11.put(var8.getSeverity(), var8);
      var11.put(var9.getSeverity(), var9);
      var11.put(var10.getSeverity(), var10);
      namesToMLevels = Collections.unmodifiableMap(var11);
   }
}
