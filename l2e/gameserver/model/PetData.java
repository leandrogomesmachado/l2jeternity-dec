package l2e.gameserver.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.templates.PetLevelTemplate;
import l2e.gameserver.model.holders.SkillHolder;

public class PetData {
   private final Map<Integer, PetLevelTemplate> _levelStats = new HashMap<>();
   private final List<PetData.L2PetSkillLearn> _skills = new ArrayList<>();
   private final int _npcId;
   private final int _itemId;
   private int _load = 20000;
   private int _hungryLimit = 1;
   private int _minlvl = 127;
   private boolean _syncLevel = false;
   private final List<Integer> _food = new ArrayList<>();

   public PetData(int npcId, int itemId) {
      this._npcId = npcId;
      this._itemId = itemId;
   }

   public int getNpcId() {
      return this._npcId;
   }

   public int getItemId() {
      return this._itemId;
   }

   public void addNewStat(int level, PetLevelTemplate data) {
      if (this._minlvl > level) {
         this._minlvl = level;
      }

      this._levelStats.put(level, data);
   }

   public PetLevelTemplate getPetLevelData(int petLevel) {
      return this._levelStats.get(petLevel);
   }

   public int getLoad() {
      return this._load;
   }

   public int getHungryLimit() {
      return this._hungryLimit;
   }

   public boolean isSynchLevel() {
      return this._syncLevel;
   }

   public int getMinLevel() {
      return this._minlvl;
   }

   public List<Integer> getFood() {
      return this._food;
   }

   public void addFood(Integer foodId) {
      this._food.add(foodId);
   }

   public void setLoad(int load) {
      this._load = load;
   }

   public void setHungryLimit(int limit) {
      this._hungryLimit = limit;
   }

   public void setSyncLevel(boolean val) {
      this._syncLevel = val;
   }

   public void addNewSkill(int skillId, int skillLvl, int petLvl, double hpPercent) {
      this._skills.add(new PetData.L2PetSkillLearn(skillId, skillLvl, petLvl, hpPercent));
   }

   public double getHpPercent(int id, int lvl) {
      for(PetData.L2PetSkillLearn temp : this._skills) {
         if (temp.getId() == id && temp.getLvl() == lvl) {
            return temp.getHpPercent();
         }
      }

      return 0.0;
   }

   public int getAvailableLevel(int skillId, int petLvl) {
      int lvl = 0;

      for(PetData.L2PetSkillLearn temp : this._skills) {
         if (temp.getId() == skillId) {
            if (temp.getLvl() == 0) {
               if (petLvl < 70) {
                  lvl = petLvl / 10;
                  if (lvl <= 0) {
                     lvl = 1;
                  }
               } else {
                  lvl = 7 + (petLvl - 70) / 5;
               }

               int maxLvl = SkillsParser.getInstance().getMaxLevel(temp.getId());
               if (lvl > maxLvl) {
                  lvl = maxLvl;
               }
               break;
            }

            if (temp.getMinLevel() <= petLvl && temp.getLvl() > lvl) {
               lvl = temp.getLvl();
            }
         }
      }

      return lvl;
   }

   public List<PetData.L2PetSkillLearn> getAvailableSkills() {
      return this._skills;
   }

   public static final class L2PetSkillLearn extends SkillHolder {
      private final int _minLevel;
      private final double _hpPercent;

      public L2PetSkillLearn(int id, int lvl, int minLvl, double hpPercent) {
         super(id, lvl);
         this._minLevel = minLvl;
         this._hpPercent = hpPercent;
      }

      public int getMinLevel() {
         return this._minLevel;
      }

      public double getHpPercent() {
         return this._hpPercent;
      }
   }
}
