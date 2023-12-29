package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.stats.Env;

public final class ConditionPeaceZone extends Condition {
   protected final boolean _self;

   public ConditionPeaceZone(boolean self) {
      this._self = self;
   }

   @Override
   public boolean testImpl(Env env) {
      Creature target = this._self ? env.getCharacter() : env.getTarget();
      return target != null && target.isPlayer() && !target.isInZonePeace();
   }
}
