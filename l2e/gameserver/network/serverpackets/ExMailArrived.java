package l2e.gameserver.network.serverpackets;

public class ExMailArrived extends GameServerPacket {
   public static final ExMailArrived STATIC_PACKET = new ExMailArrived();

   private ExMailArrived() {
   }

   @Override
   protected void writeImpl() {
   }
}
