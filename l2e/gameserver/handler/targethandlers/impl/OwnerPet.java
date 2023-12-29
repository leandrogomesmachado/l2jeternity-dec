package l2e.gameserver.handler.targethandlers.impl;

import l2e.gameserver.handler.targethandlers.ITargetTypeHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;

public class OwnerPet implements ITargetTypeHandler {
   @Override
   public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
      if (activeChar.isSummon()) {
         Creature var5 = ((l2e.gameserver.model.actor.Summon)activeChar).getOwner();
         if (var5 != null && !var5.isDead()) {
            return new Creature[]{var5};
         }
      }

      return EMPTY_TARGET_LIST;
   }

   @Override
   public Enum<TargetType> getTargetType() {
      return TargetType.OWNER_PET;
   }
}
