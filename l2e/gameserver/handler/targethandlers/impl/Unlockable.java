package l2e.gameserver.handler.targethandlers.impl;

import l2e.gameserver.handler.targethandlers.ITargetTypeHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.ChestInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;

public class Unlockable implements ITargetTypeHandler {
   @Override
   public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
      return (GameObject[])(target != null && (target.isDoor() || target instanceof ChestInstance) ? new Creature[]{target} : EMPTY_TARGET_LIST);
   }

   @Override
   public Enum<TargetType> getTargetType() {
      return TargetType.UNLOCKABLE;
   }
}
