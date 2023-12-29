package com.mchange.v2.holders;

import com.mchange.v2.ser.UnsupportedVersionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class VolatileIntHolder implements ThreadSafeIntHolder, Serializable {
   transient volatile int value;
   static final long serialVersionUID = 1L;
   private static final short VERSION = 1;

   @Override
   public int getValue() {
      return this.value;
   }

   @Override
   public void setValue(int var1) {
      this.value = var1;
   }

   private void writeObject(ObjectOutputStream var1) throws IOException {
      var1.writeShort(1);
      var1.writeInt(this.value);
   }

   private void readObject(ObjectInputStream var1) throws IOException {
      short var2 = var1.readShort();
      switch(var2) {
         case 1:
            this.value = var1.readInt();
            return;
         default:
            throw new UnsupportedVersionException(this, var2);
      }
   }
}
