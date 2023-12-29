package com.mchange.v2.log;

import java.util.ResourceBundle;

public interface MLogger {
   String getName();

   void log(MLevel var1, String var2);

   void log(MLevel var1, String var2, Object var3);

   void log(MLevel var1, String var2, Object[] var3);

   void log(MLevel var1, String var2, Throwable var3);

   void logp(MLevel var1, String var2, String var3, String var4);

   void logp(MLevel var1, String var2, String var3, String var4, Object var5);

   void logp(MLevel var1, String var2, String var3, String var4, Object[] var5);

   void logp(MLevel var1, String var2, String var3, String var4, Throwable var5);

   void logrb(MLevel var1, String var2, String var3, String var4, String var5);

   void logrb(MLevel var1, String var2, String var3, String var4, String var5, Object var6);

   void logrb(MLevel var1, String var2, String var3, String var4, String var5, Object[] var6);

   void logrb(MLevel var1, String var2, String var3, String var4, String var5, Throwable var6);

   void entering(String var1, String var2);

   void entering(String var1, String var2, Object var3);

   void entering(String var1, String var2, Object[] var3);

   void exiting(String var1, String var2);

   void exiting(String var1, String var2, Object var3);

   void throwing(String var1, String var2, Throwable var3);

   void severe(String var1);

   void warning(String var1);

   void info(String var1);

   void config(String var1);

   void fine(String var1);

   void finer(String var1);

   void finest(String var1);

   boolean isLoggable(MLevel var1);

   /** @deprecated */
   ResourceBundle getResourceBundle();

   /** @deprecated */
   String getResourceBundleName();

   /** @deprecated */
   void setFilter(Object var1) throws SecurityException;

   /** @deprecated */
   Object getFilter();

   /** @deprecated */
   void setLevel(MLevel var1) throws SecurityException;

   /** @deprecated */
   MLevel getLevel();

   /** @deprecated */
   void addHandler(Object var1) throws SecurityException;

   /** @deprecated */
   void removeHandler(Object var1) throws SecurityException;

   /** @deprecated */
   Object[] getHandlers();

   /** @deprecated */
   void setUseParentHandlers(boolean var1);

   /** @deprecated */
   boolean getUseParentHandlers();
}
