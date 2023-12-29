package l2e.gameserver.handler.targethandlers.impl;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.handler.targethandlers.ITargetTypeHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;

public class PartyClan implements ITargetTypeHandler {
   @Override
   public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
      List<Creature> targetList = new ArrayList<>();
      if (onlyFirst) {
         return new Creature[]{activeChar};
      } else {
         Player player = activeChar.getActingPlayer();
         if (player == null) {
            return EMPTY_TARGET_LIST;
         } else {
            targetList.add(player);
            int radius = skill.getAffectRange();
            boolean hasClan = player.getClan() != null;
            boolean hasParty = player.isInParty();
            if (Skill.addSummon(activeChar, player, radius, false)) {
               targetList.add(player.getSummon());
            }

            if (!hasClan && !hasParty) {
               return targetList.toArray(new Creature[targetList.size()]);
            } else {
               int maxTargets = skill.getAffectLimit();

               for(Player obj : World.getInstance().getAroundPlayers(activeChar, radius, 200)) {
                  if (obj != null
                     && (
                        !player.isInOlympiadMode()
                           || obj.isInOlympiadMode()
                              && player.getOlympiadGameId() == obj.getOlympiadGameId()
                              && player.getOlympiadSide() == obj.getOlympiadSide()
                     )
                     && (
                        !player.isInDuel()
                           || player.getDuelId() == obj.getDuelId()
                              && (!hasParty || !obj.isInParty() || player.getParty().getLeaderObjectId() == obj.getParty().getLeaderObjectId())
                     )
                     && (
                        hasClan && obj.getClanId() == player.getClanId()
                           || hasParty && obj.isInParty() && player.getParty().getLeaderObjectId() == obj.getParty().getLeaderObjectId()
                     )
                     && player.checkPvpSkill(obj, skill)) {
                     for(AbstractFightEvent e : player.getFightEvents()) {
                        if (e != null && !e.canUseMagic(player, obj, skill)) {
                        }
                     }

                     if (!onlyFirst && Skill.addSummon(activeChar, obj, radius, false)) {
                        targetList.add(obj.getSummon());
                     }

                     if (Skill.addCharacter(activeChar, obj, radius, false)) {
                        if (onlyFirst) {
                           return new Creature[]{obj};
                        }

                        if (maxTargets > 0 && targetList.size() >= maxTargets) {
                           break;
                        }

                        targetList.add(obj);
                     }
                  }
               }

               return targetList.toArray(new Creature[targetList.size()]);
            }
         }
      }
   }

   @Override
   public Enum<TargetType> getTargetType() {
      return TargetType.PARTY_CLAN;
   }
}
