package l2e.loginserver.network.serverpackets;

import l2e.loginserver.network.SessionKey;

public final class LoginOk extends LoginServerPacket {
   private final int _loginOk1;
   private final int _loginOk2;

   public LoginOk(SessionKey sessionKey) {
      this._loginOk1 = sessionKey._loginOkID1;
      this._loginOk2 = sessionKey._loginOkID2;
   }

   @Override
   protected void writeImpl() {
      this.writeC(3);
      this.writeD(this._loginOk1);
      this.writeD(this._loginOk2);
      this.writeB(new byte[8]);
      this.writeD(1002);
      this.writeH(60872);
      this.writeC(35);
      this.writeC(6);
      this.writeB(new byte[28]);
   }
}
