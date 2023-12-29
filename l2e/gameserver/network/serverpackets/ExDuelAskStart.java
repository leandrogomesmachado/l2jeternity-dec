package l2e.gameserver.network.serverpackets;

public class ExDuelAskStart extends GameServerPacket {
   private final String _requestorName;
   private final int _partyDuel;

   public ExDuelAskStart(String requestor, int partyDuel) {
      this._requestorName = requestor;
      this._partyDuel = partyDuel;
   }

   @Override
   protected void writeImpl() {
      this.writeS(this._requestorName);
      this.writeD(this._partyDuel);
   }
}
