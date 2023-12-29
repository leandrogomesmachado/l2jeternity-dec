package com.mchange.util;

import java.io.IOException;

public interface PasswordManager {
   boolean validate(String var1, String var2) throws IOException;

   boolean updatePassword(String var1, String var2, String var3) throws IOException;
}
