package l2e.gameserver.network.communication.loginserverpackets;

import l2e.gameserver.network.communication.AuthServerCommunication;
import l2e.gameserver.network.communication.ReceivablePacket;
import l2e.gameserver.network.communication.gameserverpackets.PingResponse;

public class PingRequest extends ReceivablePacket {
   @Override
   public void readImpl() {
   }

   @Override
   protected void runImpl() {
      AuthServerCommunication.getInstance().sendPacket(new PingResponse());
   }
}
