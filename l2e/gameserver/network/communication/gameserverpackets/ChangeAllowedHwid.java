package l2e.gameserver.network.communication.gameserverpackets;

import l2e.gameserver.network.communication.SendablePacket;

public class ChangeAllowedHwid extends SendablePacket {
   private final String _account;
   private final String _hwid;

   public ChangeAllowedHwid(String account, String hwid) {
      this._account = account;
      this._hwid = hwid;
   }

   @Override
   protected void writeImpl() {
      this.writeC(9);
      this.writeS(this._account);
      this.writeS(this._hwid);
   }
}
