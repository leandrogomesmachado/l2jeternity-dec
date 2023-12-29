package l2e.gameserver.network.serverpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.GameObject;

public final class TeleportToLocation extends GameServerPacket {
   private final int _targetObjId;
   private final int _x;
   private final int _y;
   private final int _z;
   private final int _heading;

   public TeleportToLocation(GameObject obj, int x, int y, int z, int heading) {
      this._targetObjId = obj.getObjectId();
      this._x = x;
      this._y = y;
      this._z = z;
      this._heading = heading;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._targetObjId);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z + Config.CLIENT_SHIFTZ);
      this.writeD(0);
      this.writeD(this._heading);
   }
}
