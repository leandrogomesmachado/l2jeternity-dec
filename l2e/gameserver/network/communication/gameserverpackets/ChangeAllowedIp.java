package l2e.gameserver.network.communication.gameserverpackets;

import l2e.gameserver.network.communication.SendablePacket;

public class ChangeAllowedIp extends SendablePacket {
   private final String _account;
   private final String _ip;

   public ChangeAllowedIp(String account, String ip) {
      this._account = account;
      this._ip = ip;
   }

   @Override
   protected void writeImpl() {
      this.writeC(7);
      this.writeS(this._account);
      this.writeS(this._ip);
   }
}
