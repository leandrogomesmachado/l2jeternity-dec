package com.mchange.v2.uid;

import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.security.SecureRandom;

public final class UidUtils {
   static final MLogger logger = MLog.getLogger(UidUtils.class);
   public static final String VM_ID = generateVmId();
   private static long within_vm_seq_counter = 0L;

   private static String generateVmId() {
      DataOutputStream var0 = null;
      DataInputStream var1 = null;

      String var3;
      try {
         try {
            SecureRandom var2 = new SecureRandom();
            ByteArrayOutputStream var28 = new ByteArrayOutputStream();
            var0 = new DataOutputStream(var28);

            try {
               var0.write(InetAddress.getLocalHost().getAddress());
            } catch (Exception var25) {
               if (logger.isLoggable(MLevel.INFO)) {
                  logger.log(
                     MLevel.INFO,
                     "Failed to get local InetAddress for VMID. This is unlikely to matter. At all. We'll add some extra randomness",
                     (Throwable)var25
                  );
               }

               var0.write(var2.nextInt());
            }

            var0.writeLong(System.currentTimeMillis());
            var0.write(var2.nextInt());
            int var4 = var28.size() % 4;
            if (var4 > 0) {
               int var5 = 4 - var4;
               byte[] var6 = new byte[var5];
               var2.nextBytes(var6);
               var0.write(var6);
            }

            StringBuffer var29 = new StringBuffer(32);
            byte[] var30 = var28.toByteArray();
            var1 = new DataInputStream(new ByteArrayInputStream(var30));
            int var7 = 0;

            for(int var8 = var30.length / 4; var7 < var8; ++var7) {
               int var9 = var1.readInt();
               long var10 = (long)var9 & 4294967295L;
               var29.append(Long.toString(var10, 36));
            }

            return var29.toString();
         } catch (IOException var26) {
            if (logger.isLoggable(MLevel.WARNING)) {
               logger.log(
                  MLevel.WARNING,
                  "Bizarro! IOException while reading/writing from ByteArray-based streams? We're skipping the VMID thing. It almost certainly doesn't matter, but please report the error.",
                  (Throwable)var26
               );
            }
         }

         var3 = "";
      } finally {
         try {
            if (var0 != null) {
               var0.close();
            }
         } catch (IOException var24) {
            logger.log(MLevel.WARNING, "Huh? Exception close()ing a byte-array bound OutputStream.", (Throwable)var24);
         }

         try {
            if (var1 != null) {
               var1.close();
            }
         } catch (IOException var23) {
            logger.log(MLevel.WARNING, "Huh? Exception close()ing a byte-array bound IntputStream.", (Throwable)var23);
         }
      }

      return var3;
   }

   private static synchronized long nextWithinVmSeq() {
      return ++within_vm_seq_counter;
   }

   public static String allocateWithinVmSequential() {
      return VM_ID + "#" + nextWithinVmSeq();
   }

   private UidUtils() {
   }
}
