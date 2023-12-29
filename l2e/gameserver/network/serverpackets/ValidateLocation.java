package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;

public class ValidateLocation extends GameServerPacket {
   private final int _charObjId;
   private final Location _loc;

   public ValidateLocation(GameObject obj) {
      this._charObjId = obj.getObjectId();
      this._loc = obj.getLocation();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._loc.getX());
      this.writeD(this._loc.getY());
      this.writeD(this._loc.getZ());
      this.writeD(this._loc.getHeading());
   }
}
