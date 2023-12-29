package com.mchange.util;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemotePasswordManager extends PasswordManager, Remote {
   @Override
   boolean validate(String var1, String var2) throws RemoteException, IOException;

   @Override
   boolean updatePassword(String var1, String var2, String var3) throws RemoteException, IOException;
}
