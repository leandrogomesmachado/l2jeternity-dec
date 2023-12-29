package com.mysql.cj.jdbc;

import java.sql.Connection;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;

public class CommentClientInfoProvider implements ClientInfoProvider {
   private Properties clientInfo;

   @Override
   public synchronized void initialize(Connection conn, Properties configurationProps) throws SQLException {
      this.clientInfo = new Properties();
   }

   @Override
   public synchronized void destroy() throws SQLException {
      this.clientInfo = null;
   }

   @Override
   public synchronized Properties getClientInfo(Connection conn) throws SQLException {
      return this.clientInfo;
   }

   @Override
   public synchronized String getClientInfo(Connection conn, String name) throws SQLException {
      return this.clientInfo.getProperty(name);
   }

   @Override
   public synchronized void setClientInfo(Connection conn, Properties properties) throws SQLClientInfoException {
      this.clientInfo = new Properties();
      Enumeration<?> propNames = properties.propertyNames();

      while(propNames.hasMoreElements()) {
         String name = (String)propNames.nextElement();
         this.clientInfo.put(name, properties.getProperty(name));
      }

      this.setComment(conn);
   }

   @Override
   public synchronized void setClientInfo(Connection conn, String name, String value) throws SQLClientInfoException {
      this.clientInfo.setProperty(name, value);
      this.setComment(conn);
   }

   private synchronized void setComment(Connection conn) {
      StringBuilder commentBuf = new StringBuilder();
      Iterator<Entry<Object, Object>> elements = this.clientInfo.entrySet().iterator();

      while(elements.hasNext()) {
         if (commentBuf.length() > 0) {
            commentBuf.append(", ");
         }

         Entry<?, ?> entry = elements.next();
         commentBuf.append("" + entry.getKey());
         commentBuf.append("=");
         commentBuf.append("" + entry.getValue());
      }

      ((JdbcConnection)conn).setStatementComment(commentBuf.toString());
   }
}
