package l2e.loginserver.network.communication.loginserverpackets;

import l2e.loginserver.network.communication.SendablePacket;

public class PingRequest extends SendablePacket {
   @Override
   protected void writeImpl() {
      this.writeC(255);
   }
}
