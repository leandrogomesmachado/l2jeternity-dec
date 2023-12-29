package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Mystic;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;

public class HotSpringsMob extends Mystic {
   public HotSpringsMob(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null) {
         if (Rnd.chance(5)) {
            this.chekDebuff(actor, attacker, 4554);
         }

         if (Rnd.chance(5) && (actor.getId() == 21317 || actor.getId() == 21322)) {
            this.chekDebuff(actor, attacker, 4553);
         }

         if (Rnd.chance(5) && (actor.getId() == 21316 || actor.getId() == 21319)) {
            this.chekDebuff(actor, attacker, 4552);
         }

         if (Rnd.chance(5) && (actor.getId() == 21314 || actor.getId() == 21321)) {
            this.chekDebuff(actor, attacker, 4551);
         }
      }

      super.onEvtAttacked(attacker, damage);
   }

   protected void chekDebuff(Attackable actor, Creature attacker, int debuff) {
      Effect effect = attacker.getFirstEffect(debuff);
      if (effect != null) {
         int level = effect.getSkill().getLevel();
         if (level < 10) {
            effect.exit();
            Skill skill = SkillsParser.getInstance().getInfo(debuff, level + 1);
            skill.getEffects(actor, attacker, false);
         }
      } else {
         Skill skill = SkillsParser.getInstance().getInfo(debuff, 1);
         if (skill != null) {
            skill.getEffects(actor, attacker, false);
         } else {
            System.out.println("Skill id " + debuff + " is null, fix it.");
         }
      }
   }
}
