package com.mchange.v2.log.jdk14logging;

import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogConfig;
import com.mchange.v2.log.MLogger;
import com.mchange.v2.util.DoubleWeakHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public final class Jdk14MLog extends MLog {
   static final String SUPPRESS_STACK_WALK_KEY = "com.mchange.v2.log.jdk14logging.suppressStackWalk";
   private static String[] UNKNOWN_ARRAY = new String[]{"UNKNOWN_CLASS", "UNKNOWN_METHOD"};
   private static final String CHECK_CLASS = "java.util.logging.Logger";
   private final Map namedLoggerMap = new DoubleWeakHashMap();
   private static final boolean suppress_stack_walk;
   MLogger global = null;

   public Jdk14MLog() throws ClassNotFoundException {
      Class.forName("java.util.logging.Logger");
   }

   @Override
   public synchronized MLogger getMLogger(String var1) {
      var1 = var1.intern();
      Object var2 = (MLogger)this.namedLoggerMap.get(var1);
      if (var2 == null) {
         Logger var3 = Logger.getLogger(var1);
         var2 = new Jdk14MLog.Jdk14MLogger(var3);
         this.namedLoggerMap.put(var1, var2);
      }

      return (MLogger)var2;
   }

   @Override
   public synchronized MLogger getMLogger() {
      if (this.global == null) {
         this.global = new Jdk14MLog.Jdk14MLogger(LogManager.getLogManager().getLogger("global"));
      }

      return this.global;
   }

   private static String[] findCallingClassAndMethod() {
      StackTraceElement[] var0 = new Throwable().getStackTrace();
      int var1 = 0;

      for(int var2 = var0.length; var1 < var2; ++var1) {
         StackTraceElement var3 = var0[var1];
         String var4 = var3.getClassName();
         if (var4 != null && !var4.startsWith("com.mchange.v2.log.jdk14logging") && !var4.startsWith("com.mchange.sc.v1.log")) {
            return new String[]{var3.getClassName(), var3.getMethodName()};
         }
      }

      return UNKNOWN_ARRAY;
   }

   static {
      String var0 = MLogConfig.getProperty("com.mchange.v2.log.jdk14logging.suppressStackWalk");
      if (var0 == null || (var0 = var0.trim()).length() == 0) {
         suppress_stack_walk = false;
      } else if (var0.equalsIgnoreCase("true")) {
         suppress_stack_walk = true;
      } else if (var0.equalsIgnoreCase("false")) {
         suppress_stack_walk = false;
      } else {
         System.err.println("Bad value for com.mchange.v2.log.jdk14logging.suppressStackWalk: '" + var0 + "'; defaulting to 'false'.");
         suppress_stack_walk = false;
      }
   }

   private static final class Jdk14MLogger implements MLogger {
      final Logger logger;
      final String name;
      final Jdk14MLog.Jdk14MLogger.ClassAndMethodFinder cmFinder;

      Jdk14MLogger(Logger var1) {
         this.logger = var1;
         this.name = var1.getName();
         if (Jdk14MLog.suppress_stack_walk) {
            this.cmFinder = new Jdk14MLog.Jdk14MLogger.ClassAndMethodFinder() {
               String[] fakedClassAndMethod = new String[]{Jdk14MLogger.this.name, ""};

               @Override
               public String[] find() {
                  return this.fakedClassAndMethod;
               }
            };
         } else {
            this.cmFinder = new Jdk14MLog.Jdk14MLogger.ClassAndMethodFinder() {
               @Override
               public String[] find() {
                  return Jdk14MLog.findCallingClassAndMethod();
               }
            };
         }
      }

      private static Level level(MLevel var0) {
         return (Level)var0.asJdk14Level();
      }

      @Override
      public ResourceBundle getResourceBundle() {
         return this.logger.getResourceBundle();
      }

      @Override
      public String getResourceBundleName() {
         return this.logger.getResourceBundleName();
      }

      @Override
      public void setFilter(Object var1) throws SecurityException {
         if (!(var1 instanceof Filter)) {
            throw new IllegalArgumentException(
               "MLogger.setFilter( ... ) requires a java.util.logging.Filter. This is not enforced by the compiler only to permit building under jdk 1.3"
            );
         } else {
            this.logger.setFilter((Filter)var1);
         }
      }

      @Override
      public Object getFilter() {
         return this.logger.getFilter();
      }

      @Override
      public void log(MLevel var1, String var2) {
         if (this.logger.isLoggable(level(var1))) {
            String[] var3 = this.cmFinder.find();
            this.logger.logp(level(var1), var3[0], var3[1], var2);
         }
      }

      @Override
      public void log(MLevel var1, String var2, Object var3) {
         if (this.logger.isLoggable(level(var1))) {
            String[] var4 = this.cmFinder.find();
            this.logger.logp(level(var1), var4[0], var4[1], var2, var3);
         }
      }

      @Override
      public void log(MLevel var1, String var2, Object[] var3) {
         if (this.logger.isLoggable(level(var1))) {
            String[] var4 = this.cmFinder.find();
            this.logger.logp(level(var1), var4[0], var4[1], var2, var3);
         }
      }

      @Override
      public void log(MLevel var1, String var2, Throwable var3) {
         if (this.logger.isLoggable(level(var1))) {
            String[] var4 = this.cmFinder.find();
            this.logger.logp(level(var1), var4[0], var4[1], var2, var3);
         }
      }

      @Override
      public void logp(MLevel var1, String var2, String var3, String var4) {
         if (this.logger.isLoggable(level(var1))) {
            if (var2 == null && var3 == null) {
               String[] var5 = this.cmFinder.find();
               var2 = var5[0];
               var3 = var5[1];
            }

            this.logger.logp(level(var1), var2, var3, var4);
         }
      }

      @Override
      public void logp(MLevel var1, String var2, String var3, String var4, Object var5) {
         if (this.logger.isLoggable(level(var1))) {
            if (var2 == null && var3 == null) {
               String[] var6 = this.cmFinder.find();
               var2 = var6[0];
               var3 = var6[1];
            }

            this.logger.logp(level(var1), var2, var3, var4, var5);
         }
      }

      @Override
      public void logp(MLevel var1, String var2, String var3, String var4, Object[] var5) {
         if (this.logger.isLoggable(level(var1))) {
            if (var2 == null && var3 == null) {
               String[] var6 = this.cmFinder.find();
               var2 = var6[0];
               var3 = var6[1];
            }

            this.logger.logp(level(var1), var2, var3, var4, var5);
         }
      }

      @Override
      public void logp(MLevel var1, String var2, String var3, String var4, Throwable var5) {
         if (this.logger.isLoggable(level(var1))) {
            if (var2 == null && var3 == null) {
               String[] var6 = this.cmFinder.find();
               var2 = var6[0];
               var3 = var6[1];
            }

            this.logger.logp(level(var1), var2, var3, var4, var5);
         }
      }

      @Override
      public void logrb(MLevel var1, String var2, String var3, String var4, String var5) {
         if (this.logger.isLoggable(level(var1))) {
            if (var2 == null && var3 == null) {
               String[] var6 = this.cmFinder.find();
               var2 = var6[0];
               var3 = var6[1];
            }

            this.logger.logrb(level(var1), var2, var3, var4, var5);
         }
      }

      @Override
      public void logrb(MLevel var1, String var2, String var3, String var4, String var5, Object var6) {
         if (this.logger.isLoggable(level(var1))) {
            if (var2 == null && var3 == null) {
               String[] var7 = this.cmFinder.find();
               var2 = var7[0];
               var3 = var7[1];
            }

            this.logger.logrb(level(var1), var2, var3, var4, var5, var6);
         }
      }

      @Override
      public void logrb(MLevel var1, String var2, String var3, String var4, String var5, Object[] var6) {
         if (this.logger.isLoggable(level(var1))) {
            if (var2 == null && var3 == null) {
               String[] var7 = this.cmFinder.find();
               var2 = var7[0];
               var3 = var7[1];
            }

            this.logger.logrb(level(var1), var2, var3, var4, var5, var6);
         }
      }

      @Override
      public void logrb(MLevel var1, String var2, String var3, String var4, String var5, Throwable var6) {
         if (this.logger.isLoggable(level(var1))) {
            if (var2 == null && var3 == null) {
               String[] var7 = this.cmFinder.find();
               var2 = var7[0];
               var3 = var7[1];
            }

            this.logger.logrb(level(var1), var2, var3, var4, var5, var6);
         }
      }

      @Override
      public void entering(String var1, String var2) {
         if (this.logger.isLoggable(Level.FINER)) {
            this.logger.entering(var1, var2);
         }
      }

      @Override
      public void entering(String var1, String var2, Object var3) {
         if (this.logger.isLoggable(Level.FINER)) {
            this.logger.entering(var1, var2, var3);
         }
      }

      @Override
      public void entering(String var1, String var2, Object[] var3) {
         if (this.logger.isLoggable(Level.FINER)) {
            this.logger.entering(var1, var2, var3);
         }
      }

      @Override
      public void exiting(String var1, String var2) {
         if (this.logger.isLoggable(Level.FINER)) {
            this.logger.exiting(var1, var2);
         }
      }

      @Override
      public void exiting(String var1, String var2, Object var3) {
         if (this.logger.isLoggable(Level.FINER)) {
            this.logger.exiting(var1, var2, var3);
         }
      }

      @Override
      public void throwing(String var1, String var2, Throwable var3) {
         if (this.logger.isLoggable(Level.FINER)) {
            this.logger.throwing(var1, var2, var3);
         }
      }

      @Override
      public void severe(String var1) {
         if (this.logger.isLoggable(Level.SEVERE)) {
            String[] var2 = this.cmFinder.find();
            this.logger.logp(Level.SEVERE, var2[0], var2[1], var1);
         }
      }

      @Override
      public void warning(String var1) {
         if (this.logger.isLoggable(Level.WARNING)) {
            String[] var2 = this.cmFinder.find();
            this.logger.logp(Level.WARNING, var2[0], var2[1], var1);
         }
      }

      @Override
      public void info(String var1) {
         if (this.logger.isLoggable(Level.INFO)) {
            String[] var2 = this.cmFinder.find();
            this.logger.logp(Level.INFO, var2[0], var2[1], var1);
         }
      }

      @Override
      public void config(String var1) {
         if (this.logger.isLoggable(Level.CONFIG)) {
            String[] var2 = this.cmFinder.find();
            this.logger.logp(Level.CONFIG, var2[0], var2[1], var1);
         }
      }

      @Override
      public void fine(String var1) {
         if (this.logger.isLoggable(Level.FINE)) {
            String[] var2 = this.cmFinder.find();
            this.logger.logp(Level.FINE, var2[0], var2[1], var1);
         }
      }

      @Override
      public void finer(String var1) {
         if (this.logger.isLoggable(Level.FINER)) {
            String[] var2 = this.cmFinder.find();
            this.logger.logp(Level.FINER, var2[0], var2[1], var1);
         }
      }

      @Override
      public void finest(String var1) {
         if (this.logger.isLoggable(Level.FINEST)) {
            String[] var2 = this.cmFinder.find();
            this.logger.logp(Level.FINEST, var2[0], var2[1], var1);
         }
      }

      @Override
      public void setLevel(MLevel var1) throws SecurityException {
         this.logger.setLevel(level(var1));
      }

      @Override
      public MLevel getLevel() {
         return MLevel.fromIntValue(this.logger.getLevel().intValue());
      }

      @Override
      public boolean isLoggable(MLevel var1) {
         return this.logger.isLoggable(level(var1));
      }

      @Override
      public String getName() {
         return this.name;
      }

      @Override
      public void addHandler(Object var1) throws SecurityException {
         if (!(var1 instanceof Handler)) {
            throw new IllegalArgumentException(
               "MLogger.addHandler( ... ) requires a java.util.logging.Handler. This is not enforced by the compiler only to permit building under jdk 1.3"
            );
         } else {
            this.logger.addHandler((Handler)var1);
         }
      }

      @Override
      public void removeHandler(Object var1) throws SecurityException {
         if (!(var1 instanceof Handler)) {
            throw new IllegalArgumentException(
               "MLogger.removeHandler( ... ) requires a java.util.logging.Handler. This is not enforced by the compiler only to permit building under jdk 1.3"
            );
         } else {
            this.logger.removeHandler((Handler)var1);
         }
      }

      @Override
      public Object[] getHandlers() {
         return this.logger.getHandlers();
      }

      @Override
      public void setUseParentHandlers(boolean var1) {
         this.logger.setUseParentHandlers(var1);
      }

      @Override
      public boolean getUseParentHandlers() {
         return this.logger.getUseParentHandlers();
      }

      interface ClassAndMethodFinder {
         String[] find();
      }
   }
}
