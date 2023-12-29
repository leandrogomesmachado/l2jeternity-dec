package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionPlayerPkCount extends Condition {
   public final int _pk;

   public ConditionPlayerPkCount(int pk) {
      this._pk = pk;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() == null) {
         return false;
      } else {
         return env.getPlayer().getPkKills() <= this._pk;
      }
   }
}
