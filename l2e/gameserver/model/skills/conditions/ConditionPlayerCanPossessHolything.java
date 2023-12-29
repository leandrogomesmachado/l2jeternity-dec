package l2e.gameserver.model.skills.conditions;

import l2e.commons.util.Util;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class ConditionPlayerCanPossessHolything extends Condition {
   private final boolean _val;

   public ConditionPlayerCanPossessHolything(boolean val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      boolean canPossessHolything = true;
      if (env.getPlayer() != null && !env.getPlayer().isAlikeDead() && !env.getPlayer().isCursedWeaponEquipped()) {
         if (env.getPlayer().getClan() == null || env.getPlayer().getClan().getLeaderId() != env.getPlayer().getObjectId()) {
            canPossessHolything = false;
         }
      } else {
         canPossessHolything = false;
      }

      Castle castle = CastleManager.getInstance().getCastle(env.getPlayer());
      if (castle == null
         || castle.getId() <= 0
         || !castle.getSiege().getIsInProgress()
         || castle.getSiege().getAttackerClan(env.getPlayer().getClan()) == null) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
         sm.addSkillName(env.getSkill());
         env.getPlayer().sendPacket(sm);
         canPossessHolything = false;
      } else if (!castle.getArtefacts().contains(env.getTarget())) {
         env.getPlayer().sendPacket(SystemMessageId.INCORRECT_TARGET);
         canPossessHolything = false;
      } else if (!Util.checkIfInRange(200, env.getPlayer(), env.getTarget(), true)) {
         env.getPlayer().sendPacket(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
         canPossessHolything = false;
      }

      if (canPossessHolything && castle != null) {
         castle.getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.OPPONENT_STARTED_ENGRAVING), false);
      }

      return this._val == canPossessHolything;
   }
}
