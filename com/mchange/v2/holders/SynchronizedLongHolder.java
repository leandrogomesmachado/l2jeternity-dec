package com.mchange.v2.holders;

import com.mchange.v2.ser.UnsupportedVersionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SynchronizedLongHolder implements ThreadSafeLongHolder, Serializable {
   transient long value;
   static final long serialVersionUID = 1L;
   private static final short VERSION = 1;

   @Override
   public synchronized long getValue() {
      return this.value;
   }

   @Override
   public synchronized void setValue(long var1) {
      this.value = var1;
   }

   public SynchronizedLongHolder(long var1) {
      this.value = var1;
   }

   public SynchronizedLongHolder() {
      this(0L);
   }

   private void writeObject(ObjectOutputStream var1) throws IOException {
      var1.writeShort(1);
      var1.writeLong(this.value);
   }

   private void readObject(ObjectInputStream var1) throws IOException {
      short var2 = var1.readShort();
      switch(var2) {
         case 1:
            this.value = var1.readLong();
            return;
         default:
            throw new UnsupportedVersionException(this, var2);
      }
   }
}
