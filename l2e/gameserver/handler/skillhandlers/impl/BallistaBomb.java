package l2e.gameserver.handler.skillhandlers.impl;

import l2e.commons.util.Rnd;
import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.FortBallistaInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;

public class BallistaBomb implements ISkillHandler {
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.BALLISTA};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      if (activeChar.isPlayer()) {
         GameObject[] targetList = skill.getTargetList(activeChar);
         if (targetList != null && targetList.length != 0) {
            Creature target = (Creature)targetList[0];
            if (target instanceof FortBallistaInstance && Rnd.get(3) == 0) {
               target.setIsInvul(false);
               target.reduceCurrentHp(target.getMaxHp() + 1.0, activeChar, skill);
               target.notifyDamageReceived(target.getMaxHp() + 1.0, activeChar, skill, false, false);
            }
         }
      }
   }

   @Override
   public SkillType[] getSkillIds() {
      return SKILL_IDS;
   }
}
