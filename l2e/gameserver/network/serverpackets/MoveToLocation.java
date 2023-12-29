package l2e.gameserver.network.serverpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Creature;

public final class MoveToLocation extends GameServerPacket {
   private final int _charObjId;
   private final int _x;
   private final int _y;
   private final int _z;
   private final int _xDst;
   private final int _yDst;
   private final int _zDst;

   public MoveToLocation(Creature cha) {
      this._charObjId = cha.getObjectId();
      this._x = cha.getX();
      this._y = cha.getY();
      this._z = cha.getZ();
      this._xDst = cha.getXdestination();
      this._yDst = cha.getYdestination();
      this._zDst = cha.getZdestination();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._xDst);
      this.writeD(this._yDst);
      this.writeD(this._zDst + Config.CLIENT_SHIFTZ);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z + Config.CLIENT_SHIFTZ);
   }
}
