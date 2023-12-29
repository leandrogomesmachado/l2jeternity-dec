package l2e.loginserver.network.communication.gameserverpackets;

import l2e.loginserver.network.communication.GameServer;
import l2e.loginserver.network.communication.ReceivablePacket;

public class PingResponse extends ReceivablePacket {
   private long _serverTime;

   @Override
   protected void readImpl() {
      this._serverTime = this.readQ();
   }

   @Override
   protected void runImpl() {
      GameServer gameServer = this.getGameServer();
      if (gameServer.isAuthed()) {
         gameServer.getConnection().onPingResponse();
         long diff = System.currentTimeMillis() - this._serverTime;
         if (Math.abs(diff) > 999L) {
            _log.warning("Gameserver IP[" + gameServer.getConnection().getIpAddress() + "]: time offset " + diff + " ms.");
         }
      }
   }
}
