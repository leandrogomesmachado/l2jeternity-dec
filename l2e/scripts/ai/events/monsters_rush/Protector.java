package l2e.scripts.ai.events.monsters_rush;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import org.apache.commons.lang.ArrayUtils;

public class Protector extends Fighter {
   private static final int[] EVENT_MOBS = new int[]{53007, 53008, 53009, 53010, 53011, 53012, 53013, 53014};

   public Protector(Attackable actor) {
      super(actor);
   }

   @Override
   protected boolean thinkActive() {
      for(Npc npc : World.getInstance().getAroundNpc(this.getActiveChar())) {
         if (npc != null && ArrayUtils.contains(EVENT_MOBS, npc.getId())) {
            this.getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, npc, Integer.valueOf(3000));
            this.getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, npc, Integer.valueOf(300));
         }
      }

      return true;
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
         for(Npc npc : World.getInstance().getAroundNpc(this.getActiveChar())) {
            if (npc != null && ArrayUtils.contains(EVENT_MOBS, npc.getId())) {
               this.getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, npc, Integer.valueOf(3000));
            }
         }

         return super.checkAggression(target);
      }
   }
}
