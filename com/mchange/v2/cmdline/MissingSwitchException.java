package com.mchange.v2.cmdline;

public class MissingSwitchException extends BadCommandLineException {
   String sw;

   MissingSwitchException(String var1, String var2) {
      super(var1);
      this.sw = var2;
   }

   public String getMissingSwitch() {
      return this.sw;
   }
}
