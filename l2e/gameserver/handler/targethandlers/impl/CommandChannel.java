package l2e.gameserver.handler.targethandlers.impl;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.handler.targethandlers.ITargetTypeHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;

public class CommandChannel implements ITargetTypeHandler {
   @Override
   public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
      List<Creature> targetList = new ArrayList<>();
      Player player = activeChar.getActingPlayer();
      if (player == null) {
         return EMPTY_TARGET_LIST;
      } else {
         targetList.add(player);
         int radius = skill.getAffectRange();
         l2e.gameserver.model.Party party = player.getParty();
         boolean hasChannel = party != null && party.isInCommandChannel();
         if (Skill.addSummon(activeChar, player, radius, false)) {
            targetList.add(player.getSummon());
         }

         if (party == null) {
            return targetList.toArray(new Creature[targetList.size()]);
         } else {
            int maxTargets = skill.getAffectLimit();

            for(Player member : hasChannel ? party.getCommandChannel().getMembers() : party.getMembers()) {
               if (activeChar != member && Skill.addCharacter(activeChar, member, radius, false)) {
                  targetList.add(member);
                  if (targetList.size() >= maxTargets) {
                     break;
                  }
               }
            }

            return targetList.toArray(new Creature[targetList.size()]);
         }
      }
   }

   @Override
   public Enum<TargetType> getTargetType() {
      return TargetType.COMMAND_CHANNEL;
   }
}
