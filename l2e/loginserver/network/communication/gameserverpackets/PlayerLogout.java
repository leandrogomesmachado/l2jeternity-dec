package l2e.loginserver.network.communication.gameserverpackets;

import l2e.loginserver.network.communication.GameServer;
import l2e.loginserver.network.communication.ReceivablePacket;

public class PlayerLogout extends ReceivablePacket {
   private String _account;

   @Override
   protected void readImpl() {
      this._account = this.readS();
   }

   @Override
   protected void runImpl() {
      GameServer gs = this.getGameServer();
      if (gs.isAuthed()) {
         gs.removeAccount(this._account);
      }
   }
}
