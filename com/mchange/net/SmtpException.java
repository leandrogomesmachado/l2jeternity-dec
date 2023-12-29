package com.mchange.net;

public class SmtpException extends ProtocolException {
   int resp_num;

   public SmtpException() {
   }

   public SmtpException(String var1) {
      super(var1);
   }

   public SmtpException(int var1, String var2) {
      this(var2);
      this.resp_num = var1;
   }

   public int getSmtpResponseNumber() {
      return this.resp_num;
   }
}
