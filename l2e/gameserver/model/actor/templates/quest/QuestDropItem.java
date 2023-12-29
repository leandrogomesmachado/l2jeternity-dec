package l2e.gameserver.model.actor.templates.quest;

public class QuestDropItem {
   private final int _itemId;
   private final double _rate;
   private final long _minCount;
   private final long _maxCount;
   private final double _chance;
   private final boolean _rateable;

   public QuestDropItem(int itemId, double rate, long minCount, long maxCount, double chance, boolean rateable) {
      this._itemId = itemId;
      this._rate = rate;
      this._minCount = minCount;
      this._maxCount = maxCount;
      this._chance = chance;
      this._rateable = rateable;
   }

   public int getId() {
      return this._itemId;
   }

   public double getRate() {
      return this._rate;
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

   public boolean isRateable() {
      return this._rateable;
   }
}
