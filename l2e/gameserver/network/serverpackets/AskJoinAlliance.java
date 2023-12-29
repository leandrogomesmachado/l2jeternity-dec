package l2e.gameserver.network.serverpackets;

public class AskJoinAlliance extends GameServerPacket {
   private final String _requestorName;
   private final String _requestorAllyName;
   private final int _requestorObjId;

   public AskJoinAlliance(int requestorObjId, String requestorName, String requestorAllyName) {
      this._requestorName = requestorName;
      this._requestorAllyName = requestorAllyName;
      this._requestorObjId = requestorObjId;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._requestorObjId);
      this.writeS(this._requestorName);
      this.writeS("");
      this.writeS(this._requestorAllyName);
   }
}
