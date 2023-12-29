package l2e.gameserver.network.serverpackets;

public class ServerClose extends GameServerPacket {
   public static final ServerClose STATIC_PACKET = new ServerClose();

   private ServerClose() {
   }

   @Override
   protected void writeImpl() {
   }
}
