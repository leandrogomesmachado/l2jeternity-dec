package l2e.gameserver.network.serverpackets;

public class ExBrLoadEventTopRankers extends GameServerPacket {
   private final int _eventId;
   private final int _day;
   private final int _count;
   private final int _bestScore;
   private final int _myScore;

   public ExBrLoadEventTopRankers(int eventId, int day, int count, int bestScore, int myScore) {
      this._eventId = eventId;
      this._day = day;
      this._count = count;
      this._bestScore = bestScore;
      this._myScore = myScore;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._eventId);
      this.writeD(this._day);
      this.writeD(this._count);
      this.writeD(this._bestScore);
      this.writeD(this._myScore);
   }
}
