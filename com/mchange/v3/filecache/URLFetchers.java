package com.mchange.v3.filecache;

import com.mchange.v1.io.InputStreamUtils;
import com.mchange.v1.io.ReaderUtils;
import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLogger;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;

public enum URLFetchers implements URLFetcher {
   DEFAULT {
      @Override
      public InputStream openStream(URL var1, MLogger var2) throws IOException {
         return var1.openStream();
      }
   },
   BUFFERED_WGET {
      @Override
      public InputStream openStream(URL var1, MLogger var2) throws IOException {
         Process var3 = new ProcessBuilder("wget", "-O", "-", var1.toString()).start();
         BufferedInputStream var4 = null;

         ByteArrayInputStream var34;
         try {
            var4 = new BufferedInputStream(var3.getInputStream(), 1048576);
            ByteArrayOutputStream var5 = new ByteArrayOutputStream(1048576);

            for(int var6 = var4.read(); var6 >= 0; var6 = var4.read()) {
               var5.write(var6);
            }

            var34 = new ByteArrayInputStream(var5.toByteArray());
         } finally {
            InputStreamUtils.attemptClose(var4);
            if (var2.isLoggable(MLevel.FINER)) {
               BufferedReader var12 = null;

               try {
                  var12 = new BufferedReader(new InputStreamReader(var3.getErrorStream()), 1048576);
                  StringWriter var13 = new StringWriter(1048576);

                  for(int var14 = var12.read(); var14 >= 0; var14 = var12.read()) {
                     var13.write(var14);
                  }

                  var2.log(MLevel.FINER, "wget error stream for '" + var1 + "':\n " + var13.toString());
               } finally {
                  ReaderUtils.attemptClose(var12);
               }
            }

            try {
               int var35 = var3.waitFor();
               if (var35 != 0) {
                  throw new IOException("wget process terminated abnormally [return code: " + var35 + "]");
               }
            } catch (InterruptedException var31) {
               if (var2.isLoggable(MLevel.FINER)) {
                  var2.log(MLevel.FINER, "InterruptedException while waiting for wget to complete.", (Throwable)var31);
               }

               throw new IOException("Interrupted while waiting for wget to complete: " + var31);
            }
         }

         return var34;
      }
   };

   private URLFetchers() {
   }
}
