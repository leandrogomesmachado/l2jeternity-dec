package com.mchange.v1.io;

import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public final class InputStreamUtils {
   private static final MLogger logger = MLog.getLogger(InputStreamUtils.class);
   private static InputStream EMPTY_ISTREAM = new ByteArrayInputStream(new byte[0]);

   public static boolean compare(InputStream var0, InputStream var1, long var2) throws IOException {
      for(long var5 = 0L; var5 < var2; ++var5) {
         int var4;
         if ((var4 = var0.read()) != var1.read()) {
            return false;
         }

         if (var4 < 0) {
            break;
         }
      }

      return true;
   }

   public static boolean compare(InputStream var0, InputStream var1) throws IOException {
      int var2 = 0;

      while(var2 >= 0) {
         if ((var2 = var0.read()) != var1.read()) {
            return false;
         }
      }

      return true;
   }

   public static byte[] getBytes(InputStream var0, int var1) throws IOException {
      ByteArrayOutputStream var2 = new ByteArrayOutputStream(var1);
      int var3 = 0;

      for(int var4 = var0.read(); var4 >= 0 && var3 < var1; ++var3) {
         var2.write(var4);
         var4 = var0.read();
      }

      return var2.toByteArray();
   }

   public static byte[] getBytes(InputStream var0) throws IOException {
      ByteArrayOutputStream var1 = new ByteArrayOutputStream();

      for(int var2 = var0.read(); var2 >= 0; var2 = var0.read()) {
         var1.write(var2);
      }

      return var1.toByteArray();
   }

   public static String getContentsAsString(InputStream var0, String var1) throws IOException, UnsupportedEncodingException {
      return new String(getBytes(var0), var1);
   }

   public static String getContentsAsString(InputStream var0) throws IOException {
      try {
         return getContentsAsString(var0, System.getProperty("file.encoding", "8859_1"));
      } catch (UnsupportedEncodingException var2) {
         throw new InternalError("You have no default character encoding, and iso-8859-1 is unsupported?!?!");
      }
   }

   public static String getContentsAsString(InputStream var0, int var1, String var2) throws IOException, UnsupportedEncodingException {
      return new String(getBytes(var0, var1), var2);
   }

   public static String getContentsAsString(InputStream var0, int var1) throws IOException {
      try {
         return getContentsAsString(var0, var1, System.getProperty("file.encoding", "8859_1"));
      } catch (UnsupportedEncodingException var3) {
         throw new InternalError("You have no default character encoding, and iso-8859-1 is unsupported?!?!");
      }
   }

   public static InputStream getEmptyInputStream() {
      return EMPTY_ISTREAM;
   }

   public static void attemptClose(InputStream var0) {
      try {
         if (var0 != null) {
            var0.close();
         }
      } catch (IOException var2) {
         if (logger.isLoggable(MLevel.WARNING)) {
            logger.log(MLevel.WARNING, "InputStream close FAILED.", (Throwable)var2);
         }
      }
   }

   public static void skipFully(InputStream var0, long var1) throws EOFException, IOException {
      long var3 = 0L;

      while(var3 < var1) {
         long var5 = var0.skip(var1 - var3);
         if (var5 > 0L) {
            var3 += var5;
         } else {
            int var7 = var0.read();
            if (var0.read() < 0) {
               throw new EOFException("Skipped only " + var3 + " bytes to end of file.");
            }

            ++var3;
         }
      }
   }

   private InputStreamUtils() {
   }
}
