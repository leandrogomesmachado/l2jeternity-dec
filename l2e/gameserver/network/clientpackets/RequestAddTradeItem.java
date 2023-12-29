package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.TradeItem;
import l2e.gameserver.model.TradeList;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.TradeOtherAdd;
import l2e.gameserver.network.serverpackets.TradeOwnAdd;
import l2e.gameserver.network.serverpackets.TradeUpdate;

public final class RequestAddTradeItem extends GameClientPacket {
   private int _tradeId;
   private int _objectId;
   private long _count;

   @Override
   protected void readImpl() {
      this._tradeId = this.readD();
      this._objectId = this.readD();
      this._count = this.readQ();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         player.isntAfk();
         TradeList trade = player.getActiveTradeList();
         if (trade == null) {
            _log.warning("Character: " + player.getName() + " requested item:" + this._objectId + " add without active tradelist:" + this._tradeId);
         } else {
            Player partner = trade.getPartner();
            if (partner != null && World.getInstance().getPlayer(partner.getObjectId()) != null && partner.getActiveTradeList() != null) {
               if (trade.isConfirmed() || partner.getActiveTradeList().isConfirmed()) {
                  player.sendPacket(SystemMessageId.CANNOT_ADJUST_ITEMS_AFTER_TRADE_CONFIRMED);
               } else if (!player.getAccessLevel().allowTransaction()) {
                  player.sendMessage("Transactions are disabled for your Access Level.");
                  player.cancelActiveTrade();
               } else if (!player.validateItemManipulation(this._objectId, "trade")) {
                  player.sendPacket(SystemMessageId.NOTHING_HAPPENED);
               } else {
                  TradeItem item = trade.addItem(this._objectId, this._count);
                  if (item != null) {
                     player.sendPacket(new TradeOwnAdd(item), new TradeUpdate(player, item));
                     trade.getPartner().sendPacket(new TradeOtherAdd(item));
                  }
               }
            } else {
               if (partner != null) {
                  _log.warning("Character:" + player.getName() + " requested invalid trade object: " + this._objectId);
               }

               player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
               player.cancelActiveTrade();
            }
         }
      }
   }
}
