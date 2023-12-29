package l2e.gameserver.network.serverpackets;

public final class JoinParty extends GameServerPacket {
   private final int _response;

   public JoinParty(int response) {
      this._response = response;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._response);
   }
}
