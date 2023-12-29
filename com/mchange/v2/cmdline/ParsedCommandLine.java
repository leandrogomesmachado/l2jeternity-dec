package com.mchange.v2.cmdline;

public interface ParsedCommandLine {
   String[] getRawArgs();

   String getSwitchPrefix();

   boolean includesSwitch(String var1);

   String getSwitchArg(String var1);

   String[] getUnswitchedArgs();
}
