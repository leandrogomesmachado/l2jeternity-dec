package l2e.gameserver.network.serverpackets;

public class GameGuardQuery extends GameServerPacket {
   @Override
   public void writeImpl() {
      this.writeD(659766745);
      this.writeD(779265309);
      this.writeD(538379147);
      this.writeD(-1017438557);
   }
}
