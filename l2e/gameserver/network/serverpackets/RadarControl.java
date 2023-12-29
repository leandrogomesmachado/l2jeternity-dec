package l2e.gameserver.network.serverpackets;

public class RadarControl extends GameServerPacket {
   private final int _showRadar;
   private final int _type;
   private final int _x;
   private final int _y;
   private final int _z;

   public RadarControl(int showRadar, int type, int x, int y, int z) {
      this._showRadar = showRadar;
      this._type = type;
      this._x = x;
      this._y = y;
      this._z = z;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._showRadar);
      this.writeD(this._type);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
   }
}
