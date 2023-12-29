package org.apache.commons.dbcp;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedPoolableObjectFactory;

public class PoolingConnection extends DelegatingConnection implements Connection, KeyedPoolableObjectFactory {
   protected KeyedObjectPool _pstmtPool = null;
   private static final byte STATEMENT_PREPAREDSTMT = 0;
   private static final byte STATEMENT_CALLABLESTMT = 1;

   public PoolingConnection(Connection c) {
      super(c);
   }

   public PoolingConnection(Connection c, KeyedObjectPool pool) {
      super(c);
      this._pstmtPool = pool;
   }

   @Override
   public synchronized void close() throws SQLException {
      if (null != this._pstmtPool) {
         KeyedObjectPool oldpool = this._pstmtPool;
         this._pstmtPool = null;

         try {
            oldpool.close();
         } catch (RuntimeException var3) {
            throw var3;
         } catch (SQLException var4) {
            throw var4;
         } catch (Exception var5) {
            throw (SQLException)new SQLException("Cannot close connection").initCause(var5);
         }
      }

      this.getInnermostDelegate().close();
   }

   @Override
   public PreparedStatement prepareStatement(String sql) throws SQLException {
      if (null == this._pstmtPool) {
         throw new SQLException("Statement pool is null - closed or invalid PoolingConnection.");
      } else {
         try {
            return (PreparedStatement)this._pstmtPool.borrowObject(this.createKey(sql));
         } catch (NoSuchElementException var3) {
            throw (SQLException)new SQLException("MaxOpenPreparedStatements limit reached").initCause(var3);
         } catch (RuntimeException var4) {
            throw var4;
         } catch (Exception var5) {
            throw new SQLNestedException("Borrow prepareStatement from pool failed", var5);
         }
      }
   }

