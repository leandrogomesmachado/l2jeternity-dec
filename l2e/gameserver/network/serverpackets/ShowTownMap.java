package l2e.gameserver.network.serverpackets;

public class ShowTownMap extends GameServerPacket {
   private final String _texture;
   private final int _x;
   private final int _y;

   public ShowTownMap(String texture, int x, int y) {
      this._texture = texture;
      this._x = x;
      this._y = y;
   }

   @Override
   protected final void writeImpl() {
      this.writeS(this._texture);
      this.writeD(this._x);
      this.writeD(this._y);
   }
}
