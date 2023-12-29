package l2e.gameserver.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.TimeStamp;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.stats.Env;

public class CharacterSkillSaveDAO {
   private static final Logger _log = Logger.getLogger(CharacterSkillSaveDAO.class.getName());
   private static final String ADD_SKILL_SAVE = "INSERT INTO character_skills_save (charId,skill_id,skill_level,effect_count,effect_cur_time,effect_total_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
   private static final String RESTORE_SKILL_SAVE = "SELECT skill_id,skill_level,effect_count,effect_cur_time,effect_total_time,reuse_delay,systime,restore_type FROM character_skills_save WHERE charId=? AND class_index=? ORDER BY buff_index ASC";
   private static final String RESTORE_SKILL_SAVE_MODIFER = "SELECT skill_id,skill_level,effect_count,effect_cur_time,effect_total_time,reuse_delay,systime,restore_type FROM character_skills_save WHERE charId=? ORDER BY buff_index ASC";
   private static final String DELETE_SKILL_SAVE = "DELETE FROM character_skills_save WHERE charId=? AND class_index=?";
   private static final String DELETE_SKILL_SAVE_MODIFER = "DELETE FROM character_skills_save WHERE charId=?";
   private static CharacterSkillSaveDAO _instance = new CharacterSkillSaveDAO();

