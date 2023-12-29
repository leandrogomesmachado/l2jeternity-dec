package l2e.gameserver.network.serverpackets;

public final class JoinPledge extends GameServerPacket {
   private final int _pledgeId;

   public JoinPledge(int pledgeId) {
      this._pledgeId = pledgeId;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._pledgeId);
   }
}
