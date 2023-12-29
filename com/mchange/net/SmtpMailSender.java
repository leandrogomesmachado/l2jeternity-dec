package com.mchange.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class SmtpMailSender implements MailSender {
   InetAddress hostAddr;
   int port;

   public SmtpMailSender(InetAddress var1, int var2) {
      this.hostAddr = var1;
      this.port = var2;
   }

   public SmtpMailSender(InetAddress var1) {
      this(var1, 25);
   }

   public SmtpMailSender(String var1, int var2) throws UnknownHostException {
      this(InetAddress.getByName(var1), var2);
   }

   public SmtpMailSender(String var1) throws UnknownHostException {
      this(var1, 25);
   }

   @Override
   public void sendMail(String var1, String[] var2, String[] var3, String[] var4, String var5, String var6, String var7) throws IOException, ProtocolException, UnsupportedEncodingException {
      if (var2 != null && var2.length >= 1) {
         Properties var8 = new Properties();
         var8.put("From", var1);
         var8.put("To", makeRecipientString(var2));
         var8.put("Subject", var5);
         var8.put("MIME-Version", "1.0");
         var8.put("Content-Type", "text/plain; charset=" + MimeUtils.normalEncoding(var7));
         var8.put("X-Generator", this.getClass().getName());
         String[] var9;
         if (var3 == null && var4 == null) {
            var9 = var2;
         } else {
            int var10 = var2.length + (var3 != null ? var3.length : 0) + (var4 != null ? var4.length : 0);
            var9 = new String[var10];
            int var11 = 0;
            System.arraycopy(var2, 0, var9, var11, var2.length);
            var11 += var2.length;
            if (var3 != null) {
               System.arraycopy(var3, 0, var9, var11, var3.length);
               var11 += var3.length;
               var8.put("CC", makeRecipientString(var3));
            }

            if (var4 != null) {
               System.arraycopy(var4, 0, var9, var11, var4.length);
            }
         }

         SmtpUtils.sendMail(this.hostAddr, this.port, var1, var9, var8, var6.getBytes(var7));
      } else {
         throw new SmtpException("You must specify at least one recipient in the \"to\" field.");
      }
   }

   @Override
   public void sendMail(String var1, String[] var2, String[] var3, String[] var4, String var5, String var6) throws IOException, ProtocolException {
      try {
         this.sendMail(var1, var2, var3, var4, var5, var6, System.getProperty("file.encoding"));
      } catch (UnsupportedEncodingException var8) {
         throw new InternalError("Default encoding [" + System.getProperty("file.encoding") + "] not supported???");
      }
   }

   private static String makeRecipientString(String[] var0) {
      StringBuffer var1 = new StringBuffer(256);
      int var2 = 0;

      for(int var3 = var0.length; var2 < var3; ++var2) {
         if (var2 != 0) {
            var1.append(", ");
         }

         var1.append(var0[var2]);
      }

      return var1.toString();
   }

   public static void main(String[] var0) {
      try {
         String[] var1 = new String[]{"stevewaldman@uky.edu"};
         String[] var2 = new String[0];
         String[] var3 = new String[]{"stevewaldman@mac.com"};
         String var4 = "swaldman@mchange.com";
         String var5 = "Test SmtpMailSender Again";
         String var6 = "Wheeeee!!!";
         SmtpMailSender var7 = new SmtpMailSender("localhost");
         var7.sendMail(var4, var1, var2, var3, var5, var6);
      } catch (Exception var8) {
         var8.printStackTrace();
      }
   }
}
