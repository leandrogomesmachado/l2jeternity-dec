package l2e.gameserver.network.serverpackets;

public class Dummy_8D extends GameServerPacket {
   private final String _url;

   public Dummy_8D(String url) {
      this._url = url;
   }

   @Override
   protected final void writeImpl() {
      this.writeC(3);
      this.writeS(this._url);
   }
}
