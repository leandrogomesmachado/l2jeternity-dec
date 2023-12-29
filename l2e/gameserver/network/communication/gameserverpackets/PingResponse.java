package l2e.gameserver.network.communication.gameserverpackets;

import l2e.gameserver.network.communication.SendablePacket;

public class PingResponse extends SendablePacket {
   @Override
   protected void writeImpl() {
      this.writeC(255);
      this.writeQ(System.currentTimeMillis());
   }
}
