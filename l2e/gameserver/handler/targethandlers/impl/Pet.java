package l2e.gameserver.handler.targethandlers.impl;

import l2e.gameserver.handler.targethandlers.ITargetTypeHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;

public class Pet implements ITargetTypeHandler {
   @Override
   public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
      return (GameObject[])(activeChar.hasPet() ? new Creature[]{activeChar.getSummon()} : EMPTY_TARGET_LIST);
   }

   @Override
   public Enum<TargetType> getTargetType() {
      return TargetType.PET;
   }
}
