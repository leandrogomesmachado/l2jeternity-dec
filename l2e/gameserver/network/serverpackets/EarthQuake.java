package l2e.gameserver.network.serverpackets;

public class EarthQuake extends GameServerPacket {
   private final int _x;
   private final int _y;
   private final int _z;
   private final int _intensity;
   private final int _duration;

   public EarthQuake(int x, int y, int z, int intensity, int duration) {
      this._x = x;
      this._y = y;
      this._z = z;
      this._intensity = intensity;
      this._duration = duration;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
      this.writeD(this._intensity);
      this.writeD(this._duration);
      this.writeD(0);
   }
}
