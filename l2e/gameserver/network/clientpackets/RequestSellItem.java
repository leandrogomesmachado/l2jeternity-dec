package l2e.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.BuyListParser;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MerchantInstance;
import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.model.items.buylist.ProductList;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.network.serverpackets.ExBuySellList;
import l2e.gameserver.network.serverpackets.StatusUpdate;

public final class RequestSellItem extends GameClientPacket {
   private static final int BATCH_LENGTH = 16;
   private int _listId;
   private List<ItemHolder> _items = null;

   @Override
   protected void readImpl() {
      this._listId = this.readD();
      int size = this.readD();
      if (size > 0 && size <= Config.MAX_ITEM_IN_PACKET && size * 16 == this._buf.remaining()) {
         this._items = new ArrayList<>(size);

         for(int i = 0; i < size; ++i) {
            int objectId = this.readD();
            int itemId = this.readD();
            long count = this.readQ();
            if (objectId < 1 || itemId < 1 || count < 1L) {
               this._items = null;
               return;
            }

            this._items.add(new ItemHolder(itemId, objectId, count));
         }
      }
   }

   @Override
   protected void runImpl() {
      this.processSell();
   }

   protected void processSell() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (player.isActionsDisabled() || player.getActiveTradeList() != null) {
            player.sendActionFailed();
         } else if (this._items == null) {
            this.sendActionFailed();
         } else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0) {
            this.sendActionFailed();
         } else {
            GameObject target = player.getTarget();
            Creature merchant = null;
            if (!player.isGM() && target != null && target instanceof MerchantInstance) {
               merchant = (Creature)target;
            }

            if (merchant == null || player.isInsideRadius(target, 150, true, false) && player.getReflectionId() == target.getReflectionId()) {
               ProductList buyList = BuyListParser.getInstance().getBuyList(this._listId);
               if (buyList == null) {
                  Util.handleIllegalPlayerAction(
                     player, "" + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + this._listId
                  );
               } else if (merchant != null && merchant instanceof MerchantInstance && !buyList.isNpcAllowed(((MerchantInstance)merchant).getId())) {
                  this.sendActionFailed();
               } else {
                  long totalPrice = 0L;

                  for(ItemHolder i : this._items) {
                     ItemInstance item = player.checkItemManipulation(i.getObjectId(), i.getCount(), "sell");
                     if (item != null && item.isSellable()) {
                        long price = (long)((double)(item.getReferencePrice() / 2) * Config.SELL_PRICE_MODIFIER);
                        totalPrice += price * i.getCount();
                        if (PcInventory.MAX_ADENA / i.getCount() < price || totalPrice > PcInventory.MAX_ADENA) {
                           Util.handleIllegalPlayerAction(
                              player,
                              ""
                                 + player.getName()
                                 + " of account "
                                 + player.getAccountName()
                                 + " tried to purchase over "
                                 + PcInventory.MAX_ADENA
                                 + " adena worth of goods."
                           );
                           return;
                        }

                        if (Config.ALLOW_REFUND) {
                           item = player.getInventory().transferItem("Sell", i.getObjectId(), i.getCount(), player.getRefund(), player, merchant);
                        } else {
                           item = player.getInventory().destroyItem("Sell", i.getObjectId(), i.getCount(), player, merchant);
                        }
                     }
                  }

                  player.addAdena("Sell", totalPrice, merchant, false);
                  StatusUpdate su = new StatusUpdate(player);
                  su.addAttribute(14, player.getCurrentLoad());
                  player.sendPacket(su);
                  player.sendPacket(new ExBuySellList(player, true));
               }
            } else {
               this.sendActionFailed();
            }
         }
      }
   }
}
