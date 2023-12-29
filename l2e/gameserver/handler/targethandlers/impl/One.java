package l2e.gameserver.handler.targethandlers.impl;

import l2e.gameserver.handler.targethandlers.ITargetTypeHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.network.SystemMessageId;

public class One implements ITargetTypeHandler {
   @Override
   public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
      if (target != null && !target.isDead() && (target != activeChar || !skill.isOffensive())) {
         return new Creature[]{target};
      } else {
         activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
         return EMPTY_TARGET_LIST;
      }
   }

   @Override
   public Enum<TargetType> getTargetType() {
      return TargetType.ONE;
   }
}
