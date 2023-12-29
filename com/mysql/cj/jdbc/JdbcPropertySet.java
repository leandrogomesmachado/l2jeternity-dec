package com.mysql.cj.jdbc;

import com.mysql.cj.conf.PropertySet;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

public interface JdbcPropertySet extends PropertySet {
   DriverPropertyInfo[] exposeAsDriverPropertyInfo(Properties var1, int var2) throws SQLException;
}
