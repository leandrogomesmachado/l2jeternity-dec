package l2e.gameserver.network.serverpackets;

public final class ShowTutorialMark extends GameServerPacket {
   private final int _markId;

   public ShowTutorialMark(boolean quest, int blink) {
      this._markId = blink;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._markId);
   }
}
