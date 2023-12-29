package l2e.gameserver.model;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.Config;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.base.SocialClass;
import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.stats.StatsSet;

public final class SkillLearn {
   private final String _skillName;
   private final int _skillId;
   private final int _skillLvl;
   private final int _getLevel;
   private final boolean _autoGet;
   private final int _levelUpSp;
   private final List<ItemHolder> _requiredItems = new ArrayList<>();
   private final List<Race> _races = new ArrayList<>();
   private final List<SkillHolder> _preReqSkills = new ArrayList<>();
   private SocialClass _socialClass;
   private final boolean _residenceSkill;
   private final List<Integer> _residenceIds = new ArrayList<>();
   private final List<SkillLearn.SubClassData> _subClassLvlNumber = new ArrayList<>();
   private final boolean _learnedByNpc;
   private final boolean _learnedByFS;

   public SkillLearn(StatsSet set) {
      this._skillName = set.getString("skillName");
      this._skillId = set.getInteger("skillId");
      this._skillLvl = set.getInteger("skillLvl");
      this._getLevel = set.getInteger("getLevel");
      this._autoGet = set.getBool("autoGet", false);
      this._levelUpSp = set.getInteger("levelUpSp", 0);
      this._residenceSkill = set.getBool("residenceSkill", false);
      this._learnedByNpc = set.getBool("learnedByNpc", false);
      this._learnedByFS = set.getBool("learnedByFS", false);
   }

   public String getName() {
      return this._skillName;
   }

   public int getId() {
      return this._skillId;
   }

   public int getLvl() {
      return this._skillLvl;
   }

   public int getGetLevel() {
      return this._getLevel;
   }

   public int getLevelUpSp() {
      return this._levelUpSp;
   }

   public boolean isAutoGet() {
      return this._autoGet;
   }

   public List<ItemHolder> getRequiredItems() {
      return this._requiredItems;
   }

   public void addRequiredItem(ItemHolder item) {
      this._requiredItems.add(item);
   }

   public List<Race> getRaces() {
      return this._races;
   }

   public void addRace(Race race) {
      this._races.add(race);
   }

   public List<SkillHolder> getPreReqSkills() {
      return this._preReqSkills;
   }

   public void addPreReqSkill(SkillHolder skill) {
      this._preReqSkills.add(skill);
   }

   public SocialClass getSocialClass() {
      return this._socialClass;
   }

   public void setSocialClass(SocialClass socialClass) {
      if (this._socialClass == null) {
         this._socialClass = socialClass;
      }
   }

   public boolean isResidencialSkill() {
      return this._residenceSkill;
   }

   public List<Integer> getResidenceIds() {
      return this._residenceIds;
   }

   public void addResidenceId(Integer id) {
      this._residenceIds.add(id);
   }

   public List<SkillLearn.SubClassData> getSubClassConditions() {
      return this._subClassLvlNumber;
   }

   public void addSubclassConditions(int slot, int lvl) {
      this._subClassLvlNumber.add(new SkillLearn.SubClassData(slot, lvl));
   }

   public boolean isLearnedByNpc() {
      return this._learnedByNpc;
   }

   public boolean isLearnedByFS() {
      return this._learnedByFS;
   }

   public int getCalculatedLevelUpSp(ClassId playerClass, ClassId learningClass) {
      if (playerClass != null && learningClass != null) {
         int levelUpSp = this._levelUpSp;
         if (Config.ALT_GAME_SKILL_LEARN && playerClass != learningClass) {
            if (playerClass.isMage() != learningClass.isMage()) {
               levelUpSp *= 3;
            } else {
               levelUpSp *= 2;
            }
         }

         return levelUpSp;
      } else {
         return this._levelUpSp;
      }
   }

   public class SubClassData {
      private final int slot;
      private final int lvl;

      public SubClassData(int pSlot, int pLvl) {
         this.slot = pSlot;
         this.lvl = pLvl;
      }

      public int getSlot() {
         return this.slot;
      }

      public int getLvl() {
         return this.lvl;
      }
   }
}
