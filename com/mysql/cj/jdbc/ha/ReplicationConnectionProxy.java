package com.mysql.cj.jdbc.ha;

import com.mysql.cj.Messages;
import com.mysql.cj.PingTarget;
import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.conf.url.LoadbalanceConnectionUrl;
import com.mysql.cj.conf.url.ReplicationConnectionUrl;
import com.mysql.cj.jdbc.JdbcConnection;
import com.mysql.cj.jdbc.JdbcStatement;
import com.mysql.cj.jdbc.exceptions.SQLError;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ReplicationConnectionProxy extends MultiHostConnectionProxy implements PingTarget {
   private ReplicationConnection thisAsReplicationConnection;
   protected boolean enableJMX = false;
   protected boolean allowMasterDownConnections = false;
   protected boolean allowSlaveDownConnections = false;
   protected boolean readFromMasterWhenNoSlaves = false;
   protected boolean readFromMasterWhenNoSlavesOriginal = false;
   protected boolean readOnly = false;
   ReplicationConnectionGroup connectionGroup;
   private long connectionGroupID = -1L;
   private List<HostInfo> masterHosts;
   protected LoadBalancedConnection masterConnection;
   private List<HostInfo> slaveHosts;
   protected LoadBalancedConnection slavesConnection;

   public static ReplicationConnection createProxyInstance(ReplicationConnectionUrl connectionUrl) throws SQLException {
      ReplicationConnectionProxy connProxy = new ReplicationConnectionProxy(connectionUrl);
      return (ReplicationConnection)Proxy.newProxyInstance(
         ReplicationConnection.class.getClassLoader(), new Class[]{ReplicationConnection.class, JdbcConnection.class}, connProxy
      );
   }

   private ReplicationConnectionProxy(ReplicationConnectionUrl connectionUrl) throws SQLException {
      Properties props = connectionUrl.getConnectionArgumentsAsProperties();
      this.thisAsReplicationConnection = (ReplicationConnection)this.thisAsConnection;
      this.connectionUrl = connectionUrl;
      String enableJMXAsString = props.getProperty("ha.enableJMX", "false");

      try {
         this.enableJMX = Boolean.parseBoolean(enableJMXAsString);
      } catch (Exception var14) {
         throw SQLError.createSQLException(Messages.getString("MultihostConnection.badValueForHaEnableJMX", new Object[]{enableJMXAsString}), "S1009", null);
      }

      String allowMasterDownConnectionsAsString = props.getProperty("allowMasterDownConnections", "false");

      try {
         this.allowMasterDownConnections = Boolean.parseBoolean(allowMasterDownConnectionsAsString);
      } catch (Exception var13) {
         throw SQLError.createSQLException(
            Messages.getString("ReplicationConnectionProxy.badValueForAllowMasterDownConnections", new Object[]{enableJMXAsString}), "S1009", null
         );
      }

      String allowSlaveDownConnectionsAsString = props.getProperty("allowSlaveDownConnections", "false");

      try {
         this.allowSlaveDownConnections = Boolean.parseBoolean(allowSlaveDownConnectionsAsString);
      } catch (Exception var12) {
         throw SQLError.createSQLException(
            Messages.getString("ReplicationConnectionProxy.badValueForAllowSlaveDownConnections", new Object[]{allowSlaveDownConnectionsAsString}),
            "S1009",
            null
         );
      }

      String readFromMasterWhenNoSlavesAsString = props.getProperty("readFromMasterWhenNoSlaves");

      try {
         this.readFromMasterWhenNoSlavesOriginal = Boolean.parseBoolean(readFromMasterWhenNoSlavesAsString);
      } catch (Exception var11) {
         throw SQLError.createSQLException(
            Messages.getString("ReplicationConnectionProxy.badValueForReadFromMasterWhenNoSlaves", new Object[]{readFromMasterWhenNoSlavesAsString}),
            "S1009",
            null
         );
      }

      String group = props.getProperty("replicationConnectionGroup", null);
      if (group != null) {
         this.connectionGroup = ReplicationConnectionGroupManager.getConnectionGroupInstance(group);
         if (this.enableJMX) {
            ReplicationConnectionGroupManager.registerJmx();
         }

         this.connectionGroupID = this.connectionGroup
            .registerReplicationConnection(
               this.thisAsReplicationConnection, connectionUrl.getMastersListAsHostPortPairs(), connectionUrl.getSlavesListAsHostPortPairs()
            );
         this.masterHosts = connectionUrl.getMasterHostsListFromHostPortPairs(this.connectionGroup.getMasterHosts());
         this.slaveHosts = connectionUrl.getSlaveHostsListFromHostPortPairs(this.connectionGroup.getSlaveHosts());
      } else {
         this.masterHosts = new ArrayList<>(connectionUrl.getMastersList());
         this.slaveHosts = new ArrayList<>(connectionUrl.getSlavesList());
      }

      this.resetReadFromMasterWhenNoSlaves();

      try {
         this.initializeSlavesConnection();
      } catch (SQLException var15) {
         if (!this.allowSlaveDownConnections) {
            if (this.connectionGroup != null) {
               this.connectionGroup.handleCloseConnection(this.thisAsReplicationConnection);
            }

            throw var15;
         }
      }

      SQLException exCaught = null;

      try {
         this.currentConnection = this.initializeMasterConnection();
      } catch (SQLException var10) {
         exCaught = var10;
      }

      if (this.currentConnection == null) {
         if (!this.allowMasterDownConnections || this.slavesConnection == null) {
            if (this.connectionGroup != null) {
               this.connectionGroup.handleCloseConnection(this.thisAsReplicationConnection);
            }

            if (exCaught != null) {
               throw exCaught;
            }

            throw SQLError.createSQLException(Messages.getString("ReplicationConnectionProxy.initializationWithEmptyHostsLists"), "S1009", null);
         }

         this.readOnly = true;
         this.currentConnection = this.slavesConnection;
      }
   }

   @Override
   JdbcConnection getNewWrapperForThisAsConnection() throws SQLException {
      return new ReplicationMySQLConnection(this);
   }

   @Override
   protected void propagateProxyDown(JdbcConnection proxyConn) {
      if (this.masterConnection != null) {
         this.masterConnection.setProxy(proxyConn);
      }

      if (this.slavesConnection != null) {
         this.slavesConnection.setProxy(proxyConn);
      }
   }

   @Override
   boolean shouldExceptionTriggerConnectionSwitch(Throwable t) {
      return false;
   }

   @Override
   public boolean isMasterConnection() {
      return this.currentConnection != null && this.currentConnection == this.masterConnection;
   }

   public boolean isSlavesConnection() {
      return this.currentConnection != null && this.currentConnection == this.slavesConnection;
   }

   @Override
   void pickNewConnection() throws SQLException {
   }

   @Override
   void syncSessionState(JdbcConnection source, JdbcConnection target, boolean readonly) throws SQLException {
      try {
         super.syncSessionState(source, target, readonly);
      } catch (SQLException var7) {
         try {
            super.syncSessionState(source, target, readonly);
         } catch (SQLException var6) {
         }
      }
   }

   @Override
   void doClose() throws SQLException {
      if (this.masterConnection != null) {
         this.masterConnection.close();
      }

      if (this.slavesConnection != null) {
         this.slavesConnection.close();
      }

      if (this.connectionGroup != null) {
         this.connectionGroup.handleCloseConnection(this.thisAsReplicationConnection);
      }
   }

   @Override
   void doAbortInternal() throws SQLException {
      this.masterConnection.abortInternal();
      this.slavesConnection.abortInternal();
      if (this.connectionGroup != null) {
         this.connectionGroup.handleCloseConnection(this.thisAsReplicationConnection);
      }
   }

   @Override
   void doAbort(Executor executor) throws SQLException {
      this.masterConnection.abort(executor);
      this.slavesConnection.abort(executor);
      if (this.connectionGroup != null) {
         this.connectionGroup.handleCloseConnection(this.thisAsReplicationConnection);
      }
   }

   @Override
   Object invokeMore(Object proxy, Method method, Object[] args) throws Throwable {
      this.checkConnectionCapabilityForMethod(method);
      boolean invokeAgain = false;

      while(true) {
         try {
            Object result = method.invoke(this.thisAsConnection, args);
            if (result != null && result instanceof JdbcStatement) {
               ((JdbcStatement)result).setPingTarget(this);
            }

            return result;
         } catch (InvocationTargetException var8) {
            if (invokeAgain) {
               invokeAgain = false;
            } else if (var8.getCause() != null
               && var8.getCause() instanceof SQLException
               && ((SQLException)var8.getCause()).getSQLState() == "25000"
               && ((SQLException)var8.getCause()).getErrorCode() == 1000001) {
               try {
                  this.setReadOnly(this.readOnly);
                  invokeAgain = true;
               } catch (SQLException var7) {
               }
            }

            if (!invokeAgain) {
               throw var8;
            }
         }
      }
   }

   private void checkConnectionCapabilityForMethod(Method method) throws Throwable {
      if (this.masterHosts.isEmpty() && this.slaveHosts.isEmpty() && !ReplicationConnection.class.isAssignableFrom(method.getDeclaringClass())) {
         throw SQLError.createSQLException(Messages.getString("ReplicationConnectionProxy.noHostsInconsistentState"), "25000", 1000002, true, null);
      }
   }

   @Override
   public void doPing() throws SQLException {
      boolean isMasterConn = this.isMasterConnection();
      SQLException mastersPingException = null;
      SQLException slavesPingException = null;
      if (this.masterConnection != null) {
         try {
            this.masterConnection.ping();
         } catch (SQLException var6) {
            mastersPingException = var6;
         }
      } else {
         this.initializeMasterConnection();
      }

      if (this.slavesConnection != null) {
         try {
            this.slavesConnection.ping();
         } catch (SQLException var5) {
            slavesPingException = var5;
         }
      } else {
         try {
            this.initializeSlavesConnection();
            if (this.switchToSlavesConnectionIfNecessary()) {
               isMasterConn = false;
            }
         } catch (SQLException var7) {
            if (this.masterConnection == null || !this.readFromMasterWhenNoSlaves) {
               throw var7;
            }
         }
      }

      if (isMasterConn && mastersPingException != null) {
         if (this.slavesConnection != null && slavesPingException == null) {
            this.masterConnection = null;
            this.currentConnection = this.slavesConnection;
            this.readOnly = true;
         }

         throw mastersPingException;
      } else {
         if (!isMasterConn && (slavesPingException != null || this.slavesConnection == null)) {
            if (this.masterConnection != null && this.readFromMasterWhenNoSlaves && mastersPingException == null) {
               this.slavesConnection = null;
               this.currentConnection = this.masterConnection;
               this.readOnly = true;
               this.currentConnection.setReadOnly(true);
            }

            if (slavesPingException != null) {
               throw slavesPingException;
            }
         }
      }
   }

   private JdbcConnection initializeMasterConnection() throws SQLException {
      this.masterConnection = null;
      if (this.masterHosts.size() == 0) {
         return null;
      } else {
         LoadBalancedConnection newMasterConn = LoadBalancedConnectionProxy.createProxyInstance(
            new LoadbalanceConnectionUrl(this.masterHosts, this.connectionUrl.getOriginalProperties())
         );
         newMasterConn.setProxy(this.getProxy());
         this.masterConnection = newMasterConn;
         return this.masterConnection;
      }
   }

   private JdbcConnection initializeSlavesConnection() throws SQLException {
      this.slavesConnection = null;
      if (this.slaveHosts.size() == 0) {
         return null;
      } else {
         LoadBalancedConnection newSlavesConn = LoadBalancedConnectionProxy.createProxyInstance(
            new LoadbalanceConnectionUrl(this.slaveHosts, this.connectionUrl.getOriginalProperties())
         );
         newSlavesConn.setProxy(this.getProxy());
         newSlavesConn.setReadOnly(true);
         this.slavesConnection = newSlavesConn;
         return this.slavesConnection;
      }
   }

   private synchronized boolean switchToMasterConnection() throws SQLException {
      if (this.masterConnection == null || this.masterConnection.isClosed()) {
         try {
            if (this.initializeMasterConnection() == null) {
               return false;
            }
         } catch (SQLException var2) {
            this.currentConnection = null;
            throw var2;
         }
      }

      if (!this.isMasterConnection() && this.masterConnection != null) {
         this.syncSessionState(this.currentConnection, this.masterConnection, false);
         this.currentConnection = this.masterConnection;
      }

      return true;
   }

   private synchronized boolean switchToSlavesConnection() throws SQLException {
      if (this.slavesConnection == null || this.slavesConnection.isClosed()) {
         try {
            if (this.initializeSlavesConnection() == null) {
               return false;
            }
         } catch (SQLException var2) {
            this.currentConnection = null;
            throw var2;
         }
      }

      if (!this.isSlavesConnection() && this.slavesConnection != null) {
         this.syncSessionState(this.currentConnection, this.slavesConnection, true);
         this.currentConnection = this.slavesConnection;
      }

      return true;
   }

   private boolean switchToSlavesConnectionIfNecessary() throws SQLException {
      return this.currentConnection != null
            && (!this.isMasterConnection() || !this.readOnly && (!this.masterHosts.isEmpty() || !this.currentConnection.isClosed()))
            && (this.isMasterConnection() || !this.currentConnection.isClosed())
         ? false
         : this.switchToSlavesConnection();
   }

   public synchronized JdbcConnection getCurrentConnection() {
      return (JdbcConnection)(this.currentConnection == null ? LoadBalancedConnectionProxy.getNullLoadBalancedConnectionInstance() : this.currentConnection);
   }

   public long getConnectionGroupId() {
      return this.connectionGroupID;
   }

   public synchronized JdbcConnection getMasterConnection() {
      return this.masterConnection;
   }

   public synchronized void promoteSlaveToMaster(String hostPortPair) throws SQLException {
      HostInfo host = this.getSlaveHost(hostPortPair);
      if (host != null) {
         this.masterHosts.add(host);
         this.removeSlave(hostPortPair);
         if (this.masterConnection != null) {
            this.masterConnection.addHost(hostPortPair);
         }

         if (!this.readOnly && !this.isMasterConnection()) {
            this.switchToMasterConnection();
         }
      }
   }

   public synchronized void removeMasterHost(String hostPortPair) throws SQLException {
      this.removeMasterHost(hostPortPair, true);
   }

   public synchronized void removeMasterHost(String hostPortPair, boolean waitUntilNotInUse) throws SQLException {
      this.removeMasterHost(hostPortPair, waitUntilNotInUse, false);
   }

   public synchronized void removeMasterHost(String hostPortPair, boolean waitUntilNotInUse, boolean isNowSlave) throws SQLException {
      HostInfo host = this.getMasterHost(hostPortPair);
      if (host != null) {
         if (isNowSlave) {
            this.slaveHosts.add(host);
            this.resetReadFromMasterWhenNoSlaves();
         }

         this.masterHosts.remove(host);
         if (this.masterConnection != null && !this.masterConnection.isClosed()) {
            if (waitUntilNotInUse) {
               this.masterConnection.removeHostWhenNotInUse(hostPortPair);
            } else {
               this.masterConnection.removeHost(hostPortPair);
            }

            if (this.masterHosts.isEmpty()) {
               this.masterConnection.close();
               this.masterConnection = null;
               this.switchToSlavesConnectionIfNecessary();
            }
         } else {
            this.masterConnection = null;
         }
      }
   }

   public boolean isHostMaster(String hostPortPair) {
      return hostPortPair == null ? false : this.masterHosts.stream().anyMatch(hi -> hostPortPair.equalsIgnoreCase(hi.getHostPortPair()));
   }

   public synchronized JdbcConnection getSlavesConnection() {
      return this.slavesConnection;
   }

   public synchronized void addSlaveHost(String hostPortPair) throws SQLException {
      if (!this.isHostSlave(hostPortPair)) {
         this.slaveHosts.add(this.getConnectionUrl().getSlaveHostOrSpawnIsolated(hostPortPair));
         this.resetReadFromMasterWhenNoSlaves();
         if (this.slavesConnection == null) {
            this.initializeSlavesConnection();
            this.switchToSlavesConnectionIfNecessary();
         } else {
            this.slavesConnection.addHost(hostPortPair);
         }
      }
   }

   public synchronized void removeSlave(String hostPortPair) throws SQLException {
      this.removeSlave(hostPortPair, true);
   }

   public synchronized void removeSlave(String hostPortPair, boolean closeGently) throws SQLException {
      HostInfo host = this.getSlaveHost(hostPortPair);
      if (host != null) {
         this.slaveHosts.remove(host);
         this.resetReadFromMasterWhenNoSlaves();
         if (this.slavesConnection != null && !this.slavesConnection.isClosed()) {
            if (closeGently) {
               this.slavesConnection.removeHostWhenNotInUse(hostPortPair);
            } else {
               this.slavesConnection.removeHost(hostPortPair);
            }

            if (this.slaveHosts.isEmpty()) {
               this.slavesConnection.close();
               this.slavesConnection = null;
               this.switchToMasterConnection();
               if (this.isMasterConnection()) {
                  this.currentConnection.setReadOnly(this.readOnly);
               }
            }
         } else {
            this.slavesConnection = null;
         }
      }
   }

   public boolean isHostSlave(String hostPortPair) {
      return hostPortPair == null ? false : this.slaveHosts.stream().anyMatch(hi -> hostPortPair.equalsIgnoreCase(hi.getHostPortPair()));
   }

   public synchronized void setReadOnly(boolean readOnly) throws SQLException {
      if (readOnly) {
         if (!this.isSlavesConnection() || this.currentConnection.isClosed()) {
            boolean switched = true;
            SQLException exceptionCaught = null;

            try {
               switched = this.switchToSlavesConnection();
            } catch (SQLException var6) {
               switched = false;
               exceptionCaught = var6;
            }

            if (!switched && this.readFromMasterWhenNoSlaves && this.switchToMasterConnection()) {
               exceptionCaught = null;
            }

            if (exceptionCaught != null) {
               throw exceptionCaught;
            }
         }
      } else if (!this.isMasterConnection() || this.currentConnection.isClosed()) {
         boolean switched = true;
         SQLException exceptionCaught = null;

         try {
            switched = this.switchToMasterConnection();
         } catch (SQLException var5) {
            switched = false;
            exceptionCaught = var5;
         }

         if (!switched && this.switchToSlavesConnectionIfNecessary()) {
            exceptionCaught = null;
         }

         if (exceptionCaught != null) {
            throw exceptionCaught;
         }
      }

      this.readOnly = readOnly;
      if (this.readFromMasterWhenNoSlaves && this.isMasterConnection()) {
         this.currentConnection.setReadOnly(this.readOnly);
      }
   }

   public boolean isReadOnly() throws SQLException {
      return !this.isMasterConnection() || this.readOnly;
   }

   private void resetReadFromMasterWhenNoSlaves() {
      this.readFromMasterWhenNoSlaves = this.slaveHosts.isEmpty() || this.readFromMasterWhenNoSlavesOriginal;
   }

   private HostInfo getMasterHost(String hostPortPair) {
      return this.masterHosts.stream().filter(hi -> hostPortPair.equalsIgnoreCase(hi.getHostPortPair())).findFirst().orElse(null);
   }

   private HostInfo getSlaveHost(String hostPortPair) {
      return this.slaveHosts.stream().filter(hi -> hostPortPair.equalsIgnoreCase(hi.getHostPortPair())).findFirst().orElse(null);
   }

   private ReplicationConnectionUrl getConnectionUrl() {
      return (ReplicationConnectionUrl)this.connectionUrl;
   }
}
