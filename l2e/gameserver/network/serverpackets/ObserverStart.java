package l2e.gameserver.network.serverpackets;

public class ObserverStart extends GameServerPacket {
   private final int _x;
   private final int _y;
   private final int _z;

   public ObserverStart(int x, int y, int z) {
      this._x = x;
      this._y = y;
      this._z = z;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
      this.writeC(0);
      this.writeC(192);
      this.writeC(0);
   }
}
