package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.BuyListParser;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MerchantInstance;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.items.buylist.ProductList;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExBuySellList;
import l2e.gameserver.network.serverpackets.StatusUpdate;

public final class RequestRefundItem extends GameClientPacket {
   private static final int BATCH_LENGTH = 4;
   private int _listId;
   private int[] _items = null;

   @Override
   protected void readImpl() {
      this._listId = this.readD();
      int count = this.readD();
      if (count > 0 && count <= Config.MAX_ITEM_IN_PACKET && count * 4 == this._buf.remaining()) {
         this._items = new int[count];

         for(int i = 0; i < count; ++i) {
            this._items[i] = this.readD();
         }
      }
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (player.isActionsDisabled()) {
            player.sendActionFailed();
         } else if (this._items == null) {
            this.sendActionFailed();
         } else if (!player.hasRefund()) {
            this.sendActionFailed();
         } else {
            if (player.getActiveTradeList() != null) {
               player.cancelActiveTrade();
            }

            GameObject target = player.getTarget();
            if (player.isGM()
               || target != null
                  && target instanceof MerchantInstance
                  && player.getReflectionId() == target.getReflectionId()
                  && player.isInsideRadius(target, 150, true, false)) {
               Creature merchant = null;
               if (target instanceof MerchantInstance) {
                  merchant = (Creature)target;
               } else if (!player.isGM()) {
                  this.sendActionFailed();
                  return;
               }

               if (merchant == null) {
                  this.sendActionFailed();
               } else {
                  ProductList buyList = BuyListParser.getInstance().getBuyList(this._listId);
                  if (buyList == null) {
                     Util.handleIllegalPlayerAction(
                        player, "" + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + this._listId
                     );
                  } else if (!buyList.isNpcAllowed(((MerchantInstance)merchant).getId())) {
                     this.sendActionFailed();
                  } else {
                     long weight = 0L;
                     long adena = 0L;
                     long slots = 0L;
                     ItemInstance[] refund = player.getRefund().getItems();
                     int[] objectIds = new int[this._items.length];

                     for(int i = 0; i < this._items.length; ++i) {
                        int idx = this._items[i];
                        if (idx < 0 || idx >= refund.length) {
                           Util.handleIllegalPlayerAction(
                              player, "" + player.getName() + " of account " + player.getAccountName() + " sent invalid refund index"
                           );
                           return;
                        }

                        for(int j = i + 1; j < this._items.length; ++j) {
                           if (idx == this._items[j]) {
                              Util.handleIllegalPlayerAction(
                                 player, "" + player.getName() + " of account " + player.getAccountName() + " sent duplicate refund index"
                              );
                              return;
                           }
                        }

                        ItemInstance item = refund[idx];
                        Item template = item.getItem();
                        objectIds[i] = item.getObjectId();

                        for(int j = 0; j < i; ++j) {
                           if (objectIds[i] == objectIds[j]) {
                              Util.handleIllegalPlayerAction(
                                 player, "" + player.getName() + " of account " + player.getAccountName() + " has duplicate items in refund list"
                              );
                              return;
                           }
                        }

                        long count = item.getCount();
                        weight += count * (long)template.getWeight();
                        adena += count * (long)template.getReferencePrice() / 2L;
                        if (!template.isStackable()) {
                           slots += count;
                        } else if (player.getInventory().getItemByItemId(template.getId()) == null) {
                           ++slots;
                        }
                     }

                     if (weight > 2147483647L || weight < 0L || !player.getInventory().validateWeight((long)((int)weight))) {
                        player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
                        this.sendActionFailed();
                     } else if (slots > 2147483647L || slots < 0L || !player.getInventory().validateCapacity((long)((int)slots))) {
                        player.sendPacket(SystemMessageId.SLOTS_FULL);
                        this.sendActionFailed();
                     } else if (adena >= 0L && player.reduceAdena("Refund", adena, player.getLastFolkNPC(), false)) {
                        for(int i = 0; i < this._items.length; ++i) {
                           ItemInstance item = player.getRefund()
                              .transferItem("Refund", objectIds[i], Long.MAX_VALUE, player.getInventory(), player, player.getLastFolkNPC());
                           if (item == null) {
                              _log.warning("Error refunding object for char " + player.getName() + " (newitem == null)");
                           }
                        }

                        StatusUpdate su = new StatusUpdate(player);
                        su.addAttribute(14, player.getCurrentLoad());
                        player.sendPacket(su);
                        player.sendPacket(new ExBuySellList(player, true));
                     } else {
                        player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
                        this.sendActionFailed();
                     }
                  }
               }
            } else {
               this.sendActionFailed();
            }
         }
      }
   }
}
