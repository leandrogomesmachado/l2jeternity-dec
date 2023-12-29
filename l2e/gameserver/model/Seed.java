package l2e.gameserver.model;

import l2e.gameserver.Config;
import l2e.gameserver.model.stats.StatsSet;

public class Seed {
   private final int _seedId;
   private final int _cropId;
   private final int _level;
   private final int _matureId;
   private final int _reward1;
   private final int _reward2;
   private final int _castleId;
   private final boolean _isAlternative;
   private final int _limitSeeds;
   private final int _limitCrops;

   public Seed(StatsSet set) {
      this._cropId = set.getInteger("id");
      this._seedId = set.getInteger("seedId");
      this._level = set.getInteger("level");
      this._matureId = set.getInteger("mature_Id");
      this._reward1 = set.getInteger("reward1");
      this._reward2 = set.getInteger("reward2");
      this._castleId = set.getInteger("castleId");
      this._isAlternative = set.getBool("alternative");
      this._limitCrops = set.getInteger("limit_crops");
      this._limitSeeds = set.getInteger("limit_seed");
   }

   public int getCastleId() {
      return this._castleId;
   }

   public int getSeedId() {
      return this._seedId;
   }

   public int getCropId() {
      return this._cropId;
   }

   public int getMatureId() {
      return this._matureId;
   }

   public int getReward(int type) {
      return type == 1 ? this._reward1 : this._reward2;
   }

   public int getLevel() {
      return this._level;
   }

   public boolean isAlternative() {
      return this._isAlternative;
   }

   public int getSeedLimit() {
      return (int)((double)this._limitSeeds * Config.RATE_DROP_MANOR);
   }

   public int getCropLimit() {
      return (int)((double)this._limitCrops * Config.RATE_DROP_MANOR);
   }

   @Override
   public String toString() {
      return "SeedData [_id="
         + this._seedId
         + ", _level="
         + this._level
         + ", _crop="
         + this._cropId
         + ", _mature="
         + this._matureId
         + ", _type1="
         + this._reward1
         + ", _type2="
         + this._reward2
         + ", _manorId="
         + this._castleId
         + ", _isAlternative="
         + this._isAlternative
         + ", _limitSeeds="
         + this._limitSeeds
         + ", _limitCrops="
         + this._limitCrops
         + "]";
   }
}
