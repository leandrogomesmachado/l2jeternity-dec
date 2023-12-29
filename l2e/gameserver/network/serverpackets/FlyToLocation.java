package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;

public final class FlyToLocation extends GameServerPacket {
   private final int _chaObjId;
   private final FlyToLocation.FlyType _type;
   private final Location _loc;
   private final Location _destLoc;

   public FlyToLocation(Creature cha, Location destLoc, FlyToLocation.FlyType type) {
      this._destLoc = destLoc;
      this._type = type;
      this._chaObjId = cha.getObjectId();
      this._loc = cha.getLocation();
   }

   public FlyToLocation(Creature cha, int destX, int destY, int destZ, FlyToLocation.FlyType type) {
      this._chaObjId = cha.getObjectId();
      this._destLoc = new Location(destX, destY, destZ);
      this._type = type;
      this._loc = cha.getLocation();
   }

   public FlyToLocation(Creature cha, GameObject dest, FlyToLocation.FlyType type) {
      this(cha, dest.getX(), dest.getY(), dest.getZ(), type);
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._chaObjId);
      this.writeD(this._destLoc.getX());
      this.writeD(this._destLoc.getY());
      this.writeD(this._destLoc.getZ());
      this.writeD(this._loc.getX());
      this.writeD(this._loc.getY());
      this.writeD(this._loc.getZ());
      this.writeD(this._type.ordinal());
   }

   public static enum FlyType {
      THROW_UP,
      THROW_HORIZONTAL,
      DUMMY,
      CHARGE,
      NONE;
   }
}
