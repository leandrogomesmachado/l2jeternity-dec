package l2e.gameserver.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ClassListParser;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;

public class CharacterSkillsDAO {
   private static final Logger _log = Logger.getLogger(CharacterSkillsDAO.class.getName());
   private static final String RESTORE_SKILLS_FOR_CHAR_DEFAULT = "SELECT skill_id,skill_level FROM character_skills WHERE charId=? AND class_index=?";
   private static final String RESTORE_SKILLS_FOR_CHAR_MODIFER = "SELECT skill_id,skill_level FROM character_skills WHERE charId=? ORDER BY skill_id , skill_level ASC";
   private static final String ADD_NEW_SKILL = "INSERT INTO character_skills (charId,skill_id,skill_level,class_index) VALUES (?,?,?,?)";
   private static final String UPDATE_CHARACTER_SKILL_LEVEL = "UPDATE character_skills SET skill_level=? WHERE skill_id=? AND charId=? AND class_index=?";
   private static final String ADD_NEW_SKILLS = "REPLACE INTO character_skills (charId,skill_id,skill_level,class_index) VALUES (?,?,?,?)";
   private static final String DELETE_SKILL_FROM_CHAR = "DELETE FROM character_skills WHERE skill_id=? AND charId=? AND class_index=?";
   private static CharacterSkillsDAO _instance = new CharacterSkillsDAO();

   public void remove(Player player, int skillId) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM character_skills WHERE skill_id=? AND charId=? AND class_index=?");
      ) {
         statement.setInt(1, skillId);
         statement.setInt(2, player.getObjectId());
         statement.setInt(3, player.getClassIndex());
         statement.execute();
      } catch (Exception var35) {
         _log.log(Level.WARNING, "Error could not delete skill: " + var35.getMessage(), (Throwable)var35);
      }
   }

   public void storeSkills(Player player, List<Skill> newSkills, int newClassIndex) {
      if (!newSkills.isEmpty()) {
         int classIndex = newClassIndex > -1 ? newClassIndex : player.getClassIndex();

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement ps = con.prepareStatement("REPLACE INTO character_skills (charId,skill_id,skill_level,class_index) VALUES (?,?,?,?)");
         ) {
            con.setAutoCommit(false);

            for(Skill addSkill : newSkills) {
               ps.setInt(1, player.getObjectId());
               ps.setInt(2, addSkill.getId());
               ps.setInt(3, addSkill.getLevel());
               ps.setInt(4, classIndex);
               ps.addBatch();
            }

            ps.executeBatch();
            con.commit();
         } catch (SQLException var38) {
            _log.log(Level.WARNING, "Error could not store char skills: " + var38.getMessage(), (Throwable)var38);
         }
      }
   }

   public void store(Player player, Skill newSkill, Skill oldSkill, int newClassIndex) {
      int classIndex = player.getClassIndex();
      if (newClassIndex > -1) {
         classIndex = newClassIndex;
      }

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         if (oldSkill != null && newSkill != null) {
            PreparedStatement statement = con.prepareStatement("UPDATE character_skills SET skill_level=? WHERE skill_id=? AND charId=? AND class_index=?");
            statement.setInt(1, newSkill.getLevel());
            statement.setInt(2, oldSkill.getId());
            statement.setInt(3, player.getObjectId());
            statement.setInt(4, classIndex);
            statement.execute();
            statement.close();
         } else if (newSkill != null) {
            PreparedStatement statement = con.prepareStatement("INSERT INTO character_skills (charId,skill_id,skill_level,class_index) VALUES (?,?,?,?)");
            statement.setInt(1, player.getObjectId());
            statement.setInt(2, newSkill.getId());
            statement.setInt(3, newSkill.getLevel());
            statement.setInt(4, classIndex);
            statement.execute();
            statement.close();
         } else {
            _log.warning("could not store new skill. its NULL");
         }
      } catch (Exception var19) {
         _log.log(Level.WARNING, "Error could not store char skills: " + var19.getMessage(), (Throwable)var19);
      }
   }

   public void restoreSkills(Player player) {
      boolean isAcumulative = Config.SUBCLASS_STORE_SKILL;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            isAcumulative
               ? "SELECT skill_id,skill_level FROM character_skills WHERE charId=? ORDER BY skill_id , skill_level ASC"
               : "SELECT skill_id,skill_level FROM character_skills WHERE charId=? AND class_index=?"
         );
      ) {
         statement.setInt(1, player.getObjectId());
         if (!isAcumulative) {
            statement.setInt(2, player.getClassIndex());
         }

         ResultSet rset = statement.executeQuery();

         while(rset.next()) {
            int id = rset.getInt("skill_id");
            int level = rset.getInt("skill_level");
            if (id <= 9000 || id >= 9007) {
               Skill skill = SkillsParser.getInstance().getInfo(id, level);
               if (skill != null) {
                  player.addSkill(skill);
                  if (Config.SKILL_CHECK_ENABLE
                     && (!player.canOverrideCond(PcCondOverride.SKILL_CONDITIONS) || Config.SKILL_CHECK_GM)
                     && !SkillTreesParser.getInstance().isSkillAllowed(player, skill)) {
                     Util.handleIllegalPlayerAction(
                        player,
                        ""
                           + player.getName()
                           + " has invalid skill "
                           + skill.getNameEn()
                           + " ("
                           + skill.getId()
                           + "/"
                           + skill.getLevel()
                           + "), class:"
                           + ClassListParser.getInstance().getClass(player.getClassId()).getClassName()
                     );
                     if (Config.SKILL_CHECK_REMOVE) {
                        player.removeSkill(skill);
                     }
                  }
               }
            }
         }

         rset.close();
      } catch (Exception var38) {
         _log.log(Level.WARNING, "Could not restore character " + this + " skills: " + var38.getMessage(), (Throwable)var38);
      }
   }

   public static CharacterSkillsDAO getInstance() {
      return _instance;
   }
}
