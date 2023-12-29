package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.ItemAuctionManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.itemauction.ItemAuction;
import l2e.gameserver.model.items.itemauction.ItemAuctionInstance;
import l2e.gameserver.network.serverpackets.ExItemAuctionInfo;

public final class RequestInfoItemAuction extends GameClientPacket {
   private int _instanceId;

   @Override
   protected final void readImpl() {
      this._instanceId = super.readD();
   }

   @Override
   protected final void runImpl() {
      Player activeChar = super.getClient().getActiveChar();
      if (activeChar != null) {
         ItemAuctionInstance instance = ItemAuctionManager.getInstance().getManagerInstance(this._instanceId);
         if (instance != null) {
            ItemAuction auction = instance.getCurrentAuction();
            if (auction != null) {
               activeChar.updateLastItemAuctionRequest();
               activeChar.sendPacket(new ExItemAuctionInfo(true, auction, instance.getNextAuction()));
            }
         }
      }
   }
}