   public void restore(Player player) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            Config.SUBCLASS_STORE_SKILL
               ? "SELECT skill_id,skill_level,effect_count,effect_cur_time,effect_total_time,reuse_delay,systime,restore_type FROM character_skills_save WHERE charId=? ORDER BY buff_index ASC"
               : "SELECT skill_id,skill_level,effect_count,effect_cur_time,effect_total_time,reuse_delay,systime,restore_type FROM character_skills_save WHERE charId=? AND class_index=? ORDER BY buff_index ASC"
         );
      ) {
         statement.setInt(1, player.getObjectId());
         if (!Config.SUBCLASS_STORE_SKILL) {
            statement.setInt(2, player.getClassIndex());
         }

         try (ResultSet rset = statement.executeQuery()) {
            while(rset.next()) {
               int effectCount = rset.getInt("effect_count");
               int effectCurTime = rset.getInt("effect_cur_time");
               int effectTotalTime = rset.getInt("effect_total_time");
               long reuseDelay = rset.getLong("reuse_delay");
               long systime = rset.getLong("systime");
               int restoreType = rset.getInt("restore_type");
               Skill skill = SkillsParser.getInstance().getInfo(rset.getInt("skill_id"), rset.getInt("skill_level"));
               if (skill != null) {
                  long remainingTime = systime - System.currentTimeMillis();
                  if (remainingTime > 10L) {
                     player.disableSkill(skill, remainingTime);
                     player.addTimeStamp(skill, reuseDelay, systime);
                  }

                  if (restoreType <= 0 && skill.hasEffects()) {
                     Env env = new Env();
                     env.setCharacter(player);
                     env.setTarget(player);
                     env.setSkill(skill);

                     for(EffectTemplate et : skill.getEffectTemplates()) {
                        Effect ef = et.getEffect(env);
                        if (ef != null) {
                           switch(ef.getEffectType()) {
                              case CANCEL:
                              case CANCEL_ALL:
                              case CANCEL_BY_SLOT:
                                 break;
                              default:
                                 ef.setCount(effectCount);
                                 ef.setAbnormalTime(effectTotalTime);
                                 ef.setFirstTime(effectCurTime);
                                 ef.scheduleEffect(true);
                           }
                        }
                     }
                  }
               }
            }
         }

         try (PreparedStatement del = con.prepareStatement(
               Config.SUBCLASS_STORE_SKILL
                  ? "DELETE FROM character_skills_save WHERE charId=?"
                  : "DELETE FROM character_skills_save WHERE charId=? AND class_index=?"
            )) {
            del.setInt(1, player.getObjectId());
            if (!Config.SUBCLASS_STORE_SKILL) {
               del.setInt(2, player.getClassIndex());
            }

            del.executeUpdate();
         }
      } catch (Exception var107) {
         _log.log(Level.WARNING, "Could not restore " + this + " active effect data: " + var107.getMessage(), (Throwable)var107);
      }
   }

   public void store(Player player, boolean isAcumulative, boolean storeEffects) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement(
            isAcumulative ? "DELETE FROM character_skills_save WHERE charId=?" : "DELETE FROM character_skills_save WHERE charId=? AND class_index=?"
         );
         statement.setInt(1, player.getObjectId());
         if (!isAcumulative) {
            statement.setInt(2, player.getClassIndex());
         }

         statement.execute();
         statement.close();
         int buff_index = 0;
         List<Integer> storedSkills = new ArrayList<>();
         statement = con.prepareStatement(
            "INSERT INTO character_skills_save (charId,skill_id,skill_level,effect_count,effect_cur_time,effect_total_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?,?,?)"
         );
         if (storeEffects) {
            for(Effect effect : player.getAllEffects()) {
               if (effect != null) {
                  switch(effect.getEffectType()) {
                     case CANCEL:
                     case CANCEL_ALL:
                     case CANCEL_BY_SLOT:
                     case HEAL_OVER_TIME:
                     case CPHEAL_OVER_TIME:
                     case HIDE:
                        break;
                     default:
                        if (!effect.getAbnormalType().equalsIgnoreCase("SEED_OF_KNIGHT") && !effect.getAbnormalType().equalsIgnoreCase("LIFE_FORCE_OTHERS")) {
                           Skill skill = effect.getSkill();
                           if (!storedSkills.contains(skill.getReuseHashCode()) && (!skill.isDance() || Config.ALT_STORE_DANCES)) {
                              storedSkills.add(skill.getReuseHashCode());
                              if (effect.isInUse() && !skill.isToggle()) {
                                 statement.setInt(1, player.getObjectId());
                                 statement.setInt(2, skill.getId());
                                 statement.setInt(3, skill.getLevel());
                                 statement.setInt(4, effect.getTickCount());
                                 statement.setInt(5, effect.getTime());
                                 statement.setInt(6, effect.getAbnormalTime());
                                 if (player.getSkillReuseTimeStamps().containsKey(skill.getReuseHashCode())) {
                                    TimeStamp t = player.getSkillReuseTimeStamps().get(skill.getReuseHashCode());
                                    statement.setLong(7, t.hasNotPassed() ? t.getReuse() : 0L);
                                    statement.setDouble(8, t.hasNotPassed() ? (double)t.getStamp() : 0.0);
                                 } else {
                                    statement.setLong(7, 0L);
                                    statement.setDouble(8, 0.0);
                                 }

                                 statement.setInt(9, 0);
                                 statement.setInt(10, player.getClassIndex());
                                 statement.setInt(11, ++buff_index);
                                 statement.execute();
                              }
                           }
                        }
                  }
               }
            }
         }

         for(Entry<Integer, TimeStamp> ts : player.getSkillReuseTimeStamps().entrySet()) {
            int hash = ts.getKey();
            if (!storedSkills.contains(hash)) {
               TimeStamp t = ts.getValue();
               if (t != null && t.hasNotPassed()) {
                  storedSkills.add(hash);
                  statement.setInt(1, player.getObjectId());
                  statement.setInt(2, t.getSkillId());
                  statement.setInt(3, t.getSkillLvl());
                  statement.setInt(4, -1);
                  statement.setInt(5, -1);
                  statement.setInt(6, -1);
                  statement.setLong(7, t.getReuse());
                  statement.setDouble(8, (double)t.getStamp());
                  statement.setInt(9, 1);
                  statement.setInt(10, player.getClassIndex());
                  statement.setInt(11, ++buff_index);
                  statement.execute();
               }
            }
         }

         statement.close();
      } catch (Exception var25) {
         _log.log(Level.WARNING, "Could not store char effect data: ", (Throwable)var25);
      }
   }

   public static CharacterSkillSaveDAO getInstance() {
      return _instance;
   }
}
