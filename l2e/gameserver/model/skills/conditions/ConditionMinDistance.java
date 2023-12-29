package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionMinDistance extends Condition {
   private final int _sqDistance;

   public ConditionMinDistance(int sqDistance) {
      this._sqDistance = sqDistance;
   }

   @Override
   public boolean testImpl(Env env) {
      return env.getTarget() != null && env.getCharacter().getDistanceSq(env.getTarget()) >= (double)this._sqDistance;
   }
}
