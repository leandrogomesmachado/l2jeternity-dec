package l2e.scripts.ai.hellbound;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.instance.DoorInstance;

public class OutpostCaptain extends Fighter {
   public OutpostCaptain(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && attacker.getActingPlayer() != null) {
         for(Npc minion : World.getInstance().getAroundNpc(actor, 3000, 200)) {
            if (minion.getId() == 22358 || minion.getId() == 22357) {
               minion.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(5000));
            }
         }
      }
   }

   @Override
   protected void onEvtDead(Creature killer) {
      if (HellboundManager.getInstance().getLevel() == 8) {
         HellboundManager.getInstance().setLevel(9);
      }

      super.onEvtDead(killer);
   }

   @Override
   protected void onEvtSpawn() {
      Attackable actor = this.getActiveChar();
      actor.setIsNoRndWalk(true);
      DoorInstance door = DoorParser.getInstance().getDoor(20250001);
      if (door != null) {
         door.closeMe();
      }

      super.onEvtSpawn();
   }
}
