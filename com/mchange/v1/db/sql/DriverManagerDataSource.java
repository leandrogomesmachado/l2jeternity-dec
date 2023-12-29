package com.mchange.v1.db.sql;

import com.mchange.io.UnsupportedVersionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;

/** @deprecated */
public class DriverManagerDataSource implements DataSource, Serializable, Referenceable {
   static final String REF_FACTORY_NAME = DriverManagerDataSource.DmdsObjectFactory.class.getName();
   static final String REF_JDBC_URL = "jdbcUrl";
   static final String REF_DFLT_USER = "dfltUser";
   static final String REF_DFLT_PWD = "dfltPassword";
   String jdbcUrl;
   String dfltUser;
   String dfltPassword;
   static final long serialVersionUID = 1L;
   private static final short VERSION = 1;

   public DriverManagerDataSource(String var1, String var2, String var3) {
      this.jdbcUrl = var1;
      this.dfltUser = var2;
      this.dfltPassword = var3;
   }

   public DriverManagerDataSource(String var1) {
      this(var1, null, null);
   }

   @Override
   public Connection getConnection() throws SQLException {
      return DriverManager.getConnection(this.jdbcUrl, this.createProps(null, null));
   }

   @Override
   public Connection getConnection(String var1, String var2) throws SQLException {
      return DriverManager.getConnection(this.jdbcUrl, this.createProps(var1, var2));
   }

   @Override
   public PrintWriter getLogWriter() throws SQLException {
      return DriverManager.getLogWriter();
   }

   @Override
   public void setLogWriter(PrintWriter var1) throws SQLException {
      DriverManager.setLogWriter(var1);
   }

   @Override
   public int getLoginTimeout() throws SQLException {
      return DriverManager.getLoginTimeout();
   }

   @Override
   public void setLoginTimeout(int var1) throws SQLException {
      DriverManager.setLoginTimeout(var1);
   }

   @Override
   public boolean isWrapperFor(Class<?> var1) throws SQLException {
      return false;
   }

   @Override
   public <T> T unwrap(Class<T> var1) throws SQLException {
      throw new SQLException(this.getClass().getName() + " is not a wrapper for an object implementing any interface.");
   }

   @Override
   public Reference getReference() throws NamingException {
      Reference var1 = new Reference(this.getClass().getName(), REF_FACTORY_NAME, null);
      var1.add(new StringRefAddr("jdbcUrl", this.jdbcUrl));
      var1.add(new StringRefAddr("dfltUser", this.dfltUser));
      var1.add(new StringRefAddr("dfltPassword", this.dfltPassword));
      return var1;
   }

   @Override
   public Logger getParentLogger() throws SQLFeatureNotSupportedException {
      throw new SQLFeatureNotSupportedException("javax.sql.DataSource.getParentLogger() is not currently supported by " + this.getClass().getName());
   }

   private Properties createProps(String var1, String var2) {
      Properties var3 = new Properties();
      if (var1 != null) {
         var3.put("user", var1);
         var3.put("password", var2);
      } else if (this.dfltUser != null) {
         var3.put("user", this.dfltUser);
         var3.put("password", this.dfltPassword);
      }

      return var3;
   }

   private void writeObject(ObjectOutputStream var1) throws IOException {
      var1.writeShort(1);
      var1.writeUTF(this.jdbcUrl);
      var1.writeUTF(this.dfltUser);
      var1.writeUTF(this.dfltPassword);
   }

   private void readObject(ObjectInputStream var1) throws IOException {
      short var2 = var1.readShort();
      switch(var2) {
         case 1:
            this.jdbcUrl = var1.readUTF();
            this.dfltUser = var1.readUTF();
            this.dfltPassword = var1.readUTF();
            return;
         default:
            throw new UnsupportedVersionException(this, var2);
      }
   }

   public static class DmdsObjectFactory implements ObjectFactory {
      @Override
      public Object getObjectInstance(Object var1, Name var2, Context var3, Hashtable var4) throws Exception {
         String var6 = DriverManagerDataSource.class.getName();
         Reference var5;
         return var1 instanceof Reference && (var5 = (Reference)var1).getClassName().equals(var6)
            ? new DriverManagerDataSource(
               (String)var5.get("jdbcUrl").getContent(), (String)var5.get("dfltUser").getContent(), (String)var5.get("dfltPassword").getContent()
            )
            : null;
      }
   }
}
