package l2e.scripts.ai;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;

public class FairyTree extends Fighter {
   public FairyTree(Attackable actor) {
      super(actor);
      actor.setIsImmobilized(true);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && attacker.isPlayer()) {
         Skill skill = SkillsParser.getInstance().getInfo(5423, 12);
         skill.getEffects(actor, attacker, false);
      } else if (attacker.isPet()) {
         super.onEvtAttacked(attacker, damage);
      }
   }

   @Override
   protected void onEvtAggression(Creature attacker, int aggro) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && attacker.isPlayer()) {
         Skill skill = SkillsParser.getInstance().getInfo(5423, 12);
         skill.getEffects(actor, attacker, false);
      }
   }
}
