package l2e.gameserver.data.holder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.PetsParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.PetData;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;

public class SummonSkillsHolder {
   private static Logger _log = Logger.getLogger(SummonSkillsHolder.class.getName());
   private final Map<Integer, Map<Integer, SummonSkillsHolder.PetSkillLearn>> _skillTrees = new HashMap<>();

   protected SummonSkillsHolder() {
      this.load();
   }

   public void load() {
      this._skillTrees.clear();
      int count = 0;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         Statement s = con.createStatement();
         ResultSet rs = s.executeQuery("SELECT templateId, minLvl, skillId, skillLvl FROM pets_skills");
      ) {
         while(rs.next()) {
            int npcId = rs.getInt("templateId");
            Map<Integer, SummonSkillsHolder.PetSkillLearn> skillTree = this._skillTrees.get(npcId);
            if (skillTree == null) {
               skillTree = new HashMap<>();
               this._skillTrees.put(npcId, skillTree);
            }

            int id = rs.getInt("skillId");
            int lvl = rs.getInt("skillLvl");
            skillTree.put(SkillsParser.getSkillHashCode(id, lvl + 1), new SummonSkillsHolder.PetSkillLearn(id, lvl, rs.getInt("minLvl")));
            ++count;
         }
      } catch (Exception var63) {
         _log.log(Level.SEVERE, this.getClass().getSimpleName() + ": Error while creating pet skill tree: " + var63.getMessage(), (Throwable)var63);
      }

      _log.info("SummonSkills: Loaded " + count + " skills.");
   }

   public int getAvailableLevel(Player player, Summon cha, int skillId) {
      int lvl = 0;
      if (!this._skillTrees.containsKey(cha.getId())) {
         Util.handleIllegalPlayerAction(player, "" + player.getName() + " used wrong action for pet id " + cha.getId() + " does not have any skills assigned.");
         if (Config.DEBUG) {
            _log.warning(this.getClass().getSimpleName() + ": Pet id " + cha.getId() + " does not have any skills assigned.");
         }

         return lvl;
      } else {
         for(SummonSkillsHolder.PetSkillLearn temp : this._skillTrees.get(cha.getId()).values()) {
            if (temp.getId() == skillId) {
               if (temp.getLevel() == 0) {
                  if (cha.getLevel() < 70) {
                     lvl = cha.getLevel() / 10;
                     if (lvl <= 0) {
                        lvl = 1;
                     }
                  } else {
                     lvl = 7 + (cha.getLevel() - 70) / 5;
                  }

                  int maxLvl = SkillsParser.getInstance().getMaxLevel(temp.getId());
                  if (lvl > maxLvl) {
                     lvl = maxLvl;
                  }
                  break;
               }

               if (temp.getMinLevel() <= cha.getLevel() && temp.getLevel() > lvl) {
                  lvl = temp.getLevel();
               }
            }
         }

         return lvl;
      }
   }

   public List<Integer> getAvailableSkills(Summon cha) {
      List<Integer> skillIds = new ArrayList<>();
      if (cha != null && cha.isPet()) {
         for(PetData.L2PetSkillLearn temp : PetsParser.getInstance().getPetData(cha.getId()).getAvailableSkills()) {
            if (temp != null && !skillIds.contains(temp.getId())) {
               skillIds.add(temp.getId());
            }
         }
      } else {
         if (!this._skillTrees.containsKey(cha.getId())) {
            _log.warning(this.getClass().getSimpleName() + ": Pet id " + cha.getId() + " does not have any skills assigned.");
            return skillIds;
         }

         for(SummonSkillsHolder.PetSkillLearn temp : this._skillTrees.get(cha.getId()).values()) {
            if (!skillIds.contains(temp.getId())) {
               skillIds.add(temp.getId());
            }
         }
      }

      return skillIds;
   }

   public static SummonSkillsHolder getInstance() {
      return SummonSkillsHolder.SingletonHolder._instance;
   }

   public static final class PetSkillLearn {
      private final int _id;
      private final int _level;
      private final int _minLevel;

      public PetSkillLearn(int id, int lvl, int minLvl) {
         this._id = id;
         this._level = lvl;
         this._minLevel = minLvl;
      }

      public int getId() {
         return this._id;
      }

      public int getLevel() {
         return this._level;
      }

      public int getMinLevel() {
         return this._minLevel;
      }
   }

   private static class SingletonHolder {
      protected static final SummonSkillsHolder _instance = new SummonSkillsHolder();
   }
}
