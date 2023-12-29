package l2e.gameserver.network.serverpackets;

public class AskJoinParty extends GameServerPacket {
   private final String _requestorName;
   private final int _itemDistribution;

   public AskJoinParty(String requestorName, int itemDistribution) {
      this._requestorName = requestorName;
      this._itemDistribution = itemDistribution;
   }

   @Override
   protected final void writeImpl() {
      this.writeS(this._requestorName);
      this.writeD(this._itemDistribution);
   }
}
