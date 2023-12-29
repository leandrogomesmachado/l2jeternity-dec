package l2e.gameserver.network.serverpackets;

public class ExAskJoinPartyRoom extends GameServerPacket {
   private final String _charName;
   private final String _roomName;

   public ExAskJoinPartyRoom(String charName, String roomName) {
      this._charName = charName;
      this._roomName = roomName;
   }

   @Override
   protected void writeImpl() {
      this.writeS(this._charName);
      this.writeS(this._roomName);
   }
}
