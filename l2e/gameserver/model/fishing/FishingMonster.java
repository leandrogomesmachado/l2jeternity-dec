package l2e.gameserver.model.fishing;

import l2e.gameserver.model.stats.StatsSet;

public class FishingMonster {
   private final int _userMinLevel;
   private final int _userMaxLevel;
   private final int _fishingMonsterId;
   private final int _probability;

   public FishingMonster(StatsSet set) {
      this._userMinLevel = set.getInteger("userMinLevel");
      this._userMaxLevel = set.getInteger("userMaxLevel");
      this._fishingMonsterId = set.getInteger("fishingMonsterId");
      this._probability = set.getInteger("probability");
   }

   public int getUserMinLevel() {
      return this._userMinLevel;
   }

   public int getUserMaxLevel() {
      return this._userMaxLevel;
   }

   public int getFishingMonsterId() {
      return this._fishingMonsterId;
   }

   public int getProbability() {
      return this._probability;
   }
}
