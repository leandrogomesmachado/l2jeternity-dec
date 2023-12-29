package l2e.scripts.ai.sevensigns;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Mystic;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class LilimServantMage extends Mystic {
   public LilimServantMage(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && Rnd.chance(30) && actor.isScriptValue(0)) {
         actor.setScriptValue(1);
         actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.WHO_DARES_ENTER_THIS_PLACE), 2000);
      }

      super.onEvtAttacked(attacker, damage);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      if (Rnd.chance(30)) {
         this.getActiveChar()
            .broadcastPacket(
               new NpcSay(
                  this.getActiveChar().getObjectId(), 0, this.getActiveChar().getId(), NpcStringId.LORD_SHILEN_SOME_DAY_YOU_WILL_ACCOMPLISH_THIS_MISSION
               ),
               2000
            );
      }

      super.onEvtDead(killer);
   }
}
