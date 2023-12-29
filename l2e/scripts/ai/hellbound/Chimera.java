package l2e.scripts.ai.hellbound;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;

public class Chimera extends Fighter {
   public Chimera(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSeeSpell(Skill skill, Creature caster) {
      if (skill.getId() == 2359) {
         Attackable actor = this.getActiveChar();
         if (actor.isDead() || !(actor.getCurrentHpPercents() > 10.0)) {
            if (HellboundManager.getInstance().getLevel() == 7) {
               HellboundManager.getInstance().updateTrust(3, true);
            }

            switch(actor.getId()) {
               case 22349:
               case 22350:
               case 22351:
               case 22352:
                  if (Rnd.chance(70)) {
                     if (Rnd.chance(30)) {
                        actor.dropItem(caster.getActingPlayer(), 9681, 1L);
                     } else {
                        actor.dropItem(caster.getActingPlayer(), 9680, 1L);
                     }
                  }
                  break;
               case 22353:
                  actor.dropItem(caster.getActingPlayer(), 9682, 1L);
            }

            actor.doDie(null);
            actor.endDecayTask();
         }
      }
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      if (attacker != null && HellboundManager.getInstance().getLevel() < 7) {
         attacker.teleToLocation(-11272, 236464, -3248, true);
      } else {
         super.onEvtAttacked(attacker, damage);
      }
   }
}
