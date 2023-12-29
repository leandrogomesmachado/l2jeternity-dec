package com.mchange.v2.log.jdk14logging;

import com.mchange.v2.log.LogUtils;
import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLogger;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class ForwardingLogger extends Logger {
   MLogger forwardTo;

   public ForwardingLogger(MLogger var1, String var2) {
      super(var1.getName(), var2);
      this.forwardTo = var1;
   }

   @Override
   public void log(LogRecord var1) {
      Level var2 = var1.getLevel();
      MLevel var3 = Jdk14LoggingUtils.mlevelFromLevel(var2);
      String var4 = var1.getResourceBundleName();
      String var5 = var1.getMessage();
      Object[] var6 = var1.getParameters();
      String var7 = LogUtils.formatMessage(var4, var5, var6);
      Throwable var8 = var1.getThrown();
      String var9 = var1.getSourceClassName();
      String var10 = var1.getSourceMethodName();
      boolean var11 = var9 != null & var10 != null;
      if (!var11) {
         this.forwardTo.log(var3, var7, var8);
      } else {
         this.forwardTo.logp(var3, var9, var10, var7, var8);
      }
   }
}
