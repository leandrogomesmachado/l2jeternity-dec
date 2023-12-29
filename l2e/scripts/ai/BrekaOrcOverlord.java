package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class BrekaOrcOverlord extends Fighter {
   private static final NpcStringId[] SHOUT = new NpcStringId[]{
      NpcStringId.S1_SHOW_YOUR_STRENGTH,
      NpcStringId.IM_THE_STRONGEST_I_LOST_EVERYTHING_TO_WIN,
      NpcStringId.USING_A_SPECIAL_SKILL_HERE_COULD_TRIGGER_A_BLOODBATH
   };

   public BrekaOrcOverlord(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && actor.isScriptValue(0) && Rnd.chance(25)) {
         actor.setScriptValue(1);
         actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), SHOUT[Rnd.get(3)]), 2000);
      }

      super.onEvtAttacked(attacker, damage);
   }
}
