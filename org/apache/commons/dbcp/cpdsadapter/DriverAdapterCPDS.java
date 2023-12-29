package org.apache.commons.dbcp.cpdsadapter;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;

public class DriverAdapterCPDS implements ConnectionPoolDataSource, Referenceable, Serializable, ObjectFactory {
   private static final long serialVersionUID = -4820523787212147844L;
   private static final String GET_CONNECTION_CALLED = "A PooledConnection was already requested from this source, further initialization is not allowed.";
   private String description;
   private String password;
   private String url;
   private String user;
   private String driver;
   private int loginTimeout;
   private transient PrintWriter logWriter = null;
   private boolean poolPreparedStatements;
   private int maxActive = 10;
   private int maxIdle = 10;
   private int _timeBetweenEvictionRunsMillis = -1;
   private int _numTestsPerEvictionRun = -1;
   private int _minEvictableIdleTimeMillis = -1;
   private int _maxPreparedStatements = -1;
   private volatile boolean getConnectionCalled = false;
   private Properties connectionProperties = null;
   private boolean accessToUnderlyingConnectionAllowed = false;

   @Override
   public PooledConnection getPooledConnection() throws SQLException {
      return this.getPooledConnection(this.getUser(), this.getPassword());
   }

   @Override
   public PooledConnection getPooledConnection(String username, String pass) throws SQLException {
      this.getConnectionCalled = true;
      KeyedObjectPool stmtPool = null;
      if (this.isPoolPreparedStatements()) {
         if (this.getMaxPreparedStatements() <= 0) {
            stmtPool = new GenericKeyedObjectPool(
               null,
               this.getMaxActive(),
               (byte)2,
               0L,
               this.getMaxIdle(),
               false,
               false,
               (long)this.getTimeBetweenEvictionRunsMillis(),
               this.getNumTestsPerEvictionRun(),
               (long)this.getMinEvictableIdleTimeMillis(),
               false
            );
         } else {
            stmtPool = new GenericKeyedObjectPool(
               null, this.getMaxActive(), (byte)2, 0L, this.getMaxIdle(), this.getMaxPreparedStatements(), false, false, -1L, 0, 0L, false
            );
         }
      }

      try {
         PooledConnectionImpl pci = null;
         if (this.connectionProperties != null) {
            this.connectionProperties.put("user", username);
            this.connectionProperties.put("password", pass);
            pci = new PooledConnectionImpl(DriverManager.getConnection(this.getUrl(), this.connectionProperties), stmtPool);
         } else {
            pci = new PooledConnectionImpl(DriverManager.getConnection(this.getUrl(), username, pass), stmtPool);
         }

         pci.setAccessToUnderlyingConnectionAllowed(this.isAccessToUnderlyingConnectionAllowed());
         return pci;
      } catch (ClassCircularityError var6) {
         PooledConnectionImpl pci = null;
         if (this.connectionProperties != null) {
            pci = new PooledConnectionImpl(DriverManager.getConnection(this.getUrl(), this.connectionProperties), stmtPool);
         } else {
            pci = new PooledConnectionImpl(DriverManager.getConnection(this.getUrl(), username, pass), stmtPool);
         }

         pci.setAccessToUnderlyingConnectionAllowed(this.isAccessToUnderlyingConnectionAllowed());
         return pci;
      }
   }

   @Override
   public Reference getReference() throws NamingException {
      String factory = this.getClass().getName();
      Reference ref = new Reference(this.getClass().getName(), factory, null);
      ref.add(new StringRefAddr("description", this.getDescription()));
      ref.add(new StringRefAddr("driver", this.getDriver()));
      ref.add(new StringRefAddr("loginTimeout", String.valueOf(this.getLoginTimeout())));
      ref.add(new StringRefAddr("password", this.getPassword()));
      ref.add(new StringRefAddr("user", this.getUser()));
      ref.add(new StringRefAddr("url", this.getUrl()));
      ref.add(new StringRefAddr("poolPreparedStatements", String.valueOf(this.isPoolPreparedStatements())));
      ref.add(new StringRefAddr("maxActive", String.valueOf(this.getMaxActive())));
      ref.add(new StringRefAddr("maxIdle", String.valueOf(this.getMaxIdle())));
      ref.add(new StringRefAddr("timeBetweenEvictionRunsMillis", String.valueOf(this.getTimeBetweenEvictionRunsMillis())));
      ref.add(new StringRefAddr("numTestsPerEvictionRun", String.valueOf(this.getNumTestsPerEvictionRun())));
      ref.add(new StringRefAddr("minEvictableIdleTimeMillis", String.valueOf(this.getMinEvictableIdleTimeMillis())));
      ref.add(new StringRefAddr("maxPreparedStatements", String.valueOf(this.getMaxPreparedStatements())));
      return ref;
   }

