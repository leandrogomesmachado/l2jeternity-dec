package l2e.gameserver.model.actor.templates.quest;

import java.util.List;
import java.util.Map;
import l2e.gameserver.model.stats.StatsSet;

public class QuestTemplate {
   private final int _id;
   private final String _nameEn;
   private final String _nameRu;
   private final int _minLvl;
   private final int _maxLvl;
   private final QuestExperience _experience;
   private final List<QuestRewardItem> _rewards;
   private final Map<Integer, List<QuestRewardItem>> _variantRewards;
   private final Map<Integer, List<QuestDropItem>> _dropList;
   private final boolean _rateable;
   private final StatsSet _params;

   public QuestTemplate(
      int id,
      String nameEn,
      String nameRu,
      int minLvl,
      int maxLvl,
      Map<Integer, List<QuestDropItem>> dropList,
      QuestExperience experience,
      List<QuestRewardItem> rewards,
      Map<Integer, List<QuestRewardItem>> variantRewards,
      boolean rateable,
      StatsSet params
   ) {
      this._id = id;
      this._nameEn = nameEn;
      this._nameRu = nameRu;
      this._minLvl = minLvl;
      this._maxLvl = maxLvl;
      this._dropList = dropList;
      this._experience = experience;
      this._rewards = rewards;
      this._variantRewards = variantRewards;
      this._rateable = rateable;
      this._params = params;
   }

   public int getId() {
      return this._id;
   }

   public String getName(String lang) {
      return lang != null && !lang.equalsIgnoreCase("en") ? this._nameRu : this._nameEn;
   }

   public int getMinLvl() {
      return this._minLvl;
   }

   public int getMaxLvl() {
      return this._maxLvl;
   }

   public Map<Integer, List<QuestDropItem>> getDropList() {
      return this._dropList;
   }

   public QuestExperience getExperienceRewards() {
      return this._experience;
   }

   public List<QuestRewardItem> getRewards() {
      return this._rewards;
   }

   public Map<Integer, List<QuestRewardItem>> getVariantRewards() {
      return this._variantRewards;
   }

   public boolean isRateable() {
      return this._rateable;
   }

   public StatsSet getParams() {
      return this._params;
   }
}
