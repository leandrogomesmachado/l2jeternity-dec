package l2e.gameserver.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.dbutils.DbUtils;
import l2e.commons.util.Strings;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.instance.player.CharacterVariable;

public class CharacterVariablesDAO {
   private static final Logger _log = Logger.getLogger(CharacterVariablesDAO.class.getName());
   private static final CharacterVariablesDAO _instance = new CharacterVariablesDAO();
   public static final String SELECT_SQL_QUERY = "SELECT name, value, expire_time FROM character_variables WHERE obj_id = ?";
   public static final String SELECT_FROM_PLAYER_SQL_QUERY = "SELECT value, expire_time FROM character_variables WHERE obj_id = ? AND name = ?";
   public static final String DELETE_SQL_QUERY = "DELETE FROM character_variables WHERE obj_id = ? AND name = ? LIMIT 1";
   public static final String DELETE_EXPIRED_SQL_QUERY = "DELETE FROM character_variables WHERE expire_time > 0 AND expire_time < ?";
   public static final String INSERT_SQL_QUERY = "REPLACE INTO character_variables (obj_id, name, value, expire_time) VALUES (?,?,?,?)";

   public CharacterVariablesDAO() {
      this.deleteExpiredVars();
   }

   public static CharacterVariablesDAO getInstance() {
      return _instance;
   }

   private void deleteExpiredVars() {
      Connection con = null;
      PreparedStatement statement = null;

      try {
         con = DatabaseFactory.getInstance().getConnection();
         statement = con.prepareStatement("DELETE FROM character_variables WHERE expire_time > 0 AND expire_time < ?");
         statement.setLong(1, System.currentTimeMillis());
         statement.execute();
      } catch (Exception var7) {
         _log.log(Level.SEVERE, "CharacterVariablesDAO:deleteExpiredVars()", (Throwable)var7);
      } finally {
         DbUtils.closeQuietly(con, statement);
      }
   }

   public boolean delete(int playerObjId, String varName) {
      Connection con = null;
      PreparedStatement statement = null;

      boolean var6;
      try {
         con = DatabaseFactory.getInstance().getConnection();
         statement = con.prepareStatement("DELETE FROM character_variables WHERE obj_id = ? AND name = ? LIMIT 1");
         statement.setInt(1, playerObjId);
         statement.setString(2, varName);
         statement.execute();
         return true;
      } catch (Exception var10) {
         _log.log(Level.SEVERE, "CharacterVariablesDAO:delete(playerObjId,varName)", (Throwable)var10);
         var6 = false;
      } finally {
         DbUtils.closeQuietly(con, statement);
      }

      return var6;
   }

   public boolean insert(int playerObjId, CharacterVariable var) {
      Connection con = null;
      PreparedStatement statement = null;

      boolean var6;
      try {
         con = DatabaseFactory.getInstance().getConnection();
         statement = con.prepareStatement("REPLACE INTO character_variables (obj_id, name, value, expire_time) VALUES (?,?,?,?)");
         statement.setInt(1, playerObjId);
         statement.setString(2, var.getName());
         statement.setString(3, var.getValue());
         statement.setLong(4, var.getExpireTime());
         statement.executeUpdate();
         return true;
      } catch (Exception var10) {
         _log.log(Level.SEVERE, "CharacterVariablesDAO:insert(playerObjId,var)", (Throwable)var10);
         var6 = false;
      } finally {
         DbUtils.closeQuietly(con, statement);
      }

      return var6;
   }

   public List<CharacterVariable> restore(int playerObjId) {
      List<CharacterVariable> result = new ArrayList<>();
      Connection con = null;
      PreparedStatement statement = null;
      ResultSet rset = null;

      try {
         con = DatabaseFactory.getInstance().getConnection();
         statement = con.prepareStatement("SELECT name, value, expire_time FROM character_variables WHERE obj_id = ?");
         statement.setInt(1, playerObjId);
         rset = statement.executeQuery();

         while(rset.next()) {
            long expireTime = rset.getLong("expire_time");
            if (expireTime <= 0L || expireTime >= System.currentTimeMillis()) {
               result.add(new CharacterVariable(rset.getString("name"), Strings.stripSlashes(rset.getString("value")), expireTime));
            }
         }
      } catch (Exception var11) {
         _log.log(Level.SEVERE, "CharacterVariablesDAO:restore(playerObjId)", (Throwable)var11);
      } finally {
         DbUtils.closeQuietly(con, statement, rset);
      }

      return result;
   }

   public String getVarFromPlayer(int playerObjId, String var) {
      String value = null;
      Connection con = null;
      PreparedStatement statement = null;
      ResultSet rset = null;

      try {
         con = DatabaseFactory.getInstance().getConnection();
         statement = con.prepareStatement("SELECT value, expire_time FROM character_variables WHERE obj_id = ? AND name = ?");
         statement.setInt(1, playerObjId);
         statement.setString(2, var);
         rset = statement.executeQuery();
         if (rset.next()) {
            long expireTime = rset.getLong("expire_time");
            if (expireTime <= 0L || expireTime >= System.currentTimeMillis()) {
               value = Strings.stripSlashes(rset.getString("value"));
            }
         }
      } catch (Exception var12) {
         _log.log(Level.SEVERE, "CharacterVariablesDAO:getVarFromPlayer(playerObjId,var)", (Throwable)var12);
      } finally {
         DbUtils.closeQuietly(con, statement, rset);
      }

      return value;
   }
}
