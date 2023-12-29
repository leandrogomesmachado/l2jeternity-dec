package l2e.gameserver.network.serverpackets;

public class TradeDone extends GameServerPacket {
   private final int _num;

   public TradeDone(int num) {
      this._num = num;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._num);
   }
}
