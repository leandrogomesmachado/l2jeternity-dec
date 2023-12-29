package l2e.scripts.ai.kamaloka;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Attackable;

public class WarlordTamuzeFollower extends Fighter {
   public WarlordTamuzeFollower(Attackable actor) {
      super(actor);
   }

   @Override
   protected void thinkAttack() {
      Attackable actor = this.getActiveChar();
      if (this.getAttackTarget() != null
         && actor.getCurrentHp() < actor.getMaxHp() * 0.5
         && actor.getDistance(this.getAttackTarget()) < 80.0
         && Rnd.calcChance(20.0)) {
         actor.setTarget(this.getAttackTarget());
         actor.doCast(SkillsParser.getInstance().getInfo(4139, 6));
         actor.doDie(null);
      }

      super.thinkAttack();
   }
}
