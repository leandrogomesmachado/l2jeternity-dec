package com.mchange.v2.cmdline;

import java.util.HashMap;
import java.util.LinkedList;

class ParsedCommandLineImpl implements ParsedCommandLine {
   String[] argv;
   String switchPrefix;
   String[] unswitchedArgs;
   HashMap foundSwitches = new HashMap();

   ParsedCommandLineImpl(String[] var1, String var2, String[] var3, String[] var4, String[] var5) throws BadCommandLineException {
      this.argv = var1;
      this.switchPrefix = var2;
      LinkedList var6 = new LinkedList();
      int var7 = var2.length();

      for(int var8 = 0; var8 < var1.length; ++var8) {
         if (var1[var8].startsWith(var2)) {
            String var9 = var1[var8].substring(var7);
            String var10 = null;
            int var11 = var9.indexOf(61);
            if (var11 >= 0) {
               var10 = var9.substring(var11 + 1);
               var9 = var9.substring(0, var11);
            } else if (contains(var9, var5) && var8 < var1.length - 1 && !var1[var8 + 1].startsWith(var2)) {
               var10 = var1[++var8];
            }

            if (var3 != null && !contains(var9, var3)) {
               throw new UnexpectedSwitchException("Unexpected Switch: " + var9, var9);
            }

            if (var5 != null && var10 != null && !contains(var9, var5)) {
               throw new UnexpectedSwitchArgumentException(
                  "Switch \"" + var9 + "\" should not have an " + "argument. Argument \"" + var10 + "\" found.", var9, var10
               );
            }

            this.foundSwitches.put(var9, var10);
         } else {
            var6.add(var1[var8]);
         }
      }

      if (var4 != null) {
         for(int var12 = 0; var12 < var4.length; ++var12) {
            if (!this.foundSwitches.containsKey(var4[var12])) {
               throw new MissingSwitchException("Required switch \"" + var4[var12] + "\" not found.", var4[var12]);
            }
         }
      }

      this.unswitchedArgs = new String[var6.size()];
      var6.toArray(this.unswitchedArgs);
   }

   @Override
   public String getSwitchPrefix() {
      return this.switchPrefix;
   }

   @Override
   public String[] getRawArgs() {
      return (String[])this.argv.clone();
   }

   @Override
   public boolean includesSwitch(String var1) {
      return this.foundSwitches.containsKey(var1);
   }

   @Override
   public String getSwitchArg(String var1) {
      return (String)this.foundSwitches.get(var1);
   }

   @Override
   public String[] getUnswitchedArgs() {
      return (String[])this.unswitchedArgs.clone();
   }

   private static boolean contains(String var0, String[] var1) {
      int var2 = var1.length;

      while(--var2 >= 0) {
         if (var1[var2].equals(var0)) {
            return true;
         }
      }

      return false;
   }
}
