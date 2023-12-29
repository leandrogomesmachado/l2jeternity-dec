package l2e.scripts.ai.sevensigns;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class LilimServantFighter extends Fighter {
   public LilimServantFighter(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && Rnd.chance(30) && actor.isScriptValue(0)) {
         actor.setScriptValue(1);
         actor.broadcastPacket(
            new NpcSay(
               actor.getObjectId(),
               0,
               actor.getId(),
               Rnd.chance(50)
                  ? NpcStringId.THOSE_WHO_ARE_AFRAID_SHOULD_GET_AWAY_AND_THOSE_WHO_ARE_BRAVE_SHOULD_FIGHT
                  : NpcStringId.THIS_PLACE_ONCE_BELONGED_TO_LORD_SHILEN
            ),
            2000
         );
      }

      super.onEvtAttacked(attacker, damage);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      if (Rnd.chance(30)) {
         this.getActiveChar()
            .broadcastPacket(
               new NpcSay(
                  this.getActiveChar().getObjectId(),
                  0,
                  this.getActiveChar().getId(),
                  Rnd.chance(50) ? NpcStringId.WHY_ARE_YOU_GETTING_IN_OUR_WAY : NpcStringId.SHILEN_OUR_SHILEN
               ),
               2000
            );
      }

      super.onEvtDead(killer);
   }
}
