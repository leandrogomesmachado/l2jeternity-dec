package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;

public class ConditionPlayerCombat extends Condition {
   private final boolean _val;

   public ConditionPlayerCombat(boolean val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      boolean canCallPlayer = true;
      Player player = env.getPlayer();
      if (player == null) {
         canCallPlayer = false;
      }

      if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player)) {
         player.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT);
         canCallPlayer = false;
      }

      return this._val == canCallPlayer;
   }
}
