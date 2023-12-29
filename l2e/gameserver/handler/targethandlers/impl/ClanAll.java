package l2e.gameserver.handler.targethandlers.impl;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Util;
import l2e.gameserver.handler.targethandlers.ITargetTypeHandler;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;

public class ClanAll implements ITargetTypeHandler {
   @Override
   public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
      List<Creature> targetList = new ArrayList<>();
      if (activeChar.isPlayable()) {
         Player player = activeChar.getActingPlayer();
         if (player == null) {
            return EMPTY_TARGET_LIST;
         }

         if (player.isInOlympiadMode()) {
            return new Creature[]{player};
         }

         if (onlyFirst) {
            return new Creature[]{player};
         }

         targetList.add(player);
         int radius = skill.getAffectRange();
         Clan clan = player.getClan();
         if (Skill.addSummon(activeChar, player, radius, false)) {
            targetList.add(player.getSummon());
         }

         if (clan != null) {
            for(l2e.gameserver.model.ClanMember member : clan.getMembers()) {
               Player obj = member.getPlayerInstance();
               if (obj != null
                  && obj != player
                  && (
                     !player.isInDuel()
                        || player.getDuelId() == obj.getDuelId()
                           && (!player.isInParty() || !obj.isInParty() || player.getParty().getLeaderObjectId() == obj.getParty().getLeaderObjectId())
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

                     targetList.add(obj);
                  }
               }
            }
         }
      } else if (activeChar.isNpc()) {
         Npc npc = (Npc)activeChar;
         if (npc.getFaction().isNone()) {
            return new Creature[]{activeChar};
         }

         targetList.add(activeChar);
         int maxTargets = skill.getAffectLimit();

         for(Npc newTarget : World.getInstance().getAroundNpc(activeChar)) {
            if (newTarget.isAttackable() && npc.isInFaction((Attackable)newTarget) && Util.checkIfInRange(skill.getCastRange(), activeChar, newTarget, true)) {
               if (maxTargets > 0 && targetList.size() >= maxTargets) {
                  break;
               }

               targetList.add(newTarget);
            }
         }
      }

      return targetList.toArray(new Creature[targetList.size()]);
   }

   @Override
   public Enum<TargetType> getTargetType() {
      return TargetType.CLAN;
   }
}
