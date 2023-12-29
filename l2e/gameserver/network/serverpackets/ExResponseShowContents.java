package l2e.gameserver.network.serverpackets;

public class ExResponseShowContents extends GameServerPacket {
   private final String _contents;

   public ExResponseShowContents(String contents) {
      this._contents = contents;
   }

   @Override
   protected void writeImpl() {
      this.writeS(this._contents);
   }
}
