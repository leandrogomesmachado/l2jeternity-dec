package l2e.gameserver.model.zone;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.model.Location;

public abstract class ZoneRespawn extends ZoneType {
   private List<Location> _spawnLocs = null;
   private List<Location> _otherSpawnLocs = null;
   private List<Location> _chaoticSpawnLocs = null;
   private List<Location> _banishSpawnLocs = null;

   protected ZoneRespawn(int id) {
      super(id);
   }

   public void parseLoc(int x, int y, int z, String type) {
      if (type != null && !type.isEmpty()) {
         switch(type) {
            case "other":
               this.addOtherSpawn(x, y, z);
               break;
            case "chaotic":
               this.addChaoticSpawn(x, y, z);
               break;
            case "banish":
               this.addBanishSpawn(x, y, z);
               break;
            default:
               _log.warning(this.getClass().getSimpleName() + ": Unknown location type: " + type);
         }
      } else {
         this.addSpawn(x, y, z);
      }
   }

   public final void addSpawn(int x, int y, int z) {
      if (this._spawnLocs == null) {
         this._spawnLocs = new ArrayList<>();
      }

      this._spawnLocs.add(new Location(x, y, z));
   }

   public final void addOtherSpawn(int x, int y, int z) {
      if (this._otherSpawnLocs == null) {
         this._otherSpawnLocs = new ArrayList<>();
      }

      this._otherSpawnLocs.add(new Location(x, y, z));
   }

   public final void addChaoticSpawn(int x, int y, int z) {
      if (this._chaoticSpawnLocs == null) {
         this._chaoticSpawnLocs = new ArrayList<>();
      }

      this._chaoticSpawnLocs.add(new Location(x, y, z));
   }

   public final void addBanishSpawn(int x, int y, int z) {
      if (this._banishSpawnLocs == null) {
         this._banishSpawnLocs = new ArrayList<>();
      }

      this._banishSpawnLocs.add(new Location(x, y, z));
   }

   public final List<Location> getSpawns() {
      return this._spawnLocs;
   }

   public final Location getSpawnLoc() {
      return Config.RANDOM_RESPAWN_IN_TOWN_ENABLED ? this._spawnLocs.get(Rnd.get(this._spawnLocs.size())) : this._spawnLocs.get(0);
   }

   public final Location getOtherSpawnLoc() {
      if (this._otherSpawnLocs != null) {
         return Config.RANDOM_RESPAWN_IN_TOWN_ENABLED ? this._otherSpawnLocs.get(Rnd.get(this._otherSpawnLocs.size())) : this._otherSpawnLocs.get(0);
      } else {
         return this.getSpawnLoc();
      }
   }

   public final Location getChaoticSpawnLoc() {
      if (this._chaoticSpawnLocs != null) {
         return Config.RANDOM_RESPAWN_IN_TOWN_ENABLED ? this._chaoticSpawnLocs.get(Rnd.get(this._chaoticSpawnLocs.size())) : this._chaoticSpawnLocs.get(0);
      } else {
         return this.getSpawnLoc();
      }
   }

   public final Location getBanishSpawnLoc() {
      if (this._banishSpawnLocs != null) {
         return Config.RANDOM_RESPAWN_IN_TOWN_ENABLED ? this._banishSpawnLocs.get(Rnd.get(this._banishSpawnLocs.size())) : this._banishSpawnLocs.get(0);
      } else {
         return this.getSpawnLoc();
      }
   }
}
