package l2e.gameserver.network.serverpackets;

public class ExAskModifyPartyLooting extends GameServerPacket {
   private final String _requestor;
   private final byte _mode;

   public ExAskModifyPartyLooting(String name, byte mode) {
      this._requestor = name;
      this._mode = mode;
   }

   @Override
   protected void writeImpl() {
      this.writeS(this._requestor);
      this.writeD(this._mode);
   }
}
