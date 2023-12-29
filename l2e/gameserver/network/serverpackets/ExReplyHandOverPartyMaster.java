package l2e.gameserver.network.serverpackets;

public class ExReplyHandOverPartyMaster extends GameServerPacket {
   public static final GameServerPacket TRUE = new ExReplyHandOverPartyMaster(true);
   public static final GameServerPacket FALSE = new ExReplyHandOverPartyMaster(false);
   private final boolean _isLeader;

   public ExReplyHandOverPartyMaster(boolean leader) {
      this._isLeader = leader;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._isLeader);
   }
}
