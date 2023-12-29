package l2e.gameserver.network.serverpackets;

public class ExChangeNpcState extends GameServerPacket {
   private final int _objId;
   private final int _state;

   public ExChangeNpcState(int objId, int state) {
      this._objId = objId;
      this._state = state;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._objId);
      this.writeD(this._state);
   }
}
