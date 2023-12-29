package l2e.scripts.ai.events.monsters_rush;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;

public class EventMonster extends Fighter {
   public EventMonster(Attackable actor) {
      super(actor);
   }

   @Override
   protected boolean thinkActive() {
      for(Npc npc : World.getInstance().getAroundNpc(this.getActiveChar())) {
         if (npc != null && npc.getId() == 53006) {
            this.getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, npc, Integer.valueOf(100));
            this.getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, npc, Integer.valueOf(100));
         }
      }

      return true;
   }

   @Override
   protected boolean checkAggression(Creature target) {
      if (target.isPlayable()) {
         return false;
      } else {
         for(Npc npc : World.getInstance().getAroundNpc(this.getActiveChar())) {
            if (npc != null && npc.getId() == 53006) {
               this.getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, npc, Integer.valueOf(300));
            }
         }

         return super.checkAggression(target);
      }
   }
}
