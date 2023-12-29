package com.mysql.cj.jdbc.ha;

import com.mysql.cj.MysqlConnection;
import com.mysql.cj.Query;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.interceptors.QueryInterceptor;
import com.mysql.cj.jdbc.JdbcConnection;
import com.mysql.cj.log.Log;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.ServerSession;
import com.mysql.cj.util.StringUtils;
import java.sql.SQLException;
import java.util.Properties;
import java.util.function.Supplier;

public class LoadBalancedAutoCommitInterceptor implements QueryInterceptor {
   private int matchingAfterStatementCount = 0;
   private int matchingAfterStatementThreshold = 0;
   private String matchingAfterStatementRegex;
   private JdbcConnection conn;
   private LoadBalancedConnectionProxy proxy = null;
   private boolean countStatements = false;

   @Override
   public void destroy() {
      this.conn = null;
      this.proxy = null;
   }

   @Override
   public boolean executeTopLevelOnly() {
      return false;
   }

   @Override
   public QueryInterceptor init(MysqlConnection connection, Properties props, Log log) {
      this.conn = (JdbcConnection)connection;
      String autoCommitSwapThresholdAsString = props.getProperty("loadBalanceAutoCommitStatementThreshold", "0");

      try {
         this.matchingAfterStatementThreshold = Integer.parseInt(autoCommitSwapThresholdAsString);
      } catch (NumberFormatException var6) {
      }

      String autoCommitSwapRegex = props.getProperty("loadBalanceAutoCommitStatementRegex", "");
      if (!"".equals(autoCommitSwapRegex)) {
         this.matchingAfterStatementRegex = autoCommitSwapRegex;
      }

      return this;
   }

   @Override
   public <T extends Resultset> T postProcess(Supplier<String> sql, Query interceptedQuery, T originalResultSet, ServerSession serverSession) {
      try {
         if (!this.countStatements || StringUtils.startsWithIgnoreCase(sql.get(), "SET") || StringUtils.startsWithIgnoreCase(sql.get(), "SHOW")) {
            return originalResultSet;
         } else if (!this.conn.getAutoCommit()) {
            this.matchingAfterStatementCount = 0;
            return originalResultSet;
         } else {
            if (this.proxy == null && this.conn.isProxySet()) {
               JdbcConnection lcl_proxy = this.conn.getMultiHostSafeProxy();

               while(lcl_proxy != null && !(lcl_proxy instanceof LoadBalancedMySQLConnection)) {
                  lcl_proxy = lcl_proxy.getMultiHostSafeProxy();
               }

               if (lcl_proxy != null) {
                  this.proxy = ((LoadBalancedMySQLConnection)lcl_proxy).getThisAsProxy();
               }
            }

            if (this.proxy == null) {
               return originalResultSet;
            } else {
               if (this.matchingAfterStatementRegex == null || sql.get().matches(this.matchingAfterStatementRegex)) {
                  ++this.matchingAfterStatementCount;
               }

               if (this.matchingAfterStatementCount >= this.matchingAfterStatementThreshold) {
                  this.matchingAfterStatementCount = 0;

                  try {
                     this.proxy.pickNewConnection();
                  } catch (SQLException var6) {
                  }
               }

               return originalResultSet;
            }
         }
      } catch (SQLException var7) {
         throw ExceptionFactory.createException(var7.getMessage(), var7);
      }
   }

   @Override
   public <T extends Resultset> T preProcess(Supplier<String> sql, Query interceptedQuery) {
      return null;
   }

   void pauseCounters() {
      this.countStatements = false;
   }

   void resumeCounters() {
      this.countStatements = true;
   }
}
