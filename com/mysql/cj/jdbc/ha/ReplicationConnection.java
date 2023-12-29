package com.mysql.cj.jdbc.ha;

import com.mysql.cj.jdbc.JdbcConnection;
import java.sql.SQLException;

public interface ReplicationConnection extends JdbcConnection {
   long getConnectionGroupId();

   JdbcConnection getCurrentConnection();

   JdbcConnection getMasterConnection();

   void promoteSlaveToMaster(String var1) throws SQLException;

   void removeMasterHost(String var1) throws SQLException;

   void removeMasterHost(String var1, boolean var2) throws SQLException;

   boolean isHostMaster(String var1);

   JdbcConnection getSlavesConnection();

   void addSlaveHost(String var1) throws SQLException;

   void removeSlave(String var1) throws SQLException;

   void removeSlave(String var1, boolean var2) throws SQLException;

   boolean isHostSlave(String var1);
}
