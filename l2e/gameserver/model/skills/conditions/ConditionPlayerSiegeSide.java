package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionPlayerSiegeSide extends Condition {
   private final int _siegeSide;

   public ConditionPlayerSiegeSide(int side) {
      this._siegeSide = side;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() == null) {
         return false;
      } else {
         return env.getPlayer().getSiegeSide() == this._siegeSide;
      }
   }
}
