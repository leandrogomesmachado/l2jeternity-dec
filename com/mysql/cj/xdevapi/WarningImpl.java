package com.mysql.cj.xdevapi;

public class WarningImpl implements Warning {
   private com.mysql.cj.protocol.Warning message;

   public WarningImpl(com.mysql.cj.protocol.Warning message) {
      this.message = message;
   }

   @Override
   public int getLevel() {
      return this.message.getLevel();
   }

   @Override
   public long getCode() {
      return this.message.getCode();
   }

   @Override
   public String getMessage() {
      return this.message.getMessage();
   }
}
