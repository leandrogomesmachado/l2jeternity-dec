package l2e.gameserver.network.serverpackets;

public final class SetSummonRemainTime extends GameServerPacket {
   private final int _maxTime;
   private final int _remainingTime;

   public SetSummonRemainTime(int maxTime, int remainingTime) {
      this._remainingTime = remainingTime;
      this._maxTime = maxTime;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._maxTime);
      this.writeD(this._remainingTime);
   }
}
