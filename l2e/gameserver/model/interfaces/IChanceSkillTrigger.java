package l2e.gameserver.model.interfaces;

import l2e.gameserver.model.ChanceCondition;

public interface IChanceSkillTrigger {
   boolean triggersChanceSkill();

   int getTriggeredChanceId();

   int getTriggeredChanceLevel();

   ChanceCondition getTriggeredChanceCondition();
}
