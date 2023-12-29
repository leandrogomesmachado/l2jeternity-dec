package l2e.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.Shutdown;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.communication.AuthServerCommunication;
import l2e.gameserver.network.communication.SessionKey;
import l2e.gameserver.network.communication.gameserverpackets.PlayerAuthRequest;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.LoginFail;
import l2e.gameserver.network.serverpackets.ServerClose;
import org.strixplatform.StrixPlatform;

public final class RequestLogin extends GameClientPacket {
   protected static final Logger _logAccounting = Logger.getLogger("accounting");
   private String _loginName;
   private int _playKey1;
   private int _playKey2;
   private int _loginKey1;
   private int _loginKey2;
   private byte[] _guardData = null;

   @Override
   protected void readImpl() {
      this._loginName = this.readS().toLowerCase();
      this._playKey2 = this.readD();
      this._playKey1 = this.readD();
      this._loginKey1 = this.readD();
      this._loginKey2 = this.readD();
      if (Config.PROTECTION.equalsIgnoreCase("ANTICHEAT") && this._buf.remaining() >= 32) {
         this._guardData = new byte[32];
         this.readB(this._guardData);
      }
   }

   @Override
   protected void runImpl() {
      GameClient client = this.getClient();
      if (this._loginName.isEmpty()) {
         client.closeNow(false);
      } else {
         SessionKey key = new SessionKey(this._loginKey1, this._loginKey2, this._playKey1, this._playKey2);
         client.setSessionId(key);
         client.setLogin(this._loginName);
         if (Shutdown.getInstance().getMode() != 0 && Shutdown.getInstance().getSeconds() <= 15) {
            client.closeNow(false);
         } else {
            if (AuthServerCommunication.getInstance().isShutdown()) {
               client.close(new LoginFail(1));
               return;
            }

            GameClient oldClient = AuthServerCommunication.getInstance().addWaitingClient(client);
            if (oldClient != null) {
               oldClient.close(ServerClose.STATIC_PACKET);
            }

            if (Config.PROTECTION.equalsIgnoreCase("ANTICHEAT")) {
               client.setHWID(Util.bytesToHex(this._guardData));
            }

            AuthServerCommunication.getInstance().sendPacket(new PlayerAuthRequest(client));
            if (StrixPlatform.getInstance().isPlatformEnabled()) {
               if (client.getStrixClientData() == null) {
                  client.close((GameServerPacket)null);
                  return;
               }

               client.getStrixClientData().setClientAccount(this._loginName);
               if (StrixPlatform.getInstance().isAuthLogEnabled()) {
                  LogRecord record = new LogRecord(
                     Level.INFO,
                     "Account: ["
                        + this._loginName
                        + "] HWID: ["
                        + client.getStrixClientData().getClientHWID()
                        + "] SessionID: ["
                        + client.getStrixClientData().getSessionId()
                        + "] entered to Game Server"
                  );
                  record.setParameters(new Object[]{client});
                  _logAccounting.log(record);
               }
            }
         }
      }
   }
}
