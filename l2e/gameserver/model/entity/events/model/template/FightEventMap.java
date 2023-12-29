package l2e.gameserver.model.entity.events.model.template;

import java.util.Map;
import l2e.commons.collections.MultiValueSet;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.zone.ZoneType;

public class FightEventMap {
   private final String _name;
   private final String[] _events;
   private final int[] _teamsCount;
   private final int _minAllPlayers;
   private final int _maxAllPlayers;
   private final Map<Integer, Location[]> _teamSpawns;
   private final Map<Integer, Map<String, ZoneType>> _territories;
   private final Map<Integer, Map<Integer, Location[]>> _npcWaypath;
   private final int[] _doors;
   private final Location[] _keyLocations;
   private final Location[] _defLocations;

   public FightEventMap(
      MultiValueSet<String> params,
      Map<Integer, Location[]> teamSpawns,
      Map<Integer, Map<String, ZoneType>> territories,
      Map<Integer, Map<Integer, Location[]>> npcWaypath,
      Location[] keyLocations,
      Location[] defLocations
   ) {
      this._name = params.getString("name");
      this._events = params.getString("events").split(";");
      this._minAllPlayers = Integer.parseInt(params.getString("minAllPlayers", "-1"));
      this._maxAllPlayers = Integer.parseInt(params.getString("maxAllPlayers", "-1"));
      String[] doorList = params.getString("doors", "0").split(";");
      this._doors = new int[doorList.length];

      for(int i = 0; i < doorList.length; ++i) {
         this._doors[i] = Integer.parseInt(doorList[i]);
      }

      String[] teamCounts = params.getString("teamsCount", "-1").split(";");
      this._teamsCount = new int[teamCounts.length];

      for(int i = 0; i < teamCounts.length; ++i) {
         this._teamsCount[i] = Integer.parseInt(teamCounts[i]);
      }

      this._teamSpawns = teamSpawns;
      this._territories = territories;
      this._npcWaypath = npcWaypath;
      this._keyLocations = keyLocations;
      this._defLocations = defLocations;
   }

   public String getName() {
      return this._name;
   }

   public String[] getEvents() {
      return this._events;
   }

   public int[] getTeamCount() {
      return this._teamsCount;
   }

   public int getMinAllPlayers() {
      return this._minAllPlayers;
   }

   public int getMaxAllPlayers() {
      return this._maxAllPlayers;
   }

   public int[] getDoors() {
      return this._doors;
   }

   public Map<Integer, Location[]> getTeamSpawns() {
      return this._teamSpawns;
   }

   public Location[] getPlayerSpawns() {
      return this._teamSpawns.get(-1);
   }

   public Map<Integer, Map<String, ZoneType>> getTerritories() {
      return this._territories;
   }

   public Map<Integer, Map<Integer, Location[]>> getNpcWaypath() {
      return this._npcWaypath;
   }

   public Location[] getKeyLocations() {
      return this._keyLocations;
   }

   public Location[] getDefLocations() {
      return this._defLocations;
   }
}
