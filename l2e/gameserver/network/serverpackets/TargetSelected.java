package l2e.gameserver.network.serverpackets;

public final class TargetSelected extends GameServerPacket {
   private final int _objectId;
   private final int _targetObjId;
   private final int _x;
   private final int _y;
   private final int _z;

   public TargetSelected(int objectId, int targetId, int x, int y, int z) {
      this._objectId = objectId;
      this._targetObjId = targetId;
      this._x = x;
      this._y = y;
      this._z = z;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._objectId);
      this.writeD(this._targetObjId);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
      this.writeD(0);
   }
}
