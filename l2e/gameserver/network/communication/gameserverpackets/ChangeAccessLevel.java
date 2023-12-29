package l2e.gameserver.network.communication.gameserverpackets;

import l2e.gameserver.network.communication.SendablePacket;

public class ChangeAccessLevel extends SendablePacket {
   private final String _account;
   private final int _level;
   private final int _banExpire;

   public ChangeAccessLevel(String account, int level, int banExpire) {
      this._account = account;
      this._level = level;
      this._banExpire = banExpire;
   }

   @Override
   protected void writeImpl() {
      this.writeC(17);
      this.writeS(this._account);
      this.writeD(this._level);
      this.writeD(this._banExpire);
   }
}
