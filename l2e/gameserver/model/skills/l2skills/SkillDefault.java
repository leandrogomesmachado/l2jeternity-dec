package l2e.gameserver.model.skills.l2skills;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.StatsSet;

public class SkillDefault extends Skill {
   public SkillDefault(StatsSet set) {
      super(set);
   }

   @Override
   public void useSkill(Creature caster, GameObject[] targets) {
      caster.sendActionFailed();
      caster.sendMessage("Skill not implemented. Skill ID: " + this.getId() + " " + this.getSkillType());
   }
}
