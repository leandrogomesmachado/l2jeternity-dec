package com.mchange.v2.log;

import com.mchange.lang.ThrowableUtils;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public final class FallbackMLog extends MLog {
   static final MLevel DEFAULT_CUTOFF_LEVEL;
   static final String SEP;
   MLogger logger = new FallbackMLog.FallbackMLogger();

   @Override
   public MLogger getMLogger(String var1) {
      return this.logger;
   }

   @Override
   public MLogger getMLogger() {
      return this.logger;
   }

   static {
      MLevel var0 = null;
      String var1 = MLogConfig.getProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL");
      if (var1 != null) {
         var0 = MLevel.fromSeverity(var1);
      }

      if (var0 == null) {
         var0 = MLevel.INFO;
      }

      DEFAULT_CUTOFF_LEVEL = var0;
      SEP = System.getProperty("line.separator");
   }

   private static final class FallbackMLogger implements MLogger {
      MLevel cutoffLevel = FallbackMLog.DEFAULT_CUTOFF_LEVEL;

      private FallbackMLogger() {
      }

      private void formatrb(MLevel var1, String var2, String var3, String var4, String var5, Object[] var6, Throwable var7) {
         ResourceBundle var8 = ResourceBundle.getBundle(var4);
         if (var5 != null && var8 != null) {
            String var9 = var8.getString(var5);
            if (var9 != null) {
               var5 = var9;
            }
         }

         this.format(var1, var2, var3, var5, var6, var7);
      }

      private void format(MLevel var1, String var2, String var3, String var4, Object[] var5, Throwable var6) {
         System.err.println(this.formatString(var1, var2, var3, var4, var5, var6));
      }

      private String formatString(MLevel var1, String var2, String var3, String var4, Object[] var5, Throwable var6) {
         boolean var7 = var3 != null && !var3.endsWith(")");
         StringBuffer var8 = new StringBuffer(256);
         var8.append(var1.getLineHeader());
         var8.append(' ');
         if (var2 != null && var3 != null) {
            var8.append('[');
            var8.append(var2);
            var8.append('.');
            var8.append(var3);
            if (var7) {
               var8.append("()");
            }

            var8.append(']');
         } else if (var2 != null) {
            var8.append('[');
            var8.append(var2);
            var8.append(']');
         } else if (var3 != null) {
            var8.append('[');
            var8.append(var3);
            if (var7) {
               var8.append("()");
            }

            var8.append(']');
         }

         if (var4 == null) {
            if (var5 != null) {
               var8.append("params: ");
               int var9 = 0;

               for(int var10 = var5.length; var9 < var10; ++var9) {
                  if (var9 != 0) {
                     var8.append(", ");
                  }

                  var8.append(var5[var9]);
               }
            }
         } else if (var5 == null) {
            var8.append(var4);
         } else {
            MessageFormat var11 = new MessageFormat(var4);
            var8.append(var11.format(var5));
         }

         if (var6 != null) {
            var8.append(FallbackMLog.SEP);
            var8.append(ThrowableUtils.extractStackTrace(var6));
         }

         return var8.toString();
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
         this.warning("Using FallbackMLog -- Filters not supported!");
      }

      @Override
      public Object getFilter() {
         return null;
      }

      @Override
      public void log(MLevel var1, String var2) {
         if (this.isLoggable(var1)) {
            this.format(var1, null, null, var2, null, null);
         }
      }

      @Override
      public void log(MLevel var1, String var2, Object var3) {
         if (this.isLoggable(var1)) {
            this.format(var1, null, null, var2, new Object[]{var3}, null);
         }
      }

      @Override
      public void log(MLevel var1, String var2, Object[] var3) {
         if (this.isLoggable(var1)) {
            this.format(var1, null, null, var2, var3, null);
         }
      }

      @Override
      public void log(MLevel var1, String var2, Throwable var3) {
         if (this.isLoggable(var1)) {
            this.format(var1, null, null, var2, null, var3);
         }
      }

      @Override
      public void logp(MLevel var1, String var2, String var3, String var4) {
         if (this.isLoggable(var1)) {
            this.format(var1, var2, var3, var4, null, null);
         }
      }

      @Override
      public void logp(MLevel var1, String var2, String var3, String var4, Object var5) {
         if (this.isLoggable(var1)) {
            this.format(var1, var2, var3, var4, new Object[]{var5}, null);
         }
      }

      @Override
      public void logp(MLevel var1, String var2, String var3, String var4, Object[] var5) {
         if (this.isLoggable(var1)) {
            this.format(var1, var2, var3, var4, var5, null);
         }
      }

      @Override
      public void logp(MLevel var1, String var2, String var3, String var4, Throwable var5) {
         if (this.isLoggable(var1)) {
            this.format(var1, var2, var3, var4, null, var5);
         }
      }

      @Override
      public void logrb(MLevel var1, String var2, String var3, String var4, String var5) {
         if (this.isLoggable(var1)) {
            this.formatrb(var1, var2, var3, var4, var5, null, null);
         }
      }

      @Override
      public void logrb(MLevel var1, String var2, String var3, String var4, String var5, Object var6) {
         if (this.isLoggable(var1)) {
            this.formatrb(var1, var2, var3, var4, var5, new Object[]{var6}, null);
         }
      }

      @Override
      public void logrb(MLevel var1, String var2, String var3, String var4, String var5, Object[] var6) {
         if (this.isLoggable(var1)) {
            this.formatrb(var1, var2, var3, var4, var5, var6, null);
         }
      }

      @Override
      public void logrb(MLevel var1, String var2, String var3, String var4, String var5, Throwable var6) {
         if (this.isLoggable(var1)) {
            this.formatrb(var1, var2, var3, var4, var5, null, var6);
         }
      }

      @Override
      public void entering(String var1, String var2) {
         if (this.isLoggable(MLevel.FINER)) {
            this.format(MLevel.FINER, var1, var2, "Entering method.", null, null);
         }
      }

      @Override
      public void entering(String var1, String var2, Object var3) {
         if (this.isLoggable(MLevel.FINER)) {
            this.format(MLevel.FINER, var1, var2, "Entering method with argument " + var3, null, null);
         }
      }

      @Override
      public void entering(String var1, String var2, Object[] var3) {
         if (this.isLoggable(MLevel.FINER)) {
            if (var3 == null) {
               this.entering(var1, var2);
            } else {
               StringBuffer var4 = new StringBuffer(128);
               var4.append("( ");
               int var5 = 0;

               for(int var6 = var3.length; var5 < var6; ++var5) {
                  if (var5 != 0) {
                     var4.append(", ");
                  }

                  var4.append(var3[var5]);
               }

               var4.append(" )");
               this.format(MLevel.FINER, var1, var2, "Entering method with arguments " + var4.toString(), null, null);
            }
         }
      }

      @Override
      public void exiting(String var1, String var2) {
         if (this.isLoggable(MLevel.FINER)) {
            this.format(MLevel.FINER, var1, var2, "Exiting method.", null, null);
         }
      }

      @Override
      public void exiting(String var1, String var2, Object var3) {
         if (this.isLoggable(MLevel.FINER)) {
            this.format(MLevel.FINER, var1, var2, "Exiting method with result " + var3, null, null);
         }
      }

      @Override
      public void throwing(String var1, String var2, Throwable var3) {
         if (this.isLoggable(MLevel.FINE)) {
            this.format(MLevel.FINE, var1, var2, "Throwing exception.", null, var3);
         }
      }

      @Override
      public void severe(String var1) {
         if (this.isLoggable(MLevel.SEVERE)) {
            this.format(MLevel.SEVERE, null, null, var1, null, null);
         }
      }

      @Override
      public void warning(String var1) {
         if (this.isLoggable(MLevel.WARNING)) {
            this.format(MLevel.WARNING, null, null, var1, null, null);
         }
      }

      @Override
      public void info(String var1) {
         if (this.isLoggable(MLevel.INFO)) {
            this.format(MLevel.INFO, null, null, var1, null, null);
         }
      }

      @Override
      public void config(String var1) {
         if (this.isLoggable(MLevel.CONFIG)) {
            this.format(MLevel.CONFIG, null, null, var1, null, null);
         }
      }

      @Override
      public void fine(String var1) {
         if (this.isLoggable(MLevel.FINE)) {
            this.format(MLevel.FINE, null, null, var1, null, null);
         }
      }

      @Override
      public void finer(String var1) {
         if (this.isLoggable(MLevel.FINER)) {
            this.format(MLevel.FINER, null, null, var1, null, null);
         }
      }

      @Override
      public void finest(String var1) {
         if (this.isLoggable(MLevel.FINEST)) {
            this.format(MLevel.FINEST, null, null, var1, null, null);
         }
      }

      @Override
      public void setLevel(MLevel var1) throws SecurityException {
         this.cutoffLevel = var1;
      }

      @Override
      public synchronized MLevel getLevel() {
         return this.cutoffLevel;
      }

      @Override
      public synchronized boolean isLoggable(MLevel var1) {
         return var1.intValue() >= this.cutoffLevel.intValue();
      }

      @Override
      public String getName() {
         return "global";
      }

      @Override
      public void addHandler(Object var1) throws SecurityException {
         this.warning("Using FallbackMLog -- Handlers not supported.");
      }

      @Override
      public void removeHandler(Object var1) throws SecurityException {
         this.warning("Using FallbackMLog -- Handlers not supported.");
      }

      @Override
      public Object[] getHandlers() {
         this.warning("Using FallbackMLog -- Handlers not supported.");
         return new Object[0];
      }

      @Override
      public void setUseParentHandlers(boolean var1) {
         this.warning("Using FallbackMLog -- Handlers not supported.");
      }

      @Override
      public boolean getUseParentHandlers() {
         return false;
      }
   }
}
