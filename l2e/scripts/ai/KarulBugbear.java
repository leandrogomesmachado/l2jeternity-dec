package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class KarulBugbear extends Fighter {
   public KarulBugbear(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null) {
         if (actor.isScriptValue(0)) {
            actor.setScriptValue(1);
            if (Rnd.chance(25)) {
               actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.YOUR_REAR_IS_PRACTICALLY_UNGUARDED), 2000);
            }
         } else if (Rnd.chance(10)) {
            actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.S1_WATCH_YOUR_BACK), 2000);
         }
      }

      super.onEvtAttacked(attacker, damage);
   }
}
