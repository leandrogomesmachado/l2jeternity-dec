package l2e.gameserver.network.serverpackets;

public class ExNoticePostArrived extends GameServerPacket {
   private static final ExNoticePostArrived STATIC_PACKET_TRUE = new ExNoticePostArrived(true);
   private static final ExNoticePostArrived STATIC_PACKET_FALSE = new ExNoticePostArrived(false);
   private final boolean _showAnim;

   public static final ExNoticePostArrived valueOf(boolean result) {
      return result ? STATIC_PACKET_TRUE : STATIC_PACKET_FALSE;
   }

   public ExNoticePostArrived(boolean showAnimation) {
      this._showAnim = showAnimation;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._showAnim ? 1 : 0);
   }
}
