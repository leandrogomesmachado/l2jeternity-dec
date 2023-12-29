package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.model.TradeList;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public final class TradeDone extends GameClientPacket {
   private int _response;

   @Override
   protected void readImpl() {
      this._response = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getActiveChar();
      if (player != null) {
         TradeList trade = player.getActiveTradeList();
         if (trade == null) {
            if (Config.DEBUG) {
               _log.warning("player.getTradeList == null in " + this.getType() + " for player " + player.getName());
            }
         } else if (!trade.isLocked()) {
            if (this._response == 1) {
               if (trade.getPartner() == null || World.getInstance().getPlayer(trade.getPartner().getObjectId()) == null) {
                  player.cancelActiveTrade();
                  player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
                  return;
               }

               if (trade.getOwner().getActiveEnchantItemId() != -1 || trade.getPartner().getActiveEnchantItemId() != -1) {
                  return;
               }

               if (!player.getAccessLevel().allowTransaction()) {
                  player.cancelActiveTrade();
                  player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                  return;
               }

               if (player.getReflectionId() != trade.getPartner().getReflectionId() && player.getReflectionId() != -1) {
                  player.cancelActiveTrade();
                  return;
               }

               if (Util.calculateDistance(player, trade.getPartner(), true) > 150.0) {
                  player.cancelActiveTrade();
                  return;
               }

               trade.confirm();
            } else {
               player.cancelActiveTrade();
            }
         }
      }
   }
}
