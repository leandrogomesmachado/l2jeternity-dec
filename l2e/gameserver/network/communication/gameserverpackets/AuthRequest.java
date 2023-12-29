package l2e.gameserver.network.communication.gameserverpackets;

import java.util.Map.Entry;
import l2e.commons.net.IPSettings;
import l2e.gameserver.Config;
import l2e.gameserver.GameServer;
import l2e.gameserver.network.communication.SendablePacket;
import org.HostInfo;
import org.utils.Net;

public class AuthRequest extends SendablePacket {
   @Override
   protected void writeImpl() {
      this.writeC(0);
      this.writeD(4);
      this.writeD(Config.SERVER_LIST_TYPE);
      this.writeD(Config.SERVER_LIST_AGE);
      this.writeC(Config.SERVER_GMONLY ? 1 : 0);
      this.writeC(Config.SERVER_LIST_BRACKET ? 1 : 0);
      this.writeC(Config.SERVER_LIST_IS_PVP ? 1 : 0);
      this.writeD(GameServer.getInstance().getOnlineLimit());
      HostInfo[] hosts = IPSettings.getInstance().getGameServerHosts();
      this.writeC(hosts.length);

      for(HostInfo host : hosts) {
         this.writeC(host.getId());
         this.writeS(host.getAddress());
         this.writeH(host.getPort());
         this.writeS(host.getKey());
         this.writeC(host.getSubnets().size());

         for(Entry<Net, String> m : host.getSubnets().entrySet()) {
            this.writeS(m.getValue());
            byte[] address = m.getKey().getAddress();
            this.writeD(address.length);
            this.writeB(address);
            byte[] mask = m.getKey().getMask();
            this.writeD(mask.length);
            this.writeB(mask);
         }
      }
   }
}
