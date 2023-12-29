package l2e.gameserver.network.serverpackets;

public class ExPutEnchantSupportItemResult extends GameServerPacket {
   private final int _result;

   public ExPutEnchantSupportItemResult(int result) {
      this._result = result;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._result);
   }
}
