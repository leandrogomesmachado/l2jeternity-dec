package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.instancemanager.EpicBossManager;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.stats.Env;

public class ConditionPlayerCanEscape extends Condition {
   private final boolean _val;

   public ConditionPlayerCanEscape(boolean val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      boolean canTeleport = true;
      Player player = env.getPlayer();
      if (player == null) {
         canTeleport = false;
      } else if (player.getFightEvent() != null && !player.getFightEvent().canUseEscape(player)) {
         canTeleport = false;
      } else if (!AerialCleftEvent.getInstance().onEscapeUse(player.getObjectId())) {
         canTeleport = false;
      } else if (player.isInDuel()) {
         canTeleport = false;
      } else if (player.isAfraid()) {
         canTeleport = false;
      } else if (player.isCombatFlagEquipped()) {
         canTeleport = false;
      } else if (player.isFlying() || player.isFlyingMounted()) {
         canTeleport = false;
      } else if (player.isInOlympiadMode()) {
         canTeleport = false;
      } else if (EpicBossManager.getInstance().getZone(player) != null && !EpicBossManager.getInstance().getZone(player).isCanTeleport()) {
         canTeleport = false;
      } else if (EpicBossManager.getInstance().getZone(player) != null && player.isGM() && player.canOverrideCond(PcCondOverride.SKILL_CONDITIONS)) {
         canTeleport = true;
      }

      return this._val == canTeleport;
   }
}
