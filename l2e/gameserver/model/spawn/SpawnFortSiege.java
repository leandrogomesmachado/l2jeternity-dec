package l2e.gameserver.model.spawn;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.interfaces.IIdentifiable;

public final class SpawnFortSiege extends Location implements IIdentifiable {
   Location _location;
   private final int _npcId;
   private final int _heading;
   private final int _fortId;
   private final int _id;

   public SpawnFortSiege(int fort_id, int x, int y, int z, int heading, int npc_id, int id) {
      super(x, y, z, heading);
      this._fortId = fort_id;
      this._location = new Location(x, y, z, heading);
      this._heading = heading;
      this._npcId = npc_id;
      this._id = id;
   }

   public int getFortId() {
      return this._fortId;
   }

   @Override
   public int getId() {
      return this._npcId;
   }

   @Override
   public int getHeading() {
      return this._heading;
   }

   public Location getLocation() {
      return this._location;
   }

   public int getMessageId() {
      return this._id;
   }
}
