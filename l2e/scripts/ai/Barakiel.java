package l2e.scripts.ai;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;

public class Barakiel extends Fighter {
   public Barakiel(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && actor.getX() < 89800 || actor.getX() > 93200 || actor.getY() < -87038) {
         actor.teleToLocation(91008, -85904, -2736, true);
         actor.getStatus().setCurrentHp(actor.getMaxHp());
      }

      super.onEvtAttacked(attacker, damage);
   }
}
