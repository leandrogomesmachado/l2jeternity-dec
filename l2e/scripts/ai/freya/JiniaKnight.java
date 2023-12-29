package l2e.scripts.ai.freya;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;

public class JiniaKnight extends Fighter {
   public JiniaKnight(Attackable actor) {
      super(actor);
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (actor.isDead()) {
         return false;
      } else {
         for(Npc npc : World.getInstance().getAroundNpc(this.getActiveChar(), 5000, 1000)) {
            if (npc.getId() == 22767) {
               actor.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, npc, Integer.valueOf(300));
            }
         }

         return true;
      }
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      if (attacker != null && !attacker.isPlayable()) {
         super.onEvtAttacked(attacker, damage);
      }
   }

   @Override
   protected boolean checkAggression(Creature target) {
      if (target.isPlayable()) {
         return false;
      } else {
         for(Npc npc : World.getInstance().getAroundNpc(this.getActiveChar(), 5000, 1000)) {
            if (npc.getId() == 22767) {
               this.getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, npc, Integer.valueOf(300));
            }
         }

         return super.checkAggression(target);
      }
   }
}
