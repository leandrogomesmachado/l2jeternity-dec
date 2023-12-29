package l2e.gameserver.data.holder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;

public class CharNameHolder {
   private static Logger _log = Logger.getLogger(CharNameHolder.class.getName());
   private final Map<Integer, String> _chars = new ConcurrentHashMap<>();
   private final Map<Integer, Integer> _accessLevels = new ConcurrentHashMap<>();

   protected CharNameHolder() {
      if (Config.CACHE_CHAR_NAMES) {
         this.loadAll();
      }
   }

   public static CharNameHolder getInstance() {
      return CharNameHolder.SingletonHolder._instance;
   }

   public final void addName(Player player) {
      if (player != null) {
         this.addName(player.getObjectId(), player.getName());
         this._accessLevels.put(player.getObjectId(), player.getAccessLevel().getLevel());
      }
   }

   public final void addName(int objectId, String name) {
      if (name != null && !name.equals(this._chars.get(objectId))) {
         this._chars.put(objectId, name);
      }
   }

   public final void removeName(int objId) {
      this._chars.remove(objId);
      this._accessLevels.remove(objId);
   }

   public final int getIdByName(String name) {
      if (name != null && !name.isEmpty()) {
         for(Entry<Integer, String> pair : this._chars.entrySet()) {
            if (pair.getValue().equalsIgnoreCase(name)) {
               return pair.getKey();
            }
         }

         if (Config.CACHE_CHAR_NAMES) {
            return -1;
         } else {
            int id = -1;
            int accessLevel = 0;

            try (
               Connection con = DatabaseFactory.getInstance().getConnection();
               PreparedStatement ps = con.prepareStatement("SELECT charId,accesslevel FROM characters WHERE char_name=?");
            ) {
               ps.setString(1, name);

               try (ResultSet rs = ps.executeQuery()) {
                  while(rs.next()) {
                     id = rs.getInt(1);
                     accessLevel = rs.getInt(2);
                  }
               }
            } catch (SQLException var64) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not check existing char name: " + var64.getMessage(), (Throwable)var64);
            }

            if (id > 0) {
               this._chars.put(id, name);
               this._accessLevels.put(id, accessLevel);
               return id;
            } else {
               return -1;
            }
         }
      } else {
         return -1;
      }
   }

   public final String getNameById(int id) {
      if (id <= 0) {
         return null;
      } else {
         String name = this._chars.get(id);
         if (name != null) {
            return name;
         } else if (Config.CACHE_CHAR_NAMES) {
            return null;
         } else {
            int accessLevel = 0;

            try (
               Connection con = DatabaseFactory.getInstance().getConnection();
               PreparedStatement ps = con.prepareStatement("SELECT char_name,accesslevel FROM characters WHERE charId=?");
            ) {
               ps.setInt(1, id);

               try (ResultSet rset = ps.executeQuery()) {
                  while(rset.next()) {
                     name = rset.getString(1);
                     accessLevel = rset.getInt(2);
                  }
               }
            } catch (SQLException var62) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not check existing char id: " + var62.getMessage(), (Throwable)var62);
            }

            if (name != null && !name.isEmpty()) {
               this._chars.put(id, name);
               this._accessLevels.put(id, accessLevel);
               return name;
            } else {
               return null;
            }
         }
      }
   }

   public final int getAccessLevelById(int objectId) {
      return this.getNameById(objectId) != null ? this._accessLevels.get(objectId) : 0;
   }

   public synchronized boolean doesCharNameExist(String name) {
      boolean result = true;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT account_name FROM characters WHERE char_name=?");
      ) {
         ps.setString(1, name);

         try (ResultSet rs = ps.executeQuery()) {
            result = rs.next();
         }
      } catch (SQLException var61) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not check existing charname: " + var61.getMessage(), (Throwable)var61);
      }

      return result;
   }

   public int accountCharNumber(String account) {
      int number = 0;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT COUNT(char_name) FROM characters WHERE account_name=?");
      ) {
         ps.setString(1, account);

         try (ResultSet rset = ps.executeQuery()) {
            while(rset.next()) {
               number = rset.getInt(1);
            }
         }
      } catch (SQLException var61) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not check existing char number: " + var61.getMessage(), (Throwable)var61);
      }

      return number;
   }

   private void loadAll() {
      int id = -1;
      int accessLevel = 0;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         Statement s = con.createStatement();
         ResultSet rs = s.executeQuery("SELECT charId,char_name,accesslevel FROM characters");
      ) {
         while(rs.next()) {
            id = rs.getInt(1);
            String name = rs.getString(2);
            accessLevel = rs.getInt(3);
            this._chars.put(id, name);
            this._accessLevels.put(id, accessLevel);
         }
      } catch (SQLException var62) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not load char name: " + var62.getMessage(), (Throwable)var62);
      }

      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._chars.size() + " char names.");
   }

   private static class SingletonHolder {
      protected static final CharNameHolder _instance = new CharNameHolder();
   }
}
