package com.mchange.v2.cfg;

import com.mchange.v2.lang.ObjectUtils;

public final class DelayedLogItem {
   private DelayedLogItem.Level level;
   private String text;
   private Throwable exception;

   public DelayedLogItem.Level getLevel() {
      return this.level;
   }

   public String getText() {
      return this.text;
   }

   public Throwable getException() {
      return this.exception;
   }

   public DelayedLogItem(DelayedLogItem.Level var1, String var2, Throwable var3) {
      this.level = var1;
      this.text = var2;
      this.exception = var3;
   }

   public DelayedLogItem(DelayedLogItem.Level var1, String var2) {
      this(var1, var2, null);
   }

   @Override
   public boolean equals(Object var1) {
      if (!(var1 instanceof DelayedLogItem)) {
         return false;
      } else {
         DelayedLogItem var2 = (DelayedLogItem)var1;
         return this.level.equals(var2.level) && this.text.equals(var2.text) && ObjectUtils.eqOrBothNull(this.exception, var2.exception);
      }
   }

   @Override
   public int hashCode() {
      return this.level.hashCode() ^ this.text.hashCode() ^ ObjectUtils.hashOrZero(this.exception);
   }

   @Override
   public String toString() {
      StringBuffer var1 = new StringBuffer();
      var1.append(this.getClass().getName());
      var1.append(String.format(" [ level -> %s, text -> \"%s\", exception -> %s]", this.level, this.text, this.exception));
      return var1.toString();
   }

   public static enum Level {
      ALL,
      CONFIG,
      FINE,
      FINER,
      FINEST,
      INFO,
      OFF,
      SEVERE,
      WARNING;
   }
}
