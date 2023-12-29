package com.mchange.lang;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/** @deprecated */
public final class ObjectUtils {
   public static final Object DUMMY_OBJECT = new Object();

   private ObjectUtils() {
   }

   public static byte[] objectToByteArray(Object var0) throws NotSerializableException {
      try {
         ByteArrayOutputStream var1 = new ByteArrayOutputStream();
         ObjectOutputStream var2 = new ObjectOutputStream(var1);
         var2.writeObject(var0);
         return var1.toByteArray();
      } catch (NotSerializableException var3) {
         var3.fillInStackTrace();
         throw var3;
      } catch (IOException var4) {
         var4.printStackTrace();
         throw new Error("IOException writing to a byte array!");
      }
   }

   public static Object objectFromByteArray(byte[] var0) throws IOException, ClassNotFoundException {
      ObjectInputStream var1 = new ObjectInputStream(new ByteArrayInputStream(var0));
      return var1.readObject();
   }
}
