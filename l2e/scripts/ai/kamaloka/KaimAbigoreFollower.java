package l2e.scripts.ai.kamaloka;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Attackable;

public class KaimAbigoreFollower extends Fighter {
   public KaimAbigoreFollower(Attackable actor) {
      super(actor);
   }

   @Override
   protected void thinkAttack() {
      Attackable actor = this.getActiveChar();
      if (actor.getDistance(this.getAttackTarget()) < 50.0) {
         actor.setTarget(this.getAttackTarget());
         actor.doCast(SkillsParser.getInstance().getInfo(4614, 6));
         actor.doDie(null);
      }

      super.thinkAttack();
   }
}
