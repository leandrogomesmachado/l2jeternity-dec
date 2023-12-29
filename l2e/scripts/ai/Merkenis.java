package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Mystic;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class Merkenis extends Mystic {
   public Merkenis(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      if (attacker != null && Rnd.chance(10)) {
         this.getActiveChar()
            .broadcastPacket(
               new NpcSay(this.getActiveChar().getObjectId(), 0, this.getActiveChar().getId(), NpcStringId.ILL_CAST_YOU_INTO_AN_ETERNAL_NIGHTMARE), 2000
            );
      }

      super.onEvtAttacked(attacker, damage);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      this.getActiveChar()
         .broadcastPacket(new NpcSay(this.getActiveChar().getObjectId(), 0, this.getActiveChar().getId(), NpcStringId.SEND_MY_SOUL_TO_LICH_KING_ICARUS), 2000);
      super.onEvtDead(killer);
   }
}
