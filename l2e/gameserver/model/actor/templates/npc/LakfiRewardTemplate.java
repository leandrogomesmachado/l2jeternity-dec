package l2e.gameserver.model.actor.templates.npc;

public class LakfiRewardTemplate {
   private final int _itemId;
   private final long _minCount;
   private final long _maxCount;
   private final double _chance;

   public LakfiRewardTemplate(int itemId, long minCount, long maxCount, double chance) {
      this._itemId = itemId;
      this._minCount = minCount;
      this._maxCount = maxCount;
      this._chance = chance;
   }

   public int getId() {
      return this._itemId;
   }

   public long getMinCount() {
      return this._minCount;
   }

   public long getMaxCount() {
      return this._maxCount;
   }

   public double getChance() {
      return this._chance;
   }
}
