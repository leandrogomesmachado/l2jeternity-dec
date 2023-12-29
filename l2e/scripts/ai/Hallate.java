package l2e.scripts.ai;

import l2e.commons.util.NpcUtils;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;

public class Hallate extends Fighter {
   public Hallate(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && actor.getZ() > -1650 || actor.getZ() < -2150) {
         actor.teleToLocation(113548, 17061, -2125, true);
         actor.getStatus().setCurrentHp(actor.getMaxHp());
      }

      super.onEvtAttacked(attacker, damage);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      NpcUtils.spawnSingleNpc(31030, Location.findAroundPosition(this.getActiveChar(), 80, 120), 60000L);
      super.onEvtDead(killer);
   }
}
