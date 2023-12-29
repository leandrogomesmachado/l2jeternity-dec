package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;

public class ConditionPlayerCallPc extends Condition {
   private final boolean _val;

   public ConditionPlayerCallPc(boolean val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      boolean canCallPlayer = true;
      Player player = env.getPlayer();
      if (player == null) {
         canCallPlayer = false;
      } else if (player.isInOlympiadMode()) {
         player.sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
         canCallPlayer = false;
      } else if (player.inObserverMode()) {
         canCallPlayer = false;
      } else if (player.getFightEvent() != null && !player.getFightEvent().canUseEscape(player)) {
         player.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
         canCallPlayer = false;
      } else if (!AerialCleftEvent.getInstance().onEscapeUse(player.getObjectId())) {
         player.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
         canCallPlayer = false;
      } else if (player.isInsideZone(ZoneId.NO_SUMMON_FRIEND) || player.isInsideZone(ZoneId.JAIL) || player.isFlyingMounted()) {
         player.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
         canCallPlayer = false;
      }

      return this._val == canCallPlayer;
   }
}
