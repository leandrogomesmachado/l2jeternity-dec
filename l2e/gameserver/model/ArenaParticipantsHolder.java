package l2e.gameserver.model;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.instancemanager.HandysBlockCheckerManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.BlockCheckerEngine;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class ArenaParticipantsHolder {
   private final int _arena;
   private final List<Player> _redPlayers;
   private final List<Player> _bluePlayers;
   private final BlockCheckerEngine _engine;

   public ArenaParticipantsHolder(int arena) {
      this._arena = arena;
      this._redPlayers = new ArrayList<>(6);
      this._bluePlayers = new ArrayList<>(6);
      this._engine = new BlockCheckerEngine(this, this._arena);
   }

   public List<Player> getRedPlayers() {
      return this._redPlayers;
   }

   public List<Player> getBluePlayers() {
      return this._bluePlayers;
   }

   public List<Player> getAllPlayers() {
      List<Player> all = new ArrayList<>(12);
      all.addAll(this._redPlayers);
      all.addAll(this._bluePlayers);
      return all;
   }

   public void addPlayer(Player player, int team) {
      if (team == 0) {
         this._redPlayers.add(player);
      } else {
         this._bluePlayers.add(player);
      }
   }

   public void removePlayer(Player player, int team) {
      if (team == 0) {
         this._redPlayers.remove(player);
      } else {
         this._bluePlayers.remove(player);
      }
   }

   public int getPlayerTeam(Player player) {
      if (this._redPlayers.contains(player)) {
         return 0;
      } else {
         return this._bluePlayers.contains(player) ? 1 : -1;
      }
   }

   public int getRedTeamSize() {
      return this._redPlayers.size();
   }

   public int getBlueTeamSize() {
      return this._bluePlayers.size();
   }

   public void broadCastPacketToTeam(GameServerPacket packet) {
      for(Player p : this._redPlayers) {
         p.sendPacket(packet);
      }

      for(Player p : this._bluePlayers) {
         p.sendPacket(packet);
      }
   }

   public void clearPlayers() {
      this._redPlayers.clear();
      this._bluePlayers.clear();
   }

   public BlockCheckerEngine getEvent() {
      return this._engine;
   }

   public void updateEvent() {
      this._engine.updatePlayersOnStart(this);
   }

   public void checkAndShuffle() {
      int redSize = this._redPlayers.size();
      int blueSize = this._bluePlayers.size();
      if (redSize > blueSize + 1) {
         this.broadCastPacketToTeam(SystemMessage.getSystemMessage(SystemMessageId.TEAM_ADJUSTED_BECAUSE_WRONG_POPULATION_RATIO));
         int needed = redSize - (blueSize + 1);

         for(int i = 0; i < needed + 1; ++i) {
            Player plr = this._redPlayers.get(i);
            if (plr != null) {
               HandysBlockCheckerManager.getInstance().changePlayerToTeam(plr, this._arena, 1);
            }
         }
      } else if (blueSize > redSize + 1) {
         this.broadCastPacketToTeam(SystemMessage.getSystemMessage(SystemMessageId.TEAM_ADJUSTED_BECAUSE_WRONG_POPULATION_RATIO));
         int needed = blueSize - (redSize + 1);

         for(int i = 0; i < needed + 1; ++i) {
            Player plr = this._bluePlayers.get(i);
            if (plr != null) {
               HandysBlockCheckerManager.getInstance().changePlayerToTeam(plr, this._arena, 0);
            }
         }
      }
   }
}
