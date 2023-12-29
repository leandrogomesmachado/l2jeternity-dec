package l2e.gameserver.data.holder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.service.buffer.PlayerScheme;
import l2e.gameserver.model.service.buffer.SchemeBuff;

public class CharSchemesHolder {
   private static final Logger _log = Logger.getLogger(CharSchemesHolder.class.getName());
   private static CharSchemesHolder _instance = null;

   public void loadSchemes(Player player, Connection con) {
      try (PreparedStatement statement = con.prepareStatement("SELECT id, scheme_name, icon FROM character_scheme_list WHERE charId=?")) {
         statement.setInt(1, player.getObjectId());

         try (ResultSet rset = statement.executeQuery()) {
            while(rset.next()) {
               int schemeId = rset.getInt("id");
               String schemeName = rset.getString("scheme_name");
               int iconId = rset.getInt("icon");
               player.getBuffSchemes().add(new PlayerScheme(schemeId, schemeName, iconId));
            }
         }
      } catch (SQLException var97) {
         _log.log(Level.WARNING, "Error while loading Scheme Content of the Player", (Throwable)var97);
      }

      for(PlayerScheme scheme : player.getBuffSchemes()) {
         try (PreparedStatement statement = con.prepareStatement(
               "SELECT skill_id, skill_level, premium_lvl, buff_class FROM character_scheme_contents WHERE schemeId=?"
            )) {
            statement.setInt(1, scheme.getSchemeId());

            try (ResultSet rset = statement.executeQuery()) {
               while(rset.next()) {
                  int skillId = rset.getInt("skill_id");
                  int skillLevel = rset.getInt("skill_level");
                  int premiumLvl = rset.getInt("premium_lvl");
                  boolean isDanceSlot = rset.getInt("buff_class") == 1 || rset.getInt("buff_class") == 2;
                  scheme.addBuff(new SchemeBuff(skillId, skillLevel, premiumLvl, isDanceSlot));
               }
            }
         } catch (SQLException var92) {
            _log.log(Level.WARNING, "Error while loading Scheme Content of the Player", (Throwable)var92);
         }
      }
   }

   public void addBuff(String scheme, String skill, String level, String premiumLvl, boolean isDanceSlot) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "INSERT INTO character_scheme_contents (schemeId,skill_id,skill_level,premium_lvl,buff_class) VALUES (?,?,?,?,?)"
         );
      ) {
         statement.setString(1, scheme);
         statement.setString(2, skill);
         statement.setString(3, level);
         statement.setString(4, premiumLvl);
         statement.setInt(5, isDanceSlot ? 1 : 0);
         statement.executeUpdate();
      } catch (SQLException var38) {
         _log.log(Level.WARNING, "Error while adding Scheme Content", (Throwable)var38);
      }
   }

   public void removeBuff(String scheme, String skill, String level) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM character_scheme_contents WHERE schemeId=? AND skill_id=? AND skill_level=? LIMIT 1");
      ) {
         statement.setString(1, scheme);
         statement.setString(2, skill);
         statement.setString(3, level);
         statement.executeUpdate();
      } catch (SQLException var36) {
         _log.log(Level.WARNING, "Error while deleting Scheme Content", (Throwable)var36);
      }
   }

   public void deleteScheme(int eventParam1) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM character_scheme_list WHERE id=? LIMIT 1");
      ) {
         statement.setString(1, String.valueOf(eventParam1));
         statement.executeUpdate();

         try (PreparedStatement statementx = con.prepareStatement("DELETE FROM character_scheme_contents WHERE schemeId=?")) {
            statementx.setString(1, String.valueOf(eventParam1));
            statementx.executeUpdate();
         }
      } catch (SQLException var58) {
         _log.log(Level.WARNING, "Error while deleting Scheme Content", (Throwable)var58);
      }
   }

   public void updateScheme(String name, int schemeId) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE character_scheme_list SET scheme_name=? WHERE id=?");
      ) {
         statement.setString(1, name);
         statement.setInt(2, schemeId);
         statement.executeUpdate();
      } catch (SQLException var35) {
         _log.log(Level.WARNING, "Error while updating Scheme List", (Throwable)var35);
      }
   }

   public void updateIcon(int iconId, int schemeId) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE character_scheme_list SET icon=? WHERE id=?");
      ) {
         statement.setInt(1, iconId);
         statement.setInt(2, schemeId);
         statement.executeUpdate();
      } catch (SQLException var35) {
         _log.log(Level.WARNING, "Error while updating Scheme List", (Throwable)var35);
      }
   }

   public static CharSchemesHolder getInstance() {
      if (_instance == null) {
         _instance = new CharSchemesHolder();
      }

      return _instance;
   }
}
