package l2e.gameserver.data.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.engines.DocumentEngine;

public class SkillsParser {
   private static Logger _log = Logger.getLogger(SkillsParser.class.getName());
   private final Map<Integer, Skill> _skills = new HashMap<>();
   private final Map<Integer, Integer> _skillMaxLevel = new HashMap<>();
   private final Set<Integer> _enchantable = new HashSet<>();

   protected SkillsParser() {
      this.load();
   }

   public void reload() {
      this.load();
      SkillTreesParser.getInstance().load();
   }

   private void load() {
      Map<Integer, Skill> temp = new HashMap<>();
      DocumentEngine.getInstance().loadAllSkills(temp);
      this._skills.clear();
      this._skills.putAll(temp);
      this._skillMaxLevel.clear();
      this._enchantable.clear();

      for(Skill skill : this._skills.values()) {
         int skillId = skill.getId();
         int skillLvl = skill.getLevel();
         if (skillLvl > 99 && !skill.isCustom()) {
            if (!this._enchantable.contains(skillId)) {
               this._enchantable.add(skillId);
            }
         } else {
            int maxLvl = this.getMaxLevel(skillId);
            if (skillLvl > maxLvl) {
               this._skillMaxLevel.put(skillId, skillLvl);
            }
         }
      }
   }

   public static int getSkillHashCode(Skill skill) {
      return getSkillHashCode(skill.getId(), skill.getLevel());
   }

   public static int getSkillHashCode(int skillId, int skillLevel) {
      return skillId * 1021 + skillLevel;
   }

   public static int getId(int skillHashCode) {
      return skillHashCode / 1021;
   }

   public static int getLvl(int skillHashCode) {
      return skillHashCode % 1021;
   }

   public final Skill getInfo(int skillId, int level) {
      Skill result = this._skills.get(getSkillHashCode(skillId, level));
      if (result != null) {
         return result;
      } else if (!this._skillMaxLevel.containsKey(skillId)) {
         _log.warning(this.getClass().getSimpleName() + ": No skill info found for skill id " + skillId + " and skill level " + level + ".");
         return null;
      } else {
         int maxLvl = this._skillMaxLevel.get(skillId);
         if (maxLvl > 0 && level > maxLvl) {
            if (Config.DEBUG) {
               _log.log(
                  Level.WARNING,
                  this.getClass().getSimpleName() + ": call to unexisting skill level id: " + skillId + " requested level: " + level + " max level: " + maxLvl,
                  new Throwable()
               );
            }

            return this._skills.get(getSkillHashCode(skillId, maxLvl));
         } else {
            return null;
         }
      }
   }

   public final int getMaxLevel(int skillId) {
      Integer maxLevel = this._skillMaxLevel.get(skillId);
      return maxLevel != null ? maxLevel : 0;
   }

   public final boolean isEnchantable(int skillId) {
      return this._enchantable.contains(skillId);
   }

   public Skill[] getSiegeSkills(boolean addNoble, boolean hasCastle) {
      Skill[] temp = new Skill[2 + (addNoble ? 1 : 0) + (hasCastle ? 2 : 0)];
      int i = 0;
      temp[i++] = this._skills.get(getSkillHashCode(246, 1));
      temp[i++] = this._skills.get(getSkillHashCode(247, 1));
      if (addNoble) {
         temp[i++] = this._skills.get(getSkillHashCode(326, 1));
      }

      if (hasCastle) {
         temp[i++] = this._skills.get(getSkillHashCode(844, 1));
         temp[i++] = this._skills.get(getSkillHashCode(845, 1));
      }

      return temp;
   }

   public static SkillsParser getInstance() {
      return SkillsParser.SingletonHolder._instance;
   }

   public static enum FrequentSkill {
      RAID_CURSE(4215, 1),
      RAID_CURSE2(4515, 1),
      SEAL_OF_RULER(246, 1),
      BUILD_HEADQUARTERS(247, 1),
      WYVERN_BREATH(4289, 1),
      STRIDER_SIEGE_ASSAULT(325, 1),
      FAKE_PETRIFICATION(4616, 1),
      FIREWORK(5965, 1),
      LARGE_FIREWORK(2025, 1),
      BLESSING_OF_PROTECTION(5182, 1),
      VOID_BURST(3630, 1),
      VOID_FLOW(3631, 1),
      THE_VICTOR_OF_WAR(5074, 1),
      THE_VANQUISHED_OF_WAR(5075, 1),
      SPECIAL_TREE_RECOVERY_BONUS(2139, 1),
      WEAPON_GRADE_PENALTY(6209, 1),
      ARMOR_GRADE_PENALTY(6213, 1);

      private final SkillHolder _holder;

      private FrequentSkill(int id, int level) {
         this._holder = new SkillHolder(id, level);
      }

      public int getId() {
         return this._holder.getId();
      }

      public int getLevel() {
         return this._holder.getLvl();
      }

      public Skill getSkill() {
         return this._holder.getSkill();
      }
   }

   private static class SingletonHolder {
      protected static final SkillsParser _instance = new SkillsParser();
   }
}
