package com.mchange.v2.log.slf4j;

import com.mchange.v2.log.FallbackMLog;
import com.mchange.v2.log.LogUtils;
import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import com.mchange.v2.log.NullMLogger;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Slf4jMLog extends MLog {
   static final Object[] EMPTY_OBJ_ARRAY = new Object[0];
   private static final int ALL_INTVAL = MLevel.ALL.intValue();
   private static final int CONFIG_INTVAL = MLevel.CONFIG.intValue();
   private static final int FINE_INTVAL = MLevel.FINE.intValue();
   private static final int FINER_INTVAL = MLevel.FINER.intValue();
   private static final int FINEST_INTVAL = MLevel.FINEST.intValue();
   private static final int INFO_INTVAL = MLevel.INFO.intValue();
   private static final int OFF_INTVAL = MLevel.OFF.intValue();
   private static final int SEVERE_INTVAL = MLevel.SEVERE.intValue();
   private static final int WARNING_INTVAL = MLevel.WARNING.intValue();
   static final String CHECK_CLASS = "org.slf4j.LoggerFactory";
   static final String DFLT_LOGGER_NAME = "global";

   public Slf4jMLog() throws ClassNotFoundException {
      Class.forName("org.slf4j.LoggerFactory");
   }

   @Override
   public MLogger getMLogger(String var1) {
      Logger var2 = LoggerFactory.getLogger(var1);
      if (var2 == null) {
         this.fallbackWarn(" with name '" + var1 + "'");
         return NullMLogger.instance();
      } else {
         return new Slf4jMLog.Slf4jMLogger(var2);
      }
   }

   @Override
   public MLogger getMLogger() {
      Logger var1 = LoggerFactory.getLogger("global");
      if (var1 == null) {
         this.fallbackWarn(" (default, with name 'global')");
         return NullMLogger.instance();
      } else {
         return new Slf4jMLog.Slf4jMLogger(var1);
      }
   }

   private void fallbackWarn(String var1) {
      FallbackMLog.getLogger()
         .warning(
            "Could not create or find slf4j Logger"
               + var1
               + ". "
               + "Using NullMLogger. All messages sent to this"
               + "logger will be silently ignored. You might want to fix this."
         );
   }

   private static final class Slf4jMLogger implements MLogger {
      static final String FQCN = Slf4jMLog.Slf4jMLogger.class.getName();
      final Logger logger;
      final Slf4jMLog.Slf4jMLogger.LevelLogger traceL;
      final Slf4jMLog.Slf4jMLogger.LevelLogger debugL;
      final Slf4jMLog.Slf4jMLogger.LevelLogger infoL;
      final Slf4jMLog.Slf4jMLogger.LevelLogger warnL;
      final Slf4jMLog.Slf4jMLogger.LevelLogger errorL;
      final Slf4jMLog.Slf4jMLogger.LevelLogger offL;
      MLevel myLevel = null;

      Slf4jMLogger(Logger var1) {
         this.logger = var1;
         this.traceL = new Slf4jMLog.Slf4jMLogger.TraceLogger();
         this.debugL = new Slf4jMLog.Slf4jMLogger.DebugLogger();
         this.infoL = new Slf4jMLog.Slf4jMLogger.InfoLogger();
         this.warnL = new Slf4jMLog.Slf4jMLogger.WarnLogger();
         this.errorL = new Slf4jMLog.Slf4jMLogger.ErrorLogger();
         this.offL = new Slf4jMLog.Slf4jMLogger.OffLogger();
      }

      private MLevel guessMLevel() {
         if (this.logger.isErrorEnabled()) {
            return MLevel.SEVERE;
         } else if (this.logger.isWarnEnabled()) {
            return MLevel.WARNING;
         } else if (this.logger.isInfoEnabled()) {
            return MLevel.INFO;
         } else if (this.logger.isDebugEnabled()) {
            return MLevel.FINER;
         } else {
            return this.logger.isTraceEnabled() ? MLevel.FINEST : MLevel.OFF;
         }
      }

      private synchronized boolean myLevelIsLoggable(int var1) {
         return this.myLevel == null || var1 >= this.myLevel.intValue();
      }

      private Slf4jMLog.Slf4jMLogger.LevelLogger levelLogger(MLevel var1) {
         int var2 = var1.intValue();
         if (!this.myLevelIsLoggable(var2)) {
            return this.offL;
         } else if (var2 >= Slf4jMLog.SEVERE_INTVAL) {
            return this.errorL;
         } else if (var2 >= Slf4jMLog.WARNING_INTVAL) {
            return this.warnL;
         } else if (var2 >= Slf4jMLog.INFO_INTVAL) {
            return this.infoL;
         } else if (var2 >= Slf4jMLog.FINER_INTVAL) {
            return this.debugL;
         } else {
            return var2 >= Slf4jMLog.FINEST_INTVAL ? this.traceL : this.offL;
         }
      }

      @Override
      public ResourceBundle getResourceBundle() {
         return null;
      }

      @Override
      public String getResourceBundleName() {
         return null;
      }

      @Override
      public void setFilter(Object var1) throws SecurityException {
         this.warning("setFilter() not supported by MLogger " + this.getClass().getName());
      }

      @Override
      public Object getFilter() {
         return null;
      }

      @Override
      public void log(MLevel var1, String var2) {
         this.levelLogger(var1).log(var2);
      }

      @Override
      public void log(MLevel var1, String var2, Object var3) {
         this.levelLogger(var1).log(var2, var3);
      }

      @Override
      public void log(MLevel var1, String var2, Object[] var3) {
         this.levelLogger(var1).log(var2, var3);
      }

      @Override
      public void log(MLevel var1, String var2, Throwable var3) {
         this.levelLogger(var1).log(var2, var3);
      }

      @Override
      public void logp(MLevel var1, String var2, String var3, String var4) {
         this.levelLogger(var1).log(LogUtils.createMessage(var2, var3, var4));
      }

      @Override
      public void logp(MLevel var1, String var2, String var3, String var4, Object var5) {
         this.levelLogger(var1).log(LogUtils.createMessage(var2, var3, var4 != null ? MessageFormat.format(var4, var5) : null));
      }

      @Override
      public void logp(MLevel var1, String var2, String var3, String var4, Object[] var5) {
         this.levelLogger(var1).log(LogUtils.createMessage(var2, var3, var4 != null ? MessageFormat.format(var4, var5) : null));
      }

      @Override
      public void logp(MLevel var1, String var2, String var3, String var4, Throwable var5) {
         this.levelLogger(var1).log(LogUtils.createMessage(var2, var3, var4), var5);
      }

      @Override
      public void logrb(MLevel var1, String var2, String var3, String var4, String var5) {
         this.levelLogger(var1).log(LogUtils.createMessage(var2, var3, LogUtils.formatMessage(var4, var5, null)));
      }

      @Override
      public void logrb(MLevel var1, String var2, String var3, String var4, String var5, Object var6) {
         this.levelLogger(var1).log(LogUtils.createMessage(var2, var3, LogUtils.formatMessage(var4, var5, new Object[]{var6})));
      }

      @Override
      public void logrb(MLevel var1, String var2, String var3, String var4, String var5, Object[] var6) {
         this.levelLogger(var1).log(LogUtils.createMessage(var2, var3, LogUtils.formatMessage(var4, var5, var6)));
      }

      @Override
      public void logrb(MLevel var1, String var2, String var3, String var4, String var5, Throwable var6) {
         this.levelLogger(var1).log(LogUtils.createMessage(var2, var3, LogUtils.formatMessage(var4, var5, null)), var6);
      }

      @Override
      public void entering(String var1, String var2) {
         this.traceL.log(LogUtils.createMessage(var1, var2, "entering method."));
      }

      @Override
      public void entering(String var1, String var2, Object var3) {
         this.traceL.log(LogUtils.createMessage(var1, var2, "entering method... param: " + var3.toString()));
      }

      @Override
      public void entering(String var1, String var2, Object[] var3) {
         this.traceL.log(LogUtils.createMessage(var1, var2, "entering method... " + LogUtils.createParamsList(var3)));
      }

      @Override
      public void exiting(String var1, String var2) {
         this.traceL.log(LogUtils.createMessage(var1, var2, "exiting method."));
      }

      @Override
      public void exiting(String var1, String var2, Object var3) {
         this.traceL.log(LogUtils.createMessage(var1, var2, "exiting method... result: " + var3.toString()));
      }

      @Override
      public void throwing(String var1, String var2, Throwable var3) {
         this.traceL.log(LogUtils.createMessage(var1, var2, "throwing exception... "), var3);
      }

      @Override
      public void severe(String var1) {
         this.errorL.log(var1);
      }

      @Override
      public void warning(String var1) {
         this.warnL.log(var1);
      }

      @Override
      public void info(String var1) {
         this.infoL.log(var1);
      }

      @Override
      public void config(String var1) {
         this.debugL.log(var1);
      }

      @Override
      public void fine(String var1) {
         this.debugL.log(var1);
      }

      @Override
      public void finer(String var1) {
         this.debugL.log(var1);
      }

      @Override
      public void finest(String var1) {
         this.traceL.log(var1);
      }

      @Override
      public synchronized void setLevel(MLevel var1) throws SecurityException {
         this.myLevel = var1;
      }

      @Override
      public synchronized MLevel getLevel() {
         if (this.myLevel == null) {
            this.myLevel = this.guessMLevel();
         }

         return this.myLevel;
      }

      @Override
      public boolean isLoggable(MLevel var1) {
         return this.levelLogger(var1) != this.offL;
      }

      @Override
      public String getName() {
         return this.logger.getName();
      }

      @Override
      public void addHandler(Object var1) throws SecurityException {
         throw new UnsupportedOperationException("Handlers not supported; the 'handler' " + var1 + " is not compatible with MLogger " + this);
      }

      @Override
      public void removeHandler(Object var1) throws SecurityException {
         throw new UnsupportedOperationException("Handlers not supported; the 'handler' " + var1 + " is not compatible with MLogger " + this);
      }

      @Override
      public Object[] getHandlers() {
         return Slf4jMLog.EMPTY_OBJ_ARRAY;
      }

      @Override
      public void setUseParentHandlers(boolean var1) {
         throw new UnsupportedOperationException("Handlers not supported.");
      }

      @Override
      public boolean getUseParentHandlers() {
         throw new UnsupportedOperationException("Handlers not supported.");
      }

      private class DebugLogger implements Slf4jMLog.Slf4jMLogger.LevelLogger {
         private DebugLogger() {
         }

         @Override
         public void log(String var1) {
            Slf4jMLogger.this.logger.debug(var1);
         }

         @Override
         public void log(String var1, Object var2) {
            Slf4jMLogger.this.logger.debug(var1, var2);
         }

         @Override
         public void log(String var1, Object[] var2) {
            Slf4jMLogger.this.logger.debug(var1, var2);
         }

         @Override
         public void log(String var1, Throwable var2) {
            Slf4jMLogger.this.logger.debug(var1, var2);
         }
      }

      private class ErrorLogger implements Slf4jMLog.Slf4jMLogger.LevelLogger {
         private ErrorLogger() {
         }

         @Override
         public void log(String var1) {
            Slf4jMLogger.this.logger.error(var1);
         }

         @Override
         public void log(String var1, Object var2) {
            Slf4jMLogger.this.logger.error(var1, var2);
         }

         @Override
         public void log(String var1, Object[] var2) {
            Slf4jMLogger.this.logger.error(var1, var2);
         }

         @Override
         public void log(String var1, Throwable var2) {
            Slf4jMLogger.this.logger.error(var1, var2);
         }
      }

      private class InfoLogger implements Slf4jMLog.Slf4jMLogger.LevelLogger {
         private InfoLogger() {
         }

         @Override
         public void log(String var1) {
            Slf4jMLogger.this.logger.info(var1);
         }

         @Override
         public void log(String var1, Object var2) {
            Slf4jMLogger.this.logger.info(var1, var2);
         }

         @Override
         public void log(String var1, Object[] var2) {
            Slf4jMLogger.this.logger.info(var1, var2);
         }

         @Override
         public void log(String var1, Throwable var2) {
            Slf4jMLogger.this.logger.info(var1, var2);
         }
      }

      private interface LevelLogger {
         void log(String var1);

         void log(String var1, Object var2);

         void log(String var1, Object[] var2);

         void log(String var1, Throwable var2);
      }

      private class OffLogger implements Slf4jMLog.Slf4jMLogger.LevelLogger {
         private OffLogger() {
         }

         @Override
         public void log(String var1) {
         }

         @Override
         public void log(String var1, Object var2) {
         }

         @Override
         public void log(String var1, Object[] var2) {
         }

         @Override
         public void log(String var1, Throwable var2) {
         }
      }

      private class TraceLogger implements Slf4jMLog.Slf4jMLogger.LevelLogger {
         private TraceLogger() {
         }

         @Override
         public void log(String var1) {
            Slf4jMLogger.this.logger.trace(var1);
         }

         @Override
         public void log(String var1, Object var2) {
            Slf4jMLogger.this.logger.trace(var1, var2);
         }

         @Override
         public void log(String var1, Object[] var2) {
            Slf4jMLogger.this.logger.trace(var1, var2);
         }

         @Override
         public void log(String var1, Throwable var2) {
            Slf4jMLogger.this.logger.trace(var1, var2);
         }
      }

      private class WarnLogger implements Slf4jMLog.Slf4jMLogger.LevelLogger {
         private WarnLogger() {
         }

         @Override
         public void log(String var1) {
            Slf4jMLogger.this.logger.warn(var1);
         }

         @Override
         public void log(String var1, Object var2) {
            Slf4jMLogger.this.logger.warn(var1, var2);
         }

         @Override
         public void log(String var1, Object[] var2) {
            Slf4jMLogger.this.logger.warn(var1, var2);
         }

         @Override
         public void log(String var1, Throwable var2) {
            Slf4jMLogger.this.logger.warn(var1, var2);
         }
      }
   }
}
