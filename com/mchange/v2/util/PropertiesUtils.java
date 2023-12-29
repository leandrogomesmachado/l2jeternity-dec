package com.mchange.v2.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public final class PropertiesUtils {
   public static int getIntProperty(Properties var0, String var1, int var2) throws NumberFormatException {
      String var3 = var0.getProperty(var1);
      return var3 != null ? Integer.parseInt(var3) : var2;
   }

   public static Properties fromString(String var0, String var1) throws UnsupportedEncodingException {
      try {
         Properties var2 = new Properties();
         if (var0 != null) {
            byte[] var3 = var0.getBytes(var1);
            var2.load(new ByteArrayInputStream(var3));
         }

         return var2;
      } catch (UnsupportedEncodingException var4) {
         throw var4;
      } catch (IOException var5) {
         throw new Error("Huh? An IOException while working with byte array streams?!", var5);
      }
   }

   public static Properties fromString(String var0) {
      try {
         return fromString(var0, "ISO-8859-1");
      } catch (UnsupportedEncodingException var2) {
         throw new Error("Huh? An ISO-8859-1 is an unsupported encoding?!", var2);
      }
   }

   public static String toString(Properties var0, String var1, String var2) throws UnsupportedEncodingException {
      try {
         ByteArrayOutputStream var3 = new ByteArrayOutputStream();
         var0.store(var3, var1);
         var3.flush();
         return new String(var3.toByteArray(), var2);
      } catch (UnsupportedEncodingException var4) {
         throw var4;
      } catch (IOException var5) {
         throw new Error("Huh? An IOException while working with byte array streams?!", var5);
      }
   }

   public static String toString(Properties var0, String var1) {
      try {
         return toString(var0, var1, "ISO-8859-1");
      } catch (UnsupportedEncodingException var3) {
         throw new Error("Huh? An ISO-8859-1 is an unsupported encoding?!", var3);
      }
   }

   private PropertiesUtils() {
   }
}
