package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.model.TradeList;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.PrivateStoreBuyManageList;
import l2e.gameserver.network.serverpackets.PrivateStoreBuyMsg;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;

public final class SetPrivateStoreBuyList extends GameClientPacket {
   private static final int BATCH_LENGTH = 40;
   private SetPrivateStoreBuyList.Item[] _items = null;

   @Override
   protected void readImpl() {
      int count = this.readD();
      if (count >= 1 && count <= Config.MAX_ITEM_IN_PACKET && count * 40 == this._buf.remaining()) {
         this._items = new SetPrivateStoreBuyList.Item[count];

         for(int i = 0; i < count; ++i) {
            int itemId = this.readD();
            int enchant = this.readD();
            long cnt = this.readQ();
            long price = this.readQ();
            if (itemId < 1 || cnt < 1L || price < 0L) {
               this._items = null;
               return;
            }

            int elemAtkType = this.readH();
            int elemAtkPower = this.readH();
            int[] elemDefAttr = new int[]{0, 0, 0, 0, 0, 0};

            for(byte e = 0; e < 6; ++e) {
               elemDefAttr[e] = this.readH();
            }

            this._items[i] = new SetPrivateStoreBuyList.Item(itemId, enchant, cnt, price, elemAtkType, elemAtkPower, elemDefAttr);
         }
      }
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (this._items == null) {
            player.setPrivateStoreType(0);
            player.broadcastCharInfo();
         } else if (!player.getAccessLevel().allowTransaction()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
         } else if (player.isActionsDisabled()) {
            player.sendActionFailed();
         } else if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) && !player.isInDuel()) {
            if (!player.canOpenPrivateStore(true)) {
               player.sendPacket(new PrivateStoreBuyManageList(player));
            } else {
               TradeList tradeList = player.getBuyList();
               tradeList.clear();
               if (this._items.length > player.getPrivateBuyStoreLimit()) {
                  player.sendPacket(new PrivateStoreBuyManageList(player));
                  player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
               } else {
                  long totalCost = 0L;

                  for(SetPrivateStoreBuyList.Item i : this._items) {
                     if (!i.addToTradeList(tradeList)) {
                        Util.handleIllegalPlayerAction(
                           player,
                           ""
                              + player.getName()
                              + " of account "
                              + player.getAccountName()
                              + " tried to set price more than "
                              + PcInventory.MAX_ADENA
                              + " adena in Private Store - Buy."
                        );
                        return;
                     }

                     totalCost += i.getCost();
                     if (totalCost > PcInventory.MAX_ADENA) {
                        Util.handleIllegalPlayerAction(
                           player,
                           ""
                              + player.getName()
                              + " of account "
                              + player.getAccountName()
                              + " tried to set total price more than "
                              + PcInventory.MAX_ADENA
                              + " adena in Private Store - Buy."
                        );
                        return;
                     }
                  }

                  if (totalCost > player.getAdena()) {
                     player.sendPacket(new PrivateStoreBuyManageList(player));
                     player.sendPacket(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY);
                  } else {
                     player.sitDown();
                     player.setPrivateStoreType(3);
                     player.saveTradeList();
                     player.setIsInStoreNow(true);
                     player.broadcastCharInfo();
                     player.broadcastPacket(new PrivateStoreBuyMsg(player));
                  }
               }
            }
         } else {
            player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT);
            player.sendPacket(new PrivateStoreBuyManageList(player));
            player.sendActionFailed();
         }
      }
   }

   private static class Item {
      private final int _itemId;
      private final int _enchant;
      private final long _count;
      private final long _price;
      private final int _elemAtkType;
      private final int _elemAtkPower;
      private final int[] _elemDefAttr;

      public Item(int id, int enchant, long num, long pri, int elemAtkType, int elemAtkPower, int[] elemDefAttr) {
         this._itemId = id;
         this._enchant = enchant;
         this._count = num;
         this._price = pri;
         this._elemAtkType = elemAtkType;
         this._elemAtkPower = elemAtkPower;
         this._elemDefAttr = elemDefAttr;
      }

      public boolean addToTradeList(TradeList list) {
         if (PcInventory.MAX_ADENA / this._count < this._price) {
            return false;
         } else {
            list.addItemByItemId(this._itemId, this._enchant, this._count, this._price, this._elemAtkType, this._elemAtkPower, this._elemDefAttr);
            return true;
         }
      }

      public long getCost() {
         return this._count * this._price;
      }
   }
}
