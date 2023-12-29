package l2e.gameserver.model.actor.instance;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.events.model.impl.BaseCaptureEvent;
import l2e.gameserver.model.skills.Skill;

public class BaseToCaptureInstance extends Npc {
   public BaseToCaptureInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
   }

   public boolean canAttack(Player player) {
      return player.isInFightEvent() && player.getFightEvent() instanceof BaseCaptureEvent;
   }

   @Override
   public void onAction(Player player, boolean interact) {
      if (this.canTarget(player)) {
         if (this != player.getTarget()) {
            player.setTarget(this);
         } else if (interact && !this.isAlikeDead() && Math.abs(player.getZ() - this.getZ()) < 400) {
            player.getAI().setIntention(CtrlIntention.ATTACK, this);
         }

         player.sendActionFailed();
      }
   }

   @Override
   public void onForcedAttack(Player player) {
      this.onAction(player);
   }

   @Override
   public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, Skill skill) {
      Player player = null;
      if (attacker.isPlayer()) {
         player = (Player)attacker;
      } else {
         if (!(attacker instanceof Summon)) {
            return;
         }

         player = ((Summon)attacker).getOwner();
      }

      if (this.canAttack(player)) {
         if (damage < this.getStatus().getCurrentHp()) {
            this.getStatus().setCurrentHp(this.getStatus().getCurrentHp() - damage);
         } else {
            this.doDie(attacker);
         }
      }
   }

   @Override
   protected void onDeath(Creature killer) {
      Player player = null;
      if (killer.isPlayer()) {
         player = (Player)killer;
      } else {
         if (!(killer instanceof Summon)) {
            return;
         }

         player = ((Summon)killer).getOwner();
      }

      ((BaseCaptureEvent)player.getFightEvent()).destroyBase(player, this);
      super.onDeath(killer);
   }
}
