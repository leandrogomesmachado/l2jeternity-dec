package l2e.gameserver.model.skills.funcs;

import l2e.gameserver.model.stats.Env;

public final class LambdaStats extends Lambda {
   private final LambdaStats.StatsType _stat;

   public LambdaStats(LambdaStats.StatsType stat) {
      this._stat = stat;
   }

   @Override
   public double calc(Env env) {
      switch(this._stat) {
         case PLAYER_LEVEL:
            if (env.getCharacter() == null) {
               return 1.0;
            }

            return (double)env.getCharacter().getLevel();
         case CUBIC_LEVEL:
            if (env.getCubic() == null) {
               return 1.0;
            }

            return (double)env.getCubic().getOwner().getLevel();
         case TARGET_LEVEL:
            if (env.getTarget() == null) {
               return 1.0;
            }

            return (double)env.getTarget().getLevel();
         case PLAYER_MAX_HP:
            if (env.getCharacter() == null) {
               return 1.0;
            }

            return env.getCharacter().getMaxHp();
         case PLAYER_MAX_MP:
            if (env.getCharacter() == null) {
               return 1.0;
            }

            return env.getCharacter().getMaxMp();
         default:
            return 0.0;
      }
   }

   public static enum StatsType {
      PLAYER_LEVEL,
      CUBIC_LEVEL,
      TARGET_LEVEL,
      PLAYER_MAX_HP,
      PLAYER_MAX_MP;
   }
}
