package l2e.gameserver.network.serverpackets;

public class ExAttributeEnchantResult extends GameServerPacket {
   private final int _result;

   public ExAttributeEnchantResult(int result) {
      this._result = result;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._result);
   }
}
