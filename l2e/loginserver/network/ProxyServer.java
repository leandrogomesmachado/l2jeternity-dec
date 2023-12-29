package l2e.loginserver.network;

import java.net.InetAddress;
import l2e.loginserver.GameServerManager;
import l2e.loginserver.network.communication.GameServer;

public class ProxyServer {
   private final int _origServerId;
   private final int _proxyServerId;
   private InetAddress _proxyAddr;
   private int _proxyPort;

   public ProxyServer(int origServerId, int proxyServerId) {
      this._origServerId = origServerId;
      this._proxyServerId = proxyServerId;
   }

   public int getOrigServerId() {
      return this._origServerId;
   }

   public int getProxyServerId() {
      return this._proxyServerId;
   }

   public InetAddress getProxyAddr() {
      return this._proxyAddr;
   }

   public void setProxyAddr(InetAddress proxyAddr) {
      this._proxyAddr = proxyAddr;
   }

   public int getProxyPort() {
      return this._proxyPort;
   }

   public void setProxyPort(int proxyPort) {
      this._proxyPort = proxyPort;
   }

   public GameServer getGameServer() {
      return GameServerManager.getInstance().getGameServerById(this.getOrigServerId());
   }
}
