package l2e.gameserver.model.actor.templates.player.online;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.model.actor.Player;

public class OnlinePlayerTemplate {
   private final String _hwid;
   private final String _ip;
   private final List<Integer> _players = new ArrayList<>();
   private final Map<Integer, Integer> _playerRewards = new HashMap<>();
   private final Map<Integer, Long> _playerTimers = new HashMap<>();

   public OnlinePlayerTemplate(String hwid, String ip) {
      this._hwid = hwid;
      this._ip = ip;
   }

   public String getHWID() {
      return this._hwid;
   }

   public String getIP() {
      return this._ip;
   }

   public List<Integer> getPlayers() {
      return this._players;
   }

   public void addPlayer(Player player) {
      if (!this._players.contains(player.getObjectId())) {
         this._players.add(player.getObjectId());
      }
   }

   public void updatePlayerTimer(Player player, long time) {
      this._playerTimers.put(player.getObjectId(), time);
   }

   public long getPlayerTimer(Player player) {
      return this._playerTimers.containsKey(player.getObjectId()) ? this._playerTimers.get(player.getObjectId()) : 0L;
   }

   public int getPlayerRewardId(Player player) {
      return this._playerRewards.containsKey(player.getObjectId()) ? this._playerRewards.get(player.getObjectId()) : 0;
   }

   public void updatePlayerRewardId(Player player, int nextId) {
      this._playerRewards.put(player.getObjectId(), nextId);
   }

   public void removePlayer(Player player) {
      if (this._players.contains(player.getObjectId())) {
         this._players.remove(this._players.indexOf(player.getObjectId()));
      }
   }

   public boolean getPlayer(Player player) {
      return this._players.contains(player.getObjectId());
   }
}
