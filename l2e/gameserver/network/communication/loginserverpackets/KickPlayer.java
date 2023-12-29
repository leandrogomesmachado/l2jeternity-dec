package l2e.gameserver.network.communication.loginserverpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.communication.AuthServerCommunication;
import l2e.gameserver.network.communication.ReceivablePacket;
import l2e.gameserver.network.serverpackets.ServerClose;

public class KickPlayer extends ReceivablePacket {
   private String _account;

   @Override
   public void readImpl() {
      this._account = this.readS();
   }

   @Override
   protected void runImpl() {
      GameClient client = AuthServerCommunication.getInstance().removeWaitingClient(this._account);
      if (client == null) {
         client = AuthServerCommunication.getInstance().removeAuthedClient(this._account);
      }

      if (client != null) {
         Player activeChar = client.getActiveChar();
         if (activeChar != null) {
            activeChar.sendPacket(SystemMessageId.ANOTHER_LOGIN_WITH_ACCOUNT);
            activeChar.kick();
         } else {
            client.close(ServerClose.STATIC_PACKET);
         }
      }
   }
}
