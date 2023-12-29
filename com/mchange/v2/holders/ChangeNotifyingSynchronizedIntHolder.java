package com.mchange.v2.holders;

import com.mchange.v2.ser.UnsupportedVersionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class ChangeNotifyingSynchronizedIntHolder implements ThreadSafeIntHolder, Serializable {
   transient int value;
   transient boolean notify_all;
   static final long serialVersionUID = 1L;
   private static final short VERSION = 1;

   public ChangeNotifyingSynchronizedIntHolder(int var1, boolean var2) {
      this.value = var1;
      this.notify_all = var2;
   }

   public ChangeNotifyingSynchronizedIntHolder() {
      this(0, true);
   }

   @Override
   public synchronized int getValue() {
      return this.value;
   }

   @Override
   public synchronized void setValue(int var1) {
      if (var1 != this.value) {
         this.value = var1;
         this.doNotify();
      }
   }

   public synchronized void increment() {
      ++this.value;
      this.doNotify();
   }

   public synchronized void decrement() {
      --this.value;
      this.doNotify();
   }

   private void doNotify() {
      if (this.notify_all) {
         this.notifyAll();
      } else {
         this.notify();
      }
   }

   private void writeObject(ObjectOutputStream var1) throws IOException {
      var1.writeShort(1);
      var1.writeInt(this.value);
      var1.writeBoolean(this.notify_all);
   }

   private void readObject(ObjectInputStream var1) throws IOException {
      short var2 = var1.readShort();
      switch(var2) {
         case 1:
            this.value = var1.readInt();
            this.notify_all = var1.readBoolean();
            return;
         default:
            throw new UnsupportedVersionException(this, var2);
      }
   }
}
