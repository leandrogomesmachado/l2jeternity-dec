package l2e.gameserver.network.serverpackets;

public class WareHouseDone extends GameServerPacket {
   @Override
   protected final void writeImpl() {
      this.writeD(0);
   }
}
