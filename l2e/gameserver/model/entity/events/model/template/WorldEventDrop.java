package l2e.gameserver.model.entity.events.model.template;

public class WorldEventDrop {
   private final int _itemId;
   private final long _minCount;
   private final long _maxCount;
   private final double _chance;
   private final int _minLvl;
   private final int _maxLvl;

   public WorldEventDrop(int itemId, long minCount, long maxCount, double chance, int minLvl, int maxLvl) {
      this._itemId = itemId;
      this._minCount = minCount;
      this._maxCount = maxCount;
      this._chance = chance;
      this._minLvl = minLvl;
      this._maxLvl = maxLvl;
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

   public int getMinLevel() {
      return this._minLvl;
   }

   public int getMaxLevel() {
      return this._maxLvl;
   }
}
