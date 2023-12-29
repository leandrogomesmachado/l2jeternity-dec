package com.mchange.v2.log;

import java.util.ResourceBundle;

public class NullMLogger implements MLogger {
   private static final MLogger INSTANCE = new NullMLogger();
   private static final String NAME = "NullMLogger";

   public static MLogger instance() {
      return INSTANCE;
   }

   private NullMLogger() {
   }

   @Override
   public void addHandler(Object var1) throws SecurityException {
   }

   @Override
   public void config(String var1) {
   }

   @Override
   public void entering(String var1, String var2) {
   }

   @Override
   public void entering(String var1, String var2, Object var3) {
   }

   @Override
   public void entering(String var1, String var2, Object[] var3) {
   }

   @Override
   public void exiting(String var1, String var2) {
   }

   @Override
   public void exiting(String var1, String var2, Object var3) {
   }

   @Override
   public void fine(String var1) {
   }

   @Override
   public void finer(String var1) {
   }

   @Override
   public void finest(String var1) {
   }

   @Override
   public Object getFilter() {
      return null;
   }

   @Override
   public Object[] getHandlers() {
      return null;
   }

   @Override
   public MLevel getLevel() {
      return MLevel.OFF;
   }

   @Override
   public String getName() {
      return "NullMLogger";
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
   public boolean getUseParentHandlers() {
      return false;
   }

   @Override
   public void info(String var1) {
   }

   @Override
   public boolean isLoggable(MLevel var1) {
      return false;
   }

   @Override
   public void log(MLevel var1, String var2) {
   }

   @Override
   public void log(MLevel var1, String var2, Object var3) {
   }

   @Override
   public void log(MLevel var1, String var2, Object[] var3) {
   }

   @Override
   public void log(MLevel var1, String var2, Throwable var3) {
   }

   @Override
   public void logp(MLevel var1, String var2, String var3, String var4) {
   }

   @Override
   public void logp(MLevel var1, String var2, String var3, String var4, Object var5) {
   }

   @Override
   public void logp(MLevel var1, String var2, String var3, String var4, Object[] var5) {
   }

   @Override
   public void logp(MLevel var1, String var2, String var3, String var4, Throwable var5) {
   }

   @Override
   public void logrb(MLevel var1, String var2, String var3, String var4, String var5) {
   }

   @Override
   public void logrb(MLevel var1, String var2, String var3, String var4, String var5, Object var6) {
   }

   @Override
   public void logrb(MLevel var1, String var2, String var3, String var4, String var5, Object[] var6) {
   }

   @Override
   public void logrb(MLevel var1, String var2, String var3, String var4, String var5, Throwable var6) {
   }

   @Override
   public void removeHandler(Object var1) throws SecurityException {
   }

   @Override
   public void setFilter(Object var1) throws SecurityException {
   }

   @Override
   public void setLevel(MLevel var1) throws SecurityException {
   }

   @Override
   public void setUseParentHandlers(boolean var1) {
   }

   @Override
   public void severe(String var1) {
   }

   @Override
   public void throwing(String var1, String var2, Throwable var3) {
   }

   @Override
   public void warning(String var1) {
   }
}
