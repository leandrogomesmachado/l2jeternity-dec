package com.mchange.v1.db.sql;

import com.mchange.v1.util.ClosableResource;
import java.sql.Connection;
import java.sql.PreparedStatement;

public interface ConnectionBundle extends ClosableResource {
   Connection getConnection();

   PreparedStatement getStatement(String var1);

   void putStatement(String var1, PreparedStatement var2);
}
