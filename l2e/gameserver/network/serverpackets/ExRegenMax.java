package l2e.gameserver.network.serverpackets;

public class ExRegenMax extends GameServerPacket {
   private final double _max;
   private final int _count;
   private final int _time;

   public ExRegenMax(double max, int count, int time) {
      this._max = max;
      this._count = count;
      this._time = time;
   }

   @Override
   protected void writeImpl() {
      this.writeD(1);
      this.writeD(this._count);
      this.writeD(this._time);
      this.writeF(this._max);
   }
}
