package l2e.gameserver.network.serverpackets;

public final class SurrenderPledgeWar extends GameServerPacket {
   private final String _pledgeName;
   private final String _playerName;

   public SurrenderPledgeWar(String pledge, String charName) {
      this._pledgeName = pledge;
      this._playerName = charName;
   }

   @Override
   protected final void writeImpl() {
      this.writeS(this._pledgeName);
      this.writeS(this._playerName);
   }
}
