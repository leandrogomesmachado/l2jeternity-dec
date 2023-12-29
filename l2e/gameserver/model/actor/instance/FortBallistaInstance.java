package l2e.gameserver.model.actor.instance;

import l2e.gameserver.Config;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.network.SystemMessageId;

public class FortBallistaInstance extends Npc {
   public FortBallistaInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.FortBallistaInstance);
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      return true;
   }

   @Override
   public boolean canBeAttacked() {
      return false;
   }

   @Override
   protected void onDeath(Creature killer) {
      if (this.getFort().getSiege().getIsInProgress() && killer != null && killer.isPlayer()) {
         Player player = (Player)killer;
         if (player.getClan() != null && player.getClan().getLevel() >= 5) {
            player.getClan().addReputationScore(Config.BALLISTA_POINTS, true);
            player.sendPacket(SystemMessageId.BALLISTA_DESTROYED_CLAN_REPU_INCREASED);
         }
      }

      super.onDeath(killer);
   }

   @Override
   public void onAction(Player player, boolean interact) {
      if (this.canTarget(player)) {
         if (this != player.getTarget()) {
            player.setTarget(this);
         } else if (interact) {
            if (this.isAutoAttackable(player) && !this.isAlikeDead() && Math.abs(player.getZ() - this.getZ()) < 600) {
               player.getAI().setIntention(CtrlIntention.ATTACK, this);
            }

            if (!this.canInteract(player)) {
               player.getAI().setIntention(CtrlIntention.INTERACT, this);
            }
         }

         player.sendActionFailed();
      }
   }

   @Override
   public boolean hasRandomAnimation() {
      return false;
   }
}
