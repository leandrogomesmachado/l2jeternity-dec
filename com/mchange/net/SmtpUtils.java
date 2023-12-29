package com.mchange.net;

import com.mchange.io.OutputStreamUtils;
import com.mchange.io.ReaderUtils;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Properties;

public final class SmtpUtils {
   private static final String ENC = "8859_1";
   private static final String CRLF = "\r\n";
   private static final String CHARSET = "charset";
   private static final int CHARSET_LEN = "charset".length();
   public static final int DEFAULT_SMTP_PORT = 25;

   public static void sendMail(InetAddress var0, int var1, String var2, String[] var3, Properties var4, byte[] var5) throws IOException, SmtpException {
      Socket var6 = null;
      DataOutputStream var7 = null;
      BufferedReader var8 = null;

      try {
         var6 = new Socket(var0, var1);
         var7 = new DataOutputStream(new BufferedOutputStream(var6.getOutputStream()));
         var8 = new BufferedReader(new InputStreamReader(var6.getInputStream(), "8859_1"));
         ensureResponse(var8, 200, 300);
         var7.writeBytes("HELO " + var6.getLocalAddress().getHostName() + "\r\n");
         var7.flush();
         ensureResponse(var8, 200, 300);
         var7.writeBytes("MAIL FROM: " + var2 + "\r\n");
         var7.flush();
         ensureResponse(var8, 200, 300);
         int var9 = var3.length;

         while(--var9 >= 0) {
            var7.writeBytes("RCPT TO: " + var3[var9] + "\r\n");
            var7.flush();
            ensureResponse(var8, 200, 300);
         }

         var7.writeBytes("DATA\r\n");
         var7.flush();
         ensureResponse(var8, 300, 400);
         Enumeration var17 = var4.keys();

         while(var17.hasMoreElements()) {
            String var10 = (String)var17.nextElement();
            String var11 = var4.getProperty(var10);
            var7.writeBytes(var10 + ": " + var11 + "\r\n");
         }

         var7.writeBytes("\r\n");
         var7.write(var5);
         var7.writeBytes("\r\n.\r\n");
         var7.flush();
         ensureResponse(var8, 200, 300);
         var7.writeBytes("QUIT\r\n");
         var7.flush();
      } catch (UnsupportedEncodingException var15) {
         var15.printStackTrace();
         throw new InternalError("8859_1 not supported???");
      } finally {
         OutputStreamUtils.attemptClose(var7);
         ReaderUtils.attemptClose(var8);
         SocketUtils.attemptClose(var6);
      }
   }

   private static String encodingFromContentType(String var0) {
      int var1 = var0.indexOf("charset");
      if (var1 >= 0) {
         String var2 = var0.substring(var1 + CHARSET_LEN);
         var2 = var2.trim();
         if (var2.charAt(0) != '=') {
            return encodingFromContentType(var2);
         } else {
            var2 = var2.substring(1).trim();
            int var3 = var2.indexOf(59);
            if (var3 >= 0) {
               var2 = var2.substring(0, var3);
            }

            return var2;
         }
      } else {
         return null;
      }
   }

   private static byte[] bytesFromBodyString(String var0, String var1) throws UnsupportedEncodingException {
      ByteArrayOutputStream var2 = new ByteArrayOutputStream();
      PrintWriter var3 = new PrintWriter(new OutputStreamWriter(var2, var1));
      var3.print(var0);
      var3.flush();
      return var2.toByteArray();
   }

   private static void ensureResponse(BufferedReader var0, int var1, int var2) throws IOException, SmtpException {
      String var3 = var0.readLine();

      try {
         int var4 = Integer.parseInt(var3.substring(0, 3));

         while(var3.charAt(3) == '-') {
            var3 = var0.readLine();
         }

         if (var4 < var1 || var4 >= var2) {
            throw new SmtpException(var4, var3);
         }
      } catch (NumberFormatException var6) {
         throw new SmtpException("Bad SMTP response while mailing document!");
      }
   }

   public static void main(String[] var0) {
      try {
         InetAddress var1 = InetAddress.getByName("mailhub.mchange.com");
         byte var2 = 25;
         String var3 = "octavia@mchange.com";
         String[] var4 = new String[]{"swaldman@mchange.com", "sw-lists@mchange.com"};
         Properties var5 = new Properties();
         var5.put("From", "goolash@mchange.com");
         var5.put("To", "garbage@mchange.com");
         var5.put("Subject", "Test test test AGAIN...");
         byte[] var6 = "This is a test AGAIN! Imagine that!".getBytes("8859_1");
         sendMail(var1, var2, var3, var4, var5, var6);
      } catch (Exception var7) {
         var7.printStackTrace();
      }
   }

   private SmtpUtils() {
   }
}
