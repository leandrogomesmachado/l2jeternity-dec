package l2e.gameserver.model.actor;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public abstract class Tower extends Npc {
   public Tower(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setIsInvul(false);
   }

   @Override
   public boolean canBeAttacked() {
      return this.getCastle() != null && this.getCastle().getId() > 0 && this.getCastle().getSiege().getIsInProgress();
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      return attacker != null
         && attacker.isPlayer()
         && this.getCastle() != null
         && this.getCastle().getId() > 0
         && this.getCastle().getSiege().getIsInProgress()
         && this.getCastle().getSiege().checkIsAttacker(((Player)attacker).getClan());
   }

   @Override
   public void onAction(Player player, boolean interact) {
      if (this.canTarget(player)) {
         if (this != player.getTarget()) {
            player.setTarget(this);
         } else if (interact && this.isAutoAttackable(player) && Math.abs(player.getZ() - this.getZ()) < 100 && GeoEngine.canSeeTarget(player, this, false)) {
            player.getAI().setIntention(CtrlIntention.ATTACK, this);
         }

         player.sendActionFailed();
      }
   }

   @Override
   public void onForcedAttack(Player player) {
      this.onAction(player);
   }
}