   @Override
   public Object getObjectInstance(Object refObj, Name name, Context context, Hashtable env) throws Exception {
      DriverAdapterCPDS cpds = null;
      if (refObj instanceof Reference) {
         Reference ref = (Reference)refObj;
         if (ref.getClassName().equals(this.getClass().getName())) {
            RefAddr ra = ref.get("description");
            if (ra != null && ra.getContent() != null) {
               this.setDescription(ra.getContent().toString());
            }

            ra = ref.get("driver");
            if (ra != null && ra.getContent() != null) {
               this.setDriver(ra.getContent().toString());
            }

            ra = ref.get("url");
            if (ra != null && ra.getContent() != null) {
               this.setUrl(ra.getContent().toString());
            }

            ra = ref.get("user");
            if (ra != null && ra.getContent() != null) {
               this.setUser(ra.getContent().toString());
            }

            ra = ref.get("password");
            if (ra != null && ra.getContent() != null) {
               this.setPassword(ra.getContent().toString());
            }

            ra = ref.get("poolPreparedStatements");
            if (ra != null && ra.getContent() != null) {
               this.setPoolPreparedStatements(Boolean.valueOf(ra.getContent().toString()));
            }

            ra = ref.get("maxActive");
            if (ra != null && ra.getContent() != null) {
               this.setMaxActive(Integer.parseInt(ra.getContent().toString()));
            }

            ra = ref.get("maxIdle");
            if (ra != null && ra.getContent() != null) {
               this.setMaxIdle(Integer.parseInt(ra.getContent().toString()));
            }

            ra = ref.get("timeBetweenEvictionRunsMillis");
            if (ra != null && ra.getContent() != null) {
               this.setTimeBetweenEvictionRunsMillis(Integer.parseInt(ra.getContent().toString()));
            }

            ra = ref.get("numTestsPerEvictionRun");
            if (ra != null && ra.getContent() != null) {
               this.setNumTestsPerEvictionRun(Integer.parseInt(ra.getContent().toString()));
            }

            ra = ref.get("minEvictableIdleTimeMillis");
            if (ra != null && ra.getContent() != null) {
               this.setMinEvictableIdleTimeMillis(Integer.parseInt(ra.getContent().toString()));
            }

            ra = ref.get("maxPreparedStatements");
            if (ra != null && ra.getContent() != null) {
               this.setMaxPreparedStatements(Integer.parseInt(ra.getContent().toString()));
            }

            cpds = this;
         }
      }

      return cpds;
   }

   private void assertInitializationAllowed() throws IllegalStateException {
      if (this.getConnectionCalled) {
         throw new IllegalStateException("A PooledConnection was already requested from this source, further initialization is not allowed.");
      }
   }

   public Properties getConnectionProperties() {
      return this.connectionProperties;
   }

   public void setConnectionProperties(Properties props) {
      this.assertInitializationAllowed();
      this.connectionProperties = props;
      if (this.connectionProperties.containsKey("user")) {
         this.setUser(this.connectionProperties.getProperty("user"));
      }

      if (this.connectionProperties.containsKey("password")) {
         this.setPassword(this.connectionProperties.getProperty("password"));
      }
   }

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String v) {
      this.description = v;
   }

   public String getPassword() {
      return this.password;
   }

   public void setPassword(String v) {
      this.assertInitializationAllowed();
      this.password = v;
      if (this.connectionProperties != null) {
         this.connectionProperties.setProperty("password", v);
      }
   }

   public String getUrl() {
      return this.url;
   }

   public void setUrl(String v) {
      this.assertInitializationAllowed();
      this.url = v;
   }

   public String getUser() {
      return this.user;
   }

   public void setUser(String v) {
      this.assertInitializationAllowed();
      this.user = v;
      if (this.connectionProperties != null) {
         this.connectionProperties.setProperty("user", v);
      }
   }

   public String getDriver() {
      return this.driver;
   }

   public void setDriver(String v) throws ClassNotFoundException {
      this.assertInitializationAllowed();
      this.driver = v;
      Class.forName(v);
   }

   @Override
   public int getLoginTimeout() {
      return this.loginTimeout;
   }

   @Override
   public PrintWriter getLogWriter() {
      return this.logWriter;
   }

   @Override
   public void setLoginTimeout(int seconds) {
      this.loginTimeout = seconds;
   }

   @Override
   public void setLogWriter(PrintWriter out) {
      this.logWriter = out;
   }

   public boolean isPoolPreparedStatements() {
      return this.poolPreparedStatements;
   }

   public void setPoolPreparedStatements(boolean v) {
      this.assertInitializationAllowed();
      this.poolPreparedStatements = v;
   }

   public int getMaxActive() {
      return this.maxActive;
   }

   public void setMaxActive(int maxActive) {
      this.assertInitializationAllowed();
      this.maxActive = maxActive;
   }

   public int getMaxIdle() {
      return this.maxIdle;
   }

   public void setMaxIdle(int maxIdle) {
      this.assertInitializationAllowed();
      this.maxIdle = maxIdle;
   }

   public int getTimeBetweenEvictionRunsMillis() {
      return this._timeBetweenEvictionRunsMillis;
   }

   public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
      this.assertInitializationAllowed();
      this._timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
   }

   public int getNumTestsPerEvictionRun() {
      return this._numTestsPerEvictionRun;
   }

   public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
      this.assertInitializationAllowed();
      this._numTestsPerEvictionRun = numTestsPerEvictionRun;
   }

   public int getMinEvictableIdleTimeMillis() {
      return this._minEvictableIdleTimeMillis;
   }

   public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
      this.assertInitializationAllowed();
      this._minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
   }

   public synchronized boolean isAccessToUnderlyingConnectionAllowed() {
      return this.accessToUnderlyingConnectionAllowed;
   }

   public synchronized void setAccessToUnderlyingConnectionAllowed(boolean allow) {
      this.accessToUnderlyingConnectionAllowed = allow;
   }

   public int getMaxPreparedStatements() {
      return this._maxPreparedStatements;
   }

   public void setMaxPreparedStatements(int maxPreparedStatements) {
      this._maxPreparedStatements = maxPreparedStatements;
   }

   static {
      DriverManager.getDrivers();
   }
}
