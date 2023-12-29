package l2e.gameserver.network.serverpackets;

public class ExRestartClient extends GameServerPacket {
   public static final ExRestartClient STATIC_PACKET = new ExRestartClient();

   private ExRestartClient() {
   }

   @Override
   protected void writeImpl() {
   }
}
