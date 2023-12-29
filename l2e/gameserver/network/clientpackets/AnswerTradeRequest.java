package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class AnswerTradeRequest extends GameClientPacket {
   private int _response;

   @Override
   protected void readImpl() {
      this._response = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (!player.getAccessLevel().allowTransaction()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            this.sendActionFailed();
         } else {
            Player partner = player.getActiveRequester();
            if (partner == null) {
               player.sendPacket(new l2e.gameserver.network.serverpackets.TradeDone(0));
               SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
               player.sendPacket(msg);
               player.setActiveRequester(null);
               msg = null;
            } else if (World.getInstance().getPlayer(partner.getObjectId()) == null) {
               player.sendPacket(new l2e.gameserver.network.serverpackets.TradeDone(0));
               SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
               player.sendPacket(msg);
               player.setActiveRequester(null);
               msg = null;
            } else if (partner.isActionsDisabled()) {
               player.sendPacket(new l2e.gameserver.network.serverpackets.TradeDone(0));
               SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
               msg.addString(partner.getName());
               player.sendPacket(msg);
               player.setActiveRequester(null);
               player.sendActionFailed();
            } else {
               if (this._response == 1 && !partner.isRequestExpired()) {
                  player.startTrade(partner);
               } else {
                  SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_DENIED_TRADE_REQUEST);
                  msg.addString(player.getName());
                  partner.sendPacket(msg);
                  Object var4 = null;
               }

               player.setActiveRequester(null);
               partner.onTransactionResponse();
            }
         }
      }
   }
}
