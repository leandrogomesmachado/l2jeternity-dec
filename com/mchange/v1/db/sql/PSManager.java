package com.mchange.v1.db.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;

public interface PSManager {
   PreparedStatement getPS(Connection var1, String var2);

   void putPS(Connection var1, String var2, PreparedStatement var3);
}
