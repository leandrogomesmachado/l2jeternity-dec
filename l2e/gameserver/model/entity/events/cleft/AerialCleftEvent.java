package l2e.gameserver.model.entity.events.cleft;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.instancemanager.DoubleSessionManager;
import l2e.gameserver.instancemanager.SoDManager;
import l2e.gameserver.instancemanager.SoIManager;
import l2e.gameserver.listener.ScriptListener;
import l2e.gameserver.listener.events.AerialCleftKillEvent;
import l2e.gameserver.listener.events.AerialCleftListener;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExCleftList;
import l2e.gameserver.network.serverpackets.ExCleftState;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class AerialCleftEvent {
   protected static final Logger _log = Logger.getLogger(AerialCleftEvent.class.getName());
   public AerialCleftTeam[] _teams = new AerialCleftTeam[2];
   AerialCleftTeam _winteam = null;
   AerialCleftTeam _loseteam = null;
   protected Future<?> _regTask;
   protected Future<?> _eventTask;
   protected Future<?> _collectTask;
   protected int _eventTime = 0;
   protected Location[] startRed = new Location[]{
      new Location(-222704, 247803, 1744), new Location(-222649, 247945, 1728), new Location(-222652, 247666, 1728)
   };
   protected Location[] exitRed = new Location[]{
      new Location(-223701, 247795, 1744), new Location(-223780, 247661, 1744), new Location(-223776, 247914, 1744)
   };
   protected Location[] startBlue = new Location[]{
      new Location(-205312, 242144, 1744), new Location(-205376, 241997, 1744), new Location(-205376, 242276, 1744)
   };
   protected Location[] exitBlue = new Location[]{
      new Location(-204350, 242148, 1744), new Location(-204284, 242288, 1744), new Location(-204288, 242026, 1728)
   };
   private AerialCleftEvent.EventState _state = AerialCleftEvent.EventState.INACTIVE;
   private final List<AerialCleftListener> cleftListeners = new LinkedList<>();

   public AerialCleftEvent() {
      this._teams[0] = new AerialCleftTeam("Blue Team", 0, this.startBlue, this.exitBlue);
      this._teams[1] = new AerialCleftTeam("Red Team", 1, this.startRed, this.exitRed);
      if (this.checkRegistration()) {
         this.startRegistration();
      }
   }

   public void startRegistration() {
      if (this._regTask == null && this.isInactive() && this.checkRegistration()) {
         this.setState(AerialCleftEvent.EventState.PARTICIPATING);
         DoubleSessionManager.getInstance().registerEvent(101);
         this.fireAerialCleftListeners(ScriptListener.EventStage.REGISTRATION_BEGIN);
         _log.info("Aerial Cleft: Registration period begin.");
      }
   }

   public boolean openRegistration() {
      if (this.isInactive()) {
         this.cancelRegTask();
         this.setState(AerialCleftEvent.EventState.PARTICIPATING);
         DoubleSessionManager.getInstance().registerEvent(101);
         this.fireAerialCleftListeners(ScriptListener.EventStage.REGISTRATION_BEGIN);
         _log.info("Aerial Cleft: Open registration for Aerial Cleft Event.");
         return true;
      } else {
         return false;
      }
   }

   public boolean cleanUpTime() {
      if (this.isInactive()) {
         this.cancelRegTask();
         return true;
      } else {
         return false;
      }
   }

   public boolean forcedEventStart() {
      if (this.isParticipating() && this._teams[0].getParticipatedPlayerCount() > 0 && this._teams[1].getParticipatedPlayerCount() > 0) {
         this.startEvent();
         return true;
      } else {
         return false;
      }
   }

   public boolean forcedEventStop() {
      if (!this.isStarted() && !this.isRewarding()) {
         return false;
      } else {
         if (this.isStarted()) {
            this.stopEvent();
         } else {
            this.cancelCollectTask();
            this.collectEndTime();
         }

         return true;
      }
   }

   public boolean checkRegistration() {
      if (Config.CLEFT_WITHOUT_SEEDS) {
         return true;
      } else {
         return SoDManager.isOpened() || SoIManager.isSeedOpen();
      }
   }

   protected void cancelRegTask() {
      if (this._regTask != null) {
         this._regTask.cancel(false);
         this._regTask = null;
      }
   }

   protected void cancelEventTask() {
      if (this._eventTask != null) {
         this._eventTask.cancel(false);
         this._eventTask = null;
      }
   }

   protected void cancelCollectTask() {
      if (this._collectTask != null) {
         this._collectTask.cancel(false);
         this._collectTask = null;
      }
   }

   protected void updateTeams(Player newPlayer, int teamId) {
      for(AerialCleftTeam team : this._teams) {
         for(Player player : team.getParticipatedPlayers().values()) {
            if (player != null && player != newPlayer) {
               player.sendPacket(new ExCleftList(ExCleftList.CleftType.ADD, newPlayer, teamId));
            }
         }
      }
   }

   protected void updateChangeTeams(int playerObjectId, int oldTeamId, int newTeamId) {
      for(AerialCleftTeam team : this._teams) {
         for(Player player : team.getParticipatedPlayers().values()) {
            if (player != null) {
               player.sendPacket(new ExCleftList(ExCleftList.CleftType.TEAM_CHANGE, playerObjectId, oldTeamId, newTeamId));
            }
         }
      }
   }

   public synchronized void registerPlayer(Player player) {
      if (player != null) {
         byte teamId = 0;
         teamId = (byte)(this._teams[0].getParticipatedPlayerCount() > this._teams[1].getParticipatedPlayerCount() ? 1 : 0);
         player.addEventListener(new AerialCleftEventListener(player));
         this._teams[teamId].addPlayer(player);
         if (this.isStarted()) {
            new AerialCleftTeleporter(player, this._teams[teamId].getLocations(), true, false);
            player.untransform();
            this._teams[teamId].startEventTime(player);

            for(Skill skill : player.getAllSkills()) {
               if (skill != null && !skill.isPassive() && skill.getId() != 932) {
                  if (skill.getId() != 840 && skill.getId() != 841 && skill.getId() != 842) {
                     player.addBlockSkill(skill);
                  } else {
                     player.disableSkill(skill, 40000L);
                  }
               }
            }

            player.sendSkillList(false);
            player.sendPacket(SystemMessageId.THE_AERIAL_CLEFT_HAS_BEEN_ACTIVATED);
            player.sendPacket(new ExCleftState(ExCleftState.CleftState.TOTAL, this, this._teams[0], this._teams[1]));
         }

         if (this.isParticipating()) {
            player.sendPacket(new ExCleftList(ExCleftList.CleftType.TOTAL, this._teams[1], this._teams[0]));
            this.updateTeams(player, this._teams[teamId].getId());
            if (Config.CLEFT_BALANCER
               && this._teams[0].getParticipatedPlayerCount() >= 2
               && this._teams[1].getParticipatedPlayerCount() >= 2
               && this._teams[0].getParticipatedPlayerCount() == this._teams[1].getParticipatedPlayerCount()) {
               this.checkPlayersBalance();
            }

            if (this._teams[0].getParticipatedPlayerCount() == Config.CLEFT_MIN_TEAM_PLAYERS
               && this._teams[1].getParticipatedPlayerCount() == Config.CLEFT_MIN_TEAM_PLAYERS) {
               this.startEvent();
            }
         }
      }
   }

   public void removePlayer(int objectId, boolean exitEvent) {
      byte teamId = this.getParticipantTeamId(objectId);
      if (teamId != -1) {
         this._teams[teamId].removePlayer(objectId);
         this._teams[teamId].removePlayerTime(objectId);
         Player listener = World.getInstance().getPlayer(objectId);
         if (listener != null) {
            this._teams[teamId].removePlayerFromList(listener);
            listener.removeEventListener(AerialCleftEventListener.class);
            if ((this.isStarted() || this.isRewarding()) && exitEvent) {
               if (listener.isCleftCat()) {
                  listener.setCleftCat(false);
                  this._teams[teamId].selectTeamCat();
               }

               new AerialCleftTeleporter(listener, this._teams[teamId].getExitLocations(), true, true);
            }
         }

         if (this.isParticipating()) {
            for(AerialCleftTeam team : this._teams) {
               for(Player player : team.getParticipatedPlayers().values()) {
                  if (player != null) {
                     player.sendPacket(new ExCleftList(ExCleftList.CleftType.REMOVE, objectId, teamId));
                  }
               }
            }
         }
      }
   }

   private int highestLevelPlayer(Map<Integer, Player> players) {
      int maxLevel = Integer.MIN_VALUE;
      int maxLevelId = -1;

      for(Player player : players.values()) {
         if (player.getLevel() >= maxLevel) {
            maxLevel = player.getLevel();
            maxLevelId = player.getObjectId();
         }
      }

      return maxLevelId;
   }

   protected void checkPlayersBalance() {
      Map<Integer, Player> allParticipants = new HashMap<>();
      allParticipants.putAll(this._teams[0].getParticipatedPlayers());
      allParticipants.putAll(this._teams[1].getParticipatedPlayers());
      Iterator<Player> iter = allParticipants.values().iterator();

      while(iter.hasNext()) {
         Player player = iter.next();
         if (!this.checkPlayer(player)) {
            iter.remove();
         }
      }

      int[] balance = new int[]{0, 0};

      for(int priority = 0; !allParticipants.isEmpty(); priority = balance[0] > balance[1] ? 1 : 0) {
         int highestLevelPlayerId = this.highestLevelPlayer(allParticipants);
         Player highestLevelPlayer = allParticipants.get(highestLevelPlayerId);
         allParticipants.remove(highestLevelPlayerId);
         int oldTeam = this.getParticipantTeamId(highestLevelPlayer.getObjectId());
         if (oldTeam != this._teams[priority].getId()) {
            this._teams[oldTeam].removePlayer(highestLevelPlayer.getObjectId());
            this._teams[priority].addPlayer(highestLevelPlayer);
            this.updateChangeTeams(highestLevelPlayer.getObjectId(), oldTeam, priority);
         }

         balance[priority] += highestLevelPlayer.getLevel();
         if (allParticipants.isEmpty()) {
            break;
         }

         priority = 1 - priority;
         highestLevelPlayerId = this.highestLevelPlayer(allParticipants);
         highestLevelPlayer = allParticipants.get(highestLevelPlayerId);
         allParticipants.remove(highestLevelPlayerId);
         int var11 = this.getParticipantTeamId(highestLevelPlayer.getObjectId());
         if (var11 != this._teams[priority].getId()) {
            this._teams[var11].removePlayer(highestLevelPlayer.getObjectId());
            this._teams[priority].addPlayer(highestLevelPlayer);
            this.updateChangeTeams(highestLevelPlayer.getObjectId(), var11, priority);
         }

         balance[priority] += highestLevelPlayer.getLevel();
      }
   }

   public void startEvent() {
      if (this._teams[0].getParticipatedPlayerCount() != 0 && this._teams[1].getParticipatedPlayerCount() != 0) {
         this.setState(AerialCleftEvent.EventState.STARTING);
         SpawnParser.getInstance().spawnGroup("cleft_fight_spawn");
         this.setState(AerialCleftEvent.EventState.STARTED);
         this._eventTime = (int)(System.currentTimeMillis() + (long)(60000 * Config.CLEFT_WAR_TIME));
         if (this._eventTask == null) {
            this._eventTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
               @Override
               public void run() {
                  AerialCleftEvent.this.calculateRewards();
               }
            }, (long)(Config.CLEFT_WAR_TIME * 60000));
         }

         for(AerialCleftTeam team : this._teams) {
            team.selectTeamCat();
         }

         for(AerialCleftTeam team : this._teams) {
            for(Player player : team.getParticipatedPlayers().values()) {
               if (player != null) {
                  player.sendPacket(new ExCleftList(ExCleftList.CleftType.CLOSE));
                  new AerialCleftTeleporter(player, team.getLocations(), true, false);
                  player.untransform();
                  team.startEventTime(player);

                  for(Skill skill : player.getAllSkills()) {
                     if (skill != null && !skill.isPassive() && skill.getId() != 932) {
                        if (skill.getId() != 840 && skill.getId() != 841 && skill.getId() != 842) {
                           player.addBlockSkill(skill);
                        } else {
                           player.disableSkill(skill, 40000L);
                        }
                     }
                  }

                  player.sendSkillList(false);
                  player.sendPacket(SystemMessageId.THE_AERIAL_CLEFT_HAS_BEEN_ACTIVATED);
                  player.sendPacket(new ExCleftState(ExCleftState.CleftState.TOTAL, this, this._teams[0], this._teams[1]));
               }
            }
         }

         this.fireAerialCleftListeners(ScriptListener.EventStage.START);
      }
   }

   public void stopEvent() {
      this.setState(AerialCleftEvent.EventState.INACTIVATING);

      for(AerialCleftTeam team : this._teams) {
         for(Player player : team.getParticipatedPlayers().values()) {
            if (player != null) {
               player.sendPacket(new ExCleftState(ExCleftState.CleftState.RESULT, this._winteam, this._loseteam));
               new AerialCleftTeleporter(player, this._loseteam.getExitLocations(), true, true);
            }
         }
      }

      this._teams[0].cleanMe();
      this._teams[1].cleanMe();
      this.setState(AerialCleftEvent.EventState.INACTIVE);
      SpawnParser.getInstance().despawnGroup("cleft_fight_spawn");
      this.cancelEventTask();
      this.cancelCollectTask();
      DoubleSessionManager.getInstance().clear(101);
      this.fireAerialCleftListeners(ScriptListener.EventStage.END);
      if (this._regTask == null) {
         this._regTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
               AerialCleftEvent.this.startRegistration();
            }
         }, (long)(Config.CLEFT_TIME_RELOAD_REG * 60000));
      }
   }

   public int getTotalEventPlayers() {
      return this._teams[0].getParticipatedPlayerCount() + this._teams[1].getParticipatedPlayerCount();
   }

   public void calculateRewards() {
      this.setState(AerialCleftEvent.EventState.REWARDING);
      if (this._teams[0].getPoints() > this._teams[1].getPoints()) {
         this._winteam = this._teams[0];
         this._loseteam = this._teams[1];
      } else {
         this._winteam = this._teams[1];
         this._loseteam = this._teams[0];
      }

      if (this._teams[0].getPoints() == 0 && this._teams[1].getPoints() == 0) {
         this.stopEvent();
      } else {
         SpawnParser.getInstance().despawnGroup("cleft_fight_spawn");
         SpawnParser.getInstance().spawnGroup("cleft_reward_spawn");

         for(AerialCleftTeam team : this._teams) {
            for(Player player : team.getParticipatedPlayers().values()) {
               player.sendPacket(new ExCleftState(ExCleftState.CleftState.RESULT, this._winteam, this._loseteam));
               if (team == this._loseteam) {
                  new AerialCleftTeleporter(player, this._loseteam.getExitLocations(), true, true);
               }
            }

            if (team == this._loseteam) {
               this.rewardTeam(team, false);
            } else {
               this.rewardTeam(team, true);
            }
         }

         this._collectTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
               AerialCleftEvent.this.collectEndTime();
            }
         }, (long)(Config.CLEFT_COLLECT_TIME * 60000));
      }
   }

   private void collectEndTime() {
      this.setState(AerialCleftEvent.EventState.INACTIVATING);

      for(Player player : this._winteam.getParticipatedPlayers().values()) {
         if (player != null) {
            new AerialCleftTeleporter(player, this._winteam.getExitLocations(), true, true);
         }
      }

      SpawnParser.getInstance().despawnGroup("cleft_reward_spawn");
      this._teams[0].cleanMe();
      this._teams[1].cleanMe();
      this._winteam = null;
      this._loseteam = null;
      this.setState(AerialCleftEvent.EventState.INACTIVE);
      this.cancelEventTask();
      this.cancelCollectTask();
      DoubleSessionManager.getInstance().clear(101);
      this.fireAerialCleftListeners(ScriptListener.EventStage.END);
      if (this._regTask == null) {
         this._regTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
               AerialCleftEvent.this.startRegistration();
            }
         }, (long)(Config.CLEFT_TIME_RELOAD_REG * 60000));
      }
   }

   private void rewardTeam(AerialCleftTeam team, boolean winner) {
      for(Player player : team.getParticipatedPlayers().values()) {
         if (player != null && this.checkPlayerTime(team, player.getObjectId())) {
            SystemMessage systemMessage = null;
            int itemId = Config.CLEFT_REWARD_ID;
            int count = winner ? Config.CLEFT_REWARD_COUNT_WINNER : Config.CLEFT_REWARD_COUNT_LOOSER;
            PcInventory inv = player.getInventory();
            if (ItemsParser.getInstance().createDummyItem(itemId).isStackable()) {
               inv.addItem("Aerial Cleft Event", itemId, (long)count, player, player);
               if (count > 1) {
                  systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
                  systemMessage.addItemName(itemId);
                  systemMessage.addItemNumber((long)count);
               } else {
                  systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
                  systemMessage.addItemName(itemId);
               }

               player.sendPacket(systemMessage);
            } else {
               for(int i = 0; i < count; ++i) {
                  inv.addItem("Aerial Cleft Event", itemId, 1L, player, player);
                  systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
                  systemMessage.addItemName(itemId);
                  player.sendPacket(systemMessage);
               }
            }

            StatusUpdate statusUpdate = new StatusUpdate(player);
            statusUpdate.addAttribute(14, player.getCurrentLoad());
            player.sendPacket(statusUpdate);
            if (team == this._winteam) {
               player.addExpAndSp(0L, team.getPoints());
            }
         }
      }
   }

   private boolean checkPlayer(Player player) {
      if (player.isInOfflineMode() || player.isJailed()) {
         return false;
      } else if (player.isCursedWeaponEquipped()) {
         return false;
      } else if (player.getLevel() < Config.CLEFT_MIN_LEVEL) {
         return false;
      } else if (player.isMounted() || player.isDead() || player.inObserverMode()) {
         return false;
      } else if (player.isInDuel()) {
         return false;
      } else if (player.getFightEventGameRoom() != null || player.isInFightEvent() || player.getTeam() != 0) {
         return false;
      } else if (player.isInOlympiadMode()) {
         return false;
      } else if (player.isInParty() && player.getParty().isInDimensionalRift()) {
         return false;
      } else if (player.isTeleporting()) {
         return false;
      } else if (player.isInSiege()) {
         return false;
      } else {
         return player.getReflectionId() == 0;
      }
   }

   public byte getParticipantTeamId(int playerObjectId) {
      return (byte)(this._teams[0].containsPlayer(playerObjectId) ? 0 : (this._teams[1].containsPlayer(playerObjectId) ? 1 : -1));
   }

   private void setState(AerialCleftEvent.EventState state) {
      synchronized(this._state) {
         this._state = state;
      }
   }

   public boolean isInactive() {
      synchronized(this._state) {
         return this._state == AerialCleftEvent.EventState.INACTIVE;
      }
   }

   public boolean isInactivating() {
      synchronized(this._state) {
         return this._state == AerialCleftEvent.EventState.INACTIVATING;
      }
   }

   public boolean isParticipating() {
      synchronized(this._state) {
         return this._state == AerialCleftEvent.EventState.PARTICIPATING;
      }
   }

   public boolean isStarting() {
      synchronized(this._state) {
         return this._state == AerialCleftEvent.EventState.STARTING;
      }
   }

   public boolean isStarted() {
      synchronized(this._state) {
         return this._state == AerialCleftEvent.EventState.STARTED;
      }
   }

   public boolean isRewarding() {
      synchronized(this._state) {
         return this._state == AerialCleftEvent.EventState.REWARDING;
      }
   }

   public boolean isValidRegistration() {
      return this.isParticipating() || this.isStarting() || this.isStarted();
   }

   public boolean isPlayerParticipant(int objectId) {
      if (!this.isParticipating() && !this.isStarting() && !this.isStarted()) {
         return false;
      } else {
         return this._teams[0].containsPlayer(objectId) || this._teams[1].containsPlayer(objectId);
      }
   }

   public void addAerialCleftListener(AerialCleftListener listener) {
      if (!this.cleftListeners.contains(listener)) {
         this.cleftListeners.add(listener);
      }
   }

   public void removeAerialCleftListener(AerialCleftListener listener) {
      this.cleftListeners.remove(listener);
   }

   private void fireAerialCleftListeners(ScriptListener.EventStage stage) {
      if (!this.cleftListeners.isEmpty()) {
         switch(stage) {
            case REGISTRATION_BEGIN:
               for(AerialCleftListener listener : this.cleftListeners) {
                  listener.onRegistrationStart();
               }
               break;
            case START:
               for(AerialCleftListener listener : this.cleftListeners) {
                  listener.onBegin();
               }
               break;
            case END:
               for(AerialCleftListener listener : this.cleftListeners) {
                  listener.onEnd();
               }
         }
      }
   }

   protected void updateTeamCat(AerialCleftTeam killedTeam) {
      for(AerialCleftTeam team : this._teams) {
         for(Player player : team.getParticipatedPlayers().values()) {
            if (player != null) {
               player.sendPacket(new ExCleftState(ExCleftState.CleftState.CAT_UPDATE, this, killedTeam));
            }
         }
      }
   }

   public boolean onEscapeUse(int playerObjectId) {
      return !this.isStarted() && !this.isRewarding() || !this.isPlayerParticipant(playerObjectId);
   }

   public void onLogout(Player player) {
      if (player != null && (this.isStarting() || this.isStarted() || this.isParticipating())) {
         this.removePlayer(player.getObjectId(), true);
         player.cleanBlockSkills();
      }
   }

   public boolean onAction(Player player, int targetedPlayerObjectId) {
      if (player != null && (this.isStarted() || this.isRewarding())) {
         if (player.isGM()) {
            return true;
         } else {
            byte playerTeamId = this.getParticipantTeamId(player.getObjectId());
            byte targetedPlayerTeamId = this.getParticipantTeamId(targetedPlayerObjectId);
            if ((playerTeamId == -1 || targetedPlayerTeamId != -1) && (playerTeamId != -1 || targetedPlayerTeamId == -1)) {
               return playerTeamId == -1
                  || targetedPlayerTeamId == -1
                  || playerTeamId != targetedPlayerTeamId
                  || player.getObjectId() == targetedPlayerObjectId;
            } else {
               return false;
            }
         }
      } else {
         return true;
      }
   }

   public boolean onScrollUse(int playerObjectId) {
      if (!this.isStarted() && !this.isRewarding()) {
         return true;
      } else {
         return !this.isPlayerParticipant(playerObjectId);
      }
   }

   public void onKill(Creature killerCharacter, Player killedPlayer) {
      if (killedPlayer != null && this.isStarted()) {
         byte killedTeamId = this.getParticipantTeamId(killedPlayer.getObjectId());
         if (killedTeamId != -1) {
            killedPlayer.setCleftDeath(1);
            new AerialCleftTeleporter(killedPlayer, this._teams[killedTeamId].getLocations(), false, false);
            if (killerCharacter != null) {
               Player killerPlayerInstance = null;
               if (killerCharacter.isPlayer()) {
                  killerPlayerInstance = (Player)killerCharacter;
                  byte killerTeamId = this.getParticipantTeamId(killerPlayerInstance.getObjectId());
                  if (killerTeamId != -1 && killedTeamId != -1 && killerTeamId != killedTeamId) {
                     AerialCleftTeam killerTeam = this._teams[killerTeamId];
                     if (killedPlayer.isCleftCat()) {
                        killerTeam.addPoints(Config.TEAM_CAT_POINT);
                     } else {
                        killerTeam.addPoints(Config.TEAM_PLAYER_POINT);
                     }

                     killerPlayerInstance.setCleftKill(1);
                     this.updatePvpKills(killerPlayerInstance, killedPlayer, this._teams[killerTeamId], this._teams[killedTeamId]);
                     this.fireAerialCleftKillListeners(killerPlayerInstance, killedPlayer, killerTeam);
                  }

                  if (killedPlayer.isCleftCat()) {
                     killedPlayer.setCleftCat(false);
                     this._teams[killedTeamId].selectTeamCat();
                     this.updateTeamCat(this._teams[killedTeamId]);
                  }
               }
            }
         }
      }
   }

   protected void updatePvpKills(Player killer, Player killed, AerialCleftTeam killerTeam, AerialCleftTeam killedTeam) {
      for(AerialCleftTeam team : this._teams) {
         for(Player player : team.getParticipatedPlayers().values()) {
            if (player != null) {
               player.sendPacket(
                  new ExCleftState(
                     ExCleftState.CleftState.PVP_KILL, this, this._teams[0], this._teams[1], killer, killed, killerTeam.getId(), killedTeam.getId()
                  )
               );
            }
         }
      }
   }

   public void checkNpcPoints(Attackable npc, Player killer) {
      if (killer != null && this.isStarted()) {
         byte killerTeamId = this.getParticipantTeamId(killer.getObjectId());
         if (killerTeamId != -1) {
            AerialCleftTeam killerTeam = this._teams[killerTeamId];
            killer.setCleftKillTower(1);
            switch(npc.getId()) {
               case 22553:
                  killerTeam.addPoints(Config.LARGE_COMPRESSOR_POINT);
                  break;
               case 22554:
               case 22555:
               case 22556:
                  killerTeam.addPoints(Config.SMALL_COMPRESSOR_POINT);
            }

            this.updateTowerCount(killer, killerTeam, npc.getId());
         }
      }
   }

   protected void updateTowerCount(Player killer, AerialCleftTeam killerTeam, int npcId) {
      for(AerialCleftTeam team : this._teams) {
         for(Player player : team.getParticipatedPlayers().values()) {
            if (player != null) {
               player.sendPacket(new ExCleftState(ExCleftState.CleftState.TOWER_DESTROY, this, killerTeam, this._teams[0], this._teams[1], npcId, killer));
            }
         }
      }
   }

   private void fireAerialCleftKillListeners(Player killer, Player victim, AerialCleftTeam killerTeam) {
      if (!this.cleftListeners.isEmpty() && killer != null && victim != null && killerTeam != null) {
         AerialCleftKillEvent event = new AerialCleftKillEvent();
         event.setKiller(killer);
         event.setVictim(victim);
         event.setKillerTeam(killerTeam);

         for(AerialCleftListener listener : this.cleftListeners) {
            listener.onKill(event);
         }
      }
   }

   public int getEventTime() {
      return this._eventTime;
   }

   public int getEventTimeEnd() {
      return (int)((long)this._eventTime - System.currentTimeMillis()) / 1000;
   }

   public boolean getPlayerTime(long time) {
      return (int)((long)this._eventTime - time) / 60000 >= Config.CLEFT_MIN_PLAYR_EVENT_TIME;
   }

   protected boolean checkPlayerTime(AerialCleftTeam team, int playerObjectId) {
      return team.containsTime(playerObjectId) ? this.getPlayerTime(team.getParticipatedTimes().get(playerObjectId)) : false;
   }

   public static AerialCleftEvent getInstance() {
      return AerialCleftEvent.SingletonHolder._instance;
   }

   static enum EventState {
      INACTIVE,
      INACTIVATING,
      PARTICIPATING,
      STARTING,
      STARTED,
      REWARDING;
   }

   private static class SingletonHolder {
      protected static final AerialCleftEvent _instance = new AerialCleftEvent();
   }
}
