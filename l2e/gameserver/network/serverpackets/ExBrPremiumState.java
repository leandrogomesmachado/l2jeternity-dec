package l2e.gameserver.network.serverpackets;

public class ExBrPremiumState extends GameServerPacket {
   private final int _objId;
   private final int _state;

   public ExBrPremiumState(int id, int state) {
      this._objId = id;
      this._state = state;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._objId);
      this.writeC(this._state);
   }
}
