package org.apache.commons.dbcp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DriverManagerConnectionFactory implements ConnectionFactory {
   protected String _connectUri = null;
   protected String _uname = null;
   protected String _passwd = null;
   protected Properties _props = null;

   public DriverManagerConnectionFactory(String connectUri, Properties props) {
      this._connectUri = connectUri;
      this._props = props;
   }

   public DriverManagerConnectionFactory(String connectUri, String uname, String passwd) {
      this._connectUri = connectUri;
      this._uname = uname;
      this._passwd = passwd;
   }

   @Override
   public Connection createConnection() throws SQLException {
      if (null == this._props) {
         return this._uname == null && this._passwd == null
            ? DriverManager.getConnection(this._connectUri)
            : DriverManager.getConnection(this._connectUri, this._uname, this._passwd);
      } else {
         return DriverManager.getConnection(this._connectUri, this._props);
      }
   }

   static {
      DriverManager.getDrivers();
   }
}
