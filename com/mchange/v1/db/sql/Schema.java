package com.mchange.v1.db.sql;

import java.sql.Connection;
import java.sql.SQLException;

public interface Schema {
   void createSchema(Connection var1) throws SQLException;

   void dropSchema(Connection var1) throws SQLException;

   String getStatementText(String var1, String var2);
}
