package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class ConditionPlayerHasServitor extends Condition {
   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() == null) {
         return false;
      } else if (!env.getPlayer().hasServitor()) {
         env.getPlayer().sendPacket(SystemMessageId.CANNOT_USE_SKILL_WITHOUT_SERVITOR);
         return false;
      } else {
         return true;
      }
   }
}
