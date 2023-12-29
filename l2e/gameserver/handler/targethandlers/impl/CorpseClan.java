package l2e.gameserver.handler.targethandlers.impl;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Util;
import l2e.gameserver.handler.targethandlers.ITargetTypeHandler;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Siege;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;

public class CorpseClan implements ITargetTypeHandler {
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

         int radius = skill.getAffectRange();
         Clan clan = player.getClan();
         if (Skill.addSummon(activeChar, player, radius, true)) {
            targetList.add(player.getSummon());
         }

         boolean condGood = true;
         SystemMessageId msgId = null;
         if (clan != null) {
            int maxTargets = skill.getAffectLimit();

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

                  if (skill.getSkillType() == SkillType.RESURRECT && player.isInsideZone(ZoneId.SIEGE) && obj.isInsideZone(ZoneId.SIEGE)) {
                     Siege siege = SiegeManager.getInstance().getSiege(activeChar);
                     boolean twWar = TerritoryWarManager.getInstance().isTWInProgress();
                     if (siege != null && siege.getIsInProgress()) {
                        if (siege.checkIsDefender(clan) && siege.getControlTowerCount() == 0) {
                           condGood = false;
                           if (activeChar.isPlayer()) {
                              msgId = SystemMessageId.TOWER_DESTROYED_NO_RESURRECTION;
                           }
                           continue;
                        }

                        if (siege.checkIsAttacker(clan) && siege.getAttackerClan(clan).getNumFlags() == 0) {
                           condGood = false;
                           if (activeChar.isPlayer()) {
                              msgId = SystemMessageId.NO_RESURRECTION_WITHOUT_BASE_CAMP;
                           }
                           continue;
                        }

                        condGood = false;
                        if (activeChar.isPlayer()) {
                           msgId = SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE;
                        }
                        continue;
                     }

                     if (twWar) {
                        if (TerritoryWarManager.getInstance().getHQForClan(clan) == null) {
                           condGood = false;
                           if (activeChar.isPlayer()) {
                              activeChar.sendPacket(SystemMessageId.NO_RESURRECTION_WITHOUT_BASE_CAMP);
                           }
                        } else {
                           condGood = false;
                           if (activeChar.isPlayer()) {
                              activeChar.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
                           }
                        }
                        continue;
                     }

                     if (obj.getSummon() != null) {
                        Siege ownerSiege = SiegeManager.getInstance()
                           .getSiege(obj.getSummon().getOwner().getX(), obj.getSummon().getOwner().getY(), obj.getSummon().getOwner().getZ());
                        if (ownerSiege != null && ownerSiege.getIsInProgress()) {
                           condGood = false;
                           msgId = SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE;
                           continue;
                        }
                     }
                  }

                  if (!onlyFirst && Skill.addSummon(activeChar, obj, radius, true)) {
                     targetList.add(obj.getSummon());
                  }

                  if (Skill.addCharacter(activeChar, obj, radius, true)) {
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

            if (!condGood && activeChar.isPlayer() && msgId != null && (targetList.isEmpty() || targetList.size() == 0)) {
               activeChar.sendPacket(msgId);
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
               if (targetList.size() >= maxTargets) {
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
      return TargetType.CORPSE_CLAN;
   }
}
