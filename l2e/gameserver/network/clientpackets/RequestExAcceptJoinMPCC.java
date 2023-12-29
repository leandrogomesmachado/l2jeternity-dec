package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.CommandChannel;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestExAcceptJoinMPCC extends GameClientPacket {
   protected int _response;
   protected int _unk;

   @Override
   protected void readImpl() {
      this._response = this._buf.hasRemaining() ? this.readD() : 0;
      this._unk = this._buf.hasRemaining() ? this.readD() : 0;
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         Player requestor = player.getActiveRequester();
         if (requestor == null) {
            return;
         }

         if (requestor.isInFightEvent() && !requestor.getFightEvent().canReceiveInvitations(player, requestor)) {
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addString(requestor.getName()));
            return;
         }

         if (this._response == 1) {
            boolean newCc = false;
            if (!requestor.getParty().isInCommandChannel()) {
               new CommandChannel(requestor);
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_FORMED);
               requestor.sendPacket(sm);
               newCc = true;
            }

            requestor.getParty().getCommandChannel().addParty(player.getParty());
            if (!newCc) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.JOINED_COMMAND_CHANNEL);
               player.sendPacket(sm);
            }
         } else {
            requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_DECLINED_CHANNEL_INVITATION).addString(player.getName()));
         }

         player.setActiveRequester(null);
         requestor.onTransactionResponse();
      }
   }
}
