package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;

public final class StopMove extends GameServerPacket {
   private final int _objectId;
   private final int _x;
   private final int _y;
   private final int _z;
   private final int _heading;

   public StopMove(Creature cha) {
      this._objectId = cha.getObjectId();
      this._x = cha.getX();
      this._y = cha.getY();
      this._z = cha.getZ();
      this._heading = cha.getHeading();
   }

   public StopMove(int objectId, int x, int y, int z, int heading) {
      this._objectId = objectId;
      this._x = x;
      this._y = y;
      this._z = z;
      this._heading = heading;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._objectId);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
      this.writeD(this._heading);
   }
}
