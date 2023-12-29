package l2e.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.ItemContainer;
import l2e.gameserver.model.items.itemcontainer.PcWarehouse;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.StatusUpdate;

public final class SendWareHouseDepositList extends GameClientPacket {
   private static final int BATCH_LENGTH = 12;
   private List<ItemHolder> _items = null;

   @Override
   protected void readImpl() {
      int size = this.readD();
      if (size > 0 && size <= Config.MAX_ITEM_IN_PACKET && size * 12 == this._buf.remaining()) {
         this._items = new ArrayList<>(size);

         for(int i = 0; i < size; ++i) {
            int objId = this.readD();
            long count = this.readQ();
            if (objId < 1 || count < 0L) {
               this._items = null;
               return;
            }

            this._items.add(new ItemHolder(objId, count));
         }
      }
   }

   @Override
   protected void runImpl() {
      if (this._items != null) {
         Player player = this.getClient().getActiveChar();
         if (player != null) {
            if (!player.isActionsDisabled() && player.getActiveTradeList() == null) {
               ItemContainer warehouse = player.getActiveWarehouse();
               if (warehouse != null) {
                  boolean isPrivate = warehouse instanceof PcWarehouse;
                  Npc manager = player.getLastFolkNPC();
                  if (manager == null || !manager.isWarehouse() || manager.canInteract(player)) {
                     if (!isPrivate && !player.getAccessLevel().allowTransaction()) {
                        player.sendMessage("Transactions are disabled for your Access Level.");
                     } else if (player.getActiveEnchantItemId() != -1) {
                        Util.handleIllegalPlayerAction(player, "" + player.getName() + " tried to use enchant Exploit!");
                     } else if (Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE || player.getKarma() <= 0) {
                        long fee = (long)(this._items.size() * 30);
                        long currentAdena = player.getAdena();
                        int slots = 0;

                        for(ItemHolder i : this._items) {
                           ItemInstance item = player.checkItemManipulation(i.getId(), i.getCount(), "deposit");
                           if (item == null) {
                              Util.handleIllegalPlayerAction(player, "Error depositing a warehouse object for char " + player.getName() + " (validity check)!");
                              if (Config.DEBUG) {
                                 _log.warning("Error depositing a warehouse object for char " + player.getName() + " (validity check)");
                              }

                              return;
                           }

                           if (item.getId() == 57) {
                              currentAdena -= i.getCount();
                           }

                           if (!item.isStackable()) {
                              slots = (int)((long)slots + i.getCount());
                           } else if (warehouse.getItemByItemId(item.getId()) == null) {
                              ++slots;
                           }
                        }

                        if (!warehouse.validateCapacity((long)slots)) {
                           player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
                        } else if (currentAdena >= fee && player.reduceAdena(warehouse.getName(), fee, manager, false)) {
                           if (player.getActiveTradeList() == null) {
                              InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();

                              for(ItemHolder i : this._items) {
                                 ItemInstance oldItem = player.checkItemManipulation(i.getId(), i.getCount(), "deposit");
                                 if (oldItem == null) {
                                    _log.warning("Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
                                    return;
                                 }

                                 if (oldItem.isDepositable(isPrivate) && oldItem.isAvailable(player, true, isPrivate)) {
                                    ItemInstance newItem = player.getInventory()
                                       .transferItem(warehouse.getName(), i.getId(), i.getCount(), warehouse, player, manager);
                                    if (newItem == null) {
                                       _log.warning("Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
                                    } else if (playerIU != null) {
                                       if (oldItem.getCount() > 0L && oldItem != newItem) {
                                          playerIU.addModifiedItem(oldItem);
                                       } else {
                                          playerIU.addRemovedItem(oldItem);
                                       }
                                    }
                                 }
                              }

                              if (playerIU != null) {
                                 player.sendPacket(playerIU);
                              } else {
                                 player.sendItemList(false);
                              }

                              StatusUpdate su = new StatusUpdate(player);
                              su.addAttribute(14, player.getCurrentLoad());
                              player.sendPacket(su);
                           }
                        } else {
                           player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
                        }
                     }
                  }
               }
            } else {
               player.sendActionFailed();
            }
         }
      }
   }
}
