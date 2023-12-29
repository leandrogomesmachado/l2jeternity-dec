package l2e.gameserver.handler.targethandlers;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;

public interface ITargetTypeHandler {
   GameObject[] EMPTY_TARGET_LIST = new GameObject[0];

   GameObject[] getTargetList(Skill var1, Creature var2, boolean var3, Creature var4);

   Enum<TargetType> getTargetType();
}
