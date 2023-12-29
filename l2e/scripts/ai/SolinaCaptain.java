package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class SolinaCaptain extends Fighter {
   public SolinaCaptain(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && Rnd.get(100) < 20 && actor.getCurrentHp() < actor.getMaxHp() * 0.5 && actor.isScriptValue(0)) {
         actor.setScriptValue(1);
         actor.broadcastPacket(new NpcSay(actor.getObjectId(), 22, actor.getId(), NpcStringId.FOR_THE_GLORY_OF_SOLINA), 2000);
      }

      super.onEvtAttacked(attacker, damage);
   }
}
