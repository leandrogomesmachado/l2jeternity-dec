package l2e.gameserver.network.communication.gameserverpackets;

import l2e.gameserver.network.communication.SendablePacket;

public class ChangePassword extends SendablePacket {
   public String _account;
   public String _oldPass;
   public String _newPass;
   public String _hwid;

   public ChangePassword(String account, String oldPass, String newPass, String hwid) {
      this._account = account;
      this._oldPass = oldPass;
      this._newPass = newPass;
      this._hwid = hwid;
   }

   @Override
   protected void writeImpl() {
      this.writeC(8);
      this.writeS(this._account);
      this.writeS(this._oldPass);
      this.writeS(this._newPass);
      this.writeS(this._hwid);
   }
}
