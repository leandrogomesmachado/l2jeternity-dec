package l2e.gameserver.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.custom.achievements.AchievementManager;

public class AchievementsDAO {
   private static final Logger _log = Logger.getLogger(AchievementsDAO.class.getName());
   private static final String RESTORE_CHAR_ACHIVEMENTS = "SELECT id,points FROM character_achievements WHERE charId=?";
   private static final String ADD_CHAR_ACHIVEMENTS = "REPLACE INTO character_achievements (charId,id,points) VALUES (?,?,?)";
   private static final String DELETE_CHAR_ACHIVEMENTS = "DELETE FROM character_achievements WHERE charId=?";
   private static AchievementsDAO _instance = new AchievementsDAO();

   public void removeAchievements(Player player) {
      if (AchievementManager.getInstance().isActive()) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("DELETE FROM character_achievements WHERE charId=?");
         ) {
            statement.setInt(1, player.getObjectId());
            statement.execute();
         } catch (Exception var34) {
            _log.log(Level.WARNING, "Error could not delete skill: " + var34.getMessage(), (Throwable)var34);
         }
      }
   }

   public void saveAchievements(Player player) {
      if (AchievementManager.getInstance().isActive()) {
         if (player.getCounters().getAchievements() != null && !player.getCounters().getAchievements().isEmpty()) {
            this.removeAchievements(player);

            try (
               Connection con = DatabaseFactory.getInstance().getConnection();
               PreparedStatement ps = con.prepareStatement("REPLACE INTO character_achievements (charId,id,points) VALUES (?,?,?)");
            ) {
               con.setAutoCommit(false);

               for(int id : player.getCounters().getAchievements().keySet()) {
                  long points = player.getCounters().getAchievements().get(id);
                  if (points > 0L) {
                     ps.setInt(1, player.getObjectId());
                     ps.setInt(2, id);
                     ps.setLong(3, points);
                     ps.addBatch();
                  }
               }

               ps.executeBatch();
               con.commit();
            } catch (SQLException var37) {
               _log.log(Level.WARNING, "Error could not save char achievements: " + var37.getMessage(), (Throwable)var37);
            }
         }
      }
   }

   public void restoreAchievements(Player player) {
      if (AchievementManager.getInstance().isActive()) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT id,points FROM character_achievements WHERE charId=?");
         ) {
            statement.setInt(1, player.getObjectId());

            try (ResultSet rset = statement.executeQuery()) {
               while(rset.next()) {
                  player.getCounters().setAchievementInfo(rset.getInt("id"), rset.getLong("points"), true);
               }
            }
         } catch (Exception var60) {
            _log.log(Level.WARNING, "Could not restore character achievements " + var60.getMessage(), (Throwable)var60);
         }
      }
   }

   public static AchievementsDAO getInstance() {
      return _instance;
   }
}
