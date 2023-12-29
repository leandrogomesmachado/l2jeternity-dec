package l2e.gameserver.network.serverpackets;

public class SunSet extends GameServerPacket {
   public static final SunSet STATIC_PACKET = new SunSet();

   private SunSet() {
   }

   @Override
   protected final void writeImpl() {
   }
}
