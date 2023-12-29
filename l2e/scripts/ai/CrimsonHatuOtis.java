package l2e.scripts.ai;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class CrimsonHatuOtis extends Fighter {
   public CrimsonHatuOtis(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable npc = this.getActiveChar();
      if (attacker != null && npc.isScriptValue(0) && npc.getCurrentHp() < npc.getMaxHp() * 0.3) {
         npc.broadcastPacket(new NpcSay(npc, 22, NpcStringId.IVE_HAD_IT_UP_TO_HERE_WITH_YOU_ILL_TAKE_CARE_OF_YOU), 2000);
         npc.setScriptValue(1);
      }

      super.onEvtAttacked(attacker, damage);
   }
}
