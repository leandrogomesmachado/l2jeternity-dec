package org.apache.commons.dbcp.datasources;

import java.sql.SQLException;
import javax.sql.PooledConnection;

interface PooledConnectionManager {
   void invalidate(PooledConnection var1) throws SQLException;

   void setPassword(String var1);

   void closePool(String var1) throws SQLException;
}
