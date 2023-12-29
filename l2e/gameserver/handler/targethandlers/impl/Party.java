package l2e.gameserver.handler.targethandlers.impl;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.handler.targethandlers.ITargetTypeHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;

public class Party implements ITargetTypeHandler {
   @Override
   public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
      List<Creature> targetList = new ArrayList<>();
      if (onlyFirst) {
         return new Creature[]{activeChar};
      } else {
         targetList.add(activeChar);
         int radius = skill.getAffectRange();
         Player player = activeChar.getActingPlayer();
         if (activeChar.isSummon()) {
            if (Skill.addCharacter(activeChar, player, radius, false)) {
               targetList.add(player);
            }
         } else if (activeChar.isPlayer() && Skill.addSummon(activeChar, player, radius, false)) {
            targetList.add(player.getSummon());
         }

         if (activeChar.isInParty()) {
            for(Player partyMember : activeChar.getParty().getMembers()) {
               if (partyMember != null && partyMember != player) {
                  if (Skill.addCharacter(activeChar, partyMember, radius, false)) {
                     targetList.add(partyMember);
                  }

                  if (Skill.addSummon(activeChar, partyMember, radius, false)) {
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
      return TargetType.PARTY;
   }
}
