package l2e.gameserver.network.serverpackets;

public class ExAskCoupleAction extends GameServerPacket {
   private final int _charObjId;
   private final int _actionId;

   public ExAskCoupleAction(int charObjId, int social) {
      this._charObjId = charObjId;
      this._actionId = social;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._actionId);
      this.writeD(this._charObjId);
   }
}
