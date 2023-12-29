package l2e.gameserver.model.actor.templates.npc.champion;

public class ChampionRewardItem {
   private final int _itemId;
   private final int _minCount;
   private final int _maxCount;
   private final int _dropChance;

   public ChampionRewardItem(int itemId, int minCount, int maxCount, int dropChance) {
      this._itemId = itemId;
      this._minCount = minCount;
      this._maxCount = maxCount;
      this._dropChance = dropChance;
   }

   public int getItemId() {
      return this._itemId;
   }

   public int getMinCount() {
      return this._minCount;
   }

   public int getMaxCount() {
      return this._maxCount;
   }

   public int getDropChance() {
      return this._dropChance;
   }
}
