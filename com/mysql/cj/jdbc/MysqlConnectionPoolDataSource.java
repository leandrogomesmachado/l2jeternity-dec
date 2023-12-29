package com.mysql.cj.jdbc;

import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

public class MysqlConnectionPoolDataSource extends MysqlDataSource implements ConnectionPoolDataSource {
   static final long serialVersionUID = -7767325445592304961L;

   @Override
   public synchronized PooledConnection getPooledConnection() throws SQLException {
      try {
         Connection connection = this.getConnection();
         return MysqlPooledConnection.getInstance((JdbcConnection)connection);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4);
      }
   }

   @Override
   public synchronized PooledConnection getPooledConnection(String u, String p) throws SQLException {
      try {
         Connection connection = this.getConnection(u, p);
         return MysqlPooledConnection.getInstance((JdbcConnection)connection);
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6);
      }
   }
}
