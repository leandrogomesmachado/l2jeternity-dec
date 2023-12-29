package l2e.gameserver.network.serverpackets;

public final class TutorialShowHtml extends GameServerPacket {
   private final String _html;

   public TutorialShowHtml(String html) {
      this._html = html;
   }

   @Override
   protected void writeImpl() {
      this.writeS(this._html);
   }
}
