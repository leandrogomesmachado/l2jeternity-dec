package com.mchange.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/** @deprecated */
public final class SerializableUtils {
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

   private SerializableUtils() {
   }
}
