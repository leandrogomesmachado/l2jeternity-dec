package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExAttributeEnchantResult;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class RequestExEnchantItemAttribute extends GameClientPacket {
   private int _objectId;

   @Override
   protected void readImpl() {
      this._objectId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getActiveChar();
      if (player != null) {
         if (player.isActionsDisabled()) {
            player.setActiveEnchantAttrItemId(-1);
            player.sendActionFailed();
         } else if (this._objectId == -1) {
            player.setActiveEnchantAttrItemId(-1);
            player.sendPacket(SystemMessageId.ELEMENTAL_ENHANCE_CANCELED);
         } else if (!player.isOnline()) {
            player.setActiveEnchantAttrItemId(-1);
         } else if (player.getPrivateStoreType() != 0) {
            player.sendPacket(SystemMessageId.CANNOT_ADD_ELEMENTAL_POWER_WHILE_OPERATING_PRIVATE_STORE_OR_WORKSHOP);
            player.setActiveEnchantAttrItemId(-1);
         } else if (player.getActiveRequester() != null) {
            player.cancelActiveTrade();
            player.setActiveEnchantAttrItemId(-1);
            player.sendMessage("You cannot add elemental power while trading.");
         } else {
            ItemInstance item = player.getInventory().getItemByObjectId(this._objectId);
            ItemInstance stone = player.getInventory().getItemByObjectId(player.getActiveEnchantAttrItemId());
            if (item == null || stone == null) {
               player.setActiveEnchantAttrItemId(-1);
               player.sendPacket(SystemMessageId.ELEMENTAL_ENHANCE_CANCELED);
            } else if (!item.isElementable()) {
               player.sendPacket(SystemMessageId.ELEMENTAL_ENHANCE_REQUIREMENT_NOT_SUFFICIENT);
               player.setActiveEnchantAttrItemId(-1);
            } else {
               switch(item.getItemLocation()) {
                  case INVENTORY:
                  case PAPERDOLL:
                     if (item.getOwnerId() != player.getObjectId()) {
                        player.setActiveEnchantAttrItemId(-1);
                        return;
                     } else {
                        int stoneId = stone.getId();
                        byte elementToAdd = Elementals.getItemElement(stoneId);
                        if (item.isArmor()) {
                           elementToAdd = Elementals.getOppositeElement(elementToAdd);
                        }

                        byte opositeElement = Elementals.getOppositeElement(elementToAdd);
                        Elementals oldElement = item.getElemental(elementToAdd);
                        int elementValue = oldElement == null ? 0 : oldElement.getValue();
                        int limit = this.getLimit(item, stoneId);
                        int powerToAdd = this.getPowerToAdd(stoneId, elementValue, item);
                        if ((!item.isWeapon() || oldElement == null || oldElement.getElement() == elementToAdd || oldElement.getElement() == -2)
                           && (!item.isArmor() || item.getElemental(elementToAdd) != null || item.getElementals() == null || item.getElementals().length < 3)) {
                           if (item.isArmor() && item.getElementals() != null) {
                              for(Elementals elm : item.getElementals()) {
                                 if (elm.getElement() == opositeElement) {
                                    player.setActiveEnchantAttrItemId(-1);
                                    Util.handleIllegalPlayerAction(player, "" + player.getName() + " tried to add oposite attribute to item!");
                                    return;
                                 }
                              }
                           }

                           int newPower = elementValue + powerToAdd;
                           if (newPower > limit) {
                              newPower = limit;
                              powerToAdd = limit - elementValue;
                           }

                           if (powerToAdd <= 0) {
                              player.sendPacket(SystemMessageId.ELEMENTAL_ENHANCE_CANCELED);
                              player.setActiveEnchantAttrItemId(-1);
                              return;
                           }

                           if (!player.destroyItem("AttrEnchant", stone, 1L, player, true)) {
                              player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                              Util.handleIllegalPlayerAction(player, "" + player.getName() + " tried to attribute enchant with a stone he doesn't have");
                              player.setActiveEnchantAttrItemId(-1);
                              return;
                           }

                           boolean success = false;
                           switch(Elementals.getItemElemental(stoneId)._type) {
                              case Stone:
                              case Roughore:
                                 success = (double)Rnd.get(100) < Config.ENCHANT_CHANCE_ELEMENT_STONE + (double)player.getPremiumBonus().getEnchantChance();
                                 break;
                              case Crystal:
                                 success = (double)Rnd.get(100) < Config.ENCHANT_CHANCE_ELEMENT_CRYSTAL + (double)player.getPremiumBonus().getEnchantChance();
                                 break;
                              case Jewel:
                                 success = (double)Rnd.get(100) < Config.ENCHANT_CHANCE_ELEMENT_JEWEL + (double)player.getPremiumBonus().getEnchantChance();
                                 break;
                              case Energy:
                                 success = (double)Rnd.get(100) < Config.ENCHANT_CHANCE_ELEMENT_ENERGY + (double)player.getPremiumBonus().getEnchantChance();
                           }

                           if (success) {
                              byte realElement = item.isArmor() ? opositeElement : elementToAdd;
                              SystemMessage sm;
                              if (item.getEnchantLevel() == 0) {
                                 if (item.isArmor()) {
                                    sm = SystemMessage.getSystemMessage(SystemMessageId.THE_S2_ATTRIBUTE_WAS_SUCCESSFULLY_BESTOWED_ON_S1_RES_TO_S3_INCREASED);
                                 } else {
                                    sm = SystemMessage.getSystemMessage(SystemMessageId.ELEMENTAL_POWER_S2_SUCCESSFULLY_ADDED_TO_S1);
                                 }

                                 sm.addItemName(item);
                                 sm.addElemental(realElement);
                                 if (item.isArmor()) {
                                    sm.addElemental(Elementals.getOppositeElement(realElement));
                                 }
                              } else {
                                 if (item.isArmor()) {
                                    sm = SystemMessage.getSystemMessage(SystemMessageId.THE_S3_ATTRIBUTE_BESTOWED_ON_S1_S2_RESISTANCE_TO_S4_INCREASED);
                                 } else {
                                    sm = SystemMessage.getSystemMessage(SystemMessageId.ELEMENTAL_POWER_S3_SUCCESSFULLY_ADDED_TO_S1_S2);
                                 }

                                 sm.addNumber(item.getEnchantLevel());
                                 sm.addItemName(item);
                                 sm.addElemental(realElement);
                                 if (item.isArmor()) {
                                    sm.addElemental(Elementals.getOppositeElement(realElement));
                                 }
                              }

                              player.sendPacket(sm);
                              item.setElementAttr(elementToAdd, newPower);
                              if (item.isEquipped()) {
                                 item.updateElementAttrBonus(player);
                              }

                              InventoryUpdate iu = new InventoryUpdate();
                              iu.addModifiedItem(item);
                              player.sendPacket(iu);
                           } else {
                              player.sendPacket(SystemMessageId.FAILED_ADDING_ELEMENTAL_POWER);
                           }

                           player.sendPacket(new ExAttributeEnchantResult(powerToAdd));
                           player.sendUserInfo();
                           player.setActiveEnchantAttrItemId(-1);
                           return;
                        }

                        player.sendPacket(SystemMessageId.ANOTHER_ELEMENTAL_POWER_ALREADY_ADDED);
                        player.setActiveEnchantAttrItemId(-1);
                        return;
                     }
                  default:
                     player.setActiveEnchantAttrItemId(-1);
                     Util.handleIllegalPlayerAction(player, "" + player.getName() + " tried to use enchant Exploit!");
               }
            }
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

   public int getPowerToAdd(int stoneId, int oldValue, ItemInstance item) {
      if (Elementals.getItemElement(stoneId) != -1) {
         if (item.isWeapon()) {
            if (oldValue == 0) {
               return 20;
            }

            return 5;
         }

         if (item.isArmor()) {
            return 6;
         }
      }

      return 0;
   }
}
