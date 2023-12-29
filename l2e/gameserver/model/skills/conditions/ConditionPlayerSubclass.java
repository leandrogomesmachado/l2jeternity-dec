package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionPlayerSubclass extends Condition {
   private final boolean _val;

   public ConditionPlayerSubclass(boolean val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() == null) {
         return true;
      } else {
         return env.getPlayer().isSubClassActive() == this._val;
      }
   }
}
