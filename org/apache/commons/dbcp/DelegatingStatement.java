package org.apache.commons.dbcp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.List;

public class DelegatingStatement extends AbandonedTrace implements Statement {
   protected Statement _stmt = null;
   protected DelegatingConnection _conn = null;
   protected boolean _closed = false;

   public DelegatingStatement(DelegatingConnection c, Statement s) {
      super(c);
      this._stmt = s;
      this._conn = c;
   }

   public Statement getDelegate() {
      return this._stmt;
   }

   @Override
   public boolean equals(Object obj) {
      Statement delegate = this.getInnermostDelegate();
      if (delegate == null) {
         return false;
      } else if (obj instanceof DelegatingStatement) {
         DelegatingStatement s = (DelegatingStatement)obj;
         return delegate.equals(s.getInnermostDelegate());
      } else {
         return delegate.equals(obj);
      }
   }

   @Override
   public int hashCode() {
      Object obj = this.getInnermostDelegate();
      return obj == null ? 0 : obj.hashCode();
   }

   public Statement getInnermostDelegate() {
      Statement s = this._stmt;

      while(s != null && s instanceof DelegatingStatement) {
         s = ((DelegatingStatement)s).getDelegate();
         if (this == s) {
            return null;
         }
      }

      return s;
   }

   public void setDelegate(Statement s) {
      this._stmt = s;
   }

   protected void checkOpen() throws SQLException {
      if (this.isClosed()) {
         throw new SQLException(this.getClass().getName() + " with address: \"" + this.toString() + "\" is closed.");
      }
   }

   @Override
   public void close() throws SQLException {
      try {
         if (this._conn != null) {
            this._conn.removeTrace(this);
            this._conn = null;
         }

         List resultSets = this.getTrace();
         if (resultSets != null) {
            ResultSet[] set = resultSets.toArray(new ResultSet[resultSets.size()]);

            for(int i = 0; i < set.length; ++i) {
               set[i].close();
            }

            this.clearTrace();
         }

         this._stmt.close();
      } catch (SQLException var7) {
         this.handleException(var7);
      } finally {
         this._closed = true;
      }
   }

   protected void handleException(SQLException e) throws SQLException {
      if (this._conn != null) {
         this._conn.handleException(e);
      } else {
         throw e;
      }
   }

   protected void activate() throws SQLException {
      if (this._stmt instanceof DelegatingStatement) {
         ((DelegatingStatement)this._stmt).activate();
      }
   }

   protected void passivate() throws SQLException {
      if (this._stmt instanceof DelegatingStatement) {
         ((DelegatingStatement)this._stmt).passivate();
      }
   }

   @Override
   public Connection getConnection() throws SQLException {
      this.checkOpen();
      return this._conn;
   }

