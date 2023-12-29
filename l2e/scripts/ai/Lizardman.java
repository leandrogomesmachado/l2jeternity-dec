package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class Lizardman extends Fighter {
   public Lizardman(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      if (attacker != null && Rnd.chance(10)) {
         this.getActiveChar()
            .broadcastPacket(new NpcSay(this.getActiveChar().getObjectId(), 0, this.getActiveChar().getId(), NpcStringId.THE_SACRED_FLAME_IS_OURS), 2000);
      }

      super.onEvtAttacked(attacker, damage);
   }
}
