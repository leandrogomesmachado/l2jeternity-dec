package l2e.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.MultiSellParser;
import l2e.gameserver.model.Augmentation;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.model.items.multisell.Entry;
import l2e.gameserver.model.items.multisell.Ingredient;
import l2e.gameserver.model.items.multisell.PreparedListContainer;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class RequestMultiSellChoose extends GameClientPacket {
   private int _listId;
   private int _entryId;
   private long _amount;

   @Override
   protected void readImpl() {
      this._listId = this.readD();
      this._entryId = this.readD();
      this._amount = this.readQ();
   }

   @Override
   public void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (player.isActionsDisabled() || player.getActiveTradeList() != null) {
            player.sendActionFailed();
         } else if (this._amount >= 1L && this._amount <= (long)Config.MAX_AMOUNT_BY_MULTISELL) {
            PreparedListContainer list = player.getMultiSell();
            if (list == null || list.getListId() != this._listId) {
               player.setMultiSell(null);
            } else if (player.isProcessingTransaction()) {
               player.setMultiSell(null);
            } else {
               Npc target = player.getLastFolkNPC();

               for(Entry entry : list.getEntries()) {
                  if (entry.getEntryId() == this._entryId) {
                     if (!entry.isStackable() && this._amount > 1L) {
                        _log.severe(
                           "Character: "
                              + player.getName()
                              + " is trying to set amount > 1 on non-stackable multisell, id:"
                              + this._listId
                              + ":"
                              + this._entryId
                        );
                        player.setMultiSell(null);
                        return;
                     }

                     PcInventory inv = player.getInventory();
                     int slots = 0;
                     int weight = 0;

                     for(Ingredient e : entry.getProducts()) {
                        if (e.getId() >= 0) {
                           if (!e.isStackable()) {
                              slots = (int)((long)slots + e.getCount() * this._amount);
                           } else if (player.getInventory().getItemByItemId(e.getId()) == null) {
                              ++slots;
                           }

                           weight = (int)((long)weight + e.getCount() * this._amount * (long)e.getWeight());
                        }
                     }

                     if (!inv.validateWeight((long)weight)) {
                        player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
                        return;
                     }

                     if (!inv.validateCapacity((long)slots)) {
                        player.sendPacket(SystemMessageId.SLOTS_FULL);
                        return;
                     }

                     ArrayList<Ingredient> ingredientsList = new ArrayList<>(entry.getIngredients().size());

                     for(Ingredient e : entry.getIngredients()) {
                        boolean newIng = true;
                        int i = ingredientsList.size();

                        while(--i >= 0) {
                           Ingredient ex = ingredientsList.get(i);
                           if (ex.getId() == e.getId() && ex.getEnchantLevel() == e.getEnchantLevel()) {
                              if (ex.getCount() + e.getCount() > Long.MAX_VALUE) {
                                 player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
                                 return;
                              }

                              Ingredient ing = ex.getCopy();
                              ing.setCount(ex.getCount() + e.getCount());
                              ingredientsList.set(i, ing);
                              newIng = false;
                              break;
                           }
                        }

                        if (newIng) {
                           ingredientsList.add(e);
                        }
                     }

                     for(Ingredient e : ingredientsList) {
                        if (e.getCount() * this._amount > Long.MAX_VALUE) {
                           player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
                           return;
                        }

                        if (e.getId() < 0) {
                           if (!MultiSellParser.checkSpecialIngredient(e.getId(), e.getCount() * this._amount, player)) {
                              return;
                           }
                        } else {
                           long required = !Config.ALT_BLACKSMITH_USE_RECIPES && e.getMaintainIngredient() ? e.getCount() : e.getCount() * this._amount;
                           if (inv.getInventoryItemCount(e.getId(), list.getMaintainEnchantment() ? e.getEnchantLevel() : -1, false) < required) {
                              SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_UNIT_OF_THE_ITEM_S1_REQUIRED);
                              sm.addItemName(e.getTemplate());
                              sm.addNumber((int)required);
                              player.sendPacket(sm);
                              return;
                           }
                        }
                     }

                     List<Augmentation> augmentation = new ArrayList<>();
                     Elementals[] elemental = null;

                     for(Ingredient e : entry.getIngredients()) {
                        if (e.getId() < 0) {
                           if (!MultiSellParser.getSpecialIngredient(e.getId(), e.getCount() * this._amount, player)) {
                              return;
                           }
                        } else {
                           ItemInstance itemToTake = inv.getItemByItemId(e.getId());
                           if (itemToTake == null) {
                              _log.severe("Character: " + player.getName() + " is trying to cheat in multisell, id:" + this._listId + ":" + this._entryId);
                              player.setMultiSell(null);
                              return;
                           }

                           if (Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMaintainIngredient()) {
                              if (itemToTake.isStackable()) {
                                 if (!player.destroyItem("Multisell", itemToTake.getObjectId(), e.getCount() * this._amount, player.getTarget(), true)) {
                                    player.setMultiSell(null);
                                    return;
                                 }
                              } else if (list.getMaintainEnchantment()) {
                                 ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getId(), e.getEnchantLevel(), false);

                                 for(int i = 0; (long)i < e.getCount() * this._amount; ++i) {
                                    if (inventoryContents[i].isAugmented()) {
                                       augmentation.add(inventoryContents[i].getAugmentation());
                                    }

                                    if (inventoryContents[i].getElementals() != null) {
                                       elemental = inventoryContents[i].getElementals();
                                    }

                                    if (!player.destroyItem("Multisell", inventoryContents[i].getObjectId(), 1L, player.getTarget(), true)) {
                                       player.setMultiSell(null);
                                       return;
                                    }
                                 }
                              } else {
                                 for(int i = 1; (long)i <= e.getCount() * this._amount; ++i) {
                                    ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getId(), false);
                                    itemToTake = inventoryContents[0];
                                    if (itemToTake.getEnchantLevel() > 0) {
                                       for(ItemInstance item : inventoryContents) {
                                          if (item.getEnchantLevel() < itemToTake.getEnchantLevel()) {
                                             itemToTake = item;
                                             if (item.getEnchantLevel() == 0) {
                                                break;
                                             }
                                          }
                                       }
                                    }

                                    if (!player.destroyItem("Multisell", itemToTake.getObjectId(), 1L, player.getTarget(), true)) {
                                       player.setMultiSell(null);
                                       return;
                                    }
                                 }
                              }
                           }
                        }
                     }

                     for(Ingredient e : entry.getProducts()) {
                        if (e.getId() < 0) {
                           MultiSellParser.addSpecialProduct(e.getId(), e.getCount() * this._amount, player);
                        } else {
                           if (e.isStackable()) {
                              inv.addItem("Multisell", e.getId(), e.getCount() * this._amount, player, player.getTarget());
                           } else {
                              ItemInstance product = null;

                              for(int i = 0; (long)i < e.getCount() * this._amount; ++i) {
                                 product = inv.addItem("Multisell", e.getId(), 1L, player, player.getTarget());
                                 if (product != null && list.getMaintainEnchantment()) {
                                    if (e.getAugmentationId() > 0) {
                                       product.setAugmentation(new Augmentation(e.getAugmentationId()));
                                    } else if (i < augmentation.size()) {
                                       product.setAugmentation(new Augmentation(augmentation.get(i).getAugmentationId()));
                                    }

                                    if (e.getElementals() != null) {
                                       for(Elementals elm : e.getElementals()) {
                                          product.setElementAttr(elm.getElement(), elm.getValue());
                                       }
                                    } else if (elemental != null) {
                                       for(Elementals elm : elemental) {
                                          product.setElementAttr(elm.getElement(), elm.getValue());
                                       }
                                    }

                                    if (e.getTimeLimit() > 0) {
                                       product.setTime((long)e.getTimeLimit());
                                    }

                                    product.setEnchantLevel(e.getEnchantLevel());
                                    product.updateDatabase();
                                 }
                              }
                           }

                           if (e.getCount() * this._amount > 1L) {
                              SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
                              sm.addItemName(e.getId());
                              sm.addItemNumber(e.getCount() * this._amount);
                              player.sendPacket(sm);
                              SystemMessage var43 = null;
                           } else {
                              SystemMessage sm;
                              if (list.getMaintainEnchantment() && e.getEnchantLevel() > 0) {
                                 sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_S2);
                                 sm.addItemNumber((long)e.getEnchantLevel());
                                 sm.addItemName(e.getId());
                              } else {
                                 sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
                                 sm.addItemName(e.getId());
                              }

                              player.sendPacket(sm);
                              Object var41 = null;
                           }
                        }
                     }

                     player.sendItemList(false);
                     StatusUpdate su = new StatusUpdate(player);
                     su.addAttribute(14, player.getCurrentLoad());
                     player.sendPacket(su);
                     StatusUpdate var32 = null;
                     if (entry.getTaxAmount() > 0L) {
                        target.getCastle().addToTreasury(entry.getTaxAmount() * this._amount);
                     }
                     break;
                  }
               }

               if (list.getMaintainEnchantment() && list.isNpcOnly()) {
                  MultiSellParser.getInstance().separateAndSend(this._listId, player, target, true);
               }
            }
         } else {
            player.setMultiSell(null);
         }
      }
   }
}