   @Override
   public ResultSet executeQuery(String sql) throws SQLException {
      this.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this, this._stmt.executeQuery(sql));
      } catch (SQLException var3) {
         this.handleException(var3);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getResultSet() throws SQLException {
      this.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this, this._stmt.getResultSet());
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public int executeUpdate(String sql) throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.executeUpdate(sql);
      } catch (SQLException var3) {
         this.handleException(var3);
         return 0;
      }
   }

   @Override
   public int getMaxFieldSize() throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.getMaxFieldSize();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public void setMaxFieldSize(int max) throws SQLException {
      this.checkOpen();

      try {
         this._stmt.setMaxFieldSize(max);
      } catch (SQLException var3) {
         this.handleException(var3);
      }
   }

   @Override
   public int getMaxRows() throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.getMaxRows();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public void setMaxRows(int max) throws SQLException {
      this.checkOpen();

      try {
         this._stmt.setMaxRows(max);
      } catch (SQLException var3) {
         this.handleException(var3);
      }
   }

   @Override
   public void setEscapeProcessing(boolean enable) throws SQLException {
      this.checkOpen();

      try {
         this._stmt.setEscapeProcessing(enable);
      } catch (SQLException var3) {
         this.handleException(var3);
      }
   }

   @Override
   public int getQueryTimeout() throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.getQueryTimeout();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public void setQueryTimeout(int seconds) throws SQLException {
      this.checkOpen();

      try {
         this._stmt.setQueryTimeout(seconds);
      } catch (SQLException var3) {
         this.handleException(var3);
      }
   }

   @Override
   public void cancel() throws SQLException {
      this.checkOpen();

      try {
         this._stmt.cancel();
      } catch (SQLException var2) {
         this.handleException(var2);
      }
   }

   @Override
   public SQLWarning getWarnings() throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.getWarnings();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public void clearWarnings() throws SQLException {
      this.checkOpen();

      try {
         this._stmt.clearWarnings();
      } catch (SQLException var2) {
         this.handleException(var2);
      }
   }

   @Override
   public void setCursorName(String name) throws SQLException {
      this.checkOpen();

      try {
         this._stmt.setCursorName(name);
      } catch (SQLException var3) {
         this.handleException(var3);
      }
   }

   @Override
   public boolean execute(String sql) throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.execute(sql);
      } catch (SQLException var3) {
         this.handleException(var3);
         return false;
      }
   }

   @Override
   public int getUpdateCount() throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.getUpdateCount();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public boolean getMoreResults() throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.getMoreResults();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public void setFetchDirection(int direction) throws SQLException {
      this.checkOpen();

      try {
         this._stmt.setFetchDirection(direction);
      } catch (SQLException var3) {
         this.handleException(var3);
      }
   }

   @Override
   public int getFetchDirection() throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.getFetchDirection();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public void setFetchSize(int rows) throws SQLException {
      this.checkOpen();

      try {
         this._stmt.setFetchSize(rows);
      } catch (SQLException var3) {
         this.handleException(var3);
      }
   }

   @Override
   public int getFetchSize() throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.getFetchSize();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getResultSetConcurrency() throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.getResultSetConcurrency();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getResultSetType() throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.getResultSetType();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public void addBatch(String sql) throws SQLException {
      this.checkOpen();

      try {
         this._stmt.addBatch(sql);
      } catch (SQLException var3) {
         this.handleException(var3);
      }
   }

   @Override
   public void clearBatch() throws SQLException {
      this.checkOpen();

      try {
         this._stmt.clearBatch();
      } catch (SQLException var2) {
         this.handleException(var2);
      }
   }

   @Override
   public int[] executeBatch() throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.executeBatch();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public String toString() {
      return this._stmt.toString();
   }

   @Override
   public boolean getMoreResults(int current) throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.getMoreResults(current);
      } catch (SQLException var3) {
         this.handleException(var3);
         return false;
      }
   }

   @Override
   public ResultSet getGeneratedKeys() throws SQLException {
      this.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this, this._stmt.getGeneratedKeys());
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.executeUpdate(sql, autoGeneratedKeys);
      } catch (SQLException var4) {
         this.handleException(var4);
         return 0;
      }
   }

   @Override
   public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.executeUpdate(sql, columnIndexes);
      } catch (SQLException var4) {
         this.handleException(var4);
         return 0;
      }
   }

   @Override
   public int executeUpdate(String sql, String[] columnNames) throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.executeUpdate(sql, columnNames);
      } catch (SQLException var4) {
         this.handleException(var4);
         return 0;
      }
   }

   @Override
   public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.execute(sql, autoGeneratedKeys);
      } catch (SQLException var4) {
         this.handleException(var4);
         return false;
      }
   }

   @Override
   public boolean execute(String sql, int[] columnIndexes) throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.execute(sql, columnIndexes);
      } catch (SQLException var4) {
         this.handleException(var4);
         return false;
      }
   }

   @Override
   public boolean execute(String sql, String[] columnNames) throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.execute(sql, columnNames);
      } catch (SQLException var4) {
         this.handleException(var4);
         return false;
      }
   }

   @Override
   public int getResultSetHoldability() throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.getResultSetHoldability();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public boolean isClosed() throws SQLException {
      return this._closed;
   }

   @Override
   public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return iface.isAssignableFrom(this.getClass()) || this._stmt.isWrapperFor(iface);
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      if (iface.isAssignableFrom(this.getClass())) {
         return iface.cast(this);
      } else {
         return (T)(iface.isAssignableFrom(this._stmt.getClass()) ? iface.cast(this._stmt) : this._stmt.unwrap(iface));
      }
   }

   @Override
   public void setPoolable(boolean poolable) throws SQLException {
      this.checkOpen();

      try {
         this._stmt.setPoolable(poolable);
      } catch (SQLException var3) {
         this.handleException(var3);
      }
   }

   @Override
   public boolean isPoolable() throws SQLException {
      this.checkOpen();

      try {
         return this._stmt.isPoolable();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }
}
