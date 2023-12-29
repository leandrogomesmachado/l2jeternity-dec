package l2e.gameserver.handler.targethandlers.impl;

import l2e.gameserver.handler.targethandlers.ITargetTypeHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.network.SystemMessageId;

public class PartyMember implements ITargetTypeHandler {
   @Override
   public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
      if (target == null) {
         activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
         return EMPTY_TARGET_LIST;
      } else {
         return (GameObject[])(target.isDead()
               || target != activeChar
                  && (!activeChar.isInParty() || !target.isInParty() || activeChar.getParty().getLeaderObjectId() != target.getParty().getLeaderObjectId())
                  && (!activeChar.isPlayer() || !target.isSummon() || activeChar.getSummon() != target)
                  && (!activeChar.isSummon() || !target.isPlayer() || activeChar != target.getSummon())
            ? EMPTY_TARGET_LIST
            : new Creature[]{target});
      }
   }

   @Override
   public Enum<TargetType> getTargetType() {
      return TargetType.PARTY_MEMBER;
   }
}
