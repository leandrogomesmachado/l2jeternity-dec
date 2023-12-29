package l2e.gameserver.network.serverpackets;

public class TradeRequest extends GameServerPacket {
   private final int _senderID;

   public TradeRequest(int senderID) {
      this._senderID = senderID;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._senderID);
   }
}
