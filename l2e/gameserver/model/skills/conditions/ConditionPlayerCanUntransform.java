package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;

public class ConditionPlayerCanUntransform extends Condition {
   private final boolean _val;

   public ConditionPlayerCanUntransform(boolean val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      boolean canUntransform = true;
      Player player = env.getPlayer();
      if (player == null) {
         canUntransform = false;
      } else if (player.isAlikeDead() || player.isCursedWeaponEquipped()) {
         canUntransform = false;
      } else if ((player.isTransformed() || player.isInStance()) && player.isFlyingMounted() && !player.isInsideZone(ZoneId.LANDING)) {
         player.sendPacket(SystemMessageId.TOO_HIGH_TO_PERFORM_THIS_ACTION);
         canUntransform = false;
      }

      return this._val == canUntransform;
   }
}
