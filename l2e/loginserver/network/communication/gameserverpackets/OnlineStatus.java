package l2e.loginserver.network.communication.gameserverpackets;

import l2e.loginserver.network.communication.GameServer;
import l2e.loginserver.network.communication.ReceivablePacket;

public class OnlineStatus extends ReceivablePacket {
   private boolean _online;

   @Override
   protected void readImpl() {
      this._online = this.readC() == 1;
   }

   @Override
   protected void runImpl() {
      GameServer gameServer = this.getGameServer();
      if (gameServer.isAuthed()) {
         gameServer.setOnline(this._online);
      }
   }
}
