package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.model.TradeItem;
import l2e.gameserver.model.TradeList;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.auction.Auction;
import l2e.gameserver.model.entity.auction.AuctionsManager;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExPrivateStorePackageMsg;
import l2e.gameserver.network.serverpackets.PrivateStoreSellManageList;
import l2e.gameserver.network.serverpackets.PrivateStoreSellMsg;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;

public class SetPrivateStoreSellList extends GameClientPacket {
   private static final int BATCH_LENGTH = 20;
   private boolean _packageSale;
   private SetPrivateStoreSellList.Item[] _items = null;

   @Override
   protected void readImpl() {
      this._packageSale = this.readD() == 1;
      int count = this.readD();
      if (count >= 1 && count <= Config.MAX_ITEM_IN_PACKET && count * 20 == this._buf.remaining()) {
         this._items = new SetPrivateStoreSellList.Item[count];

         for(int i = 0; i < count; ++i) {
            int itemId = this.readD();
            long cnt = this.readQ();
            long price = this.readQ();
            if (itemId < 1 || cnt < 1L || price < 0L) {
               this._items = null;
               return;
            }

            this._items[i] = new SetPrivateStoreSellList.Item(itemId, cnt, price);
         }
      }
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (this._items == null) {
            player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT);
            player.setPrivateStoreType(0);
            player.broadcastCharInfo();
         } else if (player.isActionsDisabled()) {
            player.sendActionFailed();
         } else if (!player.getAccessLevel().allowTransaction()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
         } else if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) || player.isInDuel()) {
            player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT);
            player.sendPacket(new PrivateStoreSellManageList(player, this._packageSale));
            player.sendActionFailed();
         } else if (!player.canOpenPrivateStore(true)) {
            player.sendPacket(new PrivateStoreSellManageList(player, this._packageSale));
         } else if (this._items.length > player.getPrivateSellStoreLimit()) {
            player.sendPacket(new PrivateStoreSellManageList(player, this._packageSale));
            player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
         } else {
            TradeList tradeList = player.getSellList();
            tradeList.clear();
            tradeList.setPackaged(this._packageSale);
            long totalCost = player.getAdena();

            for(SetPrivateStoreSellList.Item i : this._items) {
               if (!i.addToTradeList(tradeList)) {
                  Util.handleIllegalPlayerAction(
                     player,
                     ""
                        + player.getName()
                        + " of account "
                        + player.getAccountName()
                        + " tried to set price more than "
                        + PcInventory.MAX_ADENA
                        + " adena in Private Store - Sell."
                  );
                  return;
               }

               totalCost += i.getPrice();
               if (totalCost > PcInventory.MAX_ADENA) {
                  Util.handleIllegalPlayerAction(
                     player,
                     ""
                        + player.getName()
                        + " of account "
                        + player.getAccountName()
                        + " tried to set total price more than "
                        + PcInventory.MAX_ADENA
                        + " adena in Private Store - Sell."
                  );
                  return;
               }
            }

            player.sitDown();
            if (this._packageSale) {
               player.setPrivateStoreType(8);
            } else {
               player.setPrivateStoreType(1);
            }

            player.saveTradeList();
            player.setIsInStoreNow(true);
            player.broadcastCharInfo();
            if (Config.AUCTION_PRIVATE_STORE_AUTO_ADDED && !this._packageSale) {
               for(TradeItem ti : player.getSellList().getItems()) {
                  ItemInstance item = player.getInventory().getItemByObjectId(ti.getObjectId());
                  if (item != null) {
                     Auction auc = AuctionsManager.getInstance().addNewStore(player, item, 57, ti.getPrice(), ti.getCount());
                     ti.setAuctionId(auc.getAuctionId());
                  }
               }
            }

            if (this._packageSale) {
               player.broadcastPacket(new ExPrivateStorePackageMsg(player));
            } else {
               player.broadcastPacket(new PrivateStoreSellMsg(player));
            }
         }
      }
   }

   private static class Item {
      private final int _itemId;
      private final long _count;
      private final long _price;

      public Item(int id, long num, long pri) {
         this._itemId = id;
         this._count = num;
         this._price = pri;
      }

      public boolean addToTradeList(TradeList list) {
         if (PcInventory.MAX_ADENA / this._count < this._price) {
            return false;
         } else {
            list.addItem(this._itemId, this._count, this._price);
            return true;
         }
      }

      public long getPrice() {
         return this._count * this._price;
      }
   }
}
