package l2e.gameserver.handler.itemhandlers.impl;

import java.util.logging.Level;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.entity.Siege;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class ScrollOfResurrection implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         for(AbstractFightEvent e : playable.getFightEvents()) {
            if (e != null && !e.canUseScroll(playable)) {
               playable.sendActionFailed();
               return false;
            }
         }

         if (!AerialCleftEvent.getInstance().onScrollUse(playable.getObjectId())) {
            playable.sendActionFailed();
            return false;
         } else {
            Player activeChar = playable.getActingPlayer();
            if (activeChar.isSitting()) {
               activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
               return false;
            } else if (activeChar.isMovementDisabled()) {
               return false;
            } else {
               int itemId = item.getId();
               boolean petScroll = itemId == 6387;
               SkillHolder[] skills = item.getItem().getSkills();
               if (skills == null) {
                  _log.log(Level.WARNING, this.getClass().getSimpleName() + ": is missing skills!");
                  return false;
               } else {
                  Creature target = (Creature)activeChar.getTarget();
                  if (target != null && target.isDead()) {
                     Player targetPlayer = null;
                     if (target.isPlayer()) {
                        targetPlayer = (Player)target;
                     }

                     PetInstance targetPet = null;
                     if (target instanceof PetInstance) {
                        targetPet = (PetInstance)target;
                     }

                     if (targetPlayer != null || targetPet != null) {
                        boolean condGood = true;
                        if (activeChar.isInsideZone(ZoneId.SIEGE)) {
                           Siege siege = SiegeManager.getInstance().getSiege(activeChar);
                           boolean twWar = TerritoryWarManager.getInstance().isTWInProgress();
                           if (siege != null && siege.getIsInProgress()) {
                              if (targetPlayer != null && targetPlayer.isInsideZone(ZoneId.SIEGE)) {
                                 Clan clan = activeChar.getClan();
                                 if (clan == null) {
                                    condGood = false;
                                    if (activeChar.isPlayer()) {
                                       activeChar.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
                                    }
                                 } else if (siege.checkIsDefender(clan) && siege.getControlTowerCount() == 0) {
                                    condGood = false;
                                    if (activeChar.isPlayer()) {
                                       activeChar.sendPacket(SystemMessageId.TOWER_DESTROYED_NO_RESURRECTION);
                                    }
                                 } else if (siege.checkIsAttacker(clan) && siege.getAttackerClan(clan).getNumFlags() == 0) {
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
                              }
                           } else if (twWar) {
                              Clan clan = activeChar.getClan();
                              if (clan == null) {
                                 condGood = false;
                                 if (activeChar.isPlayer()) {
                                    activeChar.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
                                 }
                              } else if (TerritoryWarManager.getInstance().getHQForClan(activeChar.getClan()) == null) {
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
                           }

                           if (targetPet != null) {
                              Siege ownerSiege = SiegeManager.getInstance()
                                 .getSiege(targetPet.getOwner().getX(), targetPet.getOwner().getY(), targetPet.getOwner().getZ());
                              if (ownerSiege != null && ownerSiege.getIsInProgress()) {
                                 condGood = false;
                                 activeChar.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
                              }
                           }
                        }

                        if (targetPet != null) {
                           if (targetPet.getOwner() != activeChar && targetPet.getOwner().isReviveRequested()) {
                              if (targetPet.getOwner().isRevivingPet()) {
                                 activeChar.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
                              } else {
                                 activeChar.sendPacket(SystemMessageId.CANNOT_RES_PET2);
                              }

                              condGood = false;
                           }
                        } else if (targetPlayer != null) {
                           if (targetPlayer.isFestivalParticipant()) {
                              condGood = false;
                              activeChar.sendMessage("You may not resurrect participants in a festival.");
                           }

                           if (targetPlayer.isReviveRequested()) {
                              if (targetPlayer.isRevivingPet()) {
                                 activeChar.sendPacket(SystemMessageId.MASTER_CANNOT_RES);
                              } else {
                                 activeChar.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
                              }

                              condGood = false;
                           } else if (petScroll) {
                              condGood = false;
                              activeChar.sendMessage("You do not have the correct scroll");
                           }
                        }

                        if (condGood) {
                           if (!activeChar.destroyItem("Consume", item.getObjectId(), 1L, null, false)) {
                              return false;
                           }

                           SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
                           sm.addItemName(item);
                           activeChar.sendPacket(sm);

                           for(SkillHolder sk : skills) {
                              activeChar.useMagic(sk.getSkill(), true, true, true);
                           }

                           return true;
                        }
                     }

                     return false;
                  } else {
                     activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                     return false;
                  }
               }
            }
         }
      }
   }
}
