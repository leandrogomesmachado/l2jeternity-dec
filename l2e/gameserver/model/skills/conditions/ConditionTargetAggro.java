package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.stats.Env;

public class ConditionTargetAggro extends Condition {
   private final boolean _isAggro;

   public ConditionTargetAggro(boolean isAggro) {
      this._isAggro = isAggro;
   }

   @Override
   public boolean testImpl(Env env) {
      Creature target = env.getTarget();
      if (target instanceof MonsterInstance) {
         return ((MonsterInstance)target).isAggressive() == this._isAggro;
      } else if (target.isPlayer()) {
         return ((Player)target).getKarma() > 0;
      } else {
         return false;
      }
   }
}
