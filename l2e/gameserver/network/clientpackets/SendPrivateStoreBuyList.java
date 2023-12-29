package l2e.gameserver.network.clientpackets;

import java.util.HashSet;
import java.util.Set;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.TradeList;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.ItemRequest;

public final class SendPrivateStoreBuyList extends GameClientPacket {
   private static final int BATCH_LENGTH = 20;
   private int _storePlayerId;
   private Set<ItemRequest> _items = null;

   @Override
   protected void readImpl() {
      this._storePlayerId = this.readD();
      int count = this.readD();
      if (count > 0 && count <= Config.MAX_ITEM_IN_PACKET && count * 20 == this._buf.remaining()) {
         this._items = new HashSet<>();

         for(int i = 0; i < count; ++i) {
            int objectId = this.readD();
            long cnt = this.readQ();
            long price = this.readQ();
            if (objectId < 1 || cnt < 1L || price < 0L) {
               this._items = null;
               return;
            }

            this._items.add(new ItemRequest(objectId, cnt, price));
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
            GameObject object = World.getInstance().getPlayer(this._storePlayerId);
            if (object != null) {
               if (!player.isCursedWeaponEquipped()) {
                  Player storePlayer = (Player)object;
                  if (player.isInsideRadius(storePlayer, 150, true, false)) {
                     if (player.getReflectionId() == storePlayer.getReflectionId() || player.getReflectionId() == -1) {
                        if (storePlayer.getPrivateStoreType() == 1 || storePlayer.getPrivateStoreType() == 8) {
                           TradeList storeList = storePlayer.getSellList();
                           if (storeList != null) {
                              if (!player.getAccessLevel().allowTransaction()) {
                                 player.sendMessage("Transactions are disabled for your Access Level.");
                                 this.sendActionFailed();
                              } else if (storePlayer.getPrivateStoreType() == 8 && storeList.getItemCount() > this._items.size()) {
                                 String msgErr = "[RequestPrivateStoreBuy] "
                                    + this.getClient().getActiveChar().getName()
                                    + " tried to buy less items than sold by package-sell, ban this player for bot usage!";
                                 Util.handleIllegalPlayerAction(this.getClient().getActiveChar(), msgErr);
                              } else {
                                 int result = storeList.privateStoreBuy(player, this._items);
                                 if (result > 0) {
                                    this.sendActionFailed();
                                    if (result > 1) {
                                       _log.warning(
                                          "PrivateStore buy has failed due to invalid list or request. Player: "
                                             + player.getName()
                                             + ", Private store of: "
                                             + storePlayer.getName()
                                       );
                                    }
                                 } else {
                                    if (storeList.getItemCount() == 0) {
                                       storePlayer.setPrivateStoreType(0);
                                       storePlayer.broadcastCharInfo();
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
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
