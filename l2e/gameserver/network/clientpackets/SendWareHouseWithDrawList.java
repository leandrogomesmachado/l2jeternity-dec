package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.ClanWarehouse;
import l2e.gameserver.model.items.itemcontainer.ItemContainer;
import l2e.gameserver.model.items.itemcontainer.PcWarehouse;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.StatusUpdate;

public final class SendWareHouseWithDrawList extends GameClientPacket {
   private static final int BATCH_LENGTH = 12;
   private ItemHolder[] _items = null;

   @Override
   protected void readImpl() {
      int count = this.readD();
      if (count > 0 && count <= Config.MAX_ITEM_IN_PACKET && count * 12 == this._buf.remaining()) {
         this._items = new ItemHolder[count];

         for(int i = 0; i < count; ++i) {
            int objId = this.readD();
            long cnt = this.readQ();
            if (objId < 1 || cnt < 0L) {
               this._items = null;
               return;
            }

            this._items[i] = new ItemHolder(objId, cnt);
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
                  Npc manager = player.getLastFolkNPC();
                  if (manager == null || !manager.isWarehouse() || manager.canInteract(player)) {
                     if (!(warehouse instanceof PcWarehouse) && !player.getAccessLevel().allowTransaction()) {
                        player.sendMessage("Transactions are disabled for your Access Level.");
                     } else if (Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE || player.getKarma() <= 0) {
                        if (Config.ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH) {
                           if (warehouse instanceof ClanWarehouse && (player.getClanPrivileges() & 8) != 8) {
                              return;
                           }
                        } else if (warehouse instanceof ClanWarehouse && !player.isClanLeader()) {
                           player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE);
                           return;
                        }

                        int weight = 0;
                        int slots = 0;

                        for(ItemHolder i : this._items) {
                           ItemInstance item = warehouse.getItemByObjectId(i.getId());
                           if (item == null || item.getCount() < i.getCount()) {
                              Util.handleIllegalPlayerAction(
                                 player,
                                 "" + player.getName() + " of account " + player.getAccountName() + " tried to withdraw non-existent item from warehouse."
                              );
                              return;
                           }

                           weight = (int)((long)weight + i.getCount() * (long)item.getItem().getWeight());
                           if (!item.isStackable()) {
                              slots = (int)((long)slots + i.getCount());
                           } else if (player.getInventory().getItemByItemId(item.getId()) == null) {
                              ++slots;
                           }
                        }

                        if (!player.getInventory().validateCapacity((long)slots)) {
                           player.sendPacket(SystemMessageId.SLOTS_FULL);
                        } else if (!player.getInventory().validateWeight((long)weight)) {
                           player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
                        } else {
                           InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();

                           for(ItemHolder i : this._items) {
                              ItemInstance oldItem = warehouse.getItemByObjectId(i.getId());
                              if (oldItem == null || oldItem.getCount() < i.getCount()) {
                                 _log.warning("Error withdrawing a warehouse object for char " + player.getName() + " (olditem == null)");
                                 return;
                              }

                              ItemInstance newItem = warehouse.transferItem(
                                 warehouse.getName(), i.getId(), i.getCount(), player.getInventory(), player, manager
                              );
                              if (newItem == null) {
                                 _log.warning("Error withdrawing a warehouse object for char " + player.getName() + " (newitem == null)");
                                 return;
                              }

                              if (playerIU != null) {
                                 if (newItem.getCount() > i.getCount()) {
                                    playerIU.addModifiedItem(newItem);
                                 } else {
                                    playerIU.addNewItem(newItem);
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
