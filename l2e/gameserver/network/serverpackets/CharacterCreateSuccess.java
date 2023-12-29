package l2e.gameserver.network.serverpackets;

public class CharacterCreateSuccess extends GameServerPacket {
   @Override
   protected final void writeImpl() {
      this.writeD(1);
   }
}
