package l2e.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.EnchantItemParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Armor;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.items.enchant.EnchantItem;
import l2e.gameserver.model.items.enchant.EnchantResultType;
import l2e.gameserver.model.items.enchant.EnchantScroll;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.EnchantResult;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestEnchantItem extends GameClientPacket {
   protected static final Logger _logEnchant = Logger.getLogger("enchant");
   private int _objectId = 0;
   private int _supportId;

   @Override
   protected void readImpl() {
      this._objectId = this.readD();
      this._supportId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null && this._objectId != 0) {
         activeChar.isntAfk();
         if (activeChar.isActionsDisabled()) {
            activeChar.setActiveEnchantItemId(-1);
            activeChar.sendActionFailed();
         } else if (!activeChar.isOnline() || this.getClient().isDetached()) {
            activeChar.setActiveEnchantItemId(-1);
         } else if (!activeChar.isProcessingTransaction() && !activeChar.isInStoreMode()) {
            ItemInstance item = activeChar.getInventory().getItemByObjectId(this._objectId);
            ItemInstance scroll = activeChar.getInventory().getItemByObjectId(activeChar.getActiveEnchantItemId());
            ItemInstance support = activeChar.getInventory().getItemByObjectId(activeChar.getActiveEnchantSupportItemId());
            if (item != null && scroll != null) {
               EnchantScroll scrollTemplate = EnchantItemParser.getInstance().getEnchantScroll(scroll);
               if (scrollTemplate != null) {
                  EnchantItem supportTemplate = null;
                  if (support != null) {
                     if (support.getObjectId() != this._supportId) {
                        activeChar.setActiveEnchantItemId(-1);
                        return;
                     }

                     supportTemplate = EnchantItemParser.getInstance().getSupportItem(support);
                  }

                  if (!scrollTemplate.isValid(item, supportTemplate)) {
                     activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
                     activeChar.setActiveEnchantItemId(-1);
                     activeChar.sendPacket(new EnchantResult(2, 0, 0));
                  } else {
                     scroll = activeChar.getInventory().destroyItem("Enchant", scroll.getObjectId(), 1L, activeChar, item);
                     if (scroll == null) {
                        activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                        Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName() + " tried to enchant with a scroll he doesn't have");
                        activeChar.setActiveEnchantItemId(-1);
                        activeChar.sendPacket(new EnchantResult(2, 0, 0));
                     } else {
                        if (support != null) {
                           support = activeChar.getInventory().destroyItem("Enchant", support.getObjectId(), 1L, activeChar, item);
                           if (support == null) {
                              activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                              Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName() + " tried to enchant with a support item he doesn't have");
                              activeChar.setActiveEnchantItemId(-1);
                              activeChar.sendPacket(new EnchantResult(2, 0, 0));
                              return;
                           }
                        }

                        synchronized(item) {
                           if (item.getOwnerId() == activeChar.getObjectId() && item.isEnchantable() != 0) {
                              EnchantResultType resultType = scrollTemplate.calculateSuccess(activeChar, item, supportTemplate);
                              switch(resultType) {
                                 case ERROR:
                                    activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
                                    activeChar.setActiveEnchantItemId(-1);
                                    activeChar.sendPacket(new EnchantResult(2, 0, 0));
                                    break;
                                 case SUCCESS:
                                    Skill enchant4Skill = null;
                                    Item it = item.getItem();
                                    item.setEnchantLevel(item.getEnchantLevel() + 1);
                                    item.updateDatabase();
                                    activeChar.sendPacket(new EnchantResult(0, 0, 0));
                                    if (item.getEnchantLevel() > 3) {
                                       if (!scrollTemplate.isBlessed() && !scrollTemplate.isSafe()) {
                                          activeChar.getCounters().addAchivementInfo("enchantNormalSucceeded", 0, -1L, false, false, false);
                                       } else {
                                          activeChar.getCounters().addAchivementInfo("enchantBlessedSucceeded", 0, -1L, false, false, false);
                                       }
                                    }

                                    if (Config.LOG_ITEM_ENCHANTS) {
                                       LogRecord record = new LogRecord(Level.INFO, "Success");
                                       record.setParameters(new Object[]{activeChar, item, scroll, support});
                                       record.setLoggerName("item");
                                       _logEnchant.log(record);
                                    }

                                    if (item.isWeapon()) {
                                       activeChar.getCounters().addAchivementInfo("enchantWeaponByLvl", item.getEnchantLevel(), -1L, false, false, false);
                                    } else if (item.isJewel()) {
                                       activeChar.getCounters().addAchivementInfo("enchantJewerlyByLvl", item.getEnchantLevel(), -1L, false, false, false);
                                    } else {
                                       activeChar.getCounters().addAchivementInfo("enchantArmorByLvl", item.getEnchantLevel(), -1L, false, false, false);
                                    }

                                    int minEnchantAnnounce = item.isArmor() ? 6 : 7;
                                    int maxEnchantAnnounce = item.isArmor() ? 0 : 15;
                                    if (item.getEnchantLevel() == minEnchantAnnounce || item.getEnchantLevel() == maxEnchantAnnounce) {
                                       SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_SUCCESSFULY_ENCHANTED_A_S2_S3);
                                       sm.addCharName(activeChar);
                                       sm.addNumber(item.getEnchantLevel());
                                       sm.addItemName(item);
                                       activeChar.broadcastPacket(sm);
                                       Skill skill = SkillsParser.FrequentSkill.FIREWORK.getSkill();
                                       if (skill != null) {
                                          activeChar.broadcastPacket(
                                             new MagicSkillUse(
                                                activeChar, activeChar, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()
                                             )
                                          );
                                       }
                                    }

                                    if (it instanceof Armor
                                       && item.getEnchantLevel() == 4
                                       && activeChar.getInventory().getItemByObjectId(item.getObjectId()).isEquipped()) {
                                       enchant4Skill = ((Armor)it).getEnchant4Skill();
                                       if (enchant4Skill != null) {
                                          activeChar.addSkill(enchant4Skill, false);
                                          activeChar.sendSkillList(false);
                                       }
                                    }
                                    break;
                                 case FAILURE:
                                    if (scrollTemplate.isSafe()) {
                                       activeChar.sendPacket(SystemMessageId.SAFE_ENCHANT_FAILED);
                                       activeChar.sendPacket(new EnchantResult(5, 0, 0));
                                       if (Config.LOG_ITEM_ENCHANTS) {
                                          LogRecord record = new LogRecord(Level.INFO, "Safe Fail");
                                          record.setParameters(new Object[]{activeChar, item, scroll, support});
                                          record.setLoggerName("item");
                                          _logEnchant.log(record);
                                       }
                                    } else {
                                       if (item.isEquipped()) {
                                          if (item.getEnchantLevel() > 0) {
                                             SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
                                             sm.addNumber(item.getEnchantLevel());
                                             sm.addItemName(item);
                                             activeChar.sendPacket(sm);
                                          } else {
                                             SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
                                             sm.addItemName(item);
                                             activeChar.sendPacket(sm);
                                          }

                                          ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
                                          InventoryUpdate iu = new InventoryUpdate();

                                          for(ItemInstance itm : unequiped) {
                                             iu.addModifiedItem(itm);
                                          }

                                          activeChar.sendPacket(iu);
                                          activeChar.broadcastCharInfo();
                                       }

                                       if (scrollTemplate.isBlessed()) {
                                          activeChar.sendPacket(SystemMessageId.BLESSED_ENCHANT_FAILED);
                                          if (Config.SYSTEM_BLESSED_ENCHANT) {
                                             item.setEnchantLevel(Config.BLESSED_ENCHANT_SAVE);
                                          } else {
                                             item.setEnchantLevel(0);
                                             item.updateDatabase();
                                             activeChar.sendPacket(new EnchantResult(3, 0, 0));
                                          }

                                          if (Config.LOG_ITEM_ENCHANTS) {
                                             LogRecord record = new LogRecord(Level.INFO, "Blessed Fail");
                                             record.setParameters(new Object[]{activeChar, item, scroll, support});
                                             record.setLoggerName("item");
                                             _logEnchant.log(record);
                                          }
                                       } else {
                                          int crystalId = item.getItem().getCrystalItemId();
                                          int count = item.getCrystalCount() - (item.getItem().getCrystalCount() + 1) / 2;
                                          if (count < 1) {
                                             count = 1;
                                          }

                                          ItemInstance destroyItem = activeChar.getInventory().destroyItem("Enchant", item, activeChar, null);
                                          if (destroyItem == null) {
                                             Util.handleIllegalPlayerAction(
                                                activeChar,
                                                "Unable to delete item on enchant failure from player " + activeChar.getName() + ", possible cheater !"
                                             );
                                             activeChar.setActiveEnchantItemId(-1);
                                             activeChar.sendPacket(new EnchantResult(2, 0, 0));
                                             if (Config.LOG_ITEM_ENCHANTS) {
                                                LogRecord record = new LogRecord(Level.INFO, "Unable to destroy");
                                                record.setParameters(new Object[]{activeChar, item, scroll, support});
                                                record.setLoggerName("item");
                                                _logEnchant.log(record);
                                             }

                                             return;
                                          }

                                          ItemInstance crystals = null;
                                          if (crystalId != 0) {
                                             crystals = activeChar.getInventory().addItem("Enchant", crystalId, (long)count, activeChar, destroyItem);
                                             SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
                                             sm.addItemName(crystals);
                                             sm.addItemNumber((long)count);
                                             activeChar.sendPacket(sm);
                                          }

                                          if (!Config.FORCE_INVENTORY_UPDATE) {
                                             InventoryUpdate iu = new InventoryUpdate();
                                             if (destroyItem.getCount() == 0L) {
                                                iu.addRemovedItem(destroyItem);
                                             } else {
                                                iu.addModifiedItem(destroyItem);
                                             }

                                             if (crystals != null) {
                                                iu.addItem(crystals);
                                             }

                                             if (scroll.getCount() == 0L) {
                                                iu.addRemovedItem(scroll);
                                             } else {
                                                iu.addModifiedItem(scroll);
                                             }

                                             activeChar.sendPacket(iu);
                                          } else {
                                             activeChar.sendItemList(true);
                                          }

                                          World.getInstance().removeObject(destroyItem);
                                          if (crystalId == 0) {
                                             activeChar.sendPacket(new EnchantResult(4, 0, 0));
                                          } else {
                                             activeChar.sendPacket(new EnchantResult(1, crystalId, count));
                                          }

                                          if (Config.LOG_ITEM_ENCHANTS) {
                                             LogRecord record = new LogRecord(Level.INFO, "Fail");
                                             record.setParameters(new Object[]{activeChar, item, scroll, support});
                                             record.setLoggerName("item");
                                             _logEnchant.log(record);
                                          }
                                       }
                                    }
                              }

                              StatusUpdate su = new StatusUpdate(activeChar);
                              su.addAttribute(14, activeChar.getCurrentLoad());
                              activeChar.sendPacket(su);
                              activeChar.sendItemList(false);
                              activeChar.broadcastCharInfo();
                              activeChar.setActiveEnchantItemId(-1);
                           } else {
                              activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
                              activeChar.setActiveEnchantItemId(-1);
                              activeChar.sendPacket(new EnchantResult(2, 0, 0));
                           }
                        }
                     }
                  }
               }
            } else {
               activeChar.setActiveEnchantItemId(-1);
            }
         } else {
            activeChar.sendPacket(SystemMessageId.CANNOT_ENCHANT_WHILE_STORE);
            activeChar.setActiveEnchantItemId(-1);
         }
      }
   }
}
