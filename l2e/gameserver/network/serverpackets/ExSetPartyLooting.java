package l2e.gameserver.network.serverpackets;

public class ExSetPartyLooting extends GameServerPacket {
   private final int _result;
   private final byte _mode;

   public ExSetPartyLooting(int result, byte mode) {
      this._result = result;
      this._mode = mode;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._result);
      this.writeD(this._mode);
   }
}
