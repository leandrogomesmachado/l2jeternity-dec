package com.mchange.util.impl;

import com.mchange.util.CommandLineParser;

/** @deprecated */
public class CommandLineParserImpl implements CommandLineParser {
   String[] argv;
   String[] validSwitches;
   String[] reqSwitches;
   String[] argSwitches;
   char switch_char;

   public CommandLineParserImpl(String[] var1, String[] var2, String[] var3, String[] var4, char var5) {
      this.argv = var1;
      this.validSwitches = var2 == null ? new String[0] : var2;
      this.reqSwitches = var3 == null ? new String[0] : var3;
      this.argSwitches = var4 == null ? new String[0] : var4;
      this.switch_char = var5;
   }

   public CommandLineParserImpl(String[] var1, String[] var2, String[] var3, String[] var4) {
      this(var1, var2, var3, var4, '-');
   }

   @Override
   public boolean checkSwitch(String var1) {
      for(int var2 = 0; var2 < this.argv.length; ++var2) {
         if (this.argv[var2].charAt(0) == this.switch_char && this.argv[var2].equals(this.switch_char + var1)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public String findSwitchArg(String var1) {
      for(int var2 = 0; var2 < this.argv.length - 1; ++var2) {
         if (this.argv[var2].charAt(0) == this.switch_char && this.argv[var2].equals(this.switch_char + var1)) {
            return this.argv[var2 + 1].charAt(0) == this.switch_char ? null : this.argv[var2 + 1];
         }
      }

      return null;
   }

   @Override
   public boolean checkArgv() {
      return this.checkValidSwitches() && this.checkRequiredSwitches() && this.checkSwitchArgSyntax();
   }

   boolean checkValidSwitches() {
      label27:
      for(int var1 = 0; var1 < this.argv.length; ++var1) {
         if (this.argv[var1].charAt(0) == this.switch_char) {
            for(int var2 = 0; var2 < this.validSwitches.length; ++var2) {
               if (this.argv[var1].equals(this.switch_char + this.validSwitches[var2])) {
                  continue label27;
               }
            }

            return false;
         }
      }

      return true;
   }

   boolean checkRequiredSwitches() {
      int var1 = this.reqSwitches.length;

      while(--var1 >= 0) {
         if (!this.checkSwitch(this.reqSwitches[var1])) {
            return false;
         }
      }

      return true;
   }

   boolean checkSwitchArgSyntax() {
      int var1 = this.argSwitches.length;

      while(--var1 >= 0) {
         if (this.checkSwitch(this.argSwitches[var1])) {
            String var2 = this.findSwitchArg(this.argSwitches[var1]);
            if (var2 == null || var2.charAt(0) == this.switch_char) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   public int findLastSwitched() {
      int var1 = this.argv.length;

      while(--var1 >= 0) {
         if (this.argv[var1].charAt(0) == this.switch_char) {
            return var1;
         }
      }

      return -1;
   }

   @Override
   public String[] findUnswitchedArgs() {
      String[] var1 = new String[this.argv.length];
      int var2 = 0;

      for(int var3 = 0; var3 < this.argv.length; ++var3) {
         if (this.argv[var3].charAt(0) == this.switch_char) {
            if (contains(this.argv[var3].substring(1), this.argSwitches)) {
               ++var3;
            }
         } else {
            var1[var2++] = this.argv[var3];
         }
      }

      String[] var4 = new String[var2];
      System.arraycopy(var1, 0, var4, 0, var2);
      return var4;
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
