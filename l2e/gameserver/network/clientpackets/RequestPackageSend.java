package l2e.gameserver.network.clientpackets;

import java.util.logging.Level;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.ItemContainer;
import l2e.gameserver.model.items.itemcontainer.PcFreight;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.StatusUpdate;

public class RequestPackageSend extends GameClientPacket {
   private static final int BATCH_LENGTH = 12;
   private ItemHolder[] _items = null;
   private int _objectId;

   @Override
   protected void readImpl() {
      this._objectId = this.readD();
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
      Player player = this.getActiveChar();
      if (this._items != null && player != null && player.getAccountChars().containsKey(this._objectId)) {
         if (player.isActionsDisabled()) {
            player.sendActionFailed();
         } else {
            Npc manager = player.getLastFolkNPC();
            if (manager != null && player.isInsideRadius(manager, 150, false, false)) {
               if (player.getActiveEnchantItemId() != -1) {
                  Util.handleIllegalPlayerAction(player, "" + player.getName() + " tried to use enchant Exploit!");
               } else if (player.getActiveTradeList() == null) {
                  if (Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE || player.getKarma() <= 0) {
                     int fee = this._items.length * Config.ALT_FREIGHT_PRICE;
                     long currentAdena = player.getAdena();
                     int slots = 0;
                     ItemContainer warehouse = new PcFreight(this._objectId);

                     for(ItemHolder i : this._items) {
                        ItemInstance item = player.checkItemManipulation(i.getId(), i.getCount(), "freight");
                        if (item == null) {
                           Util.handleIllegalPlayerAction(player, "Error depositing a warehouse object for char " + player.getName() + " (validity check)!");
                           if (Config.DEBUG) {
                              _log.log(Level.WARNING, "Error depositing a warehouse object for char " + player.getName() + " (validity check)");
                           }

                           warehouse.deleteMe();
                           return;
                        }

                        if (!item.isFreightable()) {
                           warehouse.deleteMe();
                           return;
                        }

                        if (item.getId() == 57) {
                           currentAdena -= i.getCount();
                        } else if (!item.isStackable()) {
                           slots = (int)((long)slots + i.getCount());
                        } else if (warehouse.getItemByItemId(item.getId()) == null) {
                           ++slots;
                        }
                     }

                     if (!warehouse.validateCapacity((long)slots)) {
                        player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
                        warehouse.deleteMe();
                     } else if (currentAdena >= (long)fee && player.reduceAdena(warehouse.getName(), (long)fee, manager, false)) {
                        InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();

                        for(ItemHolder i : this._items) {
                           ItemInstance oldItem = player.checkItemManipulation(i.getId(), i.getCount(), "deposit");
                           if (oldItem == null) {
                              _log.log(Level.WARNING, "Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
                              warehouse.deleteMe();
                              return;
                           }

                           ItemInstance newItem = player.getInventory().transferItem("Trade", i.getId(), i.getCount(), warehouse, player, null);
                           if (newItem == null) {
                              _log.log(Level.WARNING, "Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
                           } else if (playerIU != null) {
                              if (oldItem.getCount() > 0L && oldItem != newItem) {
                                 playerIU.addModifiedItem(oldItem);
                              } else {
                                 playerIU.addRemovedItem(oldItem);
                              }
                           }
                        }

                        warehouse.deleteMe();
                        if (playerIU != null) {
                           this.sendPacket(playerIU);
                        } else {
                           player.sendItemList(false);
                        }

                        StatusUpdate su = new StatusUpdate(player);
                        su.addAttribute(14, player.getCurrentLoad());
                        this.sendPacket(su);
                     } else {
                        player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
                        warehouse.deleteMe();
                     }
                  }
               }
            }
         }
      }
   }
}
