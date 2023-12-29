package com.mchange.v2.cmdline;

public final class CommandLineUtils {
   public static ParsedCommandLine parse(String[] var0, String var1, String[] var2, String[] var3, String[] var4) throws BadCommandLineException {
      return new ParsedCommandLineImpl(var0, var1, var2, var3, var4);
   }

   private CommandLineUtils() {
   }
}
