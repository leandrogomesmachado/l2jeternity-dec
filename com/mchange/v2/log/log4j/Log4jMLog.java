package com.mchange.v2.log.log4j;

import com.mchange.v2.log.FallbackMLog;
import com.mchange.v2.log.LogUtils;
import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import com.mchange.v2.log.NullMLogger;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.ResourceBundle;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public final class Log4jMLog extends MLog {
   static final String CHECK_CLASS = "org.apache.log4j.Logger";

   public Log4jMLog() throws ClassNotFoundException {
      Class.forName("org.apache.log4j.Logger");
   }

   @Override
   public MLogger getMLogger(String var1) {
      Logger var2 = Logger.getLogger(var1);
      if (var2 == null) {
         this.fallbackWarn(" with name '" + var1 + "'");
         return NullMLogger.instance();
      } else {
         return new Log4jMLog.Log4jMLogger(var2);
      }
   }

   @Override
   public MLogger getMLogger(Class var1) {
      Logger var2 = Logger.getLogger(var1);
      if (var2 == null) {
         this.fallbackWarn(" for class '" + var1.getName() + "'");
         return NullMLogger.instance();
      } else {
         return new Log4jMLog.Log4jMLogger(var2);
      }
   }

   @Override
   public MLogger getMLogger() {
      Logger var1 = Logger.getRootLogger();
      if (var1 == null) {
         this.fallbackWarn(" (root logger)");
         return NullMLogger.instance();
      } else {
         return new Log4jMLog.Log4jMLogger(var1);
      }
   }

   private void fallbackWarn(String var1) {
      FallbackMLog.getLogger()
         .warning(
            "Could not create or find log4j Logger"
               + var1
               + ". "
               + "Using NullMLogger. All messages sent to this"
               + "logger will be silently ignored. You might want to fix this."
         );
   }

   private static final class Log4jMLogger implements MLogger {
      static final String FQCN = Log4jMLog.Log4jMLogger.class.getName();
      MLevel myLevel = null;
      final Logger logger;

      Log4jMLogger(Logger var1) {
         this.logger = var1;
      }

      private static MLevel guessMLevel(Level var0) {
         if (var0 == null) {
            return null;
         } else if (var0 == Level.ALL) {
            return MLevel.ALL;
         } else if (var0 == Level.DEBUG) {
            return MLevel.FINEST;
         } else if (var0 == Level.ERROR) {
            return MLevel.SEVERE;
         } else if (var0 == Level.FATAL) {
            return MLevel.SEVERE;
         } else if (var0 == Level.INFO) {
            return MLevel.INFO;
         } else if (var0 == Level.OFF) {
            return MLevel.OFF;
         } else if (var0 == Level.WARN) {
            return MLevel.WARNING;
         } else {
            throw new IllegalArgumentException("Unknown level: " + var0);
         }
      }

      private static Level level(MLevel var0) {
         if (var0 == null) {
            return null;
         } else if (var0 == MLevel.ALL) {
            return Level.ALL;
         } else if (var0 == MLevel.CONFIG) {
            return Level.DEBUG;
         } else if (var0 == MLevel.FINE) {
            return Level.DEBUG;
         } else if (var0 == MLevel.FINER) {
            return Level.DEBUG;
         } else if (var0 == MLevel.FINEST) {
            return Level.DEBUG;
         } else if (var0 == MLevel.INFO) {
            return Level.INFO;
         } else if (var0 == MLevel.OFF) {
            return Level.OFF;
         } else if (var0 == MLevel.SEVERE) {
            return Level.ERROR;
         } else if (var0 == MLevel.WARNING) {
            return Level.WARN;
         } else {
            throw new IllegalArgumentException("Unknown MLevel: " + var0);
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

      private void log(Level var1, Object var2, Throwable var3) {
         this.logger.log(FQCN, var1, var2, var3);
      }

      @Override
      public void log(MLevel var1, String var2) {
         this.log(level(var1), var2, null);
      }

      @Override
      public void log(MLevel var1, String var2, Object var3) {
         this.log(level(var1), var2 != null ? MessageFormat.format(var2, var3) : null, null);
      }

      @Override
      public void log(MLevel var1, String var2, Object[] var3) {
         this.log(level(var1), var2 != null ? MessageFormat.format(var2, var3) : null, null);
      }

      @Override
      public void log(MLevel var1, String var2, Throwable var3) {
         this.log(level(var1), var2, var3);
      }

      @Override
      public void logp(MLevel var1, String var2, String var3, String var4) {
         this.log(level(var1), LogUtils.createMessage(var2, var3, var4), null);
      }

      @Override
      public void logp(MLevel var1, String var2, String var3, String var4, Object var5) {
         this.log(level(var1), LogUtils.createMessage(var2, var3, var4 != null ? MessageFormat.format(var4, var5) : null), null);
      }

      @Override
      public void logp(MLevel var1, String var2, String var3, String var4, Object[] var5) {
         this.log(level(var1), LogUtils.createMessage(var2, var3, var4 != null ? MessageFormat.format(var4, var5) : null), null);
      }

      @Override
      public void logp(MLevel var1, String var2, String var3, String var4, Throwable var5) {
         this.log(level(var1), LogUtils.createMessage(var2, var3, var4), var5);
      }

      @Override
      public void logrb(MLevel var1, String var2, String var3, String var4, String var5) {
         this.log(level(var1), LogUtils.createMessage(var2, var3, LogUtils.formatMessage(var4, var5, null)), null);
      }

      @Override
      public void logrb(MLevel var1, String var2, String var3, String var4, String var5, Object var6) {
         this.log(level(var1), LogUtils.createMessage(var2, var3, LogUtils.formatMessage(var4, var5, new Object[]{var6})), null);
      }

      @Override
      public void logrb(MLevel var1, String var2, String var3, String var4, String var5, Object[] var6) {
         this.log(level(var1), LogUtils.createMessage(var2, var3, LogUtils.formatMessage(var4, var5, var6)), null);
      }

      @Override
      public void logrb(MLevel var1, String var2, String var3, String var4, String var5, Throwable var6) {
         this.log(level(var1), LogUtils.createMessage(var2, var3, LogUtils.formatMessage(var4, var5, null)), var6);
      }

      @Override
      public void entering(String var1, String var2) {
         this.log(Level.DEBUG, LogUtils.createMessage(var1, var2, "entering method."), null);
      }

      @Override
      public void entering(String var1, String var2, Object var3) {
         this.log(Level.DEBUG, LogUtils.createMessage(var1, var2, "entering method... param: " + var3.toString()), null);
      }

      @Override
      public void entering(String var1, String var2, Object[] var3) {
         this.log(Level.DEBUG, LogUtils.createMessage(var1, var2, "entering method... " + LogUtils.createParamsList(var3)), null);
      }

      @Override
      public void exiting(String var1, String var2) {
         this.log(Level.DEBUG, LogUtils.createMessage(var1, var2, "exiting method."), null);
      }

      @Override
      public void exiting(String var1, String var2, Object var3) {
         this.log(Level.DEBUG, LogUtils.createMessage(var1, var2, "exiting method... result: " + var3.toString()), null);
      }

      @Override
      public void throwing(String var1, String var2, Throwable var3) {
         this.log(Level.DEBUG, LogUtils.createMessage(var1, var2, "throwing exception... "), var3);
      }

      @Override
      public void severe(String var1) {
         this.log(Level.ERROR, var1, null);
      }

      @Override
      public void warning(String var1) {
         this.log(Level.WARN, var1, null);
      }

      @Override
      public void info(String var1) {
         this.log(Level.INFO, var1, null);
      }

      @Override
      public void config(String var1) {
         this.log(Level.DEBUG, var1, null);
      }

      @Override
      public void fine(String var1) {
         this.log(Level.DEBUG, var1, null);
      }

      @Override
      public void finer(String var1) {
         this.log(Level.DEBUG, var1, null);
      }

      @Override
      public void finest(String var1) {
         this.log(Level.DEBUG, var1, null);
      }

      @Override
      public synchronized void setLevel(MLevel var1) throws SecurityException {
         this.logger.setLevel(level(var1));
         this.myLevel = var1;
      }

      @Override
      public synchronized MLevel getLevel() {
         if (this.myLevel == null) {
            this.myLevel = guessMLevel(this.logger.getLevel());
         }

         return this.myLevel;
      }

      @Override
      public boolean isLoggable(MLevel var1) {
         return this.logger.isEnabledFor(level(var1));
      }

      @Override
      public String getName() {
         return this.logger.getName();
      }

      @Override
      public void addHandler(Object var1) throws SecurityException {
         if (!(var1 instanceof Appender)) {
            throw new IllegalArgumentException("The 'handler' " + var1 + " is not compatible with MLogger " + this);
         } else {
            this.logger.addAppender((Appender)var1);
         }
      }

      @Override
      public void removeHandler(Object var1) throws SecurityException {
         if (!(var1 instanceof Appender)) {
            throw new IllegalArgumentException("The 'handler' " + var1 + " is not compatible with MLogger " + this);
         } else {
            this.logger.removeAppender((Appender)var1);
         }
      }

      @Override
      public Object[] getHandlers() {
         LinkedList var1 = new LinkedList();
         Enumeration var2 = this.logger.getAllAppenders();

         while(var2.hasMoreElements()) {
            var1.add(var2.nextElement());
         }

         return var1.toArray();
      }

      @Override
      public void setUseParentHandlers(boolean var1) {
         this.logger.setAdditivity(var1);
      }

      @Override
      public boolean getUseParentHandlers() {
         return this.logger.getAdditivity();
      }
   }
}
