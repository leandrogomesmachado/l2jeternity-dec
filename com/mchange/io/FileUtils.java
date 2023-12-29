package com.mchange.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public final class FileUtils {
   public static byte[] getBytes(File var0, int var1) throws IOException {
      BufferedInputStream var2 = new BufferedInputStream(new FileInputStream(var0));

      byte[] var3;
      try {
         var3 = InputStreamUtils.getBytes(var2, var1);
      } finally {
         InputStreamUtils.attemptClose(var2);
      }

      return var3;
   }

   public static byte[] getBytes(File var0) throws IOException {
      BufferedInputStream var1 = new BufferedInputStream(new FileInputStream(var0));

      byte[] var2;
      try {
         var2 = InputStreamUtils.getBytes(var1);
      } finally {
         InputStreamUtils.attemptClose(var1);
      }

      return var2;
   }

   public static String getContentsAsString(File var0, String var1) throws IOException, UnsupportedEncodingException {
      BufferedInputStream var2 = new BufferedInputStream(new FileInputStream(var0));

      String var3;
      try {
         var3 = InputStreamUtils.getContentsAsString(var2, var1);
      } finally {
         InputStreamUtils.attemptClose(var2);
      }

      return var3;
   }

   public static String getContentsAsString(File var0) throws IOException {
      BufferedInputStream var1 = new BufferedInputStream(new FileInputStream(var0));

      String var2;
      try {
         var2 = InputStreamUtils.getContentsAsString(var1);
      } finally {
         InputStreamUtils.attemptClose(var1);
      }

      return var2;
   }

   public static String getContentsAsString(File var0, int var1, String var2) throws IOException, UnsupportedEncodingException {
      BufferedInputStream var3 = new BufferedInputStream(new FileInputStream(var0));

      String var4;
      try {
         var4 = InputStreamUtils.getContentsAsString(var3, var1, var2);
      } finally {
         InputStreamUtils.attemptClose(var3);
      }

      return var4;
   }

   public static String getContentsAsString(File var0, int var1) throws IOException {
      BufferedInputStream var2 = new BufferedInputStream(new FileInputStream(var0));

      String var3;
      try {
         var3 = InputStreamUtils.getContentsAsString(var2, var1);
      } finally {
         InputStreamUtils.attemptClose(var2);
      }

      return var3;
   }

   private FileUtils() {
   }
}
