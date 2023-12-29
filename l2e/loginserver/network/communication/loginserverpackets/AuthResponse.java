package l2e.loginserver.network.communication.loginserverpackets;

import l2e.loginserver.Config;
import l2e.loginserver.network.communication.GameServer;
import l2e.loginserver.network.communication.SendablePacket;
import org.HostInfo;

public class AuthResponse extends SendablePacket {
   private final HostInfo[] _hosts;

   public AuthResponse(GameServer gs) {
      this._hosts = gs.getHosts();
   }

   @Override
   protected void writeImpl() {
      this.writeC(0);
      this.writeC(0);
      this.writeS("");
      this.writeC(this._hosts.length);

      for(HostInfo host : this._hosts) {
         this.writeC(host.getId());
         this.writeS(Config.SERVER_NAMES.get(host.getId()));
      }
   }
}
