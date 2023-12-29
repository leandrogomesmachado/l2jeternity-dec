package l2e.gameserver.network.serverpackets;

public class ExAskJoinMPCC extends GameServerPacket {
   private final String _requestorName;

   public ExAskJoinMPCC(String requestorName) {
      this._requestorName = requestorName;
   }

   @Override
   protected void writeImpl() {
      this.writeS(this._requestorName);
   }
}
