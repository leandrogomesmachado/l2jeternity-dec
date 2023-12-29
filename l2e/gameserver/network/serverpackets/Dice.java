package l2e.gameserver.network.serverpackets;

public class Dice extends GameServerPacket {
   private final int _charObjId;
   private final int _itemId;
   private final int _number;
   private final int _x;
   private final int _y;
   private final int _z;

   public Dice(int charObjId, int itemId, int number, int x, int y, int z) {
      this._charObjId = charObjId;
      this._itemId = itemId;
      this._number = number;
      this._x = x;
      this._y = y;
      this._z = z;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._itemId);
      this.writeD(this._number);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
   }
}
