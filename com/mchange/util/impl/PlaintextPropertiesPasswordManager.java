package com.mchange.util.impl;

import com.mchange.util.PasswordManager;
import java.io.File;
import java.io.IOException;

public class PlaintextPropertiesPasswordManager implements PasswordManager {
   private static final String PASSWORD_PROP_PFX = "password.";
   private static final String HEADER = "com.mchange.util.impl.PlaintextPropertiesPasswordManager data";
   SyncedProperties props;

   public PlaintextPropertiesPasswordManager(File var1) throws IOException {
      this.props = new SyncedProperties(var1, "com.mchange.util.impl.PlaintextPropertiesPasswordManager data");
   }

   @Override
   public boolean validate(String var1, String var2) throws IOException {
      return var2.equals(this.props.getProperty("password." + var1));
   }

   @Override
   public boolean updatePassword(String var1, String var2, String var3) throws IOException {
      if (!this.validate(var1, var2)) {
         return false;
      } else {
         this.props.put("password." + var1, var3);
         return true;
      }
   }
}
