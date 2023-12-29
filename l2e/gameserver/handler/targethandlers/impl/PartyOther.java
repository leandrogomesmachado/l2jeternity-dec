package l2e.gameserver.handler.targethandlers.impl;

import l2e.gameserver.handler.targethandlers.ITargetTypeHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.network.SystemMessageId;

public class PartyOther implements ITargetTypeHandler {
   @Override
   public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
      if (target == null
         || target == activeChar
         || !activeChar.isInParty()
         || !target.isInParty()
         || activeChar.getParty().getLeaderObjectId() != target.getParty().getLeaderObjectId()) {
         activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
         return EMPTY_TARGET_LIST;
      } else if (target.isDead()) {
         return EMPTY_TARGET_LIST;
      } else {
         if (target.isPlayer()) {
            switch(skill.getId()) {
               case 426:
                  if (!target.getActingPlayer().isMageClass()) {
                     return new Creature[]{target};
                  }

                  return EMPTY_TARGET_LIST;
               case 427:
                  if (target.getActingPlayer().isMageClass()) {
                     return new Creature[]{target};
                  }

                  return EMPTY_TARGET_LIST;
            }
         }

         return new Creature[]{target};
      }
   }

   @Override
   public Enum<TargetType> getTargetType() {
      return TargetType.PARTY_OTHER;
   }
}
