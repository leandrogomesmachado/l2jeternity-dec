package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class ConditionPlayerInFightEvent extends Condition {
   private final boolean _val;

   public ConditionPlayerInFightEvent(boolean val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      boolean canUse = true;
      Player player = env.getPlayer();
      if (player == null) {
         canUse = false;
      }

      if (player.isInFightEvent()) {
         player.sendPacket(SystemMessageId.NOTHING_HAPPENED);
         canUse = false;
      }

      return this._val == canUse;
   }
}
