package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;

public class MoveToPawn extends GameServerPacket {
   private final int _charObjId;
   private final int _targetId;
   private final int _distance;
   private final int _x;
   private final int _y;
   private final int _z;
   private final int _tx;
   private final int _ty;
   private final int _tz;

   public MoveToPawn(Creature cha, Creature target, int distance) {
      this._charObjId = cha.getObjectId();
      this._targetId = target.getObjectId();
      this._distance = distance;
      this._x = cha.getX();
      this._y = cha.getY();
      this._z = cha.getZ();
      this._tx = target.getX();
      this._ty = target.getY();
      this._tz = target.getZ();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._targetId);
      this.writeD(this._distance);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
      this.writeD(this._tx);
      this.writeD(this._ty);
      this.writeD(this._tz);
   }
}
