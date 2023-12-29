package l2e.gameserver.handler.skillhandlers.impl;

import java.util.logging.Logger;
import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;

public class DeluxeKey implements ISkillHandler {
   private static Logger _log = Logger.getLogger(DeluxeKey.class.getName());
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.DELUXE_KEY_UNLOCK};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      if (activeChar.isPlayer()) {
         GameObject[] targetList = skill.getTargetList(activeChar);
         if (targetList != null) {
            _log.fine("Delux key casting succeded.");
         }
      }
   }

   @Override
   public SkillType[] getSkillIds() {
      return SKILL_IDS;
   }
}
