package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;

public class ExMoveToLocationAirShip extends GameServerPacket {
   private final int _objId;
   private final int _tx;
   private final int _ty;
   private final int _tz;
   private final int _x;
   private final int _y;
   private final int _z;

   public ExMoveToLocationAirShip(Creature cha) {
      this._objId = cha.getObjectId();
      this._tx = cha.getXdestination();
      this._ty = cha.getYdestination();
      this._tz = cha.getZdestination();
      this._x = cha.getX();
      this._y = cha.getY();
      this._z = cha.getZ();
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._objId);
      this.writeD(this._tx);
      this.writeD(this._ty);
      this.writeD(this._tz);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
   }
}
