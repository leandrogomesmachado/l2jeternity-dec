package l2e.gameserver.network.communication.loginserverpackets;

import l2e.gameserver.network.communication.AuthServerCommunication;
import l2e.gameserver.network.communication.ReceivablePacket;

public class LoginServerFail extends ReceivablePacket {
   private static final String[] REASONS = new String[]{
      "none", "IP banned", "IP reserved", "wrong hexid", "ID reserved", "no free ID", "not authed", "already logged in"
   };
   private String _reason;
   private boolean _restartConnection = true;

   @Override
   protected void readImpl() {
      int reasonId = this.readC();
      if (!this.getByteBuffer().hasRemaining()) {
         this._reason = "Authserver registration failed! Reason: " + REASONS[reasonId];
      } else {
         this._reason = this.readS();
         this._restartConnection = this.readC() > 0;
      }
   }

   @Override
   protected void runImpl() {
      _log.warning(this._reason);
      if (this._restartConnection) {
         AuthServerCommunication.getInstance().restart();
      }
   }
}
