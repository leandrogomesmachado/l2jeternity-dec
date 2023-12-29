package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.TerritoryWard;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.stats.Env;

public class ConditionTargetWard extends Condition {
   private final boolean _val;

   public ConditionTargetWard(boolean val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      Creature target = env.getTarget();
      boolean canCast = true;
      if (TerritoryWarManager.getInstance().getHQForClan(env.getPlayer().getClan()) != target) {
         canCast = false;
      }

      TerritoryWard ward = TerritoryWarManager.getInstance().getTerritoryWard(env.getPlayer());
      if (ward == null) {
         canCast = false;
      }

      return this._val == canCast;
   }
}
