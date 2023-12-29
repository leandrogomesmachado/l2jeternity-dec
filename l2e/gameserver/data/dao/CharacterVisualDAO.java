package l2e.gameserver.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.DressArmorParser;
import l2e.gameserver.data.parser.DressCloakParser;
import l2e.gameserver.data.parser.DressHatParser;
import l2e.gameserver.data.parser.DressShieldParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.DressArmorTemplate;
import l2e.gameserver.model.actor.templates.DressHatTemplate;

public class CharacterVisualDAO {
   private static final Logger _log = Logger.getLogger(CharacterVisualDAO.class.getName());
   private static final String INSERT_VISUAL = "INSERT INTO characters_visual (charId, skinType, skinId, active) VALUES (?,?,?,?)";
   private static final String UPDATE_VISUAL = "UPDATE characters_visual SET active=? WHERE charId=? and skinType=? and skinId=?";
   private static final String RESTORE_VISUAL = "SELECT * FROM characters_visual WHERE charId=?";
   private static CharacterVisualDAO _instance = new CharacterVisualDAO();

   public void add(Player player, String type, int skinId) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("INSERT INTO characters_visual (charId, skinType, skinId, active) VALUES (?,?,?,?)");
      ) {
         statement.setInt(1, player.getObjectId());
         statement.setString(2, type);
         statement.setInt(3, skinId);
         statement.setInt(4, 0);
         statement.executeUpdate();
      } catch (Exception var36) {
         _log.log(Level.WARNING, "Could not insert char visual skin: " + var36.getMessage(), (Throwable)var36);
      }
   }

   public void update(Player player, String type, int active, int skinId) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE characters_visual SET active=? WHERE charId=? and skinType=? and skinId=?");
      ) {
         statement.setInt(1, active);
         statement.setInt(2, player.getObjectId());
         statement.setString(3, type);
         statement.setInt(4, skinId);
         statement.execute();
      } catch (Exception var37) {
         _log.log(Level.WARNING, "Failed update character visual skins.", (Throwable)var37);
      }
   }

   public void restore(Player player) {
      if (Config.ALLOW_VISUAL_SYSTEM) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT * FROM characters_visual WHERE charId=?");
         ) {
            statement.setInt(1, player.getObjectId());

            try (ResultSet rset = statement.executeQuery()) {
               while(rset.next()) {
                  String type = rset.getString("skinType");
                  int active = rset.getInt("active");
                  switch(type) {
                     case "Weapon":
                        if (active == 1) {
                           player.setActiveWeaponSkin(rset.getInt("skinId"), true);
                        }

                        player.addWeaponSkin(rset.getInt("skinId"));
                        break;
                     case "Armor":
                        if (active == 1) {
                           DressArmorTemplate set = DressArmorParser.getInstance().getArmor(rset.getInt("skinId"));
                           if (set != null) {
                              player.setActiveArmorSkin(rset.getInt("skinId"), true);
                              if (set.getShieldId() > 0 && DressShieldParser.getInstance().getShieldId(set.getShieldId()) != -1) {
                                 player.setActiveShieldSkin(DressShieldParser.getInstance().getShieldId(set.getShieldId()), false);
                              }

                              if (set.getCloakId() > 0 && DressCloakParser.getInstance().getCloakId(set.getCloakId()) != -1) {
                                 player.setActiveCloakSkin(DressCloakParser.getInstance().getCloakId(set.getCloakId()), false);
                              }

                              if (set.getHatId() > 0 && DressHatParser.getInstance().getHatId(set.getHatId()) != -1) {
                                 if (set.getSlot() == 3) {
                                    player.setActiveMaskSkin(DressHatParser.getInstance().getHatId(set.getHatId()), false);
                                 } else {
                                    player.setActiveHairSkin(DressHatParser.getInstance().getHatId(set.getHatId()), false);
                                 }
                              }
                           }
                        }

                        player.addArmorSkin(rset.getInt("skinId"));
                        break;
                     case "Shield":
                        if (active == 1) {
                           player.setActiveShieldSkin(rset.getInt("skinId"), true);
                        }

                        player.addShieldSkin(rset.getInt("skinId"));
                        break;
                     case "Cloak":
                        if (active == 1) {
                           player.setActiveCloakSkin(rset.getInt("skinId"), true);
                        }

                        player.addCloakSkin(rset.getInt("skinId"));
                        break;
                     case "Hair":
                        if (active == 1) {
                           DressHatTemplate visual = DressHatParser.getInstance().getHat(rset.getInt("skinId"));
                           if (visual.getSlot() == 2) {
                              player.setActiveHairSkin(rset.getInt("skinId"), true);
                           } else {
                              player.setActiveMaskSkin(rset.getInt("skinId"), true);
                           }
                        }

                        player.addHairSkin(rset.getInt("skinId"));
                  }
               }
            }
         } catch (Exception var64) {
            _log.log(Level.WARNING, "Failed restore character visual skins.", (Throwable)var64);
         }
      }
   }

   public static CharacterVisualDAO getInstance() {
      return _instance;
   }
}
