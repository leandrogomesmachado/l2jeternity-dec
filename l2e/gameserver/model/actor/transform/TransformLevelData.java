package l2e.gameserver.model.actor.transform;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.stats.StatsSet;

public final class TransformLevelData {
   private final int _level;
   private final double _levelMod;
   private Map<Integer, Double> _stats;

   public TransformLevelData(StatsSet set) {
      this._level = set.getInteger("val");
      this._levelMod = set.getDouble("levelMod");
      this.addStats(Stats.MAX_HP, set.getDouble("hp"));
      this.addStats(Stats.MAX_MP, set.getDouble("mp"));
      this.addStats(Stats.MAX_CP, set.getDouble("cp"));
      this.addStats(Stats.REGENERATE_HP_RATE, set.getDouble("hpRegen"));
      this.addStats(Stats.REGENERATE_MP_RATE, set.getDouble("mpRegen"));
      this.addStats(Stats.REGENERATE_CP_RATE, set.getDouble("cpRegen"));
   }

   private void addStats(Stats stat, double val) {
      if (this._stats == null) {
         this._stats = new HashMap<>();
      }

      this._stats.put(stat.ordinal(), val);
   }

   public double getStats(Stats stats) {
      return this._stats != null && this._stats.containsKey(stats.ordinal()) ? this._stats.get(stats.ordinal()) : 0.0;
   }

   public int getLevel() {
      return this._level;
   }

   public double getLevelMod() {
      return this._levelMod;
   }
}
