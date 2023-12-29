package l2e.gameserver.network.serverpackets;

public class ExConfirmAddingPostFriend extends GameServerPacket {
   private final String _charName;
   private final boolean _added;

   public ExConfirmAddingPostFriend(String charName, boolean added) {
      this._charName = charName;
      this._added = added;
   }

   @Override
   protected void writeImpl() {
      this.writeS(this._charName);
      this.writeD(this._added ? 1 : 0);
   }
}
