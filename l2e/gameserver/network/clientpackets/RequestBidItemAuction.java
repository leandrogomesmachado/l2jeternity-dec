package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.ItemAuctionManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.itemauction.ItemAuction;
import l2e.gameserver.model.items.itemauction.ItemAuctionInstance;
import l2e.gameserver.model.items.itemcontainer.PcInventory;

public final class RequestBidItemAuction extends GameClientPacket {
   private int _instanceId;
   private long _bid;

   @Override
   protected final void readImpl() {
      this._instanceId = super.readD();
      this._bid = super.readQ();
   }

   @Override
   protected final void runImpl() {
      Player activeChar = super.getClient().getActiveChar();
      if (activeChar != null) {
         if (this._bid >= 0L && this._bid <= PcInventory.MAX_ADENA) {
            ItemAuctionInstance instance = ItemAuctionManager.getInstance().getManagerInstance(this._instanceId);
            if (instance != null) {
               ItemAuction auction = instance.getCurrentAuction();
               if (auction != null) {
                  auction.registerBid(activeChar, this._bid);
               }
            }
         }
      }
   }
}
