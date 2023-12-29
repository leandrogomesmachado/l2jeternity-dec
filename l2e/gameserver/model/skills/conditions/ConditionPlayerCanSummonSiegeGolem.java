package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.SevenSigns;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class ConditionPlayerCanSummonSiegeGolem extends Condition {
   private final boolean _val;

   public ConditionPlayerCanSummonSiegeGolem(boolean val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      boolean canSummonSiegeGolem = true;
      if (env.getPlayer() == null || env.getPlayer().isAlikeDead() || env.getPlayer().isCursedWeaponEquipped() || env.getPlayer().getClan() == null) {
         canSummonSiegeGolem = false;
      }

      Castle castle = CastleManager.getInstance().getCastle(env.getPlayer());
      Fort fort = FortManager.getInstance().getFort(env.getPlayer());
      if (castle == null && fort == null) {
         canSummonSiegeGolem = false;
      }

      Player player = env.getPlayer().getActingPlayer();
      if ((fort == null || fort.getId() != 0) && (castle == null || castle.getId() != 0)) {
         if ((castle == null || castle.getSiege().getIsInProgress()) && (fort == null || fort.getSiege().getIsInProgress())) {
            if (player.getClanId() == 0
               || (castle == null || castle.getSiege().getAttackerClan(player.getClanId()) != null)
                  && (fort == null || fort.getSiege().getAttackerClan(player.getClanId()) != null)) {
               if (SevenSigns.getInstance().checkSummonConditions(env.getPlayer())) {
                  canSummonSiegeGolem = false;
               }
            } else {
               player.sendPacket(SystemMessageId.INCORRECT_TARGET);
               canSummonSiegeGolem = false;
            }
         } else {
            player.sendPacket(SystemMessageId.INCORRECT_TARGET);
            canSummonSiegeGolem = false;
         }
      } else {
         player.sendPacket(SystemMessageId.INCORRECT_TARGET);
         canSummonSiegeGolem = false;
      }

      return this._val == canSummonSiegeGolem;
   }
}
