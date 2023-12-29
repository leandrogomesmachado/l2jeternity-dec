package com.mchange.rmi;

import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RegistryManager {
   public static Registry ensureRegistry(int var0) throws RemoteException {
      Registry var1 = findRegistry(var0);
      if (var1 == null) {
         var1 = LocateRegistry.createRegistry(var0);
      }

      return var1;
   }

   public static Registry ensureRegistry() throws RemoteException {
      return ensureRegistry(1099);
   }

   public static boolean registryAvailable(int var0) throws RemoteException, AccessException {
      try {
         Registry var1 = LocateRegistry.getRegistry(var0);
         var1.list();
         return true;
      } catch (ConnectException var2) {
         return false;
      }
   }

   public static boolean registryAvailable() throws RemoteException, AccessException {
      return registryAvailable(1099);
   }

   public static Registry findRegistry(int var0) throws RemoteException, AccessException {
      return !registryAvailable(var0) ? null : LocateRegistry.getRegistry(var0);
   }

   public static Registry findRegistry() throws RemoteException, AccessException {
      return findRegistry(1099);
   }
}
