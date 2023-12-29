package org.apache.commons.dbcp.managed;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbcp.ConnectionFactory;

public interface XAConnectionFactory extends ConnectionFactory {
   TransactionRegistry getTransactionRegistry();

   @Override
   Connection createConnection() throws SQLException;
}
