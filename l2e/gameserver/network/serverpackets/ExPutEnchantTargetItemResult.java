package l2e.gameserver.network.serverpackets;

public class ExPutEnchantTargetItemResult extends GameServerPacket {
   private final int _result;

   public ExPutEnchantTargetItemResult(int result) {
      this._result = result;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._result);
   }
}
