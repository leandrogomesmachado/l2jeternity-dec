package l2e.gameserver.model.actor.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.base.Race;

public class MapRegionTemplate {
   private final String _name;
   private final String _town;
   private final int _locId;
   private final int _castle;
   private final int _bbs;
   private List<int[]> _maps = null;
   private List<Location> _spawnLocs = null;
   private List<Location> _otherSpawnLocs = null;
   private List<Location> _chaoticSpawnLocs = null;
   private List<Location> _banishSpawnLocs = null;
   private final Map<Race, String> _bannedRace = new HashMap<>();

   public MapRegionTemplate(String name, String town, int locId, int castle, int bbs) {
      this._name = name;
      this._town = town;
      this._locId = locId;
      this._castle = castle;
      this._bbs = bbs;
   }

   public final String getName() {
      return this._name;
   }

   public final String getTown() {
      return this._town;
   }

   public final int getLocId() {
      return this._locId;
   }

   public final int getCastle() {
      return this._castle;
   }

   public final int getBbs() {
      return this._bbs;
   }

   public final void addMap(int x, int y) {
      if (this._maps == null) {
         this._maps = new ArrayList<>();
      }

      this._maps.add(new int[]{x, y});
   }

   public final List<int[]> getMaps() {
      return this._maps;
   }

   public final boolean isZoneInRegion(int x, int y) {
      if (this._maps == null) {
         return false;
      } else {
         for(int[] map : this._maps) {
            if (map[0] == x && map[1] == y) {
               return true;
            }
         }

         return false;
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

   public final void addBannedRace(String race, String point) {
      this._bannedRace.put(Race.valueOf(race), point);
   }

   public final Map<Race, String> getBannedRace() {
      return this._bannedRace;
   }
}
