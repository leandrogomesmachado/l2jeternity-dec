package l2e.gameserver.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.player.PcTeleportTemplate;

public class CharacterCBTeleportDAO {
   private static final Logger _log = Logger.getLogger(CharacterCBTeleportDAO.class.getName());
   private static final String INSERT_TELEPORT = "INSERT INTO character_teleport (charId,xPos,yPos,zPos,name) VALUES(?,?,?,?,?)";
   private static final String UPDATE_TELEPORT = "UPDATE character_teleport SET xPos=?, yPos=?, zPos=? WHERE charId=? AND name=?;";
   private static final String SELECT_TELEPORT_ALL = "SELECT COUNT(*) FROM character_teleport WHERE charId=?;";
   private static final String SELECT_TELEPORT_ALL2 = "SELECT * FROM character_teleport WHERE charId=? AND name=?;";
   private static final String SELECT_TELEPORT_NAME = "SELECT COUNT(*) FROM character_teleport WHERE charId=? AND name=?;";
   private static final String RESTORE_TELEPORT = "SELECT * FROM character_teleport WHERE charId=?;";
   private static final String DELETE_TELEPORT = "DELETE FROM character_teleport WHERE charId=? AND TpId=?;";
   private static CharacterCBTeleportDAO _instance = new CharacterCBTeleportDAO();

   public boolean add(Player player, String name) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement st = con.prepareStatement("SELECT COUNT(*) FROM character_teleport WHERE charId=?;");
         st.setLong(1, (long)player.getObjectId());
         ResultSet rs = st.executeQuery();
         rs.next();
         if (rs.getInt(1) >= Config.COMMUNITY_TELEPORT_TABS) {
            return false;
         } else {
            PreparedStatement st1 = con.prepareStatement("SELECT COUNT(*) FROM character_teleport WHERE charId=? AND name=?;");
            st1.setLong(1, (long)player.getObjectId());
            st1.setString(2, name);
            ResultSet rs1 = st1.executeQuery();
            rs1.next();
            if (rs1.getInt(1) == 0) {
               PreparedStatement stAdd = con.prepareStatement("INSERT INTO character_teleport (charId,xPos,yPos,zPos,name) VALUES(?,?,?,?,?)");
               stAdd.setInt(1, player.getObjectId());
               stAdd.setInt(2, player.getX());
               stAdd.setInt(3, player.getY());
               stAdd.setInt(4, player.getZ());
               stAdd.setString(5, name);
               stAdd.execute();
               this.addToPlayer(player, name);
            } else {
               PreparedStatement stAdd = con.prepareStatement("UPDATE character_teleport SET xPos=?, yPos=?, zPos=? WHERE charId=? AND name=?;");
               stAdd.setInt(1, player.getObjectId());
               stAdd.setInt(2, player.getX());
               stAdd.setInt(3, player.getY());
               stAdd.setInt(4, player.getZ());
               stAdd.setString(5, name);
               stAdd.execute();

               for(PcTeleportTemplate tpl : player.getCBTeleports()) {
                  if (tpl != null && tpl.getName().equals(name)) {
                     tpl.setX(player.getX());
                     tpl.setY(player.getY());
                     tpl.setZ(player.getZ());
                  }
               }
            }

            return true;
         }
      } catch (Exception var23) {
         _log.log(Level.WARNING, "Could not insert character community teleport data: " + var23.getMessage(), (Throwable)var23);
         return false;
      }
   }

   private void addToPlayer(Player player, String name) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement st = con.prepareStatement("SELECT * FROM character_teleport WHERE charId=? AND name=?;");
         st.setLong(1, (long)player.getObjectId());
         st.setString(2, name);
         ResultSet rs = st.executeQuery();

         while(rs.next()) {
            player.addCBTeleport(
               rs.getInt("TpId"), new PcTeleportTemplate(rs.getInt("TpId"), rs.getString("name"), rs.getInt("xPos"), rs.getInt("yPos"), rs.getInt("zPos"))
            );
         }
      } catch (Exception var17) {
         _log.log(Level.WARNING, "Could not restore character community teleport data: " + var17.getMessage(), (Throwable)var17);
      }
   }

   public void restore(Player player) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement st = con.prepareStatement("SELECT * FROM character_teleport WHERE charId=?;");
         st.setLong(1, (long)player.getObjectId());
         ResultSet rs = st.executeQuery();

         while(rs.next()) {
            player.addCBTeleport(
               rs.getInt("TpId"), new PcTeleportTemplate(rs.getInt("TpId"), rs.getString("name"), rs.getInt("xPos"), rs.getInt("yPos"), rs.getInt("zPos"))
            );
         }
      } catch (Exception var16) {
         _log.log(Level.WARNING, "Could not restore character community teleport data: " + var16.getMessage(), (Throwable)var16);
      }
   }

   public void delete(Player player, int id) {
      try (Connection conDel = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement stDel = conDel.prepareStatement("DELETE FROM character_teleport WHERE charId=? AND TpId=?;");
         stDel.setInt(1, player.getObjectId());
         stDel.setInt(2, id);
         stDel.execute();
      } catch (Exception var16) {
         _log.log(Level.WARNING, "Could not delete character community teleport data: " + var16.getMessage(), (Throwable)var16);
      }
   }

   public static CharacterCBTeleportDAO getInstance() {
      return _instance;
   }
}
