package l2e.gameserver.model.entity.underground_coliseum;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.ExPVPMatchRecord;
import l2e.gameserver.network.serverpackets.ExPVPMatchUserDie;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.GameServerPacket;

public class UCArena {
   private static final long MINUTES_IN_MILISECONDS = 600000L;
   private final int _id;
   private final int _minLevel;
   private final int _maxLevel;
   private final UCPoint[] _points = new UCPoint[4];
   private final UCTeam[] _teams = new UCTeam[2];
   private Npc _manager = null;
   private ScheduledFuture<?> _taskFuture = null;
   private final List<UCWaiting> _waitingPartys = new CopyOnWriteArrayList<>();
   private final List<UCReward> _rewards = new ArrayList<>();
   private boolean _isBattleNow = false;

   public UCArena(int id, int curator, int min_level, int max_level) {
      this._id = id;

      for(Spawner spawn : SpawnParser.getInstance().getSpawnData()) {
         if (spawn.getId() == curator) {
            this._manager = spawn.getLastSpawn();
         }
      }

      this._minLevel = min_level;
      this._maxLevel = max_level;
      this._rewards.clear();
   }

   public int getId() {
      return this._id;
   }

   public int getMinLevel() {
      return this._minLevel;
   }

   public int getMaxLevel() {
      return this._maxLevel;
   }

   public Npc getManager() {
      return this._manager;
   }

   public void setUCPoint(int index, UCPoint point) {
      if (index <= 4) {
         this._points[index] = point;
      }
   }

   public void setUCTeam(int index, UCTeam team) {
      if (index <= 2) {
         this._teams[index] = team;
      }
   }

   public UCTeam[] getTeams() {
      return this._teams;
   }

   public UCPoint[] getPoints() {
      return this._points;
   }

   public List<UCWaiting> getWaitingList() {
      return this._waitingPartys;
   }

   public void switchStatus(boolean start) {
      if (this._taskFuture == null && start) {
         this.runNewTask(false);
      } else {
         if (this._taskFuture != null) {
            this._taskFuture.cancel(true);
            this._taskFuture = null;
         }

         this.generateWinner();
         this.removeTeams();

         for(UCTeam team : this.getTeams()) {
            team.cleanUp();
         }

         for(UCPoint point : this.getPoints()) {
            point.actionDoors(false);
            point.getPlayers().clear();
         }

         this._isBattleNow = false;
      }
   }

   public void runNewTask(boolean isFullTime) {
      long time = isFullTime ? 600000L : 540000L;
      this._taskFuture = ThreadPoolManager.getInstance().schedule(new UCRunningTask(this), time);
   }

   public void runTaskNow() {
      if (this._taskFuture != null) {
         this._taskFuture.cancel(true);
         this._taskFuture = null;
      }

      this._taskFuture = ThreadPoolManager.getInstance().schedule(new UCRunningTask(this), 0L);
   }

   public void generateWinner() {
      UCTeam blueTeam = this._teams[0];
      UCTeam redTeam = this._teams[1];
      UCTeam winnerTeam = null;
      if (blueTeam.getStatus() == 1 || redTeam.getStatus() == 1) {
         winnerTeam = blueTeam.getStatus() == 1 ? blueTeam : redTeam;
      } else if (blueTeam.getParty() == null && redTeam.getParty() != null) {
         redTeam.setStatus((byte)1);
         winnerTeam = redTeam;
      } else if (redTeam.getParty() == null && blueTeam.getParty() != null) {
         blueTeam.setStatus((byte)1);
         winnerTeam = blueTeam;
      } else if (redTeam.getParty() != null && blueTeam.getParty() != null) {
         if (blueTeam.getKillCount() > redTeam.getKillCount()) {
            blueTeam.setStatus((byte)1);
            redTeam.setStatus((byte)2);
            winnerTeam = blueTeam;
         } else if (redTeam.getKillCount() > blueTeam.getKillCount()) {
            blueTeam.setStatus((byte)2);
            redTeam.setStatus((byte)1);
            winnerTeam = redTeam;
         } else if (blueTeam.getKillCount() == redTeam.getKillCount()) {
            if (blueTeam.getRegisterTime() > redTeam.getRegisterTime()) {
               blueTeam.setStatus((byte)2);
               redTeam.setStatus((byte)1);
               winnerTeam = redTeam;
            } else {
               blueTeam.setStatus((byte)1);
               redTeam.setStatus((byte)2);
               winnerTeam = blueTeam;
            }
         }
      }

      if (winnerTeam != null) {
         this.broadcastRecord(2, winnerTeam.getIndex() + 1);
      } else {
         this.broadcastRecord(2, 0);
      }

      blueTeam.setLastParty(redTeam.getParty());
      redTeam.setLastParty(blueTeam.getParty());
   }

   public void broadcastToAll(GameServerPacket packet) {
      for(UCTeam team : this.getTeams()) {
         Party party = team.getParty();
         if (party != null) {
            for(Player member : party.getMembers()) {
               if (member != null) {
                  member.sendPacket(packet);
               }
            }
         }
      }
   }

