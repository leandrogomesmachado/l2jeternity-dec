package l2e.scripts.ai;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class Gamlin extends Fighter {
   public Gamlin(Attackable actor) {
      super(actor);
   }

   @Override
   public boolean checkAggression(Creature killer) {
      if (!super.checkAggression(killer)) {
         return false;
      } else {
         if (this.getActiveChar().isScriptValue(0)) {
            this.getActiveChar().setScriptValue(1);
            this.getActiveChar()
               .broadcastPacket(
                  new NpcSay(this.getActiveChar().getObjectId(), 23, this.getActiveChar().getId(), NpcStringId.OH_GIANTS_AN_INTRUDER_HAS_BEEN_DISCOVERED),
                  2000
               );

            for(Npc npc : World.getInstance().getAroundNpc(this.getActiveChar(), 800, 200)) {
               if (npc.isMonster() && !npc.isDead()) {
                  npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, Integer.valueOf(5000));
               }
            }
         }

         return true;
      }
   }
}
