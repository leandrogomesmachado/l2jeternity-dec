package org.apache.commons.dbcp.cpdsadapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.commons.dbcp.PoolablePreparedStatement;
import org.apache.commons.pool.KeyedObjectPool;

class PoolablePreparedStatementStub extends PoolablePreparedStatement {
   public PoolablePreparedStatementStub(PreparedStatement stmt, Object key, KeyedObjectPool pool, Connection conn) {
      super(stmt, key, pool, conn);
   }

   @Override
   protected void activate() throws SQLException {
      super.activate();
   }

   @Override
   protected void passivate() throws SQLException {
      super.passivate();
   }
}
