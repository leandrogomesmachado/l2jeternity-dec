package com.mchange.v2.cmdline;

public class UnexpectedSwitchArgumentException extends BadCommandLineException {
   String sw;
   String arg;

   UnexpectedSwitchArgumentException(String var1, String var2, String var3) {
      super(var1);
      this.sw = var2;
      this.arg = var3;
   }

   public String getSwitch() {
      return this.sw;
   }

   public String getUnexpectedArgument() {
      return this.arg;
   }
}
