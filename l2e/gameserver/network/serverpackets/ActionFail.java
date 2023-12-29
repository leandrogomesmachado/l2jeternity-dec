package l2e.gameserver.network.serverpackets;

public final class ActionFail extends GameServerPacket {
   public static final ActionFail STATIC_PACKET = new ActionFail();

   private ActionFail() {
   }

   @Override
   protected void writeImpl() {
   }
}
