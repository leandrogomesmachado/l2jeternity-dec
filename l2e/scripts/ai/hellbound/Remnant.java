package l2e.scripts.ai.hellbound;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;

public class Remnant extends Fighter {
   public Remnant(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSpawn() {
      this.getActiveChar().setIsMortal(false);
      super.onEvtSpawn();
   }

   @Override
   protected void onEvtSeeSpell(Skill skill, Creature caster) {
      if (skill.getId() == 2358) {
         Attackable actor = this.getActiveChar();
         if (!actor.isDead() && actor.getCurrentHpPercents() < 10.0) {
            actor.doDie(caster);
         }

         super.onEvtSeeSpell(skill, caster);
      }
   }
}
