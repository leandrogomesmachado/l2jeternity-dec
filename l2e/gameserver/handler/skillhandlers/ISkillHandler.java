package l2e.gameserver.handler.skillhandlers;

import java.util.logging.Logger;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;

public interface ISkillHandler {
   Logger _log = Logger.getLogger(ISkillHandler.class.getName());

   void useSkill(Creature var1, Skill var2, GameObject[] var3);

   SkillType[] getSkillIds();
}
