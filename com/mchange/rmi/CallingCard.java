package com.mchange.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CallingCard {
   Remote findRemote() throws ServiceUnavailableException, RemoteException;

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();

   @Override
   String toString();
}
