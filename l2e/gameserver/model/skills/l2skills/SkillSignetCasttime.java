package l2e.gameserver.model.skills.l2skills;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.StatsSet;

public final class SkillSignetCasttime extends Skill {
   public SkillSignetCasttime(StatsSet set) {
      super(set);
   }

   @Override
   public void useSkill(Creature caster, GameObject[] targets) {
      if (!caster.isAlikeDead()) {
         this.getEffectsSelf(caster);
      }
   }
}
