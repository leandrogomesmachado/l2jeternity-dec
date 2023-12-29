package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;

public class ExShowLines extends GameServerPacket {
   private final int _x;
   private final int _y;
   private final int _z;

   public ExShowLines(Player player) {
      this._x = player.getX();
      this._y = player.getY();
      this._z = player.getZ();
   }

   @Override
   protected void writeImpl() {
      this.writeH(0);
      this.writeD(2);
      this.writeC(200);
      this.writeC(200);
      this.writeC(256);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
      this.writeD(0);
   }
}
