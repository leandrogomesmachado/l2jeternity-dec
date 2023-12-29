package l2e.gameserver.network.serverpackets;

public final class RestartResponse extends GameServerPacket {
   private static final RestartResponse STATIC_PACKET_TRUE = new RestartResponse(true);
   private static final RestartResponse STATIC_PACKET_FALSE = new RestartResponse(false);
   private final boolean _result;

   public static final RestartResponse valueOf(boolean result) {
      return result ? STATIC_PACKET_TRUE : STATIC_PACKET_FALSE;
   }

   public RestartResponse(boolean result) {
      this._result = result;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._result ? 1 : 0);
   }
}
