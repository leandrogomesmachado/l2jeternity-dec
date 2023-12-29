package l2e.loginserver.network.communication.loginserverpackets;

import l2e.loginserver.network.communication.SendablePacket;

public class LoginServerFail extends SendablePacket {
   private final String _reason;
   private final boolean _restartConnection;

   public LoginServerFail(String reason, boolean restartConnection) {
      this._reason = reason;
      this._restartConnection = restartConnection;
   }

   @Override
   protected void writeImpl() {
      this.writeC(1);
      this.writeC(0);
      this.writeS(this._reason);
      this.writeC(this._restartConnection ? 1 : 0);
   }
}
