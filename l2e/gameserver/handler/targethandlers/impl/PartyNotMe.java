package l2e.gameserver.handler.targethandlers.impl;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Util;
import l2e.gameserver.handler.targethandlers.ITargetTypeHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;

public class PartyNotMe implements ITargetTypeHandler {
   @Override
   public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
      List<Creature> targetList = new ArrayList<>();
      if (onlyFirst) {
         return new Creature[]{activeChar};
      } else {
         Player player = null;
         if (activeChar.isSummon()) {
            player = ((l2e.gameserver.model.actor.Summon)activeChar).getOwner();
            targetList.add(player);
         } else if (activeChar.isPlayer()) {
            player = activeChar.getActingPlayer();
            if (activeChar.getSummon() != null) {
               targetList.add(activeChar.getSummon());
            }
         }

         if (activeChar.getParty() != null) {
            for(Player partyMember : activeChar.getParty().getMembers()) {
               if (partyMember != null
                  && partyMember != player
                  && !partyMember.isDead()
                  && Util.checkIfInRange(skill.getAffectRange(), activeChar, partyMember, true)) {
                  targetList.add(partyMember);
                  if (partyMember.getSummon() != null && !partyMember.getSummon().isDead()) {
                     targetList.add(partyMember.getSummon());
                  }
               }
            }
         }

         return targetList.toArray(new Creature[targetList.size()]);
      }
   }

   @Override
   public Enum<TargetType> getTargetType() {
      return TargetType.PARTY_NOTME;
   }
}
