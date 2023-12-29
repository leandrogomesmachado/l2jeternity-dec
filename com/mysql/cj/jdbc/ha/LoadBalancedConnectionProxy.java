package com.mysql.cj.jdbc.ha;

import com.mysql.cj.Messages;
import com.mysql.cj.PingTarget;
import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.conf.url.LoadbalanceConnectionUrl;
import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.interceptors.QueryInterceptor;
import com.mysql.cj.jdbc.ConnectionGroup;
import com.mysql.cj.jdbc.ConnectionGroupManager;
import com.mysql.cj.jdbc.ConnectionImpl;
import com.mysql.cj.jdbc.JdbcConnection;
import com.mysql.cj.jdbc.JdbcStatement;
import com.mysql.cj.jdbc.exceptions.SQLError;
import com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping;
import com.mysql.cj.util.Util;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class LoadBalancedConnectionProxy extends MultiHostConnectionProxy implements PingTarget {
   private ConnectionGroup connectionGroup = null;
   private long connectionGroupProxyID = 0L;
   protected Map<String, ConnectionImpl> liveConnections;
   private Map<String, Integer> hostsToListIndexMap;
   private Map<ConnectionImpl, String> connectionsToHostsMap;
   private long totalPhysicalConnections = 0L;
   private long[] responseTimes;
   private int retriesAllDown;
   private BalanceStrategy balancer;
   private int globalBlacklistTimeout = 0;
   private static Map<String, Long> globalBlacklist = new HashMap<>();
   private int hostRemovalGracePeriod = 0;
   private Set<String> hostsToRemove = new HashSet<>();
   private boolean inTransaction = false;
   private long transactionStartTime = 0L;
   private long transactionCount = 0L;
   private LoadBalanceExceptionChecker exceptionChecker;
   private static Class<?>[] INTERFACES_TO_PROXY = new Class[]{LoadBalancedConnection.class, JdbcConnection.class};
   private static LoadBalancedConnection nullLBConnectionInstance = null;

   public static LoadBalancedConnection createProxyInstance(LoadbalanceConnectionUrl connectionUrl) throws SQLException {
      LoadBalancedConnectionProxy connProxy = new LoadBalancedConnectionProxy(connectionUrl);
      return (LoadBalancedConnection)Proxy.newProxyInstance(LoadBalancedConnection.class.getClassLoader(), INTERFACES_TO_PROXY, connProxy);
   }

   public LoadBalancedConnectionProxy(LoadbalanceConnectionUrl connectionUrl) throws SQLException {
      Properties props = connectionUrl.getConnectionArgumentsAsProperties();
      String group = props.getProperty("loadBalanceConnectionGroup", null);
      boolean enableJMX = false;
      String enableJMXAsString = props.getProperty("ha.enableJMX", "false");

      try {
         enableJMX = Boolean.parseBoolean(enableJMXAsString);
      } catch (Exception var22) {
         throw SQLError.createSQLException(Messages.getString("MultihostConnection.badValueForHaEnableJMX", new Object[]{enableJMXAsString}), "S1009", null);
      }

      List<HostInfo> hosts;
      if (group != null) {
         this.connectionGroup = ConnectionGroupManager.getConnectionGroupInstance(group);
         if (enableJMX) {
            ConnectionGroupManager.registerJmx();
         }

         this.connectionGroupProxyID = this.connectionGroup.registerConnectionProxy(this, connectionUrl.getHostInfoListAsHostPortPairs());
         hosts = connectionUrl.getHostInfoListFromHostPortPairs(this.connectionGroup.getInitialHosts());
      } else {
         hosts = connectionUrl.getHostsList();
      }

      int numHosts = this.initializeHostsSpecs(connectionUrl, hosts);
      this.liveConnections = new HashMap<>(numHosts);
      this.hostsToListIndexMap = new HashMap<>(numHosts);

      for(int i = 0; i < numHosts; ++i) {
         this.hostsToListIndexMap.put(this.hostsList.get(i).getHostPortPair(), i);
      }

      this.connectionsToHostsMap = new HashMap<>(numHosts);
      this.responseTimes = new long[numHosts];
      String retriesAllDownAsString = props.getProperty("retriesAllDown", "120");

      try {
         this.retriesAllDown = Integer.parseInt(retriesAllDownAsString);
      } catch (NumberFormatException var21) {
         throw SQLError.createSQLException(
            Messages.getString("LoadBalancedConnectionProxy.badValueForRetriesAllDown", new Object[]{retriesAllDownAsString}), "S1009", null
         );
      }

      String blacklistTimeoutAsString = props.getProperty("loadBalanceBlacklistTimeout", "0");

      try {
         this.globalBlacklistTimeout = Integer.parseInt(blacklistTimeoutAsString);
      } catch (NumberFormatException var20) {
         throw SQLError.createSQLException(
            Messages.getString("LoadBalancedConnectionProxy.badValueForLoadBalanceBlacklistTimeout", new Object[]{blacklistTimeoutAsString}), "S1009", null
         );
      }

      String hostRemovalGracePeriodAsString = props.getProperty("loadBalanceHostRemovalGracePeriod", "15000");

      try {
         this.hostRemovalGracePeriod = Integer.parseInt(hostRemovalGracePeriodAsString);
      } catch (NumberFormatException var19) {
         throw SQLError.createSQLException(
            Messages.getString("LoadBalancedConnectionProxy.badValueForLoadBalanceHostRemovalGracePeriod", new Object[]{hostRemovalGracePeriodAsString}),
            "S1009",
            null
         );
      }

      String strategy = props.getProperty("ha.loadBalanceStrategy", "random");

      try {
         switch(strategy) {
            case "random":
               this.balancer = new RandomBalanceStrategy();
               break;
            case "bestResponseTime":
               this.balancer = new BestResponseTimeBalanceStrategy();
               break;
            case "serverAffinity":
               this.balancer = new ServerAffinityStrategy(props.getProperty("serverAffinityOrder", null));
               break;
            default:
               this.balancer = (BalanceStrategy)Class.forName(strategy).newInstance();
         }
      } catch (Throwable var18) {
         throw SQLError.createSQLException(Messages.getString("InvalidLoadBalanceStrategy", new Object[]{strategy}), "S1009", var18, null);
      }

      String autoCommitSwapThresholdAsString = props.getProperty("loadBalanceAutoCommitStatementThreshold", "0");

      try {
         Integer.parseInt(autoCommitSwapThresholdAsString);
      } catch (NumberFormatException var17) {
         throw SQLError.createSQLException(
            Messages.getString("LoadBalancedConnectionProxy.badValueForLoadBalanceAutoCommitStatementThreshold", new Object[]{autoCommitSwapThresholdAsString}),
            "S1009",
            null
         );
      }

      String autoCommitSwapRegex = props.getProperty("loadBalanceAutoCommitStatementRegex", "");
      if (!"".equals(autoCommitSwapRegex)) {
         try {
            "".matches(autoCommitSwapRegex);
         } catch (Exception var16) {
            throw SQLError.createSQLException(
               Messages.getString("LoadBalancedConnectionProxy.badValueForLoadBalanceAutoCommitStatementRegex", new Object[]{autoCommitSwapRegex}),
               "S1009",
               null
            );
         }
      }

      try {
         String lbExceptionChecker = props.getProperty("loadBalanceExceptionChecker", StandardLoadBalanceExceptionChecker.class.getName());
         this.exceptionChecker = (LoadBalanceExceptionChecker)Util.getInstance(
            lbExceptionChecker, new Class[0], new Object[0], null, Messages.getString("InvalidLoadBalanceExceptionChecker")
         );
         this.exceptionChecker.init(props);
      } catch (CJException var15) {
         throw SQLExceptionsMapping.translateException(var15, null);
      }

      this.pickNewConnection();
   }

   @Override
   JdbcConnection getNewWrapperForThisAsConnection() throws SQLException {
      return new LoadBalancedMySQLConnection(this);
   }

   @Override
   protected void propagateProxyDown(JdbcConnection proxyConn) {
      for(JdbcConnection c : this.liveConnections.values()) {
         c.setProxy(proxyConn);
      }
   }

   @Deprecated
   public boolean shouldExceptionTriggerFailover(Throwable t) {
      return this.shouldExceptionTriggerConnectionSwitch(t);
   }

   @Override
   boolean shouldExceptionTriggerConnectionSwitch(Throwable t) {
      return t instanceof SQLException && this.exceptionChecker.shouldExceptionTriggerFailover(t);
   }

   @Override
   boolean isMasterConnection() {
      return true;
   }

   @Override
   synchronized void invalidateConnection(JdbcConnection conn) throws SQLException {
      super.invalidateConnection(conn);
      if (this.isGlobalBlacklistEnabled()) {
         this.addToGlobalBlacklist(this.connectionsToHostsMap.get(conn));
      }

      this.liveConnections.remove(this.connectionsToHostsMap.get(conn));
      Object mappedHost = this.connectionsToHostsMap.remove(conn);
      if (mappedHost != null && this.hostsToListIndexMap.containsKey(mappedHost)) {
         int hostIndex = this.hostsToListIndexMap.get(mappedHost);
         synchronized(this.responseTimes) {
            this.responseTimes[hostIndex] = 0L;
         }
      }
   }

   @Override
   public synchronized void pickNewConnection() throws SQLException {
      if (!this.isClosed || !this.closedExplicitly) {
         List<String> hostPortList = Collections.unmodifiableList(this.hostsList.stream().map(hi -> hi.getHostPortPair()).collect(Collectors.toList()));
         if (this.currentConnection == null) {
            this.currentConnection = this.balancer
               .pickConnection(this, hostPortList, Collections.unmodifiableMap(this.liveConnections), (long[])this.responseTimes.clone(), this.retriesAllDown);
         } else {
            if (this.currentConnection.isClosed()) {
               this.invalidateCurrentConnection();
            }

            int pingTimeout = this.currentConnection.getPropertySet().getIntegerProperty("loadBalancePingTimeout").getValue();
            boolean pingBeforeReturn = this.currentConnection.getPropertySet().getBooleanProperty("loadBalanceValidateConnectionOnSwapServer").getValue();
            int hostsTried = 0;

            for(int hostsToTry = this.hostsList.size(); hostsTried < hostsToTry; ++hostsTried) {
               ConnectionImpl newConn = null;

               try {
                  newConn = (ConnectionImpl)this.balancer
                     .pickConnection(
                        this, hostPortList, Collections.unmodifiableMap(this.liveConnections), (long[])this.responseTimes.clone(), this.retriesAllDown
                     );
                  if (this.currentConnection != null) {
                     if (pingBeforeReturn) {
                        newConn.pingInternal(true, pingTimeout);
                     }

                     this.syncSessionState(this.currentConnection, newConn);
                  }

                  this.currentConnection = newConn;
                  return;
               } catch (SQLException var8) {
                  if (this.shouldExceptionTriggerConnectionSwitch(var8) && newConn != null) {
                     this.invalidateConnection(newConn);
                  }
               }
            }

            this.isClosed = true;
            this.closedReason = "Connection closed after inability to pick valid new connection during load-balance.";
         }
      }
   }

   @Override
   public synchronized ConnectionImpl createConnectionForHost(HostInfo hostInfo) throws SQLException {
      ConnectionImpl conn = super.createConnectionForHost(hostInfo);
      this.liveConnections.put(hostInfo.getHostPortPair(), conn);
      this.connectionsToHostsMap.put(conn, hostInfo.getHostPortPair());
      ++this.totalPhysicalConnections;

      for(QueryInterceptor stmtInterceptor : conn.getQueryInterceptorsInstances()) {
         if (stmtInterceptor instanceof LoadBalancedAutoCommitInterceptor) {
            ((LoadBalancedAutoCommitInterceptor)stmtInterceptor).resumeCounters();
            break;
         }
      }

      return conn;
   }

   @Override
   void syncSessionState(JdbcConnection source, JdbcConnection target, boolean readOnly) throws SQLException {
      LoadBalancedAutoCommitInterceptor lbAutoCommitStmtInterceptor = null;

      for(QueryInterceptor stmtInterceptor : target.getQueryInterceptorsInstances()) {
         if (stmtInterceptor instanceof LoadBalancedAutoCommitInterceptor) {
            lbAutoCommitStmtInterceptor = (LoadBalancedAutoCommitInterceptor)stmtInterceptor;
            lbAutoCommitStmtInterceptor.pauseCounters();
            break;
         }
      }

      super.syncSessionState(source, target, readOnly);
      if (lbAutoCommitStmtInterceptor != null) {
         lbAutoCommitStmtInterceptor.resumeCounters();
      }
   }

   public synchronized ConnectionImpl createConnectionForHost(String hostPortPair) throws SQLException {
      for(HostInfo hi : this.hostsList) {
         if (hi.getHostPortPair().equals(hostPortPair)) {
            return this.createConnectionForHost(hi);
         }
      }

      return null;
   }

   private synchronized void closeAllConnections() {
      for(Connection c : this.liveConnections.values()) {
         try {
            c.close();
         } catch (SQLException var4) {
         }
      }

      if (!this.isClosed && this.connectionGroup != null) {
         this.connectionGroup.closeConnectionProxy(this);
      }

      this.liveConnections.clear();
      this.connectionsToHostsMap.clear();
   }

   @Override
   synchronized void doClose() {
      this.closeAllConnections();
   }

   @Override
   synchronized void doAbortInternal() {
      for(JdbcConnection c : this.liveConnections.values()) {
         try {
            c.abortInternal();
         } catch (SQLException var4) {
         }
      }

      if (!this.isClosed && this.connectionGroup != null) {
         this.connectionGroup.closeConnectionProxy(this);
      }

      this.liveConnections.clear();
      this.connectionsToHostsMap.clear();
   }

   @Override
   synchronized void doAbort(Executor executor) {
      for(Connection c : this.liveConnections.values()) {
         try {
            c.abort(executor);
         } catch (SQLException var5) {
         }
      }

      if (!this.isClosed && this.connectionGroup != null) {
         this.connectionGroup.closeConnectionProxy(this);
      }

      this.liveConnections.clear();
      this.connectionsToHostsMap.clear();
   }

   @Override
   public synchronized Object invokeMore(Object proxy, Method method, Object[] args) throws Throwable {
      String methodName = method.getName();
      if (this.isClosed && !this.allowedOnClosedConnection(method) && method.getExceptionTypes().length > 0) {
         if (!this.autoReconnect || this.closedExplicitly) {
            String reason = "No operations allowed after connection closed.";
            if (this.closedReason != null) {
               reason = reason + " " + this.closedReason;
            }

            for(Class<?> excls : method.getExceptionTypes()) {
               if (SQLException.class.isAssignableFrom(excls)) {
                  throw SQLError.createSQLException(reason, "08003", null);
               }
            }

            throw (CJCommunicationsException)ExceptionFactory.createException(CJCommunicationsException.class, reason);
         }

         this.currentConnection = null;
         this.pickNewConnection();
         this.isClosed = false;
         this.closedReason = null;
      }

      if (!this.inTransaction) {
         this.inTransaction = true;
         this.transactionStartTime = System.nanoTime();
         ++this.transactionCount;
      }

      Object result = null;

      try {
         result = method.invoke(this.thisAsConnection, args);
         if (result != null) {
            if (result instanceof JdbcStatement) {
               ((JdbcStatement)result).setPingTarget(this);
            }

            result = this.proxyIfReturnTypeIsJdbcInterface(method.getReturnType(), result);
         }
      } catch (InvocationTargetException var23) {
         this.dealWithInvocationException(var23);
      } finally {
         if ("commit".equals(methodName) || "rollback".equals(methodName)) {
            this.inTransaction = false;
            String host = this.connectionsToHostsMap.get(this.currentConnection);
            if (host != null) {
               synchronized(this.responseTimes) {
                  Integer hostIndex = this.hostsToListIndexMap.get(host);
                  if (hostIndex != null && hostIndex < this.responseTimes.length) {
                     this.responseTimes[hostIndex] = System.nanoTime() - this.transactionStartTime;
                  }
               }
            }

            this.pickNewConnection();
         }
      }

      return result;
   }

   @Override
   public synchronized void doPing() throws SQLException {
      SQLException se = null;
      boolean foundHost = false;
      int pingTimeout = this.currentConnection.getPropertySet().getIntegerProperty("loadBalancePingTimeout").getValue();
      synchronized(this) {
         for(HostInfo hi : this.hostsList) {
            String host = hi.getHostPortPair();
            ConnectionImpl conn = this.liveConnections.get(host);
            if (conn != null) {
               try {
                  if (pingTimeout == 0) {
                     conn.ping();
                  } else {
                     conn.pingInternal(true, pingTimeout);
                  }

                  foundHost = true;
               } catch (SQLException var11) {
                  if (host.equals(this.connectionsToHostsMap.get(this.currentConnection))) {
                     this.closeAllConnections();
                     this.isClosed = true;
                     this.closedReason = "Connection closed because ping of current connection failed.";
                     throw var11;
                  }

                  if (var11.getMessage().equals(Messages.getString("Connection.exceededConnectionLifetime"))) {
                     if (se == null) {
                        se = var11;
                     }
                  } else {
                     se = var11;
                     if (this.isGlobalBlacklistEnabled()) {
                        this.addToGlobalBlacklist(host);
                     }
                  }

                  this.liveConnections.remove(this.connectionsToHostsMap.get(conn));
               }
            }
         }
      }

      if (!foundHost) {
         this.closeAllConnections();
         this.isClosed = true;
         this.closedReason = "Connection closed due to inability to ping any active connections.";
         if (se != null) {
            throw se;
         }

         ((ConnectionImpl)this.currentConnection).throwConnectionClosedException();
      }
   }

   public void addToGlobalBlacklist(String host, long timeout) {
      if (this.isGlobalBlacklistEnabled()) {
         synchronized(globalBlacklist) {
            globalBlacklist.put(host, timeout);
         }
      }
   }

   public void addToGlobalBlacklist(String host) {
      this.addToGlobalBlacklist(host, System.currentTimeMillis() + (long)this.globalBlacklistTimeout);
   }

   public boolean isGlobalBlacklistEnabled() {
      return this.globalBlacklistTimeout > 0;
   }

   public synchronized Map<String, Long> getGlobalBlacklist() {
      if (this.isGlobalBlacklistEnabled()) {
         Map<String, Long> blacklistClone = new HashMap<>(globalBlacklist.size());
         synchronized(globalBlacklist) {
            blacklistClone.putAll(globalBlacklist);
         }

         Set<String> keys = blacklistClone.keySet();
         keys.retainAll(this.hostsList.stream().map(hi -> hi.getHost()).collect(Collectors.toList()));
         Iterator<String> i = keys.iterator();

         while(i.hasNext()) {
            String host = i.next();
            Long timeout = globalBlacklist.get(host);
            if (timeout != null && timeout < System.currentTimeMillis()) {
               synchronized(globalBlacklist) {
                  globalBlacklist.remove(host);
               }

               i.remove();
            }
         }

         return (Map<String, Long>)(keys.size() == this.hostsList.size() ? new HashMap<>(1) : blacklistClone);
      } else if (this.hostsToRemove.isEmpty()) {
         return new HashMap<>(1);
      } else {
         HashMap<String, Long> fakedBlacklist = new HashMap<>();

         for(String h : this.hostsToRemove) {
            fakedBlacklist.put(h, System.currentTimeMillis() + 5000L);
         }

         return fakedBlacklist;
      }
   }

   public void removeHostWhenNotInUse(String hostPortPair) throws SQLException {
      if (this.hostRemovalGracePeriod <= 0) {
         this.removeHost(hostPortPair);
      } else {
         int timeBetweenChecks = this.hostRemovalGracePeriod > 1000 ? 1000 : this.hostRemovalGracePeriod;
         synchronized(this) {
            this.addToGlobalBlacklist(hostPortPair, System.currentTimeMillis() + (long)this.hostRemovalGracePeriod + (long)timeBetweenChecks);
            long cur = System.currentTimeMillis();

            while(System.currentTimeMillis() < cur + (long)this.hostRemovalGracePeriod) {
               this.hostsToRemove.add(hostPortPair);
               if (!hostPortPair.equals(this.currentConnection.getHostPortPair())) {
                  this.removeHost(hostPortPair);
                  return;
               }

               try {
                  Thread.sleep((long)timeBetweenChecks);
               } catch (InterruptedException var8) {
               }
            }
         }

         this.removeHost(hostPortPair);
      }
   }

   public synchronized void removeHost(String hostPortPair) throws SQLException {
      if (this.connectionGroup != null && this.connectionGroup.getInitialHosts().size() == 1 && this.connectionGroup.getInitialHosts().contains(hostPortPair)) {
         throw SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.0"), null);
      } else {
         this.hostsToRemove.add(hostPortPair);
         this.connectionsToHostsMap.remove(this.liveConnections.remove(hostPortPair));
         if (this.hostsToListIndexMap.remove(hostPortPair) != null) {
            long[] newResponseTimes = new long[this.responseTimes.length - 1];
            int newIdx = 0;

            for(HostInfo hostInfo : this.hostsList) {
               String host = hostInfo.getHostPortPair();
               if (!this.hostsToRemove.contains(host)) {
                  Integer idx = this.hostsToListIndexMap.get(host);
                  if (idx != null && idx < this.responseTimes.length) {
                     newResponseTimes[newIdx] = this.responseTimes[idx];
                  }

                  this.hostsToListIndexMap.put(host, newIdx++);
               }
            }

            this.responseTimes = newResponseTimes;
         }

         if (hostPortPair.equals(this.currentConnection.getHostPortPair())) {
            this.invalidateConnection(this.currentConnection);
            this.pickNewConnection();
         }
      }
   }

   public synchronized boolean addHost(String hostPortPair) {
      if (this.hostsToListIndexMap.containsKey(hostPortPair)) {
         return false;
      } else {
         long[] newResponseTimes = new long[this.responseTimes.length + 1];
         System.arraycopy(this.responseTimes, 0, newResponseTimes, 0, this.responseTimes.length);
         this.responseTimes = newResponseTimes;
         if (this.hostsList.stream().noneMatch(hi -> hostPortPair.equals(hi.getHostPortPair()))) {
            this.hostsList.add(this.connectionUrl.getHostOrSpawnIsolated(hostPortPair));
         }

         this.hostsToListIndexMap.put(hostPortPair, this.responseTimes.length - 1);
         this.hostsToRemove.remove(hostPortPair);
         return true;
      }
   }

   public synchronized boolean inTransaction() {
      return this.inTransaction;
   }

   public synchronized long getTransactionCount() {
      return this.transactionCount;
   }

   public synchronized long getActivePhysicalConnectionCount() {
      return (long)this.liveConnections.size();
   }

   public synchronized long getTotalPhysicalConnectionCount() {
      return this.totalPhysicalConnections;
   }

   public synchronized long getConnectionGroupProxyID() {
      return this.connectionGroupProxyID;
   }

   public synchronized String getCurrentActiveHost() {
      JdbcConnection c = this.currentConnection;
      if (c != null) {
         Object o = this.connectionsToHostsMap.get(c);
         if (o != null) {
            return o.toString();
         }
      }

      return null;
   }

   public synchronized long getCurrentTransactionDuration() {
      return this.inTransaction && this.transactionStartTime > 0L ? System.nanoTime() - this.transactionStartTime : 0L;
   }

   static synchronized LoadBalancedConnection getNullLoadBalancedConnectionInstance() {
      if (nullLBConnectionInstance == null) {
         nullLBConnectionInstance = (LoadBalancedConnection)Proxy.newProxyInstance(
            LoadBalancedConnection.class.getClassLoader(), INTERFACES_TO_PROXY, new LoadBalancedConnectionProxy.NullLoadBalancedConnectionProxy()
         );
      }

      return nullLBConnectionInstance;
   }

   private static class NullLoadBalancedConnectionProxy implements InvocationHandler {
      public NullLoadBalancedConnectionProxy() {
      }

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
         SQLException exceptionToThrow = SQLError.createSQLException(
            Messages.getString("LoadBalancedConnectionProxy.unusableConnection"), "25000", 1000001, true, null
         );
         Class<?>[] declaredException = method.getExceptionTypes();

         for(Class<?> declEx : declaredException) {
            if (declEx.isAssignableFrom(exceptionToThrow.getClass())) {
               throw exceptionToThrow;
            }
         }

         throw new IllegalStateException(exceptionToThrow.getMessage(), exceptionToThrow);
      }
   }
}
