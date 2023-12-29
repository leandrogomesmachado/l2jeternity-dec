package com.mysql.cj.jdbc.jmx;

import java.sql.SQLException;

public interface ReplicationGroupManagerMBean {
   void addSlaveHost(String var1, String var2) throws SQLException;

   void removeSlaveHost(String var1, String var2) throws SQLException;

   void promoteSlaveToMaster(String var1, String var2) throws SQLException;

   void removeMasterHost(String var1, String var2) throws SQLException;

   String getMasterHostsList(String var1);

   String getSlaveHostsList(String var1);

   String getRegisteredConnectionGroups();

   int getActiveMasterHostCount(String var1);

   int getActiveSlaveHostCount(String var1);

   int getSlavePromotionCount(String var1);

   long getTotalLogicalConnectionCount(String var1);

   long getActiveLogicalConnectionCount(String var1);
}