   public void prepareStart() {
      this._isBattleNow = true;
      this.broadcastToAll(new ExShowScreenMessage(NpcStringId.MATCH_BEGINS_IN_S1_MINUTES, 2, 5000, "1"));

      try {
         Thread.sleep(30000L);
      } catch (InterruptedException var15) {
      }

      this.broadcastToAll(new ExShowScreenMessage(NpcStringId.S1_SECONDS_REMAINING, 2, 5000, "30"));

      try {
         Thread.sleep(20000L);
      } catch (InterruptedException var14) {
      }

      this.broadcastToAll(new ExShowScreenMessage(NpcStringId.S1_SECONDS_REMAINING, 2, 3000, "10"));

      try {
         Thread.sleep(5000L);
      } catch (InterruptedException var13) {
      }

      this.broadcastToAll(new ExShowScreenMessage(NpcStringId.S1_SECONDS_REMAINING, 2, 1000, "5"));

      try {
         Thread.sleep(1000L);
      } catch (InterruptedException var12) {
      }

      this.broadcastToAll(new ExShowScreenMessage(NpcStringId.S1_SECONDS_REMAINING, 2, 1000, "4"));

      try {
         Thread.sleep(1000L);
      } catch (InterruptedException var11) {
      }

      this.broadcastToAll(new ExShowScreenMessage(NpcStringId.S1_SECONDS_REMAINING, 2, 1000, "3"));

      try {
         Thread.sleep(1000L);
      } catch (InterruptedException var10) {
      }

      this.broadcastToAll(new ExShowScreenMessage(NpcStringId.S1_SECONDS_REMAINING, 2, 1000, "2"));

      try {
         Thread.sleep(1000L);
      } catch (InterruptedException var9) {
      }

      this.broadcastToAll(new ExShowScreenMessage(NpcStringId.S1_SECONDS_REMAINING, 2, 1000, "1"));

      try {
         Thread.sleep(1000L);
      } catch (InterruptedException var8) {
      }

      boolean isValid = true;

      for(UCTeam team : this._teams) {
         if (team.getParty() == null) {
            isValid = false;
         } else if (team.getParty().getMemberCount() < Config.UC_PARTY_LIMIT) {
            isValid = false;
         } else {
            for(Player pl : team.getParty().getMembers()) {
               if (pl != null && (pl.getDistance(this._manager) > 500.0 || pl.getClassId().level() < 2)) {
                  isValid = false;
               }
            }
         }
      }

      if (isValid) {
         this.runNewTask(true);
         this.splitMembersAndTeleport();
         this.startFight();
      } else {
         this.broadcastToAll(
            new ExShowScreenMessage(NpcStringId.THE_MATCH_IS_AUTOMATICALLY_CANCELED_BECAUSE_YOU_ARE_TOO_FAR_FROM_THE_ADMISSION_MANAGER, 2, 5000)
         );

         for(UCTeam team : this._teams) {
            team.setParty(null);
            team.setRegisterTime(0L);
         }

         this._isBattleNow = false;
         this.runNewTask(false);
      }
   }

   public void splitMembersAndTeleport() {
      UCPoint[] positions = this.getPoints();

      for(UCPoint point : positions) {
         point.getPlayers().clear();
      }

      this.broadcastRecord(0, 0);

      for(UCTeam team : this.getTeams()) {
         Party party = team.getParty();
         if (party != null) {
            int i = 0;

            for(Player player : party.getMembers()) {
               if (player != null) {
                  player.setUCState(1);
                  positions[i].teleportPlayer(player);
                  if (++i >= 3) {
                     i = 0;
                  }
               }
            }
         }
      }

      this.broadcastRecord(1, 0);
   }

   public void broadcastRecord(int type, int teamType) {
      ExPVPMatchRecord packet = new ExPVPMatchRecord(type, teamType, this);
      ExPVPMatchUserDie packet2 = type == 1 ? new ExPVPMatchUserDie(this) : null;

      for(UCTeam team : this.getTeams()) {
         Party party = team.getParty();
         if (party != null) {
            for(Player member : party.getMembers()) {
               if (member != null) {
                  member.sendPacket(packet);
                  if (packet2 != null) {
                     member.sendPacket(packet2);
                  }
               }
            }
         }
      }
   }

   public void startFight() {
      for(UCTeam team : this._teams) {
         team.spawnTower();

         for(Player player : team.getParty().getMembers()) {
            if (player != null) {
               player.setTeam(team.getIndex() + 1);
            }
         }
      }
   }

   public void removeTeams() {
      for(UCTeam team : this._teams) {
         if (team.getParty() != null) {
            for(Player player : team.getParty().getMembers()) {
               if (player != null) {
                  player.setTeam(0);
                  player.cleanUCStats();
                  player.setUCState(0);
                  if (player.isDead()) {
                     UCTeam.resPlayer(player);
                  }

                  if (player.getSaveLoc() != null) {
                     player.teleToLocation(player.getSaveLoc(), true);
                  } else {
                     player.teleToLocation(TeleportWhereType.TOWN, true);
                  }
               }
            }
         }
      }
   }

   public boolean isBattleNow() {
      return this._isBattleNow;
   }

   public void setIsBattleNow(boolean value) {
      this._isBattleNow = value;
   }

   public void setReward(UCReward ucReward) {
      this._rewards.add(ucReward);
   }

   public List<UCReward> getRewards() {
      return this._rewards;
   }
}
