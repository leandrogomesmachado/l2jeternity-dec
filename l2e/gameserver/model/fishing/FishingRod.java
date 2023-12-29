package l2e.gameserver.model.fishing;

import l2e.gameserver.model.stats.StatsSet;

public class FishingRod {
   private final int _fishingRodId;
   private final int _fishingRodItemId;
   private final int _fishingRodLevel;
   private final String _fishingRodName;
   private final double _fishingRodDamage;

   public FishingRod(StatsSet set) {
      this._fishingRodId = set.getInteger("fishingRodId");
      this._fishingRodItemId = set.getInteger("fishingRodItemId");
      this._fishingRodLevel = set.getInteger("fishingRodLevel");
      this._fishingRodName = set.getString("fishingRodName");
      this._fishingRodDamage = set.getDouble("fishingRodDamage");
   }

   public int getFishingRodId() {
      return this._fishingRodId;
   }

   public int getFishingRodItemId() {
      return this._fishingRodItemId;
   }

   public int getFishingRodLevel() {
      return this._fishingRodLevel;
   }

   public String getFishingRodItemName() {
      return this._fishingRodName;
   }

   public double getFishingRodDamage() {
      return this._fishingRodDamage;
   }
}
