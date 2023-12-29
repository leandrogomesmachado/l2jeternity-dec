package com.mchange.rmi;

import com.mchange.io.UnsupportedVersionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public class RMIRegistryCallingCard implements CallingCard, Serializable {
   transient Remote cached = null;
   transient String url;
   static final long serialVersionUID = 1L;
   private static final short VERSION = 1;

   public RMIRegistryCallingCard(String var1, int var2, String var3) {
      this.url = "//" + var1.toLowerCase() + ':' + var2 + '/' + var3;
   }

   public RMIRegistryCallingCard(String var1, String var2) {
      this(var1, 1099, var2);
   }

   @Override
   public boolean equals(Object var1) {
      return var1 instanceof RMIRegistryCallingCard && this.url.equals(((RMIRegistryCallingCard)var1).url);
   }

   @Override
   public int hashCode() {
      return this.url.hashCode();
   }

   @Override
   public Remote findRemote() throws ServiceUnavailableException, RemoteException {
      if (this.cached instanceof Checkable) {
         try {
            ((Checkable)this.cached).check();
            return this.cached;
         } catch (RemoteException var2) {
            this.cached = null;
            return this.findRemote();
         }
      } else {
         try {
            Remote var1 = Naming.lookup(this.url);
            if (var1 instanceof Checkable) {
               this.cached = var1;
            }

            return var1;
         } catch (NotBoundException var3) {
            throw new ServiceUnavailableException("Object Not Bound: " + this.url);
         } catch (MalformedURLException var4) {
            throw new ServiceUnavailableException("Uh oh. Bad url. It never will be available: " + this.url);
         }
      }
   }

   @Override
   public String toString() {
      return super.toString() + " [" + this.url + "];";
   }

   private void writeObject(ObjectOutputStream var1) throws IOException {
      var1.writeShort(1);
      var1.writeUTF(this.url);
   }

   private void readObject(ObjectInputStream var1) throws IOException {
      short var2 = var1.readShort();
      switch(var2) {
         case 1:
            this.url = var1.readUTF();
            return;
         default:
            throw new UnsupportedVersionException(this.getClass().getName() + "; Bad version: " + var2);
      }
   }
}
