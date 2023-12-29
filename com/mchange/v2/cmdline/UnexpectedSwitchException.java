package com.mchange.v2.cmdline;

public class UnexpectedSwitchException extends BadCommandLineException {
   String sw;

   UnexpectedSwitchException(String var1, String var2) {
      super(var1);
      this.sw = var2;
   }

   public String getUnexpectedSwitch() {
      return this.sw;
   }
}
