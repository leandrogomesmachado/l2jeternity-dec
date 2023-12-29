package com.mchange.util;

/** @deprecated */
public interface CommandLineParser {
   boolean checkSwitch(String var1);

   String findSwitchArg(String var1);

   boolean checkArgv();

   int findLastSwitched();

   String[] findUnswitchedArgs();
}
