package l2e.gameserver.model.actor.instance;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.events.model.impl.CaptureTheFlagEvent;
import l2e.gameserver.model.skills.Skill;

public class FlagToCaptureInstance extends Npc {
   public FlagToCaptureInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
   }

   @Override
   public void onAction(Player player, boolean interact) {
      if (this.canTarget(player)) {
         if (!this.isInRange(player, 150L)) {
            if (player.getAI().getIntention() != CtrlIntention.INTERACT) {
               player.getAI().setIntention(CtrlIntention.INTERACT, this, null);
            }
         } else {
            if (this != player.getTarget()) {
               player.setTarget(this);
            } else if (interact) {
               if (!this.canInteract(player)) {
                  player.getAI().setIntention(CtrlIntention.INTERACT, this);
               } else if (player.isInFightEvent()) {
                  boolean shouldDissapear = false;
                  if (player.getFightEvent() instanceof CaptureTheFlagEvent) {
                     shouldDissapear = ((CaptureTheFlagEvent)player.getFightEvent()).tryToTakeFlag(player, this);
                  }

                  if (shouldDissapear) {
                     this.deleteMe();
                  }
               }
            }

            player.sendActionFailed();
         }
      }
   }

   @Override
   public void onForcedAttack(Player player) {
      this.onAction(player);
   }

   @Override
   public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, Skill skill) {
   }
}
