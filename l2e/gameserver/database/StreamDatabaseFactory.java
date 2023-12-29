package l2e.gameserver.database;

import java.sql.Connection;
import java.sql.SQLException;
import l2e.commons.dbcp.BasicDataSource;
import l2e.gameserver.Config;

public class StreamDatabaseFactory extends BasicDataSource {
   public StreamDatabaseFactory() {
      super(
         Config.DATABASE_DRIVER,
         Config.STREAM_DB_URL,
         Config.STREAM_DB_LOGIN,
         Config.STREAM_DB_PASS,
         Config.DATABASE_MAX_CONNECTIONS,
         Config.DATABASE_MAX_CONNECTIONS,
         Config.DATABASE_MAX_IDLE_TIMEOUT,
         Config.DATABASE_IDLE_TEST_PERIOD,
         false
      );
   }

   public static Connection getStreamDatabaseConnection() throws SQLException {
      return Config.ALLOW_STREAM_SAME_DB ? DatabaseFactory.getInstance().getConnection() : StreamDatabaseFactory.SingletonHolder._instance.getConnection();
   }

   @Override
   public Connection getConnection() throws SQLException {
      return this.getConnection(null);
   }

   public static StreamDatabaseFactory getInstance() {
      return StreamDatabaseFactory.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      private static final StreamDatabaseFactory _instance = new StreamDatabaseFactory();
   }
}
