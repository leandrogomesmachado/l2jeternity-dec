package org.apache.commons.dbcp;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

public class DriverConnectionFactory implements ConnectionFactory {
   protected Driver _driver = null;
   protected String _connectUri = null;
   protected Properties _props = null;

   public DriverConnectionFactory(Driver driver, String connectUri, Properties props) {
      this._driver = driver;
      this._connectUri = connectUri;
      this._props = props;
   }

   @Override
   public Connection createConnection() throws SQLException {
      return this._driver.connect(this._connectUri, this._props);
   }

   @Override
   public String toString() {
      return this.getClass().getName() + " [" + this._driver + ";" + this._connectUri + ";" + this._props + "]";
   }
}
