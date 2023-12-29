package l2e.gameserver.network.clientpackets;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.model.NextAction;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.handler.itemhandlers.ItemHandler;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.EtcItem;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.type.ArmorType;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.type.FunPvpZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExUseSharedGroupItem;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestUseItem extends GameClientPacket {
   private int _objectId;
   private boolean _ctrlPressed;
   private int _itemId;

   @Override
   protected void readImpl() {
      this._objectId = this.readD();
      this._ctrlPressed = this.readD() != 0;
   }

   @Override
   protected void runImpl() {
      final Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         activeChar._useItemLock.writeLock().lock();

         try {
            if (Config.DEBUG) {
               _log.log(Level.INFO, activeChar + ": use item " + this._objectId);
            }

            if (activeChar.getActiveTradeList() != null) {
               activeChar.cancelActiveTrade();
            }

            if (activeChar.getPrivateStoreType() != 0) {
               activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
               activeChar.sendActionFailed();
            } else {
               ItemInstance item = activeChar.getInventory().getItemByObjectId(this._objectId);
               if (item != null) {
                  if (activeChar.isInsideZone(ZoneId.FUN_PVP)) {
                     FunPvpZone zone = ZoneManager.getInstance().getZone(activeChar, FunPvpZone.class);
                     if (zone != null && !zone.checkItem(item)) {
                        activeChar.sendMessage("You cannot use " + item.getName() + " inside this zone.");
                        return;
                     }
                  }

                  if (item.getItem().getType2() == 3) {
                     activeChar.sendPacket(SystemMessageId.CANNOT_USE_QUEST_ITEMS);
                  } else if (!activeChar.isStunned()
                     && !activeChar.isParalyzed()
                     && !activeChar.isSleeping()
                     && !activeChar.isAfraid()
                     && !activeChar.isAlikeDead()) {
                     if (activeChar.isDead() || !activeChar.getInventory().canManipulateWithItemId(item.getId())) {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
                        sm.addItemName(item);
                        activeChar.sendPacket(sm);
                     } else if (!activeChar.isGM() && !activeChar.isHero() && item.isHeroItem()) {
                        activeChar.sendMessage("Cannot use this item.");
                     } else if (item.isEquipped() || item.getItem().checkCondition(activeChar, activeChar, true)) {
                        this._itemId = item.getId();
                        if (activeChar.isFishing() && (this._itemId < 6535 || this._itemId > 6540)) {
                           activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
                        } else {
                           if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && activeChar.getKarma() > 0) {
                              SkillHolder[] skills = item.getItem().getSkills();
                              if (skills != null) {
                                 for(SkillHolder sHolder : skills) {
                                    Skill skill = sHolder.getSkill();
                                    if (skill != null && skill.hasEffectType(EffectType.TELEPORT)) {
                                       return;
                                    }
                                 }
                              }
                           }

                           int reuseDelay = item.getReuseDelay();
                           int sharedReuseGroup = item.getSharedReuseGroup();
                           if (reuseDelay > 0) {
                              long reuse = activeChar.getItemRemainingReuseTime(item.getObjectId());
                              if (reuse > 0L) {
                                 this.reuseData(activeChar, item);
                                 this.sendSharedGroupUpdate(activeChar, sharedReuseGroup, reuse, reuseDelay);
                                 return;
                              }

                              long reuseOnGroup = activeChar.getReuseDelayOnGroup(sharedReuseGroup);
                              if (reuseOnGroup > 0L) {
                                 this.reuseData(activeChar, item);
                                 this.sendSharedGroupUpdate(activeChar, sharedReuseGroup, reuseOnGroup, reuseDelay);
                                 return;
                              }
                           }

                           if (!item.isEquipable()) {
                              if (!activeChar.isCastingNow() || item.isPotion() || item.isElixir()) {
                                 Weapon weaponItem = activeChar.getActiveWeaponItem();
                                 if (weaponItem == null
                                    || weaponItem.getItemType() != WeaponType.FISHINGROD
                                    || (this._itemId < 6519 || this._itemId > 6527)
                                       && (this._itemId < 7610 || this._itemId > 7613)
                                       && (this._itemId < 7807 || this._itemId > 7809)
                                       && (this._itemId < 8484 || this._itemId > 8486)
                                       && (this._itemId < 8505 || this._itemId > 8513)
                                       && this._itemId != 8548) {
                                    EtcItem etcItem = item.getEtcItem();
                                    IItemHandler handler = ItemHandler.getInstance().getHandler(etcItem);
                                    if (handler != null) {
                                       if (handler.useItem(activeChar, item, this._ctrlPressed) && reuseDelay > 0) {
                                          activeChar.addTimeStampItem(item, (long)reuseDelay, item.isReuseByCron());
                                          this.sendSharedGroupUpdate(activeChar, sharedReuseGroup, (long)reuseDelay, reuseDelay);
                                       }
                                    } else if (etcItem != null && etcItem.getHandlerName() != null) {
                                       _log.log(Level.WARNING, "Unmanaged Item handler: " + etcItem.getHandlerName() + " for Item Id: " + this._itemId + "!");
                                    } else {
                                       if (Config.DEBUG) {
                                          _log.log(Level.WARNING, "No Item handler registered for Item Id: " + this._itemId + "!");
                                       }
                                    }
                                 } else {
                                    activeChar.getInventory().setPaperdollItem(7, item);
                                    activeChar.broadcastUserInfo(true);
                                    activeChar.sendItemList(false);
                                 }
                              }
                           } else if (!activeChar.isCursedWeaponEquipped() || this._itemId != 6408) {
                              if (!FortSiegeManager.getInstance().isCombat(this._itemId)) {
                                 if (!activeChar.isCombatFlagEquipped()) {
                                    label564:
                                    switch(item.getItem().getBodyPart()) {
                                       case 64:
                                       case 512:
                                       case 1024:
                                       case 2048:
                                       case 4096:
                                       case 8192:
                                       case 32768:
                                          if (activeChar.getRace() == Race.Kamael
                                             && (item.getItem().getItemType() == ArmorType.HEAVY || item.getItem().getItemType() == ArmorType.MAGIC)) {
                                             activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                                             return;
                                          }
                                          break;
                                       case 128:
                                       case 256:
                                       case 16384:
                                          if (activeChar.getActiveWeaponItem() != null && activeChar.getActiveWeaponItem().getId() == 9819) {
                                             activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                                             return;
                                          }

                                          if (activeChar.isMounted()) {
                                             activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                                             return;
                                          }

                                          if (activeChar.isDisarmed()) {
                                             activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                                             return;
                                          }

                                          if (activeChar.isCursedWeaponEquipped()) {
                                             return;
                                          }

                                          if (!item.isEquipped() && item.isWeapon() && !activeChar.canOverrideCond(PcCondOverride.ITEM_CONDITIONS)) {
                                             Weapon wpn = (Weapon)item.getItem();
                                             switch(activeChar.getRace()) {
                                                case Kamael:
                                                   switch(wpn.getItemType()) {
                                                      case NONE:
                                                         activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                                                         return;
                                                      default:
                                                         break label564;
                                                   }
                                                case Human:
                                                case Dwarf:
                                                case Elf:
                                                case DarkElf:
                                                case Orc:
                                                   switch(wpn.getItemType()) {
                                                      case RAPIER:
                                                      case CROSSBOW:
                                                      case ANCIENTSWORD:
                                                         activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                                                         return;
                                                   }
                                             }
                                          }
                                          break;
                                       case 4194304:
                                          if (!item.isEquipped() && activeChar.getInventory().getMaxTalismanCount() == 0) {
                                             activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                                             return;
                                          }
                                    }

                                    if (!activeChar.isCastingNow() && !activeChar.isCastingSimultaneouslyNow()) {
                                       if (activeChar.isAttackingNow()) {
                                          ThreadPoolManager.getInstance()
                                             .schedule(
                                                () -> activeChar.useEquippableItem(this._objectId, false),
                                                TimeUnit.MILLISECONDS.convert(activeChar.getAttackEndTime() - System.nanoTime(), TimeUnit.NANOSECONDS)
                                             );
                                       } else {
                                          activeChar.useEquippableItem(this._objectId, true);
                                       }
                                    } else {
                                       if (activeChar.getAI().getNextAction() != null) {
                                          activeChar.getAI().getNextAction().addCallback(new NextAction.NextActionCallback() {
                                             @Override
                                             public void doWork() {
                                                activeChar.useEquippableItem(RequestUseItem.this._objectId, true);
                                             }
                                          });
                                       } else {
                                          NextAction nextAction = new NextAction(
                                             CtrlEvent.EVT_FINISH_CASTING, CtrlIntention.CAST, new NextAction.NextActionCallback() {
                                                @Override
                                                public void doWork() {
                                                   activeChar.useEquippableItem(RequestUseItem.this._objectId, true);
                                                }
                                             }
                                          );
                                          activeChar.getAI().setNextAction(nextAction);
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         } finally {
            activeChar._useItemLock.writeLock().unlock();
         }
      }
   }

   private void reuseData(Player activeChar, ItemInstance item) {
      SystemMessage sm = null;
      long remainingTime = activeChar.getItemRemainingReuseTime(item.getObjectId());
      int hours = (int)(remainingTime / 3600000L);
      int minutes = (int)(remainingTime % 3600000L) / 60000;
      int seconds = (int)(remainingTime / 1000L % 60L);
      if (hours > 0) {
         sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_REUSE_S1);
         sm.addItemName(item);
         sm.addNumber(hours);
         sm.addNumber(minutes);
      } else if (minutes > 0) {
         sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTES_S3_SECONDS_REMAINING_FOR_REUSE_S1);
         sm.addItemName(item);
         sm.addNumber(minutes);
      } else {
         sm = SystemMessage.getSystemMessage(SystemMessageId.S2_SECONDS_REMAINING_FOR_REUSE_S1);
         sm.addItemName(item);
      }

      sm.addNumber(seconds);
      activeChar.sendPacket(sm);
   }

   private void sendSharedGroupUpdate(Player activeChar, int group, long remaining, int reuse) {
      if (group > 0) {
         activeChar.sendPacket(new ExUseSharedGroupItem(this._itemId, group, remaining, reuse));
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return !Config.SPAWN_PROTECTION_ALLOWED_ITEMS.contains(this._itemId);
   }
}
