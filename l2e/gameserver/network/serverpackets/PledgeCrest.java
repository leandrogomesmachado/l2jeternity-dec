package l2e.gameserver.network.serverpackets;

public final class PledgeCrest extends GameServerPacket {
   private final int _crestId;
   private final int _crestSize;
   private final byte[] _data;

   public PledgeCrest(int crestId, byte[] data) {
      this._crestId = crestId;
      this._data = data;
      this._crestSize = this._data.length;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._crestId);
      this.writeD(this._crestSize);
      this.writeB(this._data);
   }
}
