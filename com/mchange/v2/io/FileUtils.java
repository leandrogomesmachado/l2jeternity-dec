package com.mchange.v2.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public final class FileUtils {
   public static File findRelativeToParent(File var0, File var1) throws IOException {
      String var2 = var0.getPath();
      String var3 = var1.getPath();
      if (!var3.startsWith(var2)) {
         throw new IllegalArgumentException(var3 + " is not a child of " + var2 + " [no transformations or canonicalizations tried]");
      } else {
         String var4 = var3.substring(var2.length());
         File var5 = new File(var4);
         if (var5.isAbsolute()) {
            var5 = new File(var5.getPath().substring(1));
         }

         return var5;
      }
   }

   public static long diskSpaceUsed(File var0) throws IOException {
      long var1 = 0L;
      FileIterator var3 = DirectoryDescentUtils.depthFirstEagerDescent(var0);

      while(var3.hasNext()) {
         File var4 = var3.nextFile();
         if (var4.isFile()) {
            var1 += var4.length();
         }
      }

      return var1;
   }

   public static void touchExisting(File var0) throws IOException {
      if (var0.exists()) {
         unguardedTouch(var0);
      }
   }

   public static void touch(File var0) throws IOException {
      if (!var0.exists()) {
         createEmpty(var0);
      }

      unguardedTouch(var0);
   }

   public static void createEmpty(File var0) throws IOException {
      RandomAccessFile var1 = null;

      try {
         var1 = new RandomAccessFile(var0, "rws");
         var1.setLength(0L);
      } finally {
         try {
            if (var1 != null) {
               var1.close();
            }
         } catch (IOException var8) {
            var8.printStackTrace();
         }
      }
   }

   private static void unguardedTouch(File var0) throws IOException {
      var0.setLastModified(System.currentTimeMillis());
   }

   private FileUtils() {
   }
}
