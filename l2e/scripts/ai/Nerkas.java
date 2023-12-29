package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class Nerkas extends Fighter {
   public Nerkas(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      if (attacker != null && Rnd.chance(10)) {
         this.getActiveChar()
            .broadcastPacket(new NpcSay(this.getActiveChar().getObjectId(), 0, this.getActiveChar().getId(), NpcStringId._HOW_DARE_YOU_CHALLENGE_ME), 2000);
      }

      super.onEvtAttacked(attacker, damage);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      this.getActiveChar()
         .broadcastPacket(
            new NpcSay(this.getActiveChar().getObjectId(), 0, this.getActiveChar().getId(), NpcStringId.THE_POWER_OF_LORD_BELETH_RULES_THE_WHOLE_WORLD), 2000
         );
      super.onEvtDead(killer);
   }
}
