package l2e.gameserver.network.serverpackets;

public class EnchantResult extends GameServerPacket {
   private final int _result;
   private final int _crystal;
   private final int _count;

   public EnchantResult(int result, int crystal, int count) {
      this._result = result;
      this._crystal = crystal;
      this._count = count;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._result);
      this.writeD(this._crystal);
      this.writeQ((long)this._count);
   }
}
