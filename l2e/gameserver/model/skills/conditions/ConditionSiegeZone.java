package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.zone.ZoneId;

public final class ConditionSiegeZone extends Condition {
   protected final boolean _value;
   protected final boolean _self;

   public ConditionSiegeZone(boolean value, boolean self) {
      this._value = value;
      this._self = self;
   }

   @Override
   public boolean testImpl(Env env) {
      Creature target = this._self ? env.getCharacter() : env.getTarget();
      boolean isValid = true;
      if (!target.isPlayer() || !target.isInsideZone(ZoneId.SIEGE)) {
         isValid = false;
      }

      return this._value == isValid;
   }
}
