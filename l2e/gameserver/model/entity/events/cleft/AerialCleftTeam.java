package l2e.gameserver.model.entity.events.cleft;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.commons.util.Rnd;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;

public class AerialCleftTeam {
   private final String _name;
   private final int _teamId;
   private Location[] _coordinates = new Location[3];
   private Location[] _exitLoc = new Location[3];
   private short _points;
   private final Map<Integer, Player> _participatedPlayers = new ConcurrentHashMap<>();
   private final Map<Integer, Long> _participatedTimes = new ConcurrentHashMap<>();
   List<Player> _playersList = new ArrayList<>();
   private Player _teamCat;

   public AerialCleftTeam(String name, int teamId, Location[] coordinates, Location[] exitLoc) {
      this._name = name;
      this._teamId = teamId;
      this._coordinates = coordinates;
      this._exitLoc = exitLoc;
      this._points = 0;
   }

   public void addPlayer(Player player) {
      if (player != null) {
         this._playersList.add(player);
         synchronized(this._participatedPlayers) {
            this._participatedPlayers.put(player.getObjectId(), player);
         }
      }
   }

   public void startEventTime(Player player) {
      if (player != null) {
         synchronized(this._participatedTimes) {
            this._participatedTimes.put(player.getObjectId(), System.currentTimeMillis());
         }
      }
   }

   public void removePlayer(int objectId) {
      synchronized(this._participatedPlayers) {
         this._participatedPlayers.remove(objectId);
      }
   }

   public void removePlayerFromList(Player player) {
      this._playersList.remove(player);
   }

   public void removePlayerTime(int objectId) {
      synchronized(this._participatedTimes) {
         this._participatedTimes.remove(objectId);
      }
   }

   public void addPoints(int count) {
      this._points = (short)(this._points + count);
   }

   public void cleanMe() {
      this._participatedPlayers.clear();
      this._participatedTimes.clear();
      this._points = 0;
      this._teamCat = null;
      this._playersList.clear();
   }

   public boolean containsPlayer(int playerObjectId) {
      synchronized(this._participatedPlayers) {
         return this._participatedPlayers.containsKey(playerObjectId);
      }
   }

   public boolean containsTime(int playerObjectId) {
      synchronized(this._participatedTimes) {
         return this._participatedTimes.containsKey(playerObjectId);
      }
   }

   public String getName() {
      return this._name;
   }

   public int getId() {
      return this._teamId;
   }

   public Location[] getLocations() {
      return this._coordinates;
   }

   public Location[] getExitLocations() {
      return this._exitLoc;
   }

   public short getPoints() {
      return this._points;
   }

   public Player getTeamCat() {
      return this._teamCat;
   }

   public Map<Integer, Player> getParticipatedPlayers() {
      Map<Integer, Player> participatedPlayers = null;
      synchronized(this._participatedPlayers) {
         return this._participatedPlayers;
      }
   }

   public Map<Integer, Long> getParticipatedTimes() {
      Map<Integer, Long> participatedPlayers = null;
      synchronized(this._participatedTimes) {
         return this._participatedTimes;
      }
   }

   public int getParticipatedPlayerCount() {
      synchronized(this._participatedPlayers) {
         return this._participatedPlayers.size();
      }
   }

   public void selectTeamCat() {
      if (!this._playersList.isEmpty()) {
         Player targetPlayer = this._playersList.get(Rnd.get(this._playersList.size()));
         if (targetPlayer != null) {
            this._teamCat = targetPlayer;
            targetPlayer.setCleftCat(true);
         }
      }
   }
}
