package l2e.gameserver.network.serverpackets;

public final class AskJoinPledge extends GameServerPacket {
   private final int _requestorObjId;
   private final String _subPledgeName;
   private final int _pledgeType;
   private final String _pledgeName;

   public AskJoinPledge(int requestorObjId, String subPledgeName, int pledgeType, String pledgeName) {
      this._requestorObjId = requestorObjId;
      this._subPledgeName = subPledgeName;
      this._pledgeType = pledgeType;
      this._pledgeName = pledgeName;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._requestorObjId);
      if (this._subPledgeName != null) {
         this.writeS(this._pledgeType > 0 ? this._subPledgeName : this._pledgeName);
      }

      if (this._pledgeType != 0) {
         this.writeD(this._pledgeType);
      }

      this.writeS(this._pledgeName);
   }
}
