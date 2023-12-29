package l2e.gameserver.network.serverpackets;

public class ExNoticePostSent extends GameServerPacket {
   private static final ExNoticePostSent STATIC_PACKET_TRUE = new ExNoticePostSent(true);
   private static final ExNoticePostSent STATIC_PACKET_FALSE = new ExNoticePostSent(false);
   private final boolean _showAnim;

   public static final ExNoticePostSent valueOf(boolean result) {
      return result ? STATIC_PACKET_TRUE : STATIC_PACKET_FALSE;
   }

   public ExNoticePostSent(boolean showAnimation) {
      this._showAnim = showAnimation;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._showAnim ? 1 : 0);
   }
}
