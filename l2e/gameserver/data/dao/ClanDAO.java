package l2e.gameserver.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;

public class ClanDAO {
   private static final Logger _log = Logger.getLogger(ClanDAO.class.getName());
   private static final String SELECT_CLAN_PRIVILEGES = "SELECT `privs`, `rank`, `party` FROM `clan_privs` WHERE clan_id=?";
   private static final String INSERT_CLAN_PRIVILEGES = "INSERT INTO `clan_privs` (`clan_id`, `rank`, `party`, `privs`) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE `privs`=?";
   private static ClanDAO _instance = new ClanDAO();

   public Map<Integer, Integer> getPrivileges(int clanId) {
      Map<Integer, Integer> result = new HashMap<>();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT `privs`, `rank`, `party` FROM `clan_privs` WHERE clan_id=?");
      ) {
         ps.setInt(1, clanId);

         try (ResultSet rs = ps.executeQuery()) {
            while(rs.next()) {
               int rank = rs.getInt("rank");
               if (rank != -1) {
                  result.put(rank, rs.getInt("privs"));
               }
            }
         }
      } catch (Exception var61) {
         _log.log(Level.WARNING, "Unable to restore clan privileges for clan Id " + clanId, (Throwable)var61);
      }

      return result;
   }

   public void storePrivileges(int clanId, int rank, int privileges) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement(
            "INSERT INTO `clan_privs` (`clan_id`, `rank`, `party`, `privs`) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE `privs`=?"
         );
      ) {
         ps.setInt(1, clanId);
         ps.setInt(2, rank);
         ps.setInt(3, 0);
         ps.setInt(4, privileges);
         ps.setInt(5, privileges);
         ps.execute();
      } catch (Exception var36) {
         _log.log(Level.WARNING, "Unable to store clan privileges for clan Id " + clanId, (Throwable)var36);
      }
   }

   public static ClanDAO getInstance() {
      return _instance;
   }
}
