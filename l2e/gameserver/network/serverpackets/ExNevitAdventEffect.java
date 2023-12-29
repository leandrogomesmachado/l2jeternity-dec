package l2e.gameserver.network.serverpackets;

public class ExNevitAdventEffect extends GameServerPacket {
   private int _timeLeft;

   public ExNevitAdventEffect(int timeLeft) {
      this._timeLeft = timeLeft;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._timeLeft);
   }
}
