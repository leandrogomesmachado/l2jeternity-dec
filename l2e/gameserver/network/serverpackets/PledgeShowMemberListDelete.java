package l2e.gameserver.network.serverpackets;

public class PledgeShowMemberListDelete extends GameServerPacket {
   private final String _player;

   public PledgeShowMemberListDelete(String playerName) {
      this._player = playerName;
   }

   @Override
   protected final void writeImpl() {
      this.writeS(this._player);
   }
}
