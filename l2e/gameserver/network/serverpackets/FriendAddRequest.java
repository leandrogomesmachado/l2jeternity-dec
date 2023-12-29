package l2e.gameserver.network.serverpackets;

public class FriendAddRequest extends GameServerPacket {
   private final String _requestorName;

   public FriendAddRequest(String requestorName) {
      this._requestorName = requestorName;
   }

   @Override
   protected final void writeImpl() {
      this.writeS(this._requestorName);
      this.writeD(0);
   }
}
