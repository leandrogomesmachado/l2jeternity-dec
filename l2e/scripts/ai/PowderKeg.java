package l2e.scripts.ai;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;

public class PowderKeg extends Fighter {
   public PowderKeg(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && actor != null) {
         actor.setTarget(actor);
         actor.doCast(SkillsParser.getInstance().getInfo(5714, 1));
         actor.doDie(null);
         super.onEvtAttacked(attacker, damage);
      }
   }
}
