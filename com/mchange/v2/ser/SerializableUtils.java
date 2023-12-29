package com.mchange.v2.ser;

import com.mchange.v1.io.InputStreamUtils;
import com.mchange.v1.io.OutputStreamUtils;
import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class SerializableUtils {
   static final MLogger logger = MLog.getLogger(SerializableUtils.class);

   private SerializableUtils() {
   }

   public static byte[] toByteArray(Object var0) throws NotSerializableException {
      return serializeToByteArray(var0);
   }

   public static byte[] toByteArray(Object var0, Indirector var1, IndirectPolicy var2) throws NotSerializableException {
      try {
         if (var2 == IndirectPolicy.DEFINITELY_INDIRECT) {
            if (var1 == null) {
               throw new IllegalArgumentException("null indirector is not consistent with " + var2);
            } else {
               IndirectlySerialized var3 = var1.indirectForm(var0);
               return toByteArray(var3);
            }
         } else if (var2 == IndirectPolicy.INDIRECT_ON_EXCEPTION) {
            if (var1 == null) {
               throw new IllegalArgumentException("null indirector is not consistent with " + var2);
            } else {
               try {
                  return toByteArray(var0);
               } catch (NotSerializableException var4) {
                  return toByteArray(var0, var1, IndirectPolicy.DEFINITELY_INDIRECT);
               }
            }
         } else if (var2 == IndirectPolicy.DEFINITELY_DIRECT) {
            return toByteArray(var0);
         } else {
            throw new InternalError("unknown indirecting policy: " + var2);
         }
      } catch (NotSerializableException var5) {
         throw var5;
      } catch (Exception var6) {
         if (logger.isLoggable(MLevel.WARNING)) {
            logger.log(MLevel.WARNING, "An Exception occurred while serializing an Object to a byte[] with an Indirector.", (Throwable)var6);
         }

         throw new NotSerializableException(var6.toString());
      }
   }

   /** @deprecated */
   public static byte[] serializeToByteArray(Object var0) throws NotSerializableException {
      try {
         ByteArrayOutputStream var1 = new ByteArrayOutputStream();
         ObjectOutputStream var2 = new ObjectOutputStream(var1);
         var2.writeObject(var0);
         return var1.toByteArray();
      } catch (NotSerializableException var3) {
         var3.fillInStackTrace();
         throw var3;
      } catch (IOException var4) {
         if (logger.isLoggable(MLevel.SEVERE)) {
            logger.log(MLevel.SEVERE, "An IOException occurred while writing into a ByteArrayOutputStream?!?", (Throwable)var4);
         }

         throw new Error("IOException writing to a byte array!");
      }
   }

   public static Object fromByteArray(byte[] var0) throws IOException, ClassNotFoundException {
      Object var1 = deserializeFromByteArray(var0);
      return var1 instanceof IndirectlySerialized ? ((IndirectlySerialized)var1).getObject() : var1;
   }

   public static Object fromByteArray(byte[] var0, boolean var1) throws IOException, ClassNotFoundException {
      return var1 ? deserializeFromByteArray(var0) : fromByteArray(var0);
   }

   /** @deprecated */
   public static Object deserializeFromByteArray(byte[] var0) throws IOException, ClassNotFoundException {
      ObjectInputStream var1 = new ObjectInputStream(new ByteArrayInputStream(var0));
      return var1.readObject();
   }

   public static Object testSerializeDeserialize(Object var0) throws IOException, ClassNotFoundException {
      return deepCopy(var0);
   }

   public static Object deepCopy(Object var0) throws IOException, ClassNotFoundException {
      byte[] var1 = serializeToByteArray(var0);
      return deserializeFromByteArray(var1);
   }

   public static final Object unmarshallObjectFromFile(File var0) throws IOException, ClassNotFoundException {
      ObjectInputStream var1 = null;

      Object var2;
      try {
         var1 = new ObjectInputStream(new BufferedInputStream(new FileInputStream(var0)));
         var2 = var1.readObject();
      } finally {
         InputStreamUtils.attemptClose(var1);
      }

      return var2;
   }

   public static final void marshallObjectToFile(Object var0, File var1) throws IOException {
      ObjectOutputStream var2 = null;

      try {
         var2 = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(var1)));
         var2.writeObject(var0);
      } finally {
         OutputStreamUtils.attemptClose(var2);
      }
   }
}
