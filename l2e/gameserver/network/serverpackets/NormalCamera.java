package l2e.gameserver.network.serverpackets;

public class NormalCamera extends GameServerPacket {
   public static final NormalCamera STATIC_PACKET = new NormalCamera();

   private NormalCamera() {
   }

   @Override
   public void writeImpl() {
   }
}
