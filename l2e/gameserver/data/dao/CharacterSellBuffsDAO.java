package l2e.gameserver.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SellBuffHolder;

public class CharacterSellBuffsDAO {
   private static final Logger _log = Logger.getLogger(CharacterSellBuffsDAO.class.getName());
   private static final CharacterSellBuffsDAO _instance = new CharacterSellBuffsDAO();
   public static final String SELECT_SQL_QUERY = "SELECT * FROM character_offline_buffs WHERE charId = ?";
   public static final String DELETE_SQL_QUERY = "DELETE FROM character_offline_buffs WHERE charId = ?";
   public static final String INSERT_SQL_QUERY = "INSERT INTO character_offline_buffs (`charId`,`skillId`,`level`,`itemId`,`price`) VALUES (?,?,?,?,?)";

   public static CharacterSellBuffsDAO getInstance() {
      return _instance;
   }

   public void saveSellBuffList(Player player) {
      this.cleanSellBuffList(player);
      if (player.isSellingBuffs()) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(
               "INSERT INTO character_offline_buffs (`charId`,`skillId`,`level`,`itemId`,`price`) VALUES (?,?,?,?,?)"
            );
         ) {
            for(SellBuffHolder holder : player.getSellingBuffs()) {
               statement.setInt(1, player.getObjectId());
               statement.setInt(2, holder.getId());
               statement.setLong(3, (long)holder.getLvl());
               statement.setLong(4, (long)holder.getItemId());
               statement.setLong(5, holder.getPrice());
               statement.executeUpdate();
               statement.clearParameters();
            }
         } catch (Exception var35) {
            _log.log(Level.WARNING, "Error while saving offline sellbuffs: " + player.getObjectId() + " " + var35, (Throwable)var35);
         }
      }
   }

   public void restoreSellBuffList(Player player) {
      player.getSellingBuffs().clear();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM character_offline_buffs WHERE charId = ?");
      ) {
         statement.setInt(1, player.getObjectId());
         ResultSet rset = statement.executeQuery();

         while(rset.next()) {
            player.getSellingBuffs().add(new SellBuffHolder(rset.getInt("skillId"), rset.getInt("level"), rset.getInt("itemId"), rset.getLong("price")));
         }
      } catch (Exception var34) {
         _log.log(Level.WARNING, "Error while restore offline sellbuffs: " + player.getObjectId() + " " + var34, (Throwable)var34);
      }
   }

   public void cleanSellBuffList(Player player) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM character_offline_buffs WHERE charId = ?");
      ) {
         statement.setInt(1, player.getObjectId());
         statement.execute();
      } catch (Exception var34) {
         _log.log(Level.SEVERE, "Failed to clean up offline sellbuffs.", (Throwable)var34);
      }
   }
}
