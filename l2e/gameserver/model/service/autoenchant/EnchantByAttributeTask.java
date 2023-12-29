package l2e.gameserver.model.service.autoenchant;

import java.util.HashMap;
import java.util.Map;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFail;
import l2e.gameserver.network.serverpackets.StatusUpdate;

public class EnchantByAttributeTask implements Runnable {
   private final Player _player;
   private int stoneId = -1;

   public EnchantByAttributeTask(Player player) {
      this._player = player;
   }

   public static boolean isValidPlayer(Player player) {
      if (player == null) {
         return false;
      } else if (player.isActionsDisabled()) {
         return false;
      } else if (player.isProcessingTransaction() || player.isInStoreMode()) {
         player.sendPacket(SystemMessageId.CANNOT_ENCHANT_WHILE_STORE);
         return false;
      } else {
         return player.isOnline() && !player.getClient().isDetached();
      }
   }

   // $VF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   @Override
   public void run() {
      if (!isValidPlayer(this._player)) {
         this._player.sendMessage(new ServerMessage("Enchant.NOT_VALID", this._player.getLang()).toString());
      } else {
         boolean isNeedUpdate = false;
         boolean isNeedEquip = false;
         int stones = 0;
         int crystals = 0;
         int success = 0;
         PcInventory inventory = this._player.getInventory();
         ItemInstance itemToEnchant = this._player.getEnchantParams().targetItem;
         ItemInstance stone = this._player.getEnchantParams().upgradeItem;
         this.stoneId = stone.getId();
         boolean var23 = false /* VF: Semaphore variable */;

         label1175: {
            label1176: {
               label1177: {
                  label1178: {
                     label1179: {
                        label1180: {
                           label1181: {
                              label1182: {
                                 label1183: {
                                    label1184: {
                                       label1185: {
                                          label1186: {
                                             label1187: {
                                                label1188: {
                                                   try {
                                                      var23 = true;
                                                      int su = 0;

                                                      while(true) {
                                                         if (su >= this._player.getEnchantParams().upgradeItemLimit) {
                                                            var23 = false;
                                                            break;
                                                         }

                                                         if (!this.checkAttributeLvl(this._player)) {
                                                            var23 = false;
                                                            break;
                                                         }

                                                         if (!isValidPlayer(this._player)) {
                                                            this._player
                                                               .sendMessage(new ServerMessage("Enchant.NOT_VALID", this._player.getLang()).toString());
                                                            var23 = false;
                                                            break label1175;
                                                         }

                                                         if (itemToEnchant == null) {
                                                            this._player.sendActionFailed();
                                                            this._player
                                                               .sendMessage(new ServerMessage("Enchant.SELECT_SCROLL", this._player.getLang()).toString());
                                                            var23 = false;
                                                            break label1176;
                                                         }

                                                         if (stone == null) {
                                                            this._player.sendActionFailed();
                                                            this._player
                                                               .sendMessage(new ServerMessage("Enchant.SELECT_ATT", this._player.getLang()).toString());
                                                            var23 = false;
                                                            break label1177;
                                                         }

                                                         Item item = itemToEnchant.getItem();
                                                         if (item.isCommonItem()
                                                            || !item.isElementable()
                                                            || item.getCrystalType() < 5
                                                            || item.getBodyPart() == 256) {
                                                            this._player.sendPacket(ActionFail.STATIC_PACKET, SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
                                                            var23 = false;
                                                            break label1188;
                                                         }

                                                         if (itemToEnchant.isEnchantable() == 0) {
                                                            this._player.sendPacket(ActionFail.STATIC_PACKET, SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
                                                            var23 = false;
                                                            break label1178;
                                                         }

                                                         if (itemToEnchant.isStackable() || (stone = inventory.getItemByObjectId(stone.getObjectId())) == null
                                                            )
                                                          {
                                                            this._player
                                                               .sendMessage(new ServerMessage("Enchant.MISS_ITEMS", this._player.getLang()).toString());
                                                            var23 = false;
                                                            break label1187;
                                                         }

                                                         byte stoneElement = Elementals.getItemElement(this.stoneId);
                                                         if (stoneElement == -1) {
                                                            this._player.sendPacket(ActionFail.STATIC_PACKET, SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
                                                            var23 = false;
                                                            break label1179;
                                                         }

                                                         byte element = itemToEnchant.isArmor() ? Elementals.getReverseElement(stoneElement) : stoneElement;
                                                         if (itemToEnchant.isArmor()) {
                                                            if (itemToEnchant.getElemental(Elementals.getReverseElement(element)) != null) {
                                                               this._player.sendPacket(SystemMessageId.ANOTHER_ELEMENTAL_POWER_ALREADY_ADDED);
                                                               var23 = false;
                                                               break label1180;
                                                            }
                                                         } else {
                                                            if (!itemToEnchant.isWeapon()) {
                                                               this._player.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
                                                               var23 = false;
                                                               break label1186;
                                                            }

                                                            if (itemToEnchant.getAttackElementType() != -2
                                                               && itemToEnchant.getAttackElementType() != -1
                                                               && itemToEnchant.getAttackElementType() != element) {
                                                               this._player.sendPacket(SystemMessageId.ANOTHER_ELEMENTAL_POWER_ALREADY_ADDED);
                                                               var23 = false;
                                                               break label1181;
                                                            }
                                                         }

                                                         if (item.isUnderwear() || item.isCloak() || item.isBracelet() || item.isBelt()) {
                                                            this._player.sendPacket(ActionFail.STATIC_PACKET, SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
                                                            var23 = false;
                                                            break label1185;
                                                         }

                                                         int maxValue = this.getLimit(itemToEnchant, this.stoneId);
                                                         int currentValue = itemToEnchant.getElemental(element) != null
                                                            ? itemToEnchant.getElemental(element).getValue()
                                                            : 0;
                                                         if (currentValue >= maxValue) {
                                                            this._player.sendPacket(ActionFail.STATIC_PACKET, SystemMessageId.ELEMENTAL_ENHANCE_CANCELED);
                                                            var23 = false;
                                                            break label1182;
                                                         }

                                                         if (itemToEnchant.getOwnerId() != this._player.getObjectId()) {
                                                            this._player.sendPacket(ActionFail.STATIC_PACKET, SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
                                                            var23 = false;
                                                            break label1183;
                                                         }

                                                         if (inventory.destroyItem("[AutoEnchant]", stone.getObjectId(), 1L, this._player, null) == null) {
                                                            this._player
                                                               .sendMessage(new ServerMessage("Enchant.MISS_ITEMS", this._player.getLang()).toString());
                                                            var23 = false;
                                                            break label1184;
                                                         }

                                                         boolean boolka = false;
                                                         switch(Elementals.getItemElemental(this.stoneId)._type) {
                                                            case Stone:
                                                            case Roughore:
                                                               boolka = (double)Rnd.get(100)
                                                                  < Config.ENCHANT_CHANCE_ELEMENT_STONE
                                                                     + (double)this._player.getPremiumBonus().getEnchantChance();
                                                               break;
                                                            case Crystal:
                                                               boolka = (double)Rnd.get(100)
                                                                  < Config.ENCHANT_CHANCE_ELEMENT_CRYSTAL
                                                                     + (double)this._player.getPremiumBonus().getEnchantChance();
                                                               break;
                                                            case Jewel:
                                                               boolka = (double)Rnd.get(100)
                                                                  < Config.ENCHANT_CHANCE_ELEMENT_JEWEL
                                                                     + (double)this._player.getPremiumBonus().getEnchantChance();
                                                               break;
                                                            case Energy:
                                                               boolka = (double)Rnd.get(100)
                                                                  < Config.ENCHANT_CHANCE_ELEMENT_ENERGY
                                                                     + (double)this._player.getPremiumBonus().getEnchantChance();
                                                         }

                                                         if (boolka) {
                                                            ++success;
                                                            int value = itemToEnchant.isWeapon() ? 5 : 6;
                                                            if (itemToEnchant.getAttributeElementValue(element) == 0 && itemToEnchant.isWeapon()) {
                                                               value = 20;
                                                            }

                                                            if (itemToEnchant.isEquipped()) {
                                                               this._player.getInventory().unEquipItem(itemToEnchant);
                                                               isNeedEquip = true;
                                                            }

                                                            itemToEnchant.setElementAttr(element, itemToEnchant.getAttributeElementValue(element) + value);
                                                         }

                                                         if (EnchantUtils.getInstance().isAttributeStone(stone)) {
                                                            ++stones;
                                                         } else if (EnchantUtils.getInstance().isAttributeCrystal(stone)) {
                                                            ++crystals;
                                                         }

                                                         isNeedUpdate = true;
                                                         ++su;
                                                      }
                                                   } finally {
                                                      if (var23) {
                                                         if (stone == null || stone.getCount() <= 0L) {
                                                            this._player.getEnchantParams().upgradeItem = null;
                                                         }

                                                         if (isNeedUpdate) {
                                                            if (Config.ENCHANT_CONSUME_ITEM != 0) {
                                                               this._player
                                                                  .getInventory()
                                                                  .destroyItemByItemId(
                                                                     Config.ENCHANT_CONSUME_ITEM, (long)Config.ENCHANT_CONSUME_ITEM_COUNT, "[AutoEnchant]"
                                                                  );
                                                               Item template = ItemsParser.getInstance().getTemplate(Config.ENCHANT_CONSUME_ITEM);
                                                               ServerMessage msg = new ServerMessage("Enchant.SPET_ITEMS", this._player.getLang());
                                                               msg.add(Config.ENCHANT_CONSUME_ITEM_COUNT);
                                                               msg.add(this._player.getItemName(template));
                                                               this._player.sendMessage(msg.toString());
                                                            }

                                                            StatusUpdate su = new StatusUpdate(this._player);
                                                            su.addAttribute(14, this._player.getCurrentLoad());
                                                            this._player.sendPacket(su);
                                                            this._player.sendItemList(false);
                                                            this._player.broadcastCharInfo();
                                                            Map<String, Integer> result = new HashMap<>();
                                                            result.put("enchant", this.getAttValue());
                                                            result.put("stones", stones);
                                                            result.put("crystals", crystals);
                                                            int sum = stones + crystals;
                                                            if (sum == 0) {
                                                               ++sum;
                                                            }

                                                            result.put("chance", (int)((double)success / ((double)sum / 100.0) * 100.0));
                                                            result.put(
                                                               "success",
                                                               itemToEnchant == null
                                                                  ? 0
                                                                  : (this.getAttValue() >= this._player.getEnchantParams().maxEnchantAtt ? 1 : 0)
                                                            );
                                                            EnchantManager.getInstance().showResultPage(this._player, EnchantType.ATTRIBUTE, result);
                                                            if (isNeedEquip) {
                                                               this._player.getInventory().equipItem(itemToEnchant);
                                                            }
                                                         }
                                                      }
                                                   }

                                                   if (stone == null || stone.getCount() <= 0L) {
                                                      this._player.getEnchantParams().upgradeItem = null;
                                                   }

                                                   if (isNeedUpdate) {
                                                      if (Config.ENCHANT_CONSUME_ITEM != 0) {
                                                         this._player
                                                            .getInventory()
                                                            .destroyItemByItemId(
                                                               Config.ENCHANT_CONSUME_ITEM, (long)Config.ENCHANT_CONSUME_ITEM_COUNT, "[AutoEnchant]"
                                                            );
                                                         Item template = ItemsParser.getInstance().getTemplate(Config.ENCHANT_CONSUME_ITEM);
                                                         ServerMessage msg = new ServerMessage("Enchant.SPET_ITEMS", this._player.getLang());
                                                         msg.add(Config.ENCHANT_CONSUME_ITEM_COUNT);
                                                         msg.add(this._player.getItemName(template));
                                                         this._player.sendMessage(msg.toString());
                                                      }

                                                      StatusUpdate su = new StatusUpdate(this._player);
                                                      su.addAttribute(14, this._player.getCurrentLoad());
                                                      this._player.sendPacket(su);
                                                      this._player.sendItemList(false);
                                                      this._player.broadcastCharInfo();
                                                      Map<String, Integer> result = new HashMap<>();
                                                      result.put("enchant", this.getAttValue());
                                                      result.put("stones", stones);
                                                      result.put("crystals", crystals);
                                                      int sum = stones + crystals;
                                                      if (sum == 0) {
                                                         ++sum;
                                                      }

                                                      result.put("chance", (int)((double)success / ((double)sum / 100.0) * 100.0));
                                                      result.put(
                                                         "success",
                                                         itemToEnchant == null
                                                            ? 0
                                                            : (this.getAttValue() >= this._player.getEnchantParams().maxEnchantAtt ? 1 : 0)
                                                      );
                                                      EnchantManager.getInstance().showResultPage(this._player, EnchantType.ATTRIBUTE, result);
                                                      if (isNeedEquip) {
                                                         this._player.getInventory().equipItem(itemToEnchant);
                                                      }
                                                   }

                                                   return;
                                                }

                                                if (stone == null || stone.getCount() <= 0L) {
                                                   this._player.getEnchantParams().upgradeItem = null;
                                                }

                                                if (isNeedUpdate) {
                                                   if (Config.ENCHANT_CONSUME_ITEM != 0) {
                                                      this._player
                                                         .getInventory()
                                                         .destroyItemByItemId(
                                                            Config.ENCHANT_CONSUME_ITEM, (long)Config.ENCHANT_CONSUME_ITEM_COUNT, "[AutoEnchant]"
                                                         );
                                                      Item template = ItemsParser.getInstance().getTemplate(Config.ENCHANT_CONSUME_ITEM);
                                                      ServerMessage msg = new ServerMessage("Enchant.SPET_ITEMS", this._player.getLang());
                                                      msg.add(Config.ENCHANT_CONSUME_ITEM_COUNT);
                                                      msg.add(this._player.getItemName(template));
                                                      this._player.sendMessage(msg.toString());
                                                   }

                                                   StatusUpdate su = new StatusUpdate(this._player);
                                                   su.addAttribute(14, this._player.getCurrentLoad());
                                                   this._player.sendPacket(su);
                                                   this._player.sendItemList(false);
                                                   this._player.broadcastCharInfo();
                                                   Map<String, Integer> result = new HashMap<>();
                                                   result.put("enchant", this.getAttValue());
                                                   result.put("stones", stones);
                                                   result.put("crystals", crystals);
                                                   int sum = stones + crystals;
                                                   if (sum == 0) {
                                                      ++sum;
                                                   }

                                                   result.put("chance", (int)((double)success / ((double)sum / 100.0) * 100.0));
                                                   result.put(
                                                      "success",
                                                      itemToEnchant == null
                                                         ? 0
                                                         : (this.getAttValue() >= this._player.getEnchantParams().maxEnchantAtt ? 1 : 0)
                                                   );
                                                   EnchantManager.getInstance().showResultPage(this._player, EnchantType.ATTRIBUTE, result);
                                                   if (isNeedEquip) {
                                                      this._player.getInventory().equipItem(itemToEnchant);
                                                   }
                                                }

                                                return;
                                             }

                                             if (stone == null || stone.getCount() <= 0L) {
                                                this._player.getEnchantParams().upgradeItem = null;
                                             }

                                             if (isNeedUpdate) {
                                                if (Config.ENCHANT_CONSUME_ITEM != 0) {
                                                   this._player
                                                      .getInventory()
                                                      .destroyItemByItemId(
                                                         Config.ENCHANT_CONSUME_ITEM, (long)Config.ENCHANT_CONSUME_ITEM_COUNT, "[AutoEnchant]"
                                                      );
                                                   Item template = ItemsParser.getInstance().getTemplate(Config.ENCHANT_CONSUME_ITEM);
                                                   ServerMessage msg = new ServerMessage("Enchant.SPET_ITEMS", this._player.getLang());
                                                   msg.add(Config.ENCHANT_CONSUME_ITEM_COUNT);
                                                   msg.add(this._player.getItemName(template));
                                                   this._player.sendMessage(msg.toString());
                                                }

                                                StatusUpdate su = new StatusUpdate(this._player);
                                                su.addAttribute(14, this._player.getCurrentLoad());
                                                this._player.sendPacket(su);
                                                this._player.sendItemList(false);
                                                this._player.broadcastCharInfo();
                                                Map<String, Integer> result = new HashMap<>();
                                                result.put("enchant", this.getAttValue());
                                                result.put("stones", stones);
                                                result.put("crystals", crystals);
                                                int sum = stones + crystals;
                                                if (sum == 0) {
                                                   ++sum;
                                                }

                                                result.put("chance", (int)((double)success / ((double)sum / 100.0) * 100.0));
                                                result.put(
                                                   "success",
                                                   itemToEnchant == null ? 0 : (this.getAttValue() >= this._player.getEnchantParams().maxEnchantAtt ? 1 : 0)
                                                );
                                                EnchantManager.getInstance().showResultPage(this._player, EnchantType.ATTRIBUTE, result);
                                                if (isNeedEquip) {
                                                   this._player.getInventory().equipItem(itemToEnchant);
                                                }
                                             }

                                             return;
                                          }

                                          if (stone == null || stone.getCount() <= 0L) {
                                             this._player.getEnchantParams().upgradeItem = null;
                                          }

                                          if (isNeedUpdate) {
                                             if (Config.ENCHANT_CONSUME_ITEM != 0) {
                                                this._player
                                                   .getInventory()
                                                   .destroyItemByItemId(Config.ENCHANT_CONSUME_ITEM, (long)Config.ENCHANT_CONSUME_ITEM_COUNT, "[AutoEnchant]");
                                                Item template = ItemsParser.getInstance().getTemplate(Config.ENCHANT_CONSUME_ITEM);
                                                ServerMessage msg = new ServerMessage("Enchant.SPET_ITEMS", this._player.getLang());
                                                msg.add(Config.ENCHANT_CONSUME_ITEM_COUNT);
                                                msg.add(this._player.getItemName(template));
                                                this._player.sendMessage(msg.toString());
                                             }

                                             StatusUpdate su = new StatusUpdate(this._player);
                                             su.addAttribute(14, this._player.getCurrentLoad());
                                             this._player.sendPacket(su);
                                             this._player.sendItemList(false);
                                             this._player.broadcastCharInfo();
                                             Map<String, Integer> result = new HashMap<>();
                                             result.put("enchant", this.getAttValue());
                                             result.put("stones", stones);
                                             result.put("crystals", crystals);
                                             int sum = stones + crystals;
                                             if (sum == 0) {
                                                ++sum;
                                             }

                                             result.put("chance", (int)((double)success / ((double)sum / 100.0) * 100.0));
                                             result.put(
                                                "success",
                                                itemToEnchant == null ? 0 : (this.getAttValue() >= this._player.getEnchantParams().maxEnchantAtt ? 1 : 0)
                                             );
                                             EnchantManager.getInstance().showResultPage(this._player, EnchantType.ATTRIBUTE, result);
                                             if (isNeedEquip) {
                                                this._player.getInventory().equipItem(itemToEnchant);
                                             }
                                          }

                                          return;
                                       }

                                       if (stone == null || stone.getCount() <= 0L) {
                                          this._player.getEnchantParams().upgradeItem = null;
                                       }

                                       if (isNeedUpdate) {
                                          if (Config.ENCHANT_CONSUME_ITEM != 0) {
                                             this._player
                                                .getInventory()
                                                .destroyItemByItemId(Config.ENCHANT_CONSUME_ITEM, (long)Config.ENCHANT_CONSUME_ITEM_COUNT, "[AutoEnchant]");
                                             Item template = ItemsParser.getInstance().getTemplate(Config.ENCHANT_CONSUME_ITEM);
                                             ServerMessage msg = new ServerMessage("Enchant.SPET_ITEMS", this._player.getLang());
                                             msg.add(Config.ENCHANT_CONSUME_ITEM_COUNT);
                                             msg.add(this._player.getItemName(template));
                                             this._player.sendMessage(msg.toString());
                                          }

                                          StatusUpdate su = new StatusUpdate(this._player);
                                          su.addAttribute(14, this._player.getCurrentLoad());
                                          this._player.sendPacket(su);
                                          this._player.sendItemList(false);
                                          this._player.broadcastCharInfo();
                                          Map<String, Integer> result = new HashMap<>();
                                          result.put("enchant", this.getAttValue());
                                          result.put("stones", stones);
                                          result.put("crystals", crystals);
                                          int sum = stones + crystals;
                                          if (sum == 0) {
                                             ++sum;
                                          }

                                          result.put("chance", (int)((double)success / ((double)sum / 100.0) * 100.0));
                                          result.put(
                                             "success",
                                             itemToEnchant == null ? 0 : (this.getAttValue() >= this._player.getEnchantParams().maxEnchantAtt ? 1 : 0)
                                          );
                                          EnchantManager.getInstance().showResultPage(this._player, EnchantType.ATTRIBUTE, result);
                                          if (isNeedEquip) {
                                             this._player.getInventory().equipItem(itemToEnchant);
                                          }
                                       }

                                       return;
                                    }

                                    if (stone == null || stone.getCount() <= 0L) {
                                       this._player.getEnchantParams().upgradeItem = null;
                                    }

                                    if (isNeedUpdate) {
                                       if (Config.ENCHANT_CONSUME_ITEM != 0) {
                                          this._player
                                             .getInventory()
                                             .destroyItemByItemId(Config.ENCHANT_CONSUME_ITEM, (long)Config.ENCHANT_CONSUME_ITEM_COUNT, "[AutoEnchant]");
                                          Item template = ItemsParser.getInstance().getTemplate(Config.ENCHANT_CONSUME_ITEM);
                                          ServerMessage msg = new ServerMessage("Enchant.SPET_ITEMS", this._player.getLang());
                                          msg.add(Config.ENCHANT_CONSUME_ITEM_COUNT);
                                          msg.add(this._player.getItemName(template));
                                          this._player.sendMessage(msg.toString());
                                       }

                                       StatusUpdate su = new StatusUpdate(this._player);
                                       su.addAttribute(14, this._player.getCurrentLoad());
                                       this._player.sendPacket(su);
                                       this._player.sendItemList(false);
                                       this._player.broadcastCharInfo();
                                       Map<String, Integer> result = new HashMap<>();
                                       result.put("enchant", this.getAttValue());
                                       result.put("stones", stones);
                                       result.put("crystals", crystals);
                                       int sum = stones + crystals;
                                       if (sum == 0) {
                                          ++sum;
                                       }

                                       result.put("chance", (int)((double)success / ((double)sum / 100.0) * 100.0));
                                       result.put(
                                          "success", itemToEnchant == null ? 0 : (this.getAttValue() >= this._player.getEnchantParams().maxEnchantAtt ? 1 : 0)
                                       );
                                       EnchantManager.getInstance().showResultPage(this._player, EnchantType.ATTRIBUTE, result);
                                       if (isNeedEquip) {
                                          this._player.getInventory().equipItem(itemToEnchant);
                                       }
                                    }

                                    return;
                                 }

                                 if (stone == null || stone.getCount() <= 0L) {
                                    this._player.getEnchantParams().upgradeItem = null;
                                 }

                                 if (isNeedUpdate) {
                                    if (Config.ENCHANT_CONSUME_ITEM != 0) {
                                       this._player
                                          .getInventory()
                                          .destroyItemByItemId(Config.ENCHANT_CONSUME_ITEM, (long)Config.ENCHANT_CONSUME_ITEM_COUNT, "[AutoEnchant]");
                                       Item template = ItemsParser.getInstance().getTemplate(Config.ENCHANT_CONSUME_ITEM);
                                       ServerMessage msg = new ServerMessage("Enchant.SPET_ITEMS", this._player.getLang());
                                       msg.add(Config.ENCHANT_CONSUME_ITEM_COUNT);
                                       msg.add(this._player.getItemName(template));
                                       this._player.sendMessage(msg.toString());
                                    }

                                    StatusUpdate su = new StatusUpdate(this._player);
                                    su.addAttribute(14, this._player.getCurrentLoad());
                                    this._player.sendPacket(su);
                                    this._player.sendItemList(false);
                                    this._player.broadcastCharInfo();
                                    Map<String, Integer> result = new HashMap<>();
                                    result.put("enchant", this.getAttValue());
                                    result.put("stones", stones);
                                    result.put("crystals", crystals);
                                    int sum = stones + crystals;
                                    if (sum == 0) {
                                       ++sum;
                                    }

                                    result.put("chance", (int)((double)success / ((double)sum / 100.0) * 100.0));
                                    result.put(
                                       "success", itemToEnchant == null ? 0 : (this.getAttValue() >= this._player.getEnchantParams().maxEnchantAtt ? 1 : 0)
                                    );
                                    EnchantManager.getInstance().showResultPage(this._player, EnchantType.ATTRIBUTE, result);
                                    if (isNeedEquip) {
                                       this._player.getInventory().equipItem(itemToEnchant);
                                    }
                                 }

                                 return;
                              }

                              if (stone == null || stone.getCount() <= 0L) {
                                 this._player.getEnchantParams().upgradeItem = null;
                              }

                              if (isNeedUpdate) {
                                 if (Config.ENCHANT_CONSUME_ITEM != 0) {
                                    this._player
                                       .getInventory()
                                       .destroyItemByItemId(Config.ENCHANT_CONSUME_ITEM, (long)Config.ENCHANT_CONSUME_ITEM_COUNT, "[AutoEnchant]");
                                    Item template = ItemsParser.getInstance().getTemplate(Config.ENCHANT_CONSUME_ITEM);
                                    ServerMessage msg = new ServerMessage("Enchant.SPET_ITEMS", this._player.getLang());
                                    msg.add(Config.ENCHANT_CONSUME_ITEM_COUNT);
                                    msg.add(this._player.getItemName(template));
                                    this._player.sendMessage(msg.toString());
                                 }

                                 StatusUpdate su = new StatusUpdate(this._player);
                                 su.addAttribute(14, this._player.getCurrentLoad());
                                 this._player.sendPacket(su);
                                 this._player.sendItemList(false);
                                 this._player.broadcastCharInfo();
                                 Map<String, Integer> result = new HashMap<>();
                                 result.put("enchant", this.getAttValue());
                                 result.put("stones", stones);
                                 result.put("crystals", crystals);
                                 int sum = stones + crystals;
                                 if (sum == 0) {
                                    ++sum;
                                 }

                                 result.put("chance", (int)((double)success / ((double)sum / 100.0) * 100.0));
                                 result.put(
                                    "success", itemToEnchant == null ? 0 : (this.getAttValue() >= this._player.getEnchantParams().maxEnchantAtt ? 1 : 0)
                                 );
                                 EnchantManager.getInstance().showResultPage(this._player, EnchantType.ATTRIBUTE, result);
                                 if (isNeedEquip) {
                                    this._player.getInventory().equipItem(itemToEnchant);
                                 }
                              }

                              return;
                           }

                           if (stone == null || stone.getCount() <= 0L) {
                              this._player.getEnchantParams().upgradeItem = null;
                           }

                           if (isNeedUpdate) {
                              if (Config.ENCHANT_CONSUME_ITEM != 0) {
                                 this._player
                                    .getInventory()
                                    .destroyItemByItemId(Config.ENCHANT_CONSUME_ITEM, (long)Config.ENCHANT_CONSUME_ITEM_COUNT, "[AutoEnchant]");
                                 Item template = ItemsParser.getInstance().getTemplate(Config.ENCHANT_CONSUME_ITEM);
                                 ServerMessage msg = new ServerMessage("Enchant.SPET_ITEMS", this._player.getLang());
                                 msg.add(Config.ENCHANT_CONSUME_ITEM_COUNT);
                                 msg.add(this._player.getItemName(template));
                                 this._player.sendMessage(msg.toString());
                              }

                              StatusUpdate su = new StatusUpdate(this._player);
                              su.addAttribute(14, this._player.getCurrentLoad());
                              this._player.sendPacket(su);
                              this._player.sendItemList(false);
                              this._player.broadcastCharInfo();
                              Map<String, Integer> result = new HashMap<>();
                              result.put("enchant", this.getAttValue());
                              result.put("stones", stones);
                              result.put("crystals", crystals);
                              int sum = stones + crystals;
                              if (sum == 0) {
                                 ++sum;
                              }

                              result.put("chance", (int)((double)success / ((double)sum / 100.0) * 100.0));
                              result.put("success", itemToEnchant == null ? 0 : (this.getAttValue() >= this._player.getEnchantParams().maxEnchantAtt ? 1 : 0));
                              EnchantManager.getInstance().showResultPage(this._player, EnchantType.ATTRIBUTE, result);
                              if (isNeedEquip) {
                                 this._player.getInventory().equipItem(itemToEnchant);
                              }
                           }

                           return;
                        }

                        if (stone == null || stone.getCount() <= 0L) {
                           this._player.getEnchantParams().upgradeItem = null;
                        }

                        if (isNeedUpdate) {
                           if (Config.ENCHANT_CONSUME_ITEM != 0) {
                              this._player
                                 .getInventory()
                                 .destroyItemByItemId(Config.ENCHANT_CONSUME_ITEM, (long)Config.ENCHANT_CONSUME_ITEM_COUNT, "[AutoEnchant]");
                              Item template = ItemsParser.getInstance().getTemplate(Config.ENCHANT_CONSUME_ITEM);
                              ServerMessage msg = new ServerMessage("Enchant.SPET_ITEMS", this._player.getLang());
                              msg.add(Config.ENCHANT_CONSUME_ITEM_COUNT);
                              msg.add(this._player.getItemName(template));
                              this._player.sendMessage(msg.toString());
                           }

                           StatusUpdate su = new StatusUpdate(this._player);
                           su.addAttribute(14, this._player.getCurrentLoad());
                           this._player.sendPacket(su);
                           this._player.sendItemList(false);
                           this._player.broadcastCharInfo();
                           Map<String, Integer> result = new HashMap<>();
                           result.put("enchant", this.getAttValue());
                           result.put("stones", stones);
                           result.put("crystals", crystals);
                           int sum = stones + crystals;
                           if (sum == 0) {
                              ++sum;
                           }

                           result.put("chance", (int)((double)success / ((double)sum / 100.0) * 100.0));
                           result.put("success", itemToEnchant == null ? 0 : (this.getAttValue() >= this._player.getEnchantParams().maxEnchantAtt ? 1 : 0));
                           EnchantManager.getInstance().showResultPage(this._player, EnchantType.ATTRIBUTE, result);
                           if (isNeedEquip) {
                              this._player.getInventory().equipItem(itemToEnchant);
                           }
                        }

                        return;
                     }

                     if (stone == null || stone.getCount() <= 0L) {
                        this._player.getEnchantParams().upgradeItem = null;
                     }

                     if (isNeedUpdate) {
                        if (Config.ENCHANT_CONSUME_ITEM != 0) {
                           this._player
                              .getInventory()
                              .destroyItemByItemId(Config.ENCHANT_CONSUME_ITEM, (long)Config.ENCHANT_CONSUME_ITEM_COUNT, "[AutoEnchant]");
                           Item template = ItemsParser.getInstance().getTemplate(Config.ENCHANT_CONSUME_ITEM);
                           ServerMessage msg = new ServerMessage("Enchant.SPET_ITEMS", this._player.getLang());
                           msg.add(Config.ENCHANT_CONSUME_ITEM_COUNT);
                           msg.add(this._player.getItemName(template));
                           this._player.sendMessage(msg.toString());
                        }

                        StatusUpdate su = new StatusUpdate(this._player);
                        su.addAttribute(14, this._player.getCurrentLoad());
                        this._player.sendPacket(su);
                        this._player.sendItemList(false);
                        this._player.broadcastCharInfo();
                        Map<String, Integer> result = new HashMap<>();
                        result.put("enchant", this.getAttValue());
                        result.put("stones", stones);
                        result.put("crystals", crystals);
                        int sum = stones + crystals;
                        if (sum == 0) {
                           ++sum;
                        }

                        result.put("chance", (int)((double)success / ((double)sum / 100.0) * 100.0));
                        result.put("success", itemToEnchant == null ? 0 : (this.getAttValue() >= this._player.getEnchantParams().maxEnchantAtt ? 1 : 0));
                        EnchantManager.getInstance().showResultPage(this._player, EnchantType.ATTRIBUTE, result);
                        if (isNeedEquip) {
                           this._player.getInventory().equipItem(itemToEnchant);
                        }
                     }

                     return;
                  }

                  if (stone == null || stone.getCount() <= 0L) {
                     this._player.getEnchantParams().upgradeItem = null;
                  }

                  if (isNeedUpdate) {
                     if (Config.ENCHANT_CONSUME_ITEM != 0) {
                        this._player.getInventory().destroyItemByItemId(Config.ENCHANT_CONSUME_ITEM, (long)Config.ENCHANT_CONSUME_ITEM_COUNT, "[AutoEnchant]");
                        Item template = ItemsParser.getInstance().getTemplate(Config.ENCHANT_CONSUME_ITEM);
                        ServerMessage msg = new ServerMessage("Enchant.SPET_ITEMS", this._player.getLang());
                        msg.add(Config.ENCHANT_CONSUME_ITEM_COUNT);
                        msg.add(this._player.getItemName(template));
                        this._player.sendMessage(msg.toString());
                     }

                     StatusUpdate su = new StatusUpdate(this._player);
                     su.addAttribute(14, this._player.getCurrentLoad());
                     this._player.sendPacket(su);
                     this._player.sendItemList(false);
                     this._player.broadcastCharInfo();
                     Map<String, Integer> result = new HashMap<>();
                     result.put("enchant", this.getAttValue());
                     result.put("stones", stones);
                     result.put("crystals", crystals);
                     int sum = stones + crystals;
                     if (sum == 0) {
                        ++sum;
                     }

                     result.put("chance", (int)((double)success / ((double)sum / 100.0) * 100.0));
                     result.put("success", itemToEnchant == null ? 0 : (this.getAttValue() >= this._player.getEnchantParams().maxEnchantAtt ? 1 : 0));
                     EnchantManager.getInstance().showResultPage(this._player, EnchantType.ATTRIBUTE, result);
                     if (isNeedEquip) {
                        this._player.getInventory().equipItem(itemToEnchant);
                     }
                  }

                  return;
               }

               if (stone == null || stone.getCount() <= 0L) {
                  this._player.getEnchantParams().upgradeItem = null;
               }

               if (isNeedUpdate) {
                  if (Config.ENCHANT_CONSUME_ITEM != 0) {
                     this._player.getInventory().destroyItemByItemId(Config.ENCHANT_CONSUME_ITEM, (long)Config.ENCHANT_CONSUME_ITEM_COUNT, "[AutoEnchant]");
                     Item template = ItemsParser.getInstance().getTemplate(Config.ENCHANT_CONSUME_ITEM);
                     ServerMessage msg = new ServerMessage("Enchant.SPET_ITEMS", this._player.getLang());
                     msg.add(Config.ENCHANT_CONSUME_ITEM_COUNT);
                     msg.add(this._player.getItemName(template));
                     this._player.sendMessage(msg.toString());
                  }

                  StatusUpdate su = new StatusUpdate(this._player);
                  su.addAttribute(14, this._player.getCurrentLoad());
                  this._player.sendPacket(su);
                  this._player.sendItemList(false);
                  this._player.broadcastCharInfo();
                  Map<String, Integer> result = new HashMap<>();
                  result.put("enchant", this.getAttValue());
                  result.put("stones", stones);
                  result.put("crystals", crystals);
                  int sum = stones + crystals;
                  if (sum == 0) {
                     ++sum;
                  }

                  result.put("chance", (int)((double)success / ((double)sum / 100.0) * 100.0));
                  result.put("success", itemToEnchant == null ? 0 : (this.getAttValue() >= this._player.getEnchantParams().maxEnchantAtt ? 1 : 0));
                  EnchantManager.getInstance().showResultPage(this._player, EnchantType.ATTRIBUTE, result);
                  if (isNeedEquip) {
                     this._player.getInventory().equipItem(itemToEnchant);
                  }
               }

               return;
            }

            if (stone == null || stone.getCount() <= 0L) {
               this._player.getEnchantParams().upgradeItem = null;
            }

            if (isNeedUpdate) {
               if (Config.ENCHANT_CONSUME_ITEM != 0) {
                  this._player.getInventory().destroyItemByItemId(Config.ENCHANT_CONSUME_ITEM, (long)Config.ENCHANT_CONSUME_ITEM_COUNT, "[AutoEnchant]");
                  Item template = ItemsParser.getInstance().getTemplate(Config.ENCHANT_CONSUME_ITEM);
                  ServerMessage msg = new ServerMessage("Enchant.SPET_ITEMS", this._player.getLang());
                  msg.add(Config.ENCHANT_CONSUME_ITEM_COUNT);
                  msg.add(this._player.getItemName(template));
                  this._player.sendMessage(msg.toString());
               }

               StatusUpdate su = new StatusUpdate(this._player);
               su.addAttribute(14, this._player.getCurrentLoad());
               this._player.sendPacket(su);
               this._player.sendItemList(false);
               this._player.broadcastCharInfo();
               Map<String, Integer> result = new HashMap<>();
               result.put("enchant", this.getAttValue());
               result.put("stones", stones);
               result.put("crystals", crystals);
               int sum = stones + crystals;
               if (sum == 0) {
                  ++sum;
               }

               result.put("chance", (int)((double)success / ((double)sum / 100.0) * 100.0));
               result.put("success", itemToEnchant == null ? 0 : (this.getAttValue() >= this._player.getEnchantParams().maxEnchantAtt ? 1 : 0));
               EnchantManager.getInstance().showResultPage(this._player, EnchantType.ATTRIBUTE, result);
               if (isNeedEquip) {
                  this._player.getInventory().equipItem(itemToEnchant);
               }
            }

            return;
         }

         if (stone == null || stone.getCount() <= 0L) {
            this._player.getEnchantParams().upgradeItem = null;
         }

         if (isNeedUpdate) {
            if (Config.ENCHANT_CONSUME_ITEM != 0) {
               this._player.getInventory().destroyItemByItemId(Config.ENCHANT_CONSUME_ITEM, (long)Config.ENCHANT_CONSUME_ITEM_COUNT, "[AutoEnchant]");
               Item template = ItemsParser.getInstance().getTemplate(Config.ENCHANT_CONSUME_ITEM);
               ServerMessage msg = new ServerMessage("Enchant.SPET_ITEMS", this._player.getLang());
               msg.add(Config.ENCHANT_CONSUME_ITEM_COUNT);
               msg.add(this._player.getItemName(template));
               this._player.sendMessage(msg.toString());
            }

            StatusUpdate su = new StatusUpdate(this._player);
            su.addAttribute(14, this._player.getCurrentLoad());
            this._player.sendPacket(su);
            this._player.sendItemList(false);
            this._player.broadcastCharInfo();
            Map<String, Integer> result = new HashMap<>();
            result.put("enchant", this.getAttValue());
            result.put("stones", stones);
            result.put("crystals", crystals);
            int sum = stones + crystals;
            if (sum == 0) {
               ++sum;
            }

            result.put("chance", (int)((double)success / ((double)sum / 100.0) * 100.0));
            result.put("success", itemToEnchant == null ? 0 : (this.getAttValue() >= this._player.getEnchantParams().maxEnchantAtt ? 1 : 0));
            EnchantManager.getInstance().showResultPage(this._player, EnchantType.ATTRIBUTE, result);
            if (isNeedEquip) {
               this._player.getInventory().equipItem(itemToEnchant);
            }
         }
      }
   }

   private boolean checkAttributeLvl(Player player) {
      if (player == null) {
         return false;
      } else {
         return this.getAttValue() < player.getEnchantParams().maxEnchantAtt;
      }
   }

   private int getAttValue() {
      ItemInstance targetItem = this._player.getEnchantParams().targetItem;
      ItemInstance enchantItem = this._player.getEnchantParams().upgradeItem;
      if (targetItem == null) {
         return 0;
      } else {
         int usedStoneId;
         if (enchantItem == null) {
            usedStoneId = this.stoneId;
         } else {
            usedStoneId = enchantItem.getId();
         }

         if (targetItem.isWeapon()) {
            return targetItem.getAttackElementPower();
         } else {
            Elementals element = targetItem.getElemental(Elementals.getReverseElement(Elementals.getElementById(usedStoneId)));
            return element == null ? 0 : element.getValue();
         }
      }
   }

   public int getLimit(ItemInstance item, int sotneId) {
      Elementals.ElementalItems elementItem = Elementals.getItemElemental(sotneId);
      if (elementItem == null) {
         return 0;
      } else {
         return item.isWeapon() ? Elementals.WEAPON_VALUES[elementItem._type._maxLevel] : Elementals.ARMOR_VALUES[elementItem._type._maxLevel];
      }
   }
}
