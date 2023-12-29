package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class OlMahumGeneral extends Fighter {
   public OlMahumGeneral(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null) {
         if (actor.isScriptValue(0)) {
            actor.setScriptValue(1);
            if (Rnd.chance(25)) {
               actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.WE_SHALL_SEE_ABOUT_THAT), 2000);
            }
         } else if (Rnd.chance(10)) {
            actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.I_WILL_DEFINITELY_REPAY_THIS_HUMILIATION), 2000);
         }
      }

      super.onEvtAttacked(attacker, damage);
   }
}
