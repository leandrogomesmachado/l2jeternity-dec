package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;

public final class WorldEventManager {
   private static final Logger _log = Logger.getLogger(WorldEventManager.class.getName());
   private final Map<String, Long> _events = new HashMap<>();

   protected WorldEventManager() {
      this.load();
   }

   public void reload() {
      this._events.clear();
      this.load();
   }

   protected void load() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM events_custom_data");
         ResultSet rset = statement.executeQuery();
      ) {
         while(rset.next()) {
            this._events.put(rset.getString("event_name"), rset.getLong("expire_time"));
         }

         _log.info(this.getClass().getSimpleName() + ": Loaded " + this._events.size() + " event statuses.");
      } catch (SQLException var62) {
         _log.warning(this.getClass().getSimpleName() + ": Couldnt load events_custom_data table");
      } catch (Exception var63) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Error while initializing WorldEventManager: " + var63.getMessage(), (Throwable)var63);
      }
   }

   public long getEventExpireTime(String name) {
      return this._events.containsKey(name) ? this._events.get(name) : 0L;
   }

   public void updateEventExpireTime(String name, long expireTime) {
      this._events.put(name, expireTime);
   }

   public static final WorldEventManager getInstance() {
      return WorldEventManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final WorldEventManager _instance = new WorldEventManager();
   }
}
