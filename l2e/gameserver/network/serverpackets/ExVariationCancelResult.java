package l2e.gameserver.network.serverpackets;

public class ExVariationCancelResult extends GameServerPacket {
   private final int _result;

   public ExVariationCancelResult(int result) {
      this._result = result;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._result);
   }
}
