package l2e.gameserver.network.serverpackets;

public class FriendRemove extends GameServerPacket {
   private final String _friendName;

   public FriendRemove(String name) {
      this._friendName = name;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(1);
      this.writeS(this._friendName);
   }
}
