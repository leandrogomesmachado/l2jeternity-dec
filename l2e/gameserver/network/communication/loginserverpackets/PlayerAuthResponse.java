package l2e.gameserver.network.communication.loginserverpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.communication.AuthServerCommunication;
import l2e.gameserver.network.communication.ReceivablePacket;
import l2e.gameserver.network.communication.SessionKey;
import l2e.gameserver.network.communication.gameserverpackets.PlayerInGame;
import l2e.gameserver.network.serverpackets.CharacterSelectionInfo;
import l2e.gameserver.network.serverpackets.LoginFail;
import l2e.gameserver.network.serverpackets.ServerClose;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class PlayerAuthResponse extends ReceivablePacket {
   private String _account;
   private boolean _authed;
   private int _playOkId1;
   private int _playOkId2;
   private int _loginOkId1;
   private int _loginOkId2;
   private String _ip;

   @Override
   public void readImpl() {
      this._account = this.readS();
      this._authed = this.readC() == 1;
      if (this._authed) {
         this._playOkId1 = this.readD();
         this._playOkId2 = this.readD();
         this._loginOkId1 = this.readD();
         this._loginOkId2 = this.readD();
         this._ip = this.readS();
      }
   }

   @Override
   protected void runImpl() {
      SessionKey skey = new SessionKey(this._loginOkId1, this._loginOkId2, this._playOkId1, this._playOkId2);
      GameClient client = AuthServerCommunication.getInstance().removeWaitingClient(this._account);
      if (client != null) {
         if (this._authed && client.getSessionId().equals(skey)) {
            client.setAuthed(true);
            client.setState(GameClient.GameClientState.AUTHED);
            GameClient oldClient = AuthServerCommunication.getInstance().addAuthedClient(client);
            if (!Config.ALLOW_MULILOGIN && oldClient != null) {
               oldClient.setAuthed(false);
               Player activeChar = oldClient.getActiveChar();
               if (activeChar != null) {
                  activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ANOTHER_LOGIN_WITH_ACCOUNT));
                  activeChar.logout();
               } else {
                  oldClient.close(ServerClose.STATIC_PACKET);
               }
            }

            this.sendPacket(new PlayerInGame(client.getLogin()));
            CharacterSelectionInfo csi = new CharacterSelectionInfo(client.getLogin(), client.getSessionId().playOkID1);
            client.sendPacket(csi);
            client.setCharSelection(csi.getCharInfo());
            client.setRealIpAddress(this._ip);
         } else {
            client.close(new LoginFail(4));
         }
      }
   }
}
