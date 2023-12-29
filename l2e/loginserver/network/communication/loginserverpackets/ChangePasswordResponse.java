package l2e.loginserver.network.communication.loginserverpackets;

import l2e.loginserver.network.communication.SendablePacket;

public class ChangePasswordResponse extends SendablePacket {
   public String _account;
   public boolean _hasChanged;

   public ChangePasswordResponse(String account, boolean hasChanged) {
      this._account = account;
      this._hasChanged = hasChanged;
   }

   @Override
   protected void writeImpl() {
      this.writeC(6);
      this.writeS(this._account);
      this.writeD(this._hasChanged ? 1 : 0);
   }
}
