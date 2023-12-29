package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.TradeList;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.ItemRequest;

public final class SendPrivateStoreSellList extends GameClientPacket {
   private static final int BATCH_LENGTH = 28;
   private int _storePlayerId;
   private ItemRequest[] _items = null;

   @Override
   protected void readImpl() {
      this._storePlayerId = this.readD();
      int count = this.readD();
      if (count > 0 && count <= Config.MAX_ITEM_IN_PACKET && count * 28 == this._buf.remaining()) {
         this._items = new ItemRequest[count];

         for(int i = 0; i < count; ++i) {
            int objectId = this.readD();
            int itemId = this.readD();
            this.readH();
            this.readH();
            long cnt = this.readQ();
            long price = this.readQ();
            if (objectId < 1 || itemId < 1 || cnt < 1L || price < 0L) {
               this._items = null;
               return;
            }

            this._items[i] = new ItemRequest(objectId, itemId, cnt, price);
         }
      }
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (this._items == null) {
            this.sendActionFailed();
         } else {
            Player object = World.getInstance().getPlayer(this._storePlayerId);
            if (object != null) {
               if (player.isInsideRadius(object, 150, true, false)) {
                  if (player.getReflectionId() == object.getReflectionId() || player.getReflectionId() == -1) {
                     if (object.getPrivateStoreType() == 3) {
                        if (!player.isCursedWeaponEquipped()) {
                           TradeList storeList = object.getBuyList();
                           if (storeList != null) {
                              if (!player.getAccessLevel().allowTransaction()) {
                                 player.sendMessage("Transactions are disabled for your Access Level.");
                                 this.sendActionFailed();
                              } else if (!storeList.privateStoreSell(player, this._items)) {
                                 this.sendActionFailed();
                                 _log.warning(
                                    "PrivateStore sell has failed due to invalid list or request. Player: "
                                       + player.getName()
                                       + ", Private store of: "
                                       + object.getName()
                                 );
                              } else {
                                 if (storeList.getItemCount() == 0) {
                                    object.setPrivateStoreType(0);
                                    object.broadcastCharInfo();
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
