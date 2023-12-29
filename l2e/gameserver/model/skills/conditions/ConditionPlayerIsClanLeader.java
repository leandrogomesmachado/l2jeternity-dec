package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionPlayerIsClanLeader extends Condition {
   private final boolean _val;

   public ConditionPlayerIsClanLeader(boolean val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() == null) {
         return false;
      } else {
         return env.getPlayer().isClanLeader() == this._val;
      }
   }
}