   @Override
   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      if (null == this._pstmtPool) {
         throw new SQLException("Statement pool is null - closed or invalid PoolingConnection.");
      } else {
         try {
            return (PreparedStatement)this._pstmtPool.borrowObject(this.createKey(sql, resultSetType, resultSetConcurrency));
         } catch (NoSuchElementException var5) {
            throw (SQLException)new SQLException("MaxOpenPreparedStatements limit reached").initCause(var5);
         } catch (RuntimeException var6) {
            throw var6;
         } catch (Exception var7) {
            throw (SQLException)new SQLException("Borrow prepareStatement from pool failed").initCause(var7);
         }
      }
   }

   @Override
   public CallableStatement prepareCall(String sql) throws SQLException {
      try {
         return (CallableStatement)this._pstmtPool.borrowObject(this.createKey(sql, (byte)1));
      } catch (NoSuchElementException var3) {
         throw new SQLNestedException("MaxOpenCallableStatements limit reached", var3);
      } catch (RuntimeException var4) {
         throw var4;
      } catch (Exception var5) {
         throw new SQLNestedException("Borrow callableStatement from pool failed", var5);
      }
   }

   @Override
   public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      try {
         return (CallableStatement)this._pstmtPool.borrowObject(this.createKey(sql, resultSetType, resultSetConcurrency, (byte)1));
      } catch (NoSuchElementException var5) {
         throw new SQLNestedException("MaxOpenCallableStatements limit reached", var5);
      } catch (RuntimeException var6) {
         throw var6;
      } catch (Exception var7) {
         throw new SQLNestedException("Borrow callableStatement from pool failed", var7);
      }
   }

   protected Object createKey(String sql, int resultSetType, int resultSetConcurrency) {
      String catalog = null;

      try {
         catalog = this.getCatalog();
      } catch (SQLException var6) {
      }

      return new PoolingConnection.PStmtKey(this.normalizeSQL(sql), catalog, resultSetType, resultSetConcurrency);
   }

   protected Object createKey(String sql, int resultSetType, int resultSetConcurrency, byte stmtType) {
      String catalog = null;

      try {
         catalog = this.getCatalog();
      } catch (SQLException var7) {
      }

      return new PoolingConnection.PStmtKey(this.normalizeSQL(sql), catalog, resultSetType, resultSetConcurrency, stmtType);
   }

   protected Object createKey(String sql) {
      String catalog = null;

      try {
         catalog = this.getCatalog();
      } catch (SQLException var4) {
      }

      return new PoolingConnection.PStmtKey(this.normalizeSQL(sql), catalog);
   }

   protected Object createKey(String sql, byte stmtType) {
      String catalog = null;

      try {
         catalog = this.getCatalog();
      } catch (SQLException var5) {
      }

      return new PoolingConnection.PStmtKey(this.normalizeSQL(sql), catalog, stmtType);
   }

   protected String normalizeSQL(String sql) {
      return sql.trim();
   }

   @Override
   public Object makeObject(Object obj) throws Exception {
      if (null != obj && obj instanceof PoolingConnection.PStmtKey) {
         PoolingConnection.PStmtKey key = (PoolingConnection.PStmtKey)obj;
         if (null == key._resultSetType && null == key._resultSetConcurrency) {
            return key._stmtType == 0
               ? new PoolablePreparedStatement(this.getDelegate().prepareStatement(key._sql), key, this._pstmtPool, this)
               : new PoolableCallableStatement(this.getDelegate().prepareCall(key._sql), key, this._pstmtPool, this);
         } else {
            return key._stmtType == 0
               ? new PoolablePreparedStatement(
                  this.getDelegate().prepareStatement(key._sql, key._resultSetType, key._resultSetConcurrency), key, this._pstmtPool, this
               )
               : new PoolableCallableStatement(
                  this.getDelegate().prepareCall(key._sql, key._resultSetType, key._resultSetConcurrency), key, this._pstmtPool, this
               );
         }
      } else {
         throw new IllegalArgumentException("Prepared statement key is null or invalid.");
      }
   }

   @Override
   public void destroyObject(Object key, Object obj) throws Exception {
      if (obj instanceof DelegatingPreparedStatement) {
         ((DelegatingPreparedStatement)obj).getInnermostDelegate().close();
      } else {
         ((PreparedStatement)obj).close();
      }
   }

   @Override
   public boolean validateObject(Object key, Object obj) {
      return true;
   }

   @Override
   public void activateObject(Object key, Object obj) throws Exception {
      ((DelegatingPreparedStatement)obj).activate();
   }

   @Override
   public void passivateObject(Object key, Object obj) throws Exception {
      ((PreparedStatement)obj).clearParameters();
      ((DelegatingPreparedStatement)obj).passivate();
   }

   @Override
   public String toString() {
      return this._pstmtPool != null ? "PoolingConnection: " + this._pstmtPool.toString() : "PoolingConnection: null";
   }

   static class PStmtKey {
      protected String _sql = null;
      protected Integer _resultSetType = null;
      protected Integer _resultSetConcurrency = null;
      protected String _catalog = null;
      protected byte _stmtType = 0;

      PStmtKey(String sql) {
         this._sql = sql;
      }

      PStmtKey(String sql, String catalog) {
         this._sql = sql;
         this._catalog = catalog;
      }

      PStmtKey(String sql, String catalog, byte stmtType) {
         this._sql = sql;
         this._catalog = catalog;
         this._stmtType = stmtType;
      }

      PStmtKey(String sql, int resultSetType, int resultSetConcurrency) {
         this._sql = sql;
         this._resultSetType = new Integer(resultSetType);
         this._resultSetConcurrency = new Integer(resultSetConcurrency);
      }

      PStmtKey(String sql, String catalog, int resultSetType, int resultSetConcurrency) {
         this._sql = sql;
         this._catalog = catalog;
         this._resultSetType = new Integer(resultSetType);
         this._resultSetConcurrency = new Integer(resultSetConcurrency);
      }

      PStmtKey(String sql, String catalog, int resultSetType, int resultSetConcurrency, byte stmtType) {
         this._sql = sql;
         this._catalog = catalog;
         this._resultSetType = new Integer(resultSetType);
         this._resultSetConcurrency = new Integer(resultSetConcurrency);
         this._stmtType = stmtType;
      }

      @Override
      public boolean equals(Object that) {
         try {
            PoolingConnection.PStmtKey key = (PoolingConnection.PStmtKey)that;
            return (null == this._sql && null == key._sql || this._sql.equals(key._sql))
               && (null == this._catalog && null == key._catalog || this._catalog.equals(key._catalog))
               && (null == this._resultSetType && null == key._resultSetType || this._resultSetType.equals(key._resultSetType))
               && (null == this._resultSetConcurrency && null == key._resultSetConcurrency || this._resultSetConcurrency.equals(key._resultSetConcurrency))
               && this._stmtType == key._stmtType;
         } catch (ClassCastException var3) {
            return false;
         } catch (NullPointerException var4) {
            return false;
         }
      }

      @Override
      public int hashCode() {
         if (this._catalog == null) {
            return null == this._sql ? 0 : this._sql.hashCode();
         } else {
            return null == this._sql ? this._catalog.hashCode() : (this._catalog + this._sql).hashCode();
         }
      }

      @Override
      public String toString() {
         StringBuffer buf = new StringBuffer();
         buf.append("PStmtKey: sql=");
         buf.append(this._sql);
         buf.append(", catalog=");
         buf.append(this._catalog);
         buf.append(", resultSetType=");
         buf.append(this._resultSetType);
         buf.append(", resultSetConcurrency=");
         buf.append(this._resultSetConcurrency);
         buf.append(", statmentType=");
         buf.append(this._stmtType);
         return buf.toString();
      }
   }
}
