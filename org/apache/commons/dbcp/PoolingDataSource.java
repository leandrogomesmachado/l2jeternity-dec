package org.apache.commons.dbcp;

import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.sql.DataSource;
import org.apache.commons.pool.ObjectPool;

public class PoolingDataSource implements DataSource {
   private boolean accessToUnderlyingConnectionAllowed = false;
   protected PrintWriter _logWriter = null;
   protected ObjectPool _pool = null;

   public PoolingDataSource() {
      this(null);
   }

   public PoolingDataSource(ObjectPool pool) {
      this._pool = pool;
   }

   public void setPool(ObjectPool pool) throws IllegalStateException, NullPointerException {
      if (null != this._pool) {
         throw new IllegalStateException("Pool already set");
      } else if (null == pool) {
         throw new NullPointerException("Pool must not be null.");
      } else {
         this._pool = pool;
      }
   }

   public boolean isAccessToUnderlyingConnectionAllowed() {
      return this.accessToUnderlyingConnectionAllowed;
   }

   public void setAccessToUnderlyingConnectionAllowed(boolean allow) {
      this.accessToUnderlyingConnectionAllowed = allow;
   }

   @Override
   public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return false;
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      throw new SQLException("PoolingDataSource is not a wrapper.");
   }

   @Override
   public Connection getConnection() throws SQLException {
      try {
         Connection conn = (Connection)this._pool.borrowObject();
         if (conn != null) {
            conn = new PoolingDataSource.PoolGuardConnectionWrapper(conn);
         }

         return conn;
      } catch (SQLException var2) {
         throw var2;
      } catch (NoSuchElementException var3) {
         throw new SQLNestedException("Cannot get a connection, pool error " + var3.getMessage(), var3);
      } catch (RuntimeException var4) {
         throw var4;
      } catch (Exception var5) {
         throw new SQLNestedException("Cannot get a connection, general error", var5);
      }
   }

   @Override
   public Connection getConnection(String uname, String passwd) throws SQLException {
      throw new UnsupportedOperationException();
   }

   @Override
   public PrintWriter getLogWriter() {
      return this._logWriter;
   }

   @Override
   public int getLoginTimeout() {
      throw new UnsupportedOperationException("Login timeout is not supported.");
   }

   @Override
   public void setLoginTimeout(int seconds) {
      throw new UnsupportedOperationException("Login timeout is not supported.");
   }

   @Override
   public void setLogWriter(PrintWriter out) {
      this._logWriter = out;
   }

   private class PoolGuardConnectionWrapper extends DelegatingConnection {
      private Connection delegate;

      PoolGuardConnectionWrapper(Connection delegate) {
         super(delegate);
         this.delegate = delegate;
      }

      @Override
      protected void checkOpen() throws SQLException {
         if (this.delegate == null) {
            throw new SQLException("Connection is closed.");
         }
      }

      @Override
      public void close() throws SQLException {
         if (this.delegate != null) {
            this.delegate.close();
            this.delegate = null;
            super.setDelegate(null);
         }
      }

      @Override
      public boolean isClosed() throws SQLException {
         return this.delegate == null ? true : this.delegate.isClosed();
      }

      @Override
      public void clearWarnings() throws SQLException {
         this.checkOpen();
         this.delegate.clearWarnings();
      }

      @Override
      public void commit() throws SQLException {
         this.checkOpen();
         this.delegate.commit();
      }

      @Override
      public Statement createStatement() throws SQLException {
         this.checkOpen();
         return new DelegatingStatement(this, this.delegate.createStatement());
      }

      @Override
      public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
         this.checkOpen();
         return new DelegatingStatement(this, this.delegate.createStatement(resultSetType, resultSetConcurrency));
      }

      @Override
      public boolean innermostDelegateEquals(Connection c) {
         Connection innerCon = super.getInnermostDelegate();
         if (innerCon == null) {
            return c == null;
         } else {
            return innerCon.equals(c);
         }
      }

      @Override
      public boolean getAutoCommit() throws SQLException {
         this.checkOpen();
         return this.delegate.getAutoCommit();
      }

      @Override
      public String getCatalog() throws SQLException {
         this.checkOpen();
         return this.delegate.getCatalog();
      }

      @Override
      public DatabaseMetaData getMetaData() throws SQLException {
         this.checkOpen();
         return this.delegate.getMetaData();
      }

      @Override
      public int getTransactionIsolation() throws SQLException {
         this.checkOpen();
         return this.delegate.getTransactionIsolation();
      }

      @Override
      public Map getTypeMap() throws SQLException {
         this.checkOpen();
         return this.delegate.getTypeMap();
      }

      @Override
      public SQLWarning getWarnings() throws SQLException {
         this.checkOpen();
         return this.delegate.getWarnings();
      }

      @Override
      public int hashCode() {
         return this.delegate == null ? 0 : this.delegate.hashCode();
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == null) {
            return false;
         } else if (obj == this) {
            return true;
         } else {
            Connection conn = super.getInnermostDelegate();
            if (conn == null) {
               return false;
            } else if (obj instanceof DelegatingConnection) {
               DelegatingConnection c = (DelegatingConnection)obj;
               return c.innermostDelegateEquals(conn);
            } else {
               return conn.equals(obj);
            }
         }
      }

      @Override
      public boolean isReadOnly() throws SQLException {
         this.checkOpen();
         return this.delegate.isReadOnly();
      }

      @Override
      public String nativeSQL(String sql) throws SQLException {
         this.checkOpen();
         return this.delegate.nativeSQL(sql);
      }

      @Override
      public CallableStatement prepareCall(String sql) throws SQLException {
         this.checkOpen();
         return new DelegatingCallableStatement(this, this.delegate.prepareCall(sql));
      }

      @Override
      public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
         this.checkOpen();
         return new DelegatingCallableStatement(this, this.delegate.prepareCall(sql, resultSetType, resultSetConcurrency));
      }

      @Override
      public PreparedStatement prepareStatement(String sql) throws SQLException {
         this.checkOpen();
         return new DelegatingPreparedStatement(this, this.delegate.prepareStatement(sql));
      }

      @Override
      public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
         this.checkOpen();
         return new DelegatingPreparedStatement(this, this.delegate.prepareStatement(sql, resultSetType, resultSetConcurrency));
      }

      @Override
      public void rollback() throws SQLException {
         this.checkOpen();
         this.delegate.rollback();
      }

      @Override
      public void setAutoCommit(boolean autoCommit) throws SQLException {
         this.checkOpen();
         this.delegate.setAutoCommit(autoCommit);
      }

      @Override
      public void setCatalog(String catalog) throws SQLException {
         this.checkOpen();
         this.delegate.setCatalog(catalog);
      }

      @Override
      public void setReadOnly(boolean readOnly) throws SQLException {
         this.checkOpen();
         this.delegate.setReadOnly(readOnly);
      }

      @Override
      public void setTransactionIsolation(int level) throws SQLException {
         this.checkOpen();
         this.delegate.setTransactionIsolation(level);
      }

      @Override
      public void setTypeMap(Map map) throws SQLException {
         this.checkOpen();
         this.delegate.setTypeMap(map);
      }

      @Override
      public String toString() {
         return this.delegate == null ? "NULL" : this.delegate.toString();
      }

      @Override
      public int getHoldability() throws SQLException {
         this.checkOpen();
         return this.delegate.getHoldability();
      }

      @Override
      public void setHoldability(int holdability) throws SQLException {
         this.checkOpen();
         this.delegate.setHoldability(holdability);
      }

      @Override
      public Savepoint setSavepoint() throws SQLException {
         this.checkOpen();
         return this.delegate.setSavepoint();
      }

      @Override
      public Savepoint setSavepoint(String name) throws SQLException {
         this.checkOpen();
         return this.delegate.setSavepoint(name);
      }

      @Override
      public void releaseSavepoint(Savepoint savepoint) throws SQLException {
         this.checkOpen();
         this.delegate.releaseSavepoint(savepoint);
      }

      @Override
      public void rollback(Savepoint savepoint) throws SQLException {
         this.checkOpen();
         this.delegate.rollback(savepoint);
      }

      @Override
      public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
         this.checkOpen();
         return new DelegatingStatement(this, this.delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
      }

      @Override
      public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
         this.checkOpen();
         return new DelegatingCallableStatement(this, this.delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
      }

      @Override
      public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
         this.checkOpen();
         return new DelegatingPreparedStatement(this, this.delegate.prepareStatement(sql, autoGeneratedKeys));
      }

      @Override
      public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
         this.checkOpen();
         return new DelegatingPreparedStatement(this, this.delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
      }

      @Override
      public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
         this.checkOpen();
         return new DelegatingPreparedStatement(this, this.delegate.prepareStatement(sql, columnIndexes));
      }

      @Override
      public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
         this.checkOpen();
         return new DelegatingPreparedStatement(this, this.delegate.prepareStatement(sql, columnNames));
      }

      @Override
      public Connection getDelegate() {
         return PoolingDataSource.this.isAccessToUnderlyingConnectionAllowed() ? super.getDelegate() : null;
      }

      @Override
      public Connection getInnermostDelegate() {
         return PoolingDataSource.this.isAccessToUnderlyingConnectionAllowed() ? super.getInnermostDelegate() : null;
      }
   }
}
