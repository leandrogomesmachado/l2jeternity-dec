package l2e.gameserver.network.communication.loginserverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.GameServer;
import l2e.gameserver.network.communication.AuthServerCommunication;
import l2e.gameserver.network.communication.ReceivablePacket;
import l2e.gameserver.network.communication.gameserverpackets.OnlineStatus;
import l2e.gameserver.network.communication.gameserverpackets.PlayerInGame;

public class AuthResponse extends ReceivablePacket {
   private List<AuthResponse.ServerInfo> _servers;

   @Override
   protected void readImpl() {
      int serverId = this.readC();
      String serverName = this.readS();
      if (!this.getByteBuffer().hasRemaining()) {
         this._servers = new ArrayList<>(1);
         this._servers.add(new AuthResponse.ServerInfo(serverId, serverName));
      } else {
         int serversCount = this.readC();
         this._servers = new ArrayList<>(serversCount);

         for(int i = 0; i < serversCount; ++i) {
            this._servers.add(new AuthResponse.ServerInfo(this.readC(), this.readS()));
         }
      }
   }

   @Override
   protected void runImpl() {
      for(AuthResponse.ServerInfo info : this._servers) {
         _log.info(
            "Registered on login as Server " + info.getId() + " : " + info.getName() + " [Players Limit: " + GameServer.getInstance().getOnlineLimit() + "]"
         );
      }

      this.sendPacket(new OnlineStatus(true));
      String[] accounts = AuthServerCommunication.getInstance().getAccounts();

      for(String account : accounts) {
         this.sendPacket(new PlayerInGame(account));
      }
   }

   private static class ServerInfo {
      private final int _id;
      private final String _name;

      public ServerInfo(int id, String name) {
         this._id = id;
         this._name = name;
      }

      public int getId() {
         return this._id;
      }

      public String getName() {
         return this._name;
      }
   }
}
