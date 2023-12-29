package l2e.gameserver.model.entity.events;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import l2e.commons.annotations.Nullable;
import l2e.commons.collections.MultiValueSet;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Rnd;
import l2e.commons.util.StringUtil;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.handler.voicedcommandhandlers.VoicedCommandHandler;
import l2e.gameserver.instancemanager.DailyTaskManager;
import l2e.gameserver.instancemanager.DoubleSessionManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.listener.ScriptListener;
import l2e.gameserver.listener.events.FightEventRegisterListener;
import l2e.gameserver.listener.events.OnZoneEnterLeaveListener;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.actor.tasks.player.InvisibleTask;
import l2e.gameserver.model.actor.templates.daily.DailyTaskTemplate;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.actor.templates.player.PlayerTaskTemplate;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.entity.events.model.FightEventManager;
import l2e.gameserver.model.entity.events.model.FightLastStatsManager;
import l2e.gameserver.model.entity.events.model.listener.FightEventListener;
import l2e.gameserver.model.entity.events.model.template.FightEventGameRoom;
import l2e.gameserver.model.entity.events.model.template.FightEventMap;
import l2e.gameserver.model.entity.events.model.template.FightEventPlayer;
import l2e.gameserver.model.entity.events.model.template.FightEventTeam;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.EarthQuake;
import l2e.gameserver.network.serverpackets.ExPVPMatchCCRecord;
import l2e.gameserver.network.serverpackets.ExPVPMatchCCRetire;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.ShowTutorialMark;
import org.apache.commons.lang3.reflect.MethodUtils;

public abstract class AbstractFightEvent {
   protected static final Logger _log = Logger.getLogger(AbstractFightEvent.class.getName());
   protected final Map<String, List<Serializable>> _objects = new HashMap<>(0);
   protected final int _id;
   protected final String _nameEn;
   protected final String _nameRu;
   protected final String _timerName;
   public static final String REGISTERED_PLAYERS = "registered_players";
   public static final String LOGGED_OFF_PLAYERS = "logged_off_players";
   public static final String FIGHTING_PLAYERS = "fighting_players";
   private static final int CLOSE_LOCATIONS_VALUE = 150;
   protected static final int ITEMS_FOR_MINUTE_OF_AFK = Config.ITEMS_FOR_MINUTE_OF_AFK;
   protected static final int TIME_FIRST_TELEPORT = Config.TIME_FIRST_TELEPORT;
   protected static final int TIME_PLAYER_TELEPORTING = Config.TIME_PLAYER_TELEPORTING;
   protected static final int TIME_PREPARATION_BEFORE_FIRST_ROUND = Config.TIME_PREPARATION_BEFORE_FIRST_ROUND;
   protected static final int TIME_PREPARATION_BETWEEN_NEXT_ROUNDS = Config.TIME_PREPARATION_BETWEEN_NEXT_ROUNDS;
   protected static final int TIME_AFTER_ROUND_END_TO_RETURN_SPAWN = Config.TIME_AFTER_ROUND_END_TO_RETURN_SPAWN;
   protected static final int TIME_TELEPORT_BACK_TOWN = Config.TIME_TELEPORT_BACK_TOWN;
   protected static final int TIME_MAX_SECONDS_OUTSIDE_ZONE = Config.TIME_MAX_SECONDS_OUTSIDE_ZONE;
   protected static final int TIME_TO_BE_AFK = Config.TIME_TO_BE_AFK;
   private static final String[] ROUND_NUMBER_IN_STRING = new String[]{
      "",
      "FightEvents.ROUND_1",
      "FightEvents.ROUND_2",
      "FightEvents.ROUND_3",
      "FightEvents.ROUND_4",
      "FightEvents.ROUND_5",
      "FightEvents.ROUND_6",
      "FightEvents.ROUND_7",
      "FightEvents.ROUND_8",
      "FightEvents.ROUND_9",
      "FightEvents.ROUND_10"
   };
   private final String _descEn;
   private final String _descRu;
   private final String _icon;
   private final int _roundRunTime;
   private final boolean _isAutoTimed;
   private final int[][] _autoStartTimes;
   private final boolean _teamed;
   private final boolean _teamTargets;
   private final boolean _givePvpPoints;
   private final boolean _attackPlayers;
   private final boolean _useScrolls;
   private final boolean _usePotions;
   private final boolean _useItemSummon;
   private final boolean _buffer;
   private final boolean _loseBuffsOnDeath;
   private final int[][] _fighterBuffs;
   private final int[][] _mageBuffs;
   private final boolean _rootBetweenRounds;
   private final FightEventManager.CLASSES[] _excludedClasses;
   private final int[] _excludedSkills;
   private final boolean _roundEvent;
   private final int _rounds;
   private final int _respawnTime;
   private final boolean _ressAllowed;
   private final boolean _instanced;
   private final int[][] _rewardByParticipation;
   private final int[][] _rewardByKillPlayer;
   protected final int[][] _rewardByWinner;
   private final int[][] _rewardByTopKiller;
   protected AbstractFightEvent.EVENT_STATE _state = AbstractFightEvent.EVENT_STATE.NOT_ACTIVE;
   private AbstractFightEvent.ZoneListener _zoneListener = new AbstractFightEvent.ZoneListener();
   private static final List<FightEventRegisterListener> _listeners = new LinkedList<>();
   private FightEventMap _map;
   private int _reflectionId = 0;
   private final List<FightEventTeam> _teams = new CopyOnWriteArrayList<>();
   private final Map<FightEventPlayer, ZoneType> _leftZone = new ConcurrentHashMap<>();
   private final Map<String, ZoneType> _activeZones = new HashMap<>();
   private int _currentRound = 0;
   private boolean _dontLetAnyoneIn = false;
   private FightEventGameRoom _room;
   private MultiValueSet<String> _set;
   private final Map<String, Integer> _scores = new ConcurrentHashMap<>();
   private Map<String, Integer> _bestScores = new ConcurrentHashMap<>();
   private boolean _scoredUpdated = true;
   private ScheduledFuture<?> _timer;

   public AbstractFightEvent(MultiValueSet<String> set) {
      this._id = set.getInteger("id");
      this._nameEn = set.getString("nameEn");
      this._nameRu = set.getString("nameRu");
      this._timerName = this._id + "_" + this._nameEn.toLowerCase().replace(" ", "_");
      this._descEn = set.getString("descEn");
      this._descRu = set.getString("descRu");
      this._icon = set.getString("icon");
      this._roundRunTime = set.getInteger("roundRunTime", -1);
      this._teamed = set.getBool("teamed");
      this._teamTargets = set.getBool("canTargetTeam", false);
      this._givePvpPoints = set.getBool("givePvpPoints", false);
      this._attackPlayers = set.getBool("canAttackPlayers", true);
      this._useScrolls = set.getBool("canUseScrolls", false);
      this._usePotions = set.getBool("canUsePotions", false);
      this._useItemSummon = set.getBool("canItemSummons", false);
      this._loseBuffsOnDeath = set.getBool("loseBuffsOnDeath", false);
      this._buffer = set.getBool("useBuffs", false);
      this._fighterBuffs = this.parseBuffs(set.getString("fighterBuffs", null));
      this._mageBuffs = this.parseBuffs(set.getString("mageBuffs", null));
      this._rootBetweenRounds = set.getBool("rootBetweenRounds");
      this._excludedClasses = this.parseExcludedClasses(set.getString("excludedClasses", ""));
      this._excludedSkills = this.parseExcludedSkills(set.getString("excludedSkills", null));
      this._isAutoTimed = set.getBool("isAutoTimed", false);
      this._autoStartTimes = this.parseAutoStartTimes(set.getString("autoTimes", ""));
      this._roundEvent = set.getBool("roundEvent");
      this._rounds = set.getInteger("rounds", -1);
      this._respawnTime = set.getInteger("respawnTime");
      this._ressAllowed = set.getBool("ressAllowed");
      this._instanced = set.getBool("instanced");
      this._rewardByParticipation = this.parseItemsList(set.getString("rewardByParticipation", null));
      this._rewardByKillPlayer = this.parseItemsList(set.getString("rewardByKillPlayer", null));
      this._rewardByWinner = this.parseItemsList(set.getString("rewardByWinner", null));
      this._rewardByTopKiller = this.parseItemsList(set.getString("rewardByTopKiller", null));
      this._set = set;
   }

   public void prepareEvent(FightEventGameRoom room) {
      this._map = room.getMap();
      this._room = room;

      for(Player player : room.getAllPlayers()) {
         this.addObject("registered_players", new FightEventPlayer(player));
         player.addEvent(this);
      }

      this.startTeleportTimer(room);
   }

   public void startEvent() {
      this._state = AbstractFightEvent.EVENT_STATE.PREPARATION;
      FightLastStatsManager.getInstance().clearStats(this.getId());
      List<Integer> doors = new ArrayList<>();

      for(int door : this.getMap().getDoors()) {
         if (door != 0) {
            doors.add(door);
         }
      }

      for(Entry<Integer, Map<String, ZoneType>> entry : this.getMap().getTerritories().entrySet()) {
         for(Entry<String, ZoneType> team : entry.getValue().entrySet()) {
            this._activeZones.put(team.getKey(), team.getValue());
         }
      }

      if (this.isInstanced()) {
         this.createInstance(doors, this._activeZones);
      }

      for(ZoneType zone : this._activeZones.values()) {
         zone.addListener(this._zoneListener);
      }

      List<FightEventPlayer> playersToRemove = new ArrayList<>();

      for(FightEventPlayer iFPlayer : this.getPlayers("registered_players")) {
         this.stopInvisibility(iFPlayer.getPlayer());
         if (!this.checkIfRegisteredPlayerMeetCriteria(iFPlayer)) {
            playersToRemove.add(iFPlayer);
         }
      }

      for(FightEventPlayer playerToRemove : playersToRemove) {
         this.unregister(playerToRemove.getPlayer());
      }

      if (this.isTeamed()) {
         this.spreadIntoTeamsAndPartys();
      }

      this.teleportRegisteredPlayers();
      this.updateEveryScore();

      for(FightEventPlayer iFPlayer : this.getPlayers("fighting_players", "registered_players")) {
         iFPlayer.getPlayer().isntAfk();
         iFPlayer.getPlayer().setFightEventGameRoom(null);
      }

      this.startNewTimer(true, TIME_PLAYER_TELEPORTING * 1000, "startRoundTimer", TIME_PREPARATION_BEFORE_FIRST_ROUND);
      ThreadPoolManager.getInstance().schedule(new AbstractFightEvent.LeftZoneThread(), 5000L);
      fightEventListeners(ScriptListener.EventStage.START);
   }

   public void startRound() {
      this._state = AbstractFightEvent.EVENT_STATE.STARTED;
      ++this._currentRound;
      if (this.isRoundEvent()) {
         if (this._currentRound == this._rounds) {
            this.sendMessageToFighting(AbstractFightEvent.MESSAGE_TYPES.SCREEN_BIG, "FightEvents.LAST_ROUND_START", true);
         } else {
            this.sendMessageToFighting(AbstractFightEvent.MESSAGE_TYPES.SCREEN_BIG, "FightEvents.ROUND_START", true, String.valueOf(this._currentRound));
         }
      } else {
         this.sendMessageToFighting(AbstractFightEvent.MESSAGE_TYPES.SCREEN_BIG, "FightEvents.FIGHT", true);
      }

      this.unrootPlayers();
      if (this.getRoundRuntime() > 0) {
         this.startNewTimer(true, (int)((double)this.getRoundRuntime() / 2.0 * 60000.0), "endRoundTimer", (int)((double)this.getRoundRuntime() / 2.0 * 60.0));
      }

      if (this._currentRound == 1) {
         ThreadPoolManager.getInstance().schedule(new AbstractFightEvent.TimeSpentOnEventThread(), 10000L);
         ThreadPoolManager.getInstance().schedule(new AbstractFightEvent.CheckAfkThread(), 1000L);
      }

      for(FightEventPlayer iFPlayer : this.getPlayers("fighting_players")) {
         this.hideScores(iFPlayer.getPlayer());
         iFPlayer.getPlayer().broadcastUserInfo(true);
      }
   }

   public void endRound() {
      this._state = AbstractFightEvent.EVENT_STATE.OVER;
      if (!this.isLastRound()) {
         this.sendMessageToFighting(AbstractFightEvent.MESSAGE_TYPES.SCREEN_BIG, "FightEvents.ROUND_OVER", false, String.valueOf(this._currentRound));
      } else {
         this.sendMessageToFighting(AbstractFightEvent.MESSAGE_TYPES.SCREEN_BIG, "FightEvents.EVENT_OVER", false);
      }

      this.ressAndHealPlayers();

      for(FightEventPlayer iFPlayer : this.getPlayers("fighting_players")) {
         this.showScores(iFPlayer.getPlayer());
      }

      if (!this.isLastRound()) {
         if (this.isTeamed()) {
            for(FightEventTeam team : this.getTeams()) {
               team.setSpawnLoc(null);
            }
         }

         ThreadPoolManager.getInstance().schedule(() -> {
            for(FightEventPlayer iFPlayerxx : this.getPlayers("fighting_players")) {
               this.teleportSinglePlayer(iFPlayerxx, false, true);
            }

            this.startNewTimer(true, 0, "startRoundTimer", TIME_PREPARATION_BETWEEN_NEXT_ROUNDS);
         }, (long)(TIME_AFTER_ROUND_END_TO_RETURN_SPAWN * 1000));
      } else {
         ThreadPoolManager.getInstance().schedule(() -> this.stopEvent(), 10000L);
         if (this.isTeamed()) {
            this.announceWinnerTeam(true, null);
         } else {
            this.announceWinnerPlayer(true, null);
         }
      }

      for(FightEventPlayer iFPlayer : this.getPlayers("fighting_players")) {
         iFPlayer.getPlayer().broadcastUserInfo(true);
      }
   }

   public void stopEvent() {
      this._state = AbstractFightEvent.EVENT_STATE.NOT_ACTIVE;
      this._room = null;
      this.showLastAFkMessage();
      FightEventPlayer[] topKillers = this.getTopKillers();
      this.announceTopKillers(topKillers);
      this.giveRewards(topKillers);

      for(FightEventPlayer iFPlayer : this.getPlayers("fighting_players")) {
         iFPlayer.getPlayer().updateAndBroadcastStatus(1);
         if (iFPlayer.getPlayer().getSummon() != null) {
            iFPlayer.getPlayer().getSummon().updateAndBroadcastStatus(0);
         }
      }

      for(Player player : this.getAllFightingPlayers()) {
         this.showScores(player);
      }

      FightLastStatsManager.getInstance().updateEventStats(this.getId());
      ThreadPoolManager.getInstance().schedule(() -> {
         for(Player playerxx : this.getAllFightingPlayers()) {
            this.leaveEvent(playerxx, true);
            playerxx.sendPacket(new ExShowScreenMessage("", 10, (byte)1, false));
         }
      }, 10000L);
      ThreadPoolManager.getInstance().schedule(() -> this.destroyMe(), (long)((15 + TIME_TELEPORT_BACK_TOWN) * 1000));
   }

   public void destroyMe() {
      if (this.getReflectionId() != 0) {
         ReflectionManager.getInstance().destroyReflection(this.getReflectionId());
      }

      for(ZoneType zone : this._activeZones.values()) {
         zone.removeListener(this._zoneListener);
      }

      if (this._timer != null) {
         this._timer.cancel(false);
      }

      this._timer = null;
      this._bestScores.clear();
      this._scores.clear();
      this._leftZone.clear();
      this._activeZones.clear();
      this.getObjects().clear();
      this._set = null;
      this._room = null;
      this._zoneListener = null;
      DoubleSessionManager.getInstance().clear(this.getId());
      FightEventManager.getInstance().removeEventId(this.getId());
      fightEventListeners(ScriptListener.EventStage.END);
   }

   public void onKilled(Creature actor, Creature victim) {
      if (victim.isPlayer() && this.getRespawnTime() > 0) {
         this.showScores(victim);
      }

      if (actor != null && actor.isPlayer() && this.getFightEventPlayer(actor) != null) {
         FightLastStatsManager.getInstance()
            .updateStat(
               this.getId(), actor.getActingPlayer(), FightLastStatsManager.FightEventStatType.KILL_PLAYER, this.getFightEventPlayer(actor).getKills()
            );
      }

      if (victim.isPlayer() && this.getRespawnTime() > 0 && !this._ressAllowed && this.getFightEventPlayer(victim.getActingPlayer()) != null) {
         this.startNewTimer(false, 0, "ressurectionTimer", this.getRespawnTime(), this.getFightEventPlayer(victim));
      }
   }

   public void onDamage(Creature actor, Creature victim, double damage) {
   }

   public void requestRespawn(Player activeChar) {
      if (this.getRespawnTime() > 0) {
         this.startNewTimer(false, 0, "ressurectionTimer", this.getRespawnTime(), this.getFightEventPlayer(activeChar));
      }
   }

   public boolean canAttack(Creature target, Creature attacker) {
      if (this._state != AbstractFightEvent.EVENT_STATE.STARTED) {
         return false;
      } else {
         Player player = attacker.getActingPlayer();
         if (player == null) {
            return true;
         } else if (target != null && target.isMonster()) {
            return true;
         } else if (player != null && player.isRespawnProtected()) {
            return false;
         } else if (target != null && target.isPlayer() && target.getActingPlayer().isRespawnProtected()) {
            return false;
         } else {
            if (this.isTeamed()) {
               FightEventPlayer targetFPlayer = this.getFightEventPlayer(target);
               FightEventPlayer attackerFPlayer = this.getFightEventPlayer(attacker);
               if (targetFPlayer == null || attackerFPlayer == null || targetFPlayer.getTeam().equals(attackerFPlayer.getTeam())) {
                  return false;
               }
            }

            return this.canAttackPlayers();
         }
      }
   }

   public boolean canAction(Creature target, Creature attacker) {
      if (this._state != AbstractFightEvent.EVENT_STATE.STARTED) {
         return false;
      } else {
         Player player = attacker.getActingPlayer();
         if (player == null) {
            return true;
         } else if (attacker != null && target != null && attacker.getObjectId() == target.getObjectId()) {
            return true;
         } else if (target != null && target.isMonster()) {
            return true;
         } else {
            if (this.isTeamed()) {
               FightEventPlayer targetFPlayer = this.getFightEventPlayer(target);
               FightEventPlayer attackerFPlayer = this.getFightEventPlayer(attacker);
               if (targetFPlayer == null || attackerFPlayer == null || targetFPlayer.getTeam().equals(attackerFPlayer.getTeam()) && !this.canTeamTarget()) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public boolean canUseMagic(Creature target, Creature attacker, Skill skill) {
      if (this._state != AbstractFightEvent.EVENT_STATE.STARTED) {
         return false;
      } else {
         if (attacker != null && target != null) {
            if (!this.canUseSkill(attacker, target, skill)) {
               return false;
            }

            if (attacker.getObjectId() == target.getObjectId()) {
               return true;
            }
         }

         if (target != null && target.isMonster()) {
            return true;
         } else if (attacker != null && attacker.isPlayer() && attacker.getActingPlayer().isRespawnProtected()) {
            return false;
         } else if (target != null && target.isPlayer() && target.getActingPlayer().isRespawnProtected()) {
            return false;
         } else {
            if (this.isTeamed()) {
               FightEventPlayer targetFPlayer = this.getFightEventPlayer(target);
               FightEventPlayer attackerFPlayer = this.getFightEventPlayer(attacker);
               if (targetFPlayer == null || attackerFPlayer == null || targetFPlayer.getTeam().equals(attackerFPlayer.getTeam()) && skill.isOffensive()) {
                  return false;
               }
            }

            return this.canAttackPlayers();
         }
      }
   }

   public boolean canUseSkill(Creature caster, Creature target, Skill skill) {
      if (this._excludedSkills != null) {
         for(int id : this._excludedSkills) {
            if (skill.getId() == id) {
               return false;
            }
         }
      }

      return true;
   }

   public boolean canUseScroll(Creature caster) {
      if (this._state != AbstractFightEvent.EVENT_STATE.STARTED) {
         return false;
      } else {
         FightEventPlayer FPlayer = this.getFightEventPlayer(caster);
         return FPlayer == null || this.canUseScrolls();
      }
   }

   public boolean canUsePotion(Creature caster) {
      if (this._state != AbstractFightEvent.EVENT_STATE.STARTED) {
         return false;
      } else {
         FightEventPlayer FPlayer = this.getFightEventPlayer(caster);
         return FPlayer == null || this.canUsePotions();
      }
   }

   public boolean canUseEscape(Creature caster) {
      if (this._state != AbstractFightEvent.EVENT_STATE.STARTED) {
         return false;
      } else {
         FightEventPlayer FPlayer = this.getFightEventPlayer(caster);
         return FPlayer == null;
      }
   }

   public boolean canUseItemSummon(Creature caster) {
      if (this._state != AbstractFightEvent.EVENT_STATE.STARTED) {
         return false;
      } else {
         FightEventPlayer FPlayer = this.getFightEventPlayer(caster);
         return FPlayer == null || this.canUseItemSummons();
      }
   }

   public boolean canRessurect(Player player, Creature creature) {
      return this._ressAllowed;
   }

   public int getMySpeed(Player player) {
      return -1;
   }

   public int getPAtkSpd(Player player) {
      return -1;
   }

   public void checkRestartLocs(Player player, Map<TeleportWhereType, Boolean> r) {
      r.clear();
      if (this.isTeamed() && this.getRespawnTime() > 0 && this.getFightEventPlayer(player) != null && this._ressAllowed) {
         r.put(TeleportWhereType.SIEGEFLAG, true);
      }
   }

   public boolean canUseBuffer(Player player, boolean heal) {
      FightEventPlayer fPlayer = this.getFightEventPlayer(player);
      if (!this.getBuffer()) {
         return false;
      } else if (player.isInCombat()) {
         return false;
      } else if (heal) {
         if (player.isDead()) {
            return false;
         } else if (this._state != AbstractFightEvent.EVENT_STATE.STARTED) {
            return true;
         } else {
            return fPlayer.isInvisible();
         }
      } else {
         return true;
      }
   }

   public boolean canUsePositiveMagic(Creature user, Creature target) {
      Player player = user.getActingPlayer();
      return player == null ? true : this.isFriend(user, target);
   }

   public int getRelation(Player thisPlayer, Player target, int oldRelation) {
      if (this._state == AbstractFightEvent.EVENT_STATE.STARTED) {
         return this.isFriend(thisPlayer, target) ? this.getFriendRelation() : this.getWarRelation();
      } else {
         return oldRelation;
      }
   }

   public boolean canJoinParty(Player sender, Player receiver) {
      return this.isFriend(sender, receiver);
   }

   public boolean canReceiveInvitations(Player sender, Player receiver) {
      return true;
   }

   public boolean canOpenStore(Player player) {
      return false;
   }

   public boolean canStandUp(Player player) {
      return true;
   }

   public boolean loseBuffsOnDeath(Player player) {
      return this._loseBuffsOnDeath;
   }

   protected boolean inScreenShowBeScoreNotKills() {
      return true;
   }

   protected boolean inScreenShowBeTeamNotInvidual() {
      return this.isTeamed();
   }

   public boolean isFriend(Creature c1, Creature c2) {
      if (c1.equals(c2)) {
         return true;
      } else if (c1.isPlayable() && c2.isPlayable()) {
         if (c1.isSummon() && c2.isPlayer() && c2.getActingPlayer().getSummon() != null && c2.getActingPlayer().getSummon().equals(c1)) {
            return true;
         } else if (c2.isSummon() && c1.isPlayer() && c1.getActingPlayer().getSummon() != null && c1.getActingPlayer().getSummon().equals(c2)) {
            return true;
         } else {
            FightEventPlayer fPlayer1 = this.getFightEventPlayer(c1.getActingPlayer());
            FightEventPlayer fPlayer2 = this.getFightEventPlayer(c2.getActingPlayer());
            if (this.isTeamed()) {
               return fPlayer1 != null && fPlayer2 != null && fPlayer1.getTeam().equals(fPlayer2.getTeam());
            } else {
               return false;
            }
         }
      } else {
         return true;
      }
   }

   public boolean isInvisible(Player actor, Player watcher) {
      return actor.isVisible();
   }

   public String getVisibleTitle(Player player, Player viewer, String currentTitle, boolean toMe) {
      return currentTitle;
   }

   public int getVisibleNameColor(Player player, int currentNameColor, boolean toMe) {
      if (this.isTeamed()) {
         FightEventPlayer fPlayer = this.getFightEventPlayer(player);
         return fPlayer.getTeam().getNickColor();
      } else {
         return currentNameColor;
      }
   }

   protected void giveItemRewardsForPlayer(FightEventPlayer fPlayer, Map<Integer, Long> rewards, boolean topKiller) {
      if (fPlayer != null) {
         if (rewards == null) {
            rewards = new HashMap<>();
         }

         rewards = this.giveRewardByParticipation(fPlayer, rewards);
         rewards = this.giveRewardByKillPlayer(fPlayer, fPlayer.getKills(), rewards);
         rewards = this.giveRewardForWinningTeam(fPlayer, rewards, true);
         if (topKiller) {
            fPlayer.getPlayer().getCounters().addAchivementInfo("eventTopKiller", 0, -1L, false, false, false);
            rewards = this.giveRewardForTopKiller(fPlayer, rewards);
         }

         int minutesAFK = (int)Math.round((double)fPlayer.getTotalAfkSeconds() / 60.0) * ITEMS_FOR_MINUTE_OF_AFK;
         if (rewards != null && rewards.size() > 0) {
            for(int item : rewards.keySet()) {
               if (item != 0) {
                  long totalAmount = rewards.get(item) * (long)Config.FIGHT_EVENTS_REWARD_MULTIPLIER - (long)minutesAFK;
                  if (totalAmount > 0L) {
                     fPlayer.getPlayer().addItem("Event Reward", item, totalAmount, fPlayer.getPlayer(), true);
                  }
               }
            }

            rewards.clear();
         }
      }
   }

   private Map<Integer, Long> giveRewardByParticipation(FightEventPlayer fPlayer, Map<Integer, Long> rewards) {
      if (this._rewardByParticipation != null && this._rewardByParticipation.length != 0 && rewards != null) {
         for(int[] item : this._rewardByParticipation) {
            if (item != null && item.length == 2) {
               if (rewards.containsKey(item[0])) {
                  long amount = rewards.get(item[0]) + (long)item[1];
                  rewards.put(item[0], amount);
               } else {
                  rewards.put(item[0], (long)item[1]);
               }
            }
         }

         return rewards;
      } else {
         return rewards;
      }
   }

   private Map<Integer, Long> giveRewardForTopKiller(FightEventPlayer fPlayer, Map<Integer, Long> rewards) {
      if (this._rewardByTopKiller != null && this._rewardByTopKiller.length != 0) {
         for(int[] item : this._rewardByTopKiller) {
            if (item != null && item.length == 2) {
               if (rewards.containsKey(item[0])) {
                  long amount = rewards.get(item[0]) + (long)item[1];
                  rewards.put(item[0], amount);
               } else {
                  rewards.put(item[0], (long)item[1]);
               }
            }
         }

         return rewards;
      } else {
         return rewards;
      }
   }

   protected Map<Integer, Long> giveRewardForWinningTeam(FightEventPlayer fPlayer, Map<Integer, Long> rewards, boolean atLeast1Kill) {
      if (this._teamed && (this._state == AbstractFightEvent.EVENT_STATE.OVER || this._state == AbstractFightEvent.EVENT_STATE.NOT_ACTIVE)) {
         if (this._rewardByWinner == null || this._rewardByWinner.length == 0) {
            return rewards;
         } else if (atLeast1Kill
            && fPlayer.getKills() <= 0
            && FightEventGameRoom.getPlayerClassGroup(fPlayer.getPlayer()) != FightEventManager.CLASSES.HEALERS) {
            return rewards;
         } else {
            FightEventTeam winner = null;
            int winnerPoints = -1;
            boolean sameAmount = false;

            for(FightEventTeam team : this.getTeams()) {
               if (team.getScore() > winnerPoints) {
                  winner = team;
                  winnerPoints = team.getScore();
                  sameAmount = false;
               } else if (team.getScore() == winnerPoints) {
                  sameAmount = true;
               }
            }

            if (!sameAmount && fPlayer.getTeam().equals(winner)) {
               for(int[] item : this._rewardByWinner) {
                  if (item != null && item.length == 2) {
                     if (rewards.containsKey(item[0])) {
                        long amount = rewards.get(item[0]) + (long)item[1];
                        rewards.put(item[0], amount);
                     } else {
                        rewards.put(item[0], (long)item[1]);
                     }
                  }
               }
            }

            return rewards;
         }
      } else {
         return rewards;
      }
   }

   private Map<Integer, Long> giveRewardByKillPlayer(FightEventPlayer fPlayer, int kills, Map<Integer, Long> rewards) {
      if (this._rewardByKillPlayer != null && this._rewardByKillPlayer.length > 0 && kills > 0) {
         for(int[] item : this._rewardByKillPlayer) {
            if (item != null && item.length == 2) {
               if (rewards.containsKey(item[0])) {
                  long amount = rewards.get(item[0]) + (long)(item[1] * kills);
                  rewards.put(item[0], amount);
               } else {
                  rewards.put(item[0], (long)item[1] * (long)kills);
               }
            }
         }
      }

      return rewards;
   }

   public void startTeleportTimer(FightEventGameRoom room) {
      this.setState(AbstractFightEvent.EVENT_STATE.COUNT_DOWN);
      this.startNewTimer(true, 0, "teleportWholeRoomTimer", TIME_FIRST_TELEPORT);
   }

   protected void teleportRegisteredPlayers() {
      for(FightEventPlayer player : this.getPlayers("registered_players")) {
         this.teleportSinglePlayer(player, true, true);
      }
   }

   protected void teleportSinglePlayer(FightEventPlayer fPlayer, boolean firstTime, boolean healAndRess) {
      Player player = fPlayer.getPlayer();
      if (healAndRess && player.isDead()) {
         player.doRevive(100.0);
      }

      if (firstTime) {
         player.setSaveLoc(player.getLocation());
      }

      Location loc = this.getSinglePlayerSpawnLocation(fPlayer);
      if (this.isInstanced()) {
         player.setReflectionId(this._reflectionId);
      }

      player.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), true);
      if (this._state == AbstractFightEvent.EVENT_STATE.PREPARATION || this._state == AbstractFightEvent.EVENT_STATE.OVER) {
         this.rootPlayer(player);
      }

      if (player.getSummon() instanceof PetInstance) {
         player.getSummon().unSummon(player);
      }

      if (firstTime) {
         this.removeObject("registered_players", fPlayer);
         this.addObject("fighting_players", fPlayer);
         player.stopAllEffects();
         if (player.getSummon() != null) {
            player.getSummon().stopAllEffects();
         }

         player.store(true);
         player.sendPacket(new ShowTutorialMark(false, 100));

         for(ItemInstance o : player.getInventory().getItems()) {
            if (o != null && o.isEquipable() && o.isEquipped() && o.isEventRestrictedItem()) {
               int slot = player.getInventory().getSlotFromItem(o);
               player.getInventory().unEquipItemInBodySlot(slot);
            }
         }

         player.sendPacket(
            new CreatureSay(
               player.getObjectId(), 0, player.getEventName(this.getId()), ServerStorage.getInstance().getString(player.getLang(), "FightEvents.NCHAT")
            )
         );
         if (this.isTeamed()) {
            player.sendPacket(
               new CreatureSay(
                  player.getObjectId(), 0, player.getEventName(this.getId()), ServerStorage.getInstance().getString(player.getLang(), "FightEvents.BCHAT")
               )
            );
            player.sendPacket(
               new CreatureSay(
                  player.getObjectId(), 20, player.getEventName(this.getId()), ServerStorage.getInstance().getString(player.getLang(), "FightEvents.BCHAT")
               )
            );
         }
      }

      if (!firstTime && Config.ALLOW_RESPAWN_PROTECT_PLAYER) {
         Skill skill = SkillsParser.getInstance().getInfo(5576, 1);
         if (skill != null) {
            skill.getEffects(player, player, false);
         }

         player.setRespawnProtect();
         ThreadPoolManager.getInstance().schedule(new InvisibleTask(player), 5000L);
      }

      if (healAndRess) {
         player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
         player.setCurrentCp(player.getMaxCp());
         if (player.getSummon() != null && !player.getSummon().isDead()) {
            Summon pet = player.getSummon();
            pet.setCurrentHpMp(pet.getMaxHp(), pet.getMaxMp());
            pet.updateAndBroadcastStatus(1);
         }

         player.broadcastStatusUpdate();
         player.updateAndBroadcastStatus(1);
      }

      if (player.isMounted()) {
         player.dismount();
      }

      if (player.getTransformationId() > 0) {
         player.untransform();
      }

      this.buffPlayer(player);
      player.broadcastUserInfo(true);
   }

   protected Location getSinglePlayerSpawnLocation(FightEventPlayer fPlayer) {
      Location[] spawns = null;
      Location loc = null;
      if (!this.isTeamed()) {
         spawns = this.getMap().getPlayerSpawns();
      } else {
         loc = this.getTeamSpawn(fPlayer, true);
      }

      if (!this.isTeamed()) {
         loc = this.getSafeLocation(spawns);
      }

      return Location.findPointToStay(loc, 0, 75, fPlayer.getPlayer().getGeoIndex(), true);
   }

   public void unregister(Player player) {
      FightEventPlayer fPlayer = this.getFightEventPlayer(player, "registered_players");
      player.removeEvent(this);
      this.removeObject("registered_players", fPlayer);
      player.sendMessage(new ServerMessage("FightEvents.LONG_REGISTER", player.getLang()).toString());
   }

   public boolean leaveEvent(Player player, boolean teleportTown) {
      FightEventPlayer fPlayer = this.getFightEventPlayer(player);
      if (fPlayer == null) {
         return true;
      } else {
         if (this._state == AbstractFightEvent.EVENT_STATE.NOT_ACTIVE) {
            if (fPlayer.isInvisible()) {
               this.stopInvisibility(player);
            }

            this.removeObject("fighting_players", fPlayer);
            if (this.isTeamed()) {
               fPlayer.getTeam().removePlayer(fPlayer);
            }

            player.removeEvent(this);
            if (teleportTown) {
               this.teleportBackToTown(player);
            } else {
               player.doRevive();
            }
         } else {
            this.rewardPlayer(fPlayer, false);
            if (teleportTown) {
               this.setInvisible(player, TIME_TELEPORT_BACK_TOWN, false);
            } else {
               this.setInvisible(player, -1, false);
            }

            this.removeObject("fighting_players", fPlayer);
            player.doDie(null);
            if (teleportTown) {
               this.startNewTimer(false, 0, "teleportBackSinglePlayerTimer", TIME_TELEPORT_BACK_TOWN, player);
            } else {
               player.doRevive();
            }

            player.removeEvent(this);
         }

         this.hideScores(player);
         this.updateScreenScores();
         if (this.getPlayers("fighting_players", "registered_players").isEmpty()) {
            ThreadPoolManager.getInstance().schedule(() -> this.destroyMe(), (long)((15 + TIME_TELEPORT_BACK_TOWN) * 1000));
         }

         if (player.isRooted()) {
            player.startRooted(false);
            player.stopAbnormalEffect(AbnormalEffect.ROOT);
            if (player.hasSummon()) {
               player.getSummon().startRooted(false);
               player.getSummon().stopAbnormalEffect(AbnormalEffect.ROOT);
            }
         }

         Effect eInvis = player.getFirstEffect(EffectType.INVINCIBLE);
         if (eInvis != null) {
            eInvis.exit();
         }

         player.startHealBlocked(false);
         player.setIsInvul(false);
         if (player.getParty() != null) {
            player.getParty().removePartyMember(player, Party.messageType.Expelled);
         }

         return true;
      }
   }

   public void loggedOut(Player player) {
      this.leaveEvent(player, true);
   }

   protected void teleportBackToTown(Player player) {
      if (player.isDead()) {
         player.doRevive();
      }

      Location loc = null;
      if (player.getSaveLoc() != null) {
         loc = player.getSaveLoc();
      } else {
         loc = Location.findPointToStay(FightEventManager.RETURN_LOC, 0, 100, player.getGeoIndex(), true);
      }

      player.teleToLocation(loc, 0, true);
      player.setReflectionId(0);
      if (this.isInstanced()) {
         Reflection ref = ReflectionManager.getInstance().getReflection(this._reflectionId);
         if (ref != null && ref.containsPlayer(player.getObjectId())) {
            ref.removePlayer(player.getObjectId());
         }
      }
   }

   protected void rewardPlayer(FightEventPlayer fPlayer, boolean isTopKiller) {
      if (fPlayer != null) {
         this.giveItemRewardsForPlayer(fPlayer, null, isTopKiller);
      }
   }

   @Nullable
   private FightEventPlayer[] getTopKillers() {
      if (this._rewardByTopKiller != null && this._rewardByTopKiller.length != 0) {
         if (!this._teamed) {
            FightEventPlayer[] topKillers = new FightEventPlayer[1];
            int topKillersKills = 0;

            for(FightEventPlayer iFPlayer : this.getPlayers("fighting_players")) {
               if (iFPlayer != null && iFPlayer.getKills() > topKillersKills) {
                  topKillers[0] = null;
                  topKillers[0] = iFPlayer;
                  topKillersKills = iFPlayer.getKills();
               }
            }

            return topKillers;
         } else {
            FightEventPlayer[] topKillers = new FightEventPlayer[this._teams.size()];
            int[] topKillersKills = new int[this._teams.size()];
            int teamIndex = 0;

            for(FightEventTeam team : this._teams) {
               for(FightEventPlayer fPlayer : team.getPlayers()) {
                  if (fPlayer != null) {
                     if (fPlayer.getKills() == topKillersKills[teamIndex]) {
                        topKillers[teamIndex] = null;
                     } else if (fPlayer.getKills() > topKillersKills[teamIndex]) {
                        topKillers[teamIndex] = fPlayer;
                        topKillersKills[teamIndex] = fPlayer.getKills();
                     }
                  }
               }

               ++teamIndex;
            }

            return topKillers;
         }
      } else {
         return null;
      }
   }

   protected void announceWinnerTeam(boolean wholeEvent, FightEventTeam winnerOfTheRound) {
      int bestScore = -1;
      FightEventTeam bestTeam = null;
      boolean draw = false;
      if (wholeEvent) {
         for(FightEventTeam team : this.getTeams()) {
            if (team.getScore() > bestScore) {
               draw = false;
               bestScore = team.getScore();
               bestTeam = team;
            } else if (team.getScore() == bestScore) {
               draw = true;
            }
         }
      } else {
         bestTeam = winnerOfTheRound;
      }

      if (!draw) {
         for(FightEventPlayer iFPlayer : this.getPlayers("fighting_players")) {
            ServerMessage msg = wholeEvent
               ? new ServerMessage("FightEvents.WE_WON_EVENT", iFPlayer.getPlayer().getLang())
               : new ServerMessage("FightEvents.WE_WON_ROUND", iFPlayer.getPlayer().getLang());
            if (wholeEvent) {
               msg.add(iFPlayer.getPlayer().getEventName(this.getId()));
            }

            iFPlayer.getPlayer()
               .sendPacket(
                  new CreatureSay(
                     0, 15, new ServerMessage("FightEvents." + bestTeam.getName() + "", iFPlayer.getPlayer().getLang()).toString(), msg.toString()
                  )
               );
         }
      }

      this.updateScreenScores();
   }

   protected void announceWinnerPlayer(boolean wholeEvent, FightEventPlayer winnerOfTheRound) {
      int bestScore = -1;
      FightEventPlayer bestPlayer = null;
      boolean draw = false;
      if (wholeEvent) {
         for(FightEventPlayer iFPlayer : this.getPlayers("fighting_players")) {
            if (iFPlayer.getPlayer() != null && iFPlayer.getPlayer().isOnline()) {
               if (iFPlayer.getScore() > bestScore) {
                  bestScore = iFPlayer.getScore();
                  bestPlayer = iFPlayer;
               } else if (iFPlayer.getScore() == bestScore) {
                  draw = true;
               }
            }
         }
      } else {
         bestPlayer = winnerOfTheRound;
      }

      if (!draw && bestPlayer != null) {
         for(FightEventPlayer iFPlayer : this.getPlayers("fighting_players")) {
            ServerMessage msg = wholeEvent
               ? new ServerMessage("FightEvents.I_WON_EVENT", iFPlayer.getPlayer().getLang())
               : new ServerMessage("FightEvents.I_WON_ROUND", iFPlayer.getPlayer().getLang());
            if (wholeEvent) {
               msg.add(iFPlayer.getPlayer().getEventName(this.getId()));
            }

            iFPlayer.getPlayer().sendPacket(new CreatureSay(0, 15, bestPlayer.getPlayer().getName(), msg.toString()));
         }
      }

      this.updateScreenScores();
   }

   protected void updateScreenScores() {
      for(FightEventPlayer iFPlayer : this.getPlayers("fighting_players")) {
         String msg = this.getScreenScores(iFPlayer.getPlayer(), this.inScreenShowBeScoreNotKills(), this.inScreenShowBeTeamNotInvidual());
         iFPlayer.getPlayer().sendPacket(new ExShowScreenMessage(msg.toString(), 600000, (byte)1, false));
      }
   }

   protected void updateScreenScores(Player player) {
      if (this.getFightEventPlayer(player) != null) {
         String msg = this.getScreenScores(player, this.inScreenShowBeScoreNotKills(), this.inScreenShowBeTeamNotInvidual());
         player.sendPacket(new ExShowScreenMessage(msg.toString(), 600000, (byte)1, false));
      }
   }

   protected String getScorePlayerName(FightEventPlayer fPlayer) {
      return fPlayer.getPlayer().getName()
         + (
            this.isTeamed()
               ? " ("
                  + ""
                  + ServerStorage.getInstance().getString(fPlayer.getPlayer().getLang(), "FightEvents." + fPlayer.getTeam().getName() + "")
                  + ""
                  + ")"
               : ""
         );
   }

   protected void updatePlayerScore(FightEventPlayer fPlayer) {
      this._scores.put(this.getScorePlayerName(fPlayer), fPlayer.getKills());
      this._scoredUpdated = true;
      if (!this.isTeamed()) {
         this.updateScreenScores();
      }
   }

   protected void showScores(Creature c) {
      Map<String, Integer> scores = this.getBestScores();
      if (scores != null && !scores.isEmpty()) {
         FightEventPlayer fPlayer = this.getFightEventPlayer(c);
         if (fPlayer != null) {
            fPlayer.setShowRank(true);
         }

         c.sendPacket(new ExPVPMatchCCRecord(scores));
      }
   }

   protected void hideScores(Creature c) {
      c.sendPacket(ExPVPMatchCCRetire.STATIC);
   }

   protected void handleAfk(FightEventPlayer fPlayer, boolean setAsAfk) {
      Player player = fPlayer.getPlayer();
      if (setAsAfk) {
         fPlayer.setAfk(true);
         fPlayer.setAfkStartTime(player.getLastNotAfkTime());
         this.sendMessageToPlayer(player, AbstractFightEvent.MESSAGE_TYPES.CRITICAL, new ServerMessage("FightEvents.YOU_AFK", player.getLang()));
      } else if (fPlayer.isAfk()) {
         int totalAfkTime = (int)((System.currentTimeMillis() - fPlayer.getAfkStartTime()) / 1000L);
         totalAfkTime -= TIME_TO_BE_AFK;
         if (totalAfkTime > 5) {
            fPlayer.setAfk(false);
            fPlayer.addTotalAfkSeconds(totalAfkTime);
            ServerMessage msg = new ServerMessage("FightEvents.WAS_AFK_SEC", player.getLang());
            msg.add(totalAfkTime);
            this.sendMessageToPlayer(player, AbstractFightEvent.MESSAGE_TYPES.CRITICAL, msg);
         }
      }
   }

   protected void setInvisible(Player player, int seconds, boolean sendMessages) {
      FightEventPlayer fPlayer = this.getFightEventPlayer(player);
      fPlayer.setInvisible(true);
      player.setInvisible(true);
      player.startAbnormalEffect(AbnormalEffect.STEALTH);
      player.broadcastUserInfo(true);
      if (seconds > 0) {
         this.startNewTimer(false, 0, "setInvisible", seconds, fPlayer, sendMessages);
      }
   }

   protected void stopInvisibility(Player player) {
      FightEventPlayer fPlayer = this.getFightEventPlayer(player);
      if (fPlayer != null) {
         fPlayer.setInvisible(false);
      }

      player.setInvisible(false);
      player.stopAbnormalEffect(AbnormalEffect.STEALTH);
      player.updateAndBroadcastStatus(1);
      if (player.getSummon() != null) {
         player.getSummon().updateAndBroadcastStatus(0);
      }
   }

   protected void rootPlayer(Player player) {
      if (this.isRootBetweenRounds()) {
         player.startRooted(true);
         player.stopMove(null);
         player.startAbnormalEffect(AbnormalEffect.ROOT);
         if (player.hasSummon()) {
            player.getSummon().startRooted(true);
            player.getSummon().stopMove(null);
            player.getSummon().startAbnormalEffect(AbnormalEffect.ROOT);
         }
      }
   }

   protected void unrootPlayers() {
      if (this.isRootBetweenRounds()) {
         for(FightEventPlayer iFPlayer : this.getPlayers("fighting_players")) {
            Player player = iFPlayer.getPlayer();
            if (player != null) {
               player.startRooted(false);
               player.stopAbnormalEffect(AbnormalEffect.ROOT);
               if (player.hasSummon()) {
                  player.getSummon().startRooted(false);
                  player.getSummon().stopAbnormalEffect(AbnormalEffect.ROOT);
               }
            }
         }
      }
   }

   protected void ressAndHealPlayers() {
      for(FightEventPlayer fPlayer : this.getPlayers("fighting_players")) {
         Player player = fPlayer.getPlayer();
         if (player.isDead()) {
            player.doRevive(100.0);
         }

         this.buffPlayer(player);
      }
   }

   protected int getWarRelation() {
      int result = 0;
      result |= 64;
      result |= 32768;
      return result | 16384;
   }

   protected int getFriendRelation() {
      int result = 0;
      result |= 64;
      return result | 256;
   }

   protected Npc chooseLocAndSpawnNpc(int id, Location[] locs, int respawnInSeconds, boolean findPos) {
      return this.spawnNpc(id, this.getSafeLocation(locs), respawnInSeconds, findPos);
   }

   protected Npc spawnNpc(int id, Location loc, int respawnInSeconds, boolean findPos) {
      Npc npc = null;
      NpcTemplate template = NpcsParser.getInstance().getTemplate(id);

      try {
         Location location = null;
         if (findPos) {
            Reflection instance = ReflectionManager.getInstance().getReflection(this.getReflectionId());
            location = Location.findPointToStay(loc, 0, 75, instance.getGeoIndex(), true);
         } else {
            location = loc;
         }

         Spawner spawn = new Spawner(template);
         spawn.setX(location.getX());
         spawn.setY(location.getY());
         spawn.setZ(location.getZ());
         spawn.setHeading(location.getHeading());
         spawn.setReflectionId(this.getReflectionId());
         spawn.setAmount(1);
         spawn.setRespawnDelay(Math.max(0, respawnInSeconds));
         if (respawnInSeconds <= 0) {
            spawn.stopRespawn();
         }

         SpawnParser.getInstance().addNewSpawn(spawn);
         spawn.init();
         npc = spawn.getLastSpawn();
      } catch (Exception var9) {
         _log.warning(this.getClass().getSimpleName() + ": " + var9.getMessage());
      }

      return npc;
   }

   protected static ServerMessage getFixedTime(Player player, int seconds) {
      int minutes = seconds / 60;
      ServerMessage msg = new ServerMessage("FightEvents.FIX_TIME", player.getLang());
      if (seconds >= 60) {
         msg.add(minutes);
         msg.add(
            minutes > 1
               ? (
                  minutes < 5
                     ? new ServerMessage("FightEvents.CHECK_MIN2", player.getLang()).toString()
                     : new ServerMessage("FightEvents.CHECK_MIN3", player.getLang()).toString()
               )
               : new ServerMessage("FightEvents.CHECK_MIN1", player.getLang()).toString()
         );
      } else {
         msg.add(seconds);
         msg.add(
            seconds > 1
               ? (
                  seconds < 5
                     ? new ServerMessage("FightEvents.CHECK_SEC2", player.getLang()).toString()
                     : new ServerMessage("FightEvents.CHECK_SEC3", player.getLang()).toString()
               )
               : new ServerMessage("FightEvents.CHECK_SEC1", player.getLang()).toString()
         );
      }

      return msg;
   }

   private void buffPlayer(Player player) {
      if (this.getBuffer()) {
         int[][] buffs;
         if (player.isMageClass()) {
            buffs = this._mageBuffs;
         } else {
            buffs = this._fighterBuffs;
         }

         if (buffs != null) {
            giveBuffs(player, buffs, false);
            if (player.getSummon() != null) {
               giveBuffs(player, this._fighterBuffs, true);
            }
         }
      }
   }

   private static void giveBuffs(Player player, int[][] buffs, boolean petbuff) {
      for(int[] buff1 : buffs) {
         Skill buff = SkillsParser.getInstance().getInfo(buff1[0], buff1[1]);
         if (buff != null) {
            if (!petbuff) {
               buff.getEffects(player, player, false);
            } else if (player.hasSummon()) {
               buff.getEffects(player, player.getSummon(), false);
            }
         }
      }

      ThreadPoolManager.getInstance().schedule(() -> {
         if (!petbuff) {
            player.setCurrentHp(player.getMaxHp());
            player.setCurrentMp(player.getMaxMp());
            player.setCurrentCp(player.getMaxCp());
         } else if (player.hasSummon()) {
            player.getSummon().setCurrentHp(player.getSummon().getMaxHp());
            player.getSummon().setCurrentMp(player.getSummon().getMaxMp());
            player.getSummon().setCurrentCp(player.getSummon().getMaxCp());
         }
      }, 1000L);
   }

   private void announceTopKillers(FightEventPlayer[] topKillers) {
      if (topKillers != null) {
         for(FightEventPlayer fPlayer : topKillers) {
            if (fPlayer != null) {
               for(Player player : World.getInstance().getAllPlayers()) {
                  ServerMessage message = new ServerMessage("FightEvents.MOST_KILL", player.getLang());
                  message.add(fPlayer.getPlayer().getName());
                  message.add(player.getEventName(this.getId()));
                  player.sendPacket(new CreatureSay(0, 18, player.getEventName(this.getId()), message.toString()));
               }
            }
         }
      }
   }

   protected void sendMessageWithCheckRound(AbstractFightEvent event, AbstractFightEvent.MESSAGE_TYPES type, boolean skipJustTeleported, int secondsLeft) {
      for(FightEventPlayer iFPlayer : this.getPlayers("fighting_players")) {
         if (!skipJustTeleported || !iFPlayer.isInvisible()) {
            ServerMessage msg;
            if (event.isRoundEvent()) {
               msg = event.getCurrentRound() + 1 == event.getTotalRounds()
                  ? new ServerMessage("FightEvents.GOING_START_LAST_ROUND", iFPlayer.getPlayer().getLang())
                  : new ServerMessage(ROUND_NUMBER_IN_STRING[event.getCurrentRound() + 1], iFPlayer.getPlayer().getLang());
            } else {
               msg = new ServerMessage("FightEvents.GOING_START_MATCH", iFPlayer.getPlayer().getLang());
            }

            msg.add(getFixedTime(iFPlayer.getPlayer(), secondsLeft).toString());
            this.sendMessageToPlayer(iFPlayer.getPlayer(), type, msg);
         }
      }
   }

   protected void sendMessageToTeam(FightEventTeam team, AbstractFightEvent.MESSAGE_TYPES type, String msg) {
      for(FightEventPlayer iFPlayer : team.getPlayers()) {
         this.sendMessageToPlayer(iFPlayer.getPlayer(), type, new ServerMessage(msg, iFPlayer.getPlayer().getLang()));
      }
   }

   protected void sendMessageToTeam(FightEventTeam team, AbstractFightEvent.MESSAGE_TYPES type, String msg, FightEventTeam flagTeam) {
      for(FightEventPlayer iFPlayer : team.getPlayers()) {
         ServerMessage message = new ServerMessage(msg, iFPlayer.getPlayer().getLang());
         message.add(new ServerMessage("FightEvents." + flagTeam.getName() + "", iFPlayer.getPlayer().getLang()).toString());
         this.sendMessageToPlayer(iFPlayer.getPlayer(), type, message);
      }
   }

   protected void sendMessageToFighting(AbstractFightEvent.MESSAGE_TYPES type, String msg, boolean skipJustTeleported) {
      for(FightEventPlayer iFPlayer : this.getPlayers("fighting_players")) {
         if (!skipJustTeleported || !iFPlayer.isInvisible()) {
            this.sendMessageToPlayer(iFPlayer.getPlayer(), type, new ServerMessage(msg, iFPlayer.getPlayer().getLang()));
         }
      }
   }

   protected void sendMessageToFighting(AbstractFightEvent.MESSAGE_TYPES type, String msg, boolean skipJustTeleported, String value) {
      for(FightEventPlayer iFPlayer : this.getPlayers("fighting_players")) {
         if (!skipJustTeleported || !iFPlayer.isInvisible()) {
            ServerMessage message = new ServerMessage(msg, iFPlayer.getPlayer().getLang());
            message.add(value);
            this.sendMessageToPlayer(iFPlayer.getPlayer(), type, message);
         }
      }
   }

   protected void sendMessageToFighting(AbstractFightEvent.MESSAGE_TYPES type, String msg, boolean skipJustTeleported, int secondsLeft) {
      for(FightEventPlayer iFPlayer : this.getPlayers("fighting_players")) {
         if (!skipJustTeleported || !iFPlayer.isInvisible()) {
            ServerMessage message = new ServerMessage(msg, iFPlayer.getPlayer().getLang());
            message.add(getFixedTime(iFPlayer.getPlayer(), secondsLeft).toString());
            this.sendMessageToPlayer(iFPlayer.getPlayer(), type, message);
         }
      }
   }

   protected void sendMessageToRegistered(AbstractFightEvent.MESSAGE_TYPES type, String msg) {
      for(FightEventPlayer iFPlayer : this.getPlayers("registered_players")) {
         this.sendMessageToPlayer(iFPlayer.getPlayer(), type, new ServerMessage(msg, iFPlayer.getPlayer().getLang()));
      }
   }

   protected void sendMessageToRegistered(AbstractFightEvent.MESSAGE_TYPES type, String msg, int secondsLeft) {
      for(FightEventPlayer iFPlayer : this.getPlayers("registered_players")) {
         ServerMessage message = new ServerMessage(msg, iFPlayer.getPlayer().getLang());
         message.add(getFixedTime(iFPlayer.getPlayer(), secondsLeft).toString());
         this.sendMessageToPlayer(iFPlayer.getPlayer(), type, message);
      }
   }

   protected void sendMessageToPlayer(Player player, AbstractFightEvent.MESSAGE_TYPES type, ServerMessage msg) {
      switch(type) {
         case GM:
            player.sendPacket(new CreatureSay(player.getObjectId(), 18, player.getName(), msg.toString()));
            this.updateScreenScores(player);
            break;
         case NORMAL_MESSAGE:
            player.sendMessage(msg.toString());
            break;
         case SCREEN_BIG:
            player.sendPacket(new ExShowScreenMessage(msg.toString(), 3000, (byte)2, true));
            this.updateScreenScores(player);
            break;
         case SCREEN_SMALL:
            player.sendPacket(new ExShowScreenMessage(msg.toString(), 600000, (byte)1, false));
            break;
         case CRITICAL:
            player.sendPacket(new CreatureSay(player.getObjectId(), 15, player.getName(), msg.toString()));
            this.updateScreenScores(player);
      }
   }

   public void setState(AbstractFightEvent.EVENT_STATE state) {
      this._state = state;
   }

   public AbstractFightEvent.EVENT_STATE getState() {
      return this._state;
   }

   public String getDescriptionEn() {
      return this._descEn;
   }

   public String getDescriptionRu() {
      return this._descRu;
   }

   public String getIcon() {
      return this._icon;
   }

   public boolean isAutoTimed() {
      return this._isAutoTimed;
   }

   public int[][] getAutoStartTimes() {
      return this._autoStartTimes;
   }

   public FightEventMap getMap() {
      return this._map;
   }

   public boolean isTeamed() {
      return this._teamed;
   }

   public boolean canTeamTarget() {
      return this._teamTargets;
   }

   public boolean givePvpPoints() {
      return this._givePvpPoints;
   }

   public boolean canAttackPlayers() {
      return this._attackPlayers;
   }

   public boolean canUseScrolls() {
      return this._useScrolls;
   }

   public boolean canUsePotions() {
      return this._usePotions;
   }

   public boolean canUseItemSummons() {
      return this._useItemSummon;
   }

   protected boolean isInstanced() {
      return this._instanced;
   }

   public int getReflectionId() {
      return this._reflectionId;
   }

   public int getRoundRuntime() {
      return this._roundRunTime;
   }

   public int getRespawnTime() {
      return this._respawnTime;
   }

   public boolean isRoundEvent() {
      return this._roundEvent;
   }

   public int getTotalRounds() {
      return this._rounds;
   }

   public int getCurrentRound() {
      return this._currentRound;
   }

   public boolean getBuffer() {
      return this._buffer;
   }

   protected boolean isRootBetweenRounds() {
      return this._rootBetweenRounds;
   }

   public boolean isLastRound() {
      return !this.isRoundEvent() || this.getCurrentRound() == this.getTotalRounds();
   }

   protected List<FightEventTeam> getTeams() {
      return this._teams;
   }

   public MultiValueSet<String> getSet() {
      return this._set;
   }

   public void clearSet() {
      this._set = null;
   }

   public FightEventManager.CLASSES[] getExcludedClasses() {
      return this._excludedClasses;
   }

   protected int getTeamTotalKills(FightEventTeam team) {
      if (!this.isTeamed()) {
         return 0;
      } else {
         int totalKills = 0;

         for(FightEventPlayer iFPlayer : team.getPlayers()) {
            totalKills += iFPlayer.getKills();
         }

         return totalKills;
      }
   }

   public int getPlayersCount(String... groups) {
      return this.getPlayers(groups).size();
   }

   public List<FightEventPlayer> getPlayers(String... groups) {
      if (groups.length == 1) {
         return this.getObjects(groups[0]);
      } else {
         List<FightEventPlayer> newList = new ArrayList<>();

         for(String group : groups) {
            List<FightEventPlayer> fPlayers = this.getObjects(group);
            newList.addAll(fPlayers);
         }

         return newList;
      }
   }

   public List<Player> getAllFightingPlayers() {
      List<FightEventPlayer> fPlayers = this.getPlayers("fighting_players");
      List<Player> players = new ArrayList<>(fPlayers.size());

      for(FightEventPlayer fPlayer : fPlayers) {
         players.add(fPlayer.getPlayer());
      }

      return players;
   }

   public List<Player> getMyTeamFightingPlayers(Player player) {
      FightEventTeam fTeam = this.getFightEventPlayer(player).getTeam();
      List<FightEventPlayer> fPlayers = this.getPlayers("fighting_players");
      List<Player> players = new ArrayList<>(fPlayers.size());
      if (!this.isTeamed()) {
         player.sendPacket(
            new CreatureSay(
               player.getObjectId(), 20, player.getEventName(this.getId()), new ServerMessage("FightEvents.NO_TEAMS", player.getLang()).toString()
            )
         );
         players.add(player);
      } else {
         for(FightEventPlayer iFPlayer : fPlayers) {
            if (iFPlayer.getTeam().equals(fTeam)) {
               players.add(iFPlayer.getPlayer());
            }
         }
      }

      return players;
   }

   public FightEventPlayer getFightEventPlayer(Creature creature) {
      return this.getFightEventPlayer(creature, "fighting_players");
   }

   public FightEventPlayer getFightEventPlayer(Creature creature, String... groups) {
      if (creature != null && creature.isPlayable()) {
         int lookedPlayerId = creature.getActingPlayer().getObjectId();

         for(FightEventPlayer iFPlayer : this.getPlayers(groups)) {
            if (iFPlayer.getPlayer().getObjectId() == lookedPlayerId) {
               return iFPlayer;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   protected void spreadIntoTeamsAndPartys() {
      for(int i = 0; i < this._room.getTeamsCount(); ++i) {
         this._teams.add(new FightEventTeam(i + 1));
      }

      int index = 0;

      for(Player player : this._room.getAllPlayers()) {
         FightEventPlayer fPlayer = this.getFightEventPlayer(player, "registered_players");
         if (fPlayer != null) {
            FightEventTeam team = this._teams.get(index % this._room.getTeamsCount());
            fPlayer.setTeam(team);
            team.addPlayer(fPlayer);
            ++index;
         }
      }

      for(FightEventTeam team : this._teams) {
         for(List<Player> party : this.spreadTeamInPartys(team)) {
            this.createParty(party);
         }
      }
   }

   protected List<List<Player>> spreadTeamInPartys(FightEventTeam team) {
      Map<FightEventManager.CLASSES, List<Player>> classesMap = new HashMap<>();

      for(FightEventManager.CLASSES clazz : FightEventManager.CLASSES.values()) {
         classesMap.put(clazz, new ArrayList<>());
      }

      for(FightEventPlayer iFPlayer : team.getPlayers()) {
         Player player = iFPlayer.getPlayer();
         FightEventManager.CLASSES clazz = FightEventGameRoom.getPlayerClassGroup(player);
         if (clazz != null) {
            classesMap.get(clazz).add(player);
         } else {
            _log.warning("AbstractFightEvent: Problem with add player - " + player.getName());
            _log.warning("AbstractFightEvent: Class - " + player.getClassId().name() + " null for event!");
         }
      }

      int partyCount = (int)Math.ceil((double)(team.getPlayers().size() / 9));
      List<List<Player>> partys = new ArrayList<>();

      for(int i = 0; i < partyCount; ++i) {
         partys.add(new ArrayList<>());
      }

      if (partyCount == 0) {
         return partys;
      } else {
         int finishedOnIndex = 0;

         for(Entry<FightEventManager.CLASSES, List<Player>> clazzEntry : classesMap.entrySet()) {
            for(Player player : clazzEntry.getValue()) {
               partys.get(finishedOnIndex).add(player);
               if (++finishedOnIndex == partyCount) {
                  finishedOnIndex = 0;
               }
            }
         }

         return partys;
      }
   }

   protected void createParty(List<Player> listOfPlayers) {
      if (listOfPlayers.size() > 1) {
         Party newParty = null;

         for(Player player : listOfPlayers) {
            if (player.getParty() != null) {
               player.getParty().removePartyMember(player, Party.messageType.Expelled);
            }

            if (newParty == null) {
               player.setParty(newParty = new Party(player, 4));
            } else {
               player.joinParty(newParty);
            }
         }
      }
   }

   private synchronized void createInstance(List<Integer> doors, Map<String, ZoneType> zones) {
      this._reflectionId = ReflectionManager.getInstance().createReflection();
      Reflection ref = ReflectionManager.getInstance().getReflection(this._reflectionId);
      ref.setPvPInstance(true);
      if (doors != null && !doors.isEmpty()) {
         for(int doorId : doors) {
            ref.addEventDoor(doorId, new StatsSet());
         }
      }
   }

   private Location getSafeLocation(Location[] locations) {
      Location safeLoc = null;
      int checkedCount = 0;
      boolean isOk = false;

      while(!isOk) {
         safeLoc = Rnd.get((Location[])locations);
         isOk = this.nobodyIsClose(safeLoc);
         if (++checkedCount > locations.length * 2) {
            isOk = true;
         }
      }

      return safeLoc;
   }

   protected Location getTeamSpawn(FightEventPlayer fPlayer, boolean randomNotClosestToPt) {
      FightEventTeam team = fPlayer.getTeam();
      Location[] spawnLocs = (Location[])this.getMap().getTeamSpawns().get(team.getIndex());
      if (!randomNotClosestToPt && this._state == AbstractFightEvent.EVENT_STATE.STARTED) {
         List<Player> playersToCheck = new ArrayList<>();
         if (fPlayer.getParty() != null) {
            playersToCheck = fPlayer.getParty().getMembers();
         } else {
            for(FightEventPlayer iFPlayer : team.getPlayers()) {
               playersToCheck.add(iFPlayer.getPlayer());
            }
         }

         Map<Location, Integer> spawnLocations = new HashMap<>(spawnLocs.length);

         for(Location loc : spawnLocs) {
            spawnLocations.put(loc, 0);
         }

         for(Player player : playersToCheck) {
            if (player != null && player.isOnline() && !player.isDead()) {
               Location winner = null;
               double winnerDist = -1.0;

               for(Location loc : spawnLocs) {
                  if (winnerDist <= 0.0 || winnerDist < player.getDistance(loc)) {
                     winner = loc;
                     winnerDist = player.getDistance(loc);
                  }
               }

               if (winner != null) {
                  spawnLocations.put(winner, spawnLocations.get(winner) + 1);
               }
            }
         }

         Location winner = null;
         double points = -1.0;

         for(Entry<Location, Integer> spawn : spawnLocations.entrySet()) {
            if (points < (double)spawn.getValue().intValue()) {
               winner = spawn.getKey();
               points = (double)spawn.getValue().intValue();
            }
         }

         return points <= 0.0 ? Rnd.get((Location[])spawnLocs) : winner;
      } else {
         return Rnd.get((Location[])spawnLocs);
      }
   }

   protected boolean isPlayerActive(Player player) {
      if (player == null) {
         return false;
      } else if (player.isDead()) {
         return false;
      } else if (player.getReflectionId() != this.getReflectionId()) {
         return false;
      } else if (System.currentTimeMillis() - player.getLastNotAfkTime() > 120000L) {
         return false;
      } else {
         boolean insideZone = false;

         for(ZoneType zone : this._activeZones.values()) {
            if (zone.isInsideZone(player.getX(), player.getY(), player.getZ())) {
               insideZone = true;
            }
         }

         return insideZone;
      }
   }

   private void giveRewards(FightEventPlayer[] topKillers) {
      for(FightEventPlayer iFPlayer : this.getPlayers("fighting_players")) {
         if (iFPlayer != null) {
            iFPlayer.getPlayer().getCounters().addAchivementInfo("eventsParticipate", 0, -1L, false, false, false);
            this.rewardPlayer(iFPlayer, Util.arrayContains(topKillers, iFPlayer));
            if (Config.ALLOW_DAILY_TASKS && iFPlayer.getPlayer().getActiveDailyTasks() != null) {
               for(PlayerTaskTemplate taskTemplate : iFPlayer.getPlayer().getActiveDailyTasks()) {
                  if (taskTemplate.getType().equalsIgnoreCase("Event") && !taskTemplate.isComplete()) {
                     DailyTaskTemplate task = DailyTaskManager.getInstance().getDailyTask(taskTemplate.getId());
                     if (taskTemplate.getCurrentEventsCount() < task.getEventsCount()) {
                        taskTemplate.setCurrentEventsCount(taskTemplate.getCurrentEventsCount() + 1);
                        if (taskTemplate.isComplete()) {
                           IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getHandler("missions");
                           if (vch != null) {
                              iFPlayer.getPlayer().updateDailyStatus(taskTemplate);
                              vch.useVoicedCommand("missions", iFPlayer.getPlayer(), null);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void showLastAFkMessage() {
      for(FightEventPlayer iFPlayer : this.getPlayers("fighting_players")) {
         int minutesAFK = (int)Math.round((double)iFPlayer.getTotalAfkSeconds() / 60.0);
         int badgesDecreased = -minutesAFK * ITEMS_FOR_MINUTE_OF_AFK;
         if (badgesDecreased > 0) {
            ServerMessage msg = new ServerMessage("FightEvents.DECREASED", iFPlayer.getPlayer().getLang());
            msg.add(badgesDecreased);
            this.sendMessageToPlayer(iFPlayer.getPlayer(), AbstractFightEvent.MESSAGE_TYPES.NORMAL_MESSAGE, msg);
         }
      }
   }

   private Map<String, Integer> getBestScores() {
      if (!this._scoredUpdated) {
         return this._bestScores;
      } else if (this._scores.isEmpty()) {
         return null;
      } else {
         List<Integer> points = new ArrayList<>(this._scores.values());
         Collections.sort(points);
         Collections.reverse(points);
         int cap;
         if (points.size() <= 26) {
            cap = points.get(points.size() - 1);
         } else {
            cap = points.get(25);
         }

         Map<String, Integer> finalResult = new LinkedHashMap<>();
         List<Entry<String, Integer>> toAdd = new ArrayList<>();

         for(Entry<String, Integer> i : this._scores.entrySet()) {
            if (i.getValue() > cap && finalResult.size() < 25) {
               toAdd.add(i);
            }
         }

         if (finalResult.size() < 25) {
            for(Entry<String, Integer> i : this._scores.entrySet()) {
               if (i.getValue() == cap) {
                  toAdd.add(i);
                  if (finalResult.size() == 25) {
                     break;
                  }
               }
            }
         }

         for(int i = 0; i < toAdd.size(); ++i) {
            Entry<String, Integer> biggestEntry = null;

            for(Entry<String, Integer> entry : toAdd) {
               if (!finalResult.containsKey(entry.getKey()) && (biggestEntry == null || entry.getValue() > biggestEntry.getValue())) {
                  biggestEntry = entry;
               }
            }

            if (biggestEntry != null) {
               finalResult.put(biggestEntry.getKey(), biggestEntry.getValue());
            }
         }

         this._bestScores = finalResult;
         this._scoredUpdated = false;
         return finalResult;
      }
   }

   private void updateEveryScore() {
      for(FightEventPlayer iFPlayer : this.getPlayers("fighting_players")) {
         this._scores.put(this.getScorePlayerName(iFPlayer), iFPlayer.getKills());
         this._scoredUpdated = true;
      }
   }

   private String getScreenScores(Player player, boolean showScoreNotKills, boolean teamPointsNotInvidual) {
      String msg = "";
      if (this.isTeamed() && teamPointsNotInvidual) {
         List<FightEventTeam> teams = this.getTeams();
         Collections.sort(teams, new AbstractFightEvent.BestTeamComparator(showScoreNotKills));

         for(FightEventTeam team : teams) {
            msg = msg
               + ServerStorage.getInstance().getString(player.getLang(), "FightEvents." + team.getName() + "")
               + ": "
               + (showScoreNotKills ? team.getScore() : this.getTeamTotalKills(team))
               + " "
               + (
                  showScoreNotKills
                     ? "" + ServerStorage.getInstance().getString(player.getLang(), "FightEvents.POINTS") + ""
                     : "" + ServerStorage.getInstance().getString(player.getLang(), "FightEvents.KILLS") + ""
               )
               + "\n";
         }
      } else {
         List<FightEventPlayer> fPlayers = this.getPlayers("fighting_players");
         List<FightEventPlayer> changedFPlayers = new ArrayList<>(fPlayers.size());
         changedFPlayers.addAll(fPlayers);
         Collections.sort(changedFPlayers, new AbstractFightEvent.BestPlayerComparator(showScoreNotKills));
         int max = Math.min(10, changedFPlayers.size());

         for(int i = 0; i < max; ++i) {
            msg = msg
               + changedFPlayers.get(i).getPlayer().getName()
               + " "
               + (
                  showScoreNotKills
                     ? "" + ServerStorage.getInstance().getString(player.getLang(), "FightEvents.SCORE") + ""
                     : "" + ServerStorage.getInstance().getString(player.getLang(), "FightEvents.KILLS") + ""
               )
               + ": "
               + (showScoreNotKills ? changedFPlayers.get(i).getScore() : changedFPlayers.get(i).getKills())
               + "\n";
         }
      }

      return msg;
   }

   private boolean nobodyIsClose(Location loc) {
      for(FightEventPlayer iFPlayer : this.getPlayers("fighting_players")) {
         Location playerLoc = iFPlayer.getPlayer().getLocation();
         if (Math.abs(playerLoc.getX() - loc.getX()) <= 150) {
            return false;
         }

         if (Math.abs(playerLoc.getY() - loc.getY()) <= 150) {
            return false;
         }
      }

      return true;
   }

   private void checkIfRegisteredMeetCriteria() {
      for(FightEventPlayer iFPlayer : this.getPlayers("registered_players")) {
         if (iFPlayer != null) {
            this.checkIfRegisteredPlayerMeetCriteria(iFPlayer);
         }
      }
   }

   private boolean checkIfRegisteredPlayerMeetCriteria(FightEventPlayer fPlayer) {
      return FightEventManager.getInstance().canPlayerParticipate(fPlayer.getPlayer(), true, true);
   }

   private FightEventManager.CLASSES[] parseExcludedClasses(String classes) {
      if (classes.equals("")) {
         return new FightEventManager.CLASSES[0];
      } else {
         String[] classType = classes.split(";");
         FightEventManager.CLASSES[] realTypes = new FightEventManager.CLASSES[classType.length];

         for(int i = 0; i < classType.length; ++i) {
            realTypes[i] = FightEventManager.CLASSES.valueOf(classType[i]);
         }

         return realTypes;
      }
   }

   protected int[] parseExcludedSkills(String ids) {
      if (ids != null && !ids.isEmpty()) {
         StringTokenizer st = new StringTokenizer(ids, ";");
         int[] realIds = new int[st.countTokens()];

         for(int index = 0; st.hasMoreTokens(); ++index) {
            realIds[index] = Integer.parseInt(st.nextToken());
         }

         return realIds;
      } else {
         return null;
      }
   }

   protected int[][] parseItemsList(String line) {
      if (line != null && !line.isEmpty()) {
         String[] propertySplit = line.split(";");
         if (propertySplit.length == 0) {
            return (int[][])null;
         } else {
            int i = 0;
            int[][] result = new int[propertySplit.length][];

            for(String value : propertySplit) {
               String[] valueSplit = value.split(",");
               if (valueSplit.length != 2) {
                  _log.warning(
                     StringUtil.concat("parseItemsList[" + this.getNameEn() + "]: invalid entry -> \"", valueSplit[0], "\", should be itemId,itemNumber")
                  );
                  return (int[][])null;
               }

               result[i] = new int[2];

               try {
                  result[i][0] = Integer.parseInt(valueSplit[0]);
               } catch (NumberFormatException var12) {
                  _log.warning(StringUtil.concat("parseItemsList[" + this.getNameEn() + "]: invalid itemId -> \"", valueSplit[0], "\""));
                  return (int[][])null;
               }

               try {
                  result[i][1] = Integer.parseInt(valueSplit[1]);
               } catch (NumberFormatException var11) {
                  _log.warning(StringUtil.concat("parseItemsList[" + this.getNameEn() + "]: invalid item number -> \"", valueSplit[1], "\""));
                  return (int[][])null;
               }

               ++i;
            }

            return result;
         }
      } else {
         return (int[][])null;
      }
   }

   private int[][] parseAutoStartTimes(String times) {
      if (times != null && !times.isEmpty()) {
         StringTokenizer st = new StringTokenizer(times, ",");
         int[][] realTimes = new int[st.countTokens()][2];

         for(int index = 0; st.hasMoreTokens(); ++index) {
            String[] hourMin = st.nextToken().split(":");
            int[] realHourMin = new int[]{Integer.parseInt(hourMin[0]), Integer.parseInt(hourMin[1])};
            realTimes[index] = realHourMin;
         }

         return realTimes;
      } else {
         return (int[][])null;
      }
   }

   private int[][] parseBuffs(String buffs) {
      if (buffs != null && !buffs.isEmpty()) {
         StringTokenizer st = new StringTokenizer(buffs, ";");
         int[][] realBuffs = new int[st.countTokens()][2];

         for(int index = 0; st.hasMoreTokens(); ++index) {
            String[] skillLevel = st.nextToken().split(",");
            int[] realHourMin = new int[]{Integer.parseInt(skillLevel[0]), Integer.parseInt(skillLevel[1])};
            realBuffs[index] = realHourMin;
         }

         return realBuffs;
      } else {
         return (int[][])null;
      }
   }

   private int getTimeToWait(int totalLeftTimeInSeconds) {
      int toWait = 1;
      int[] stops = new int[]{5, 15, 30, 60, 300, 600, 900};

      for(int stop : stops) {
         if (totalLeftTimeInSeconds > stop) {
            toWait = stop;
         }
      }

      return toWait;
   }

   protected boolean isAfkTimerStopped(Player player) {
      return player.isDead() && !this._ressAllowed && this._respawnTime <= 0;
   }

   public static boolean teleportWholeRoomTimer(int eventObjId, int secondsLeft) {
      AbstractFightEvent event = FightEventManager.getInstance().getEventById(eventObjId);
      if (secondsLeft == 0) {
         event._dontLetAnyoneIn = true;
         event.startEvent();
      } else {
         event.checkIfRegisteredMeetCriteria();
         event.sendMessageToRegistered(AbstractFightEvent.MESSAGE_TYPES.SCREEN_BIG, "FightEvents.WILL_TELE", secondsLeft);
      }

      return true;
   }

   public static boolean startRoundTimer(int eventObjId, int secondsLeft) {
      AbstractFightEvent event = FightEventManager.getInstance().getEventById(eventObjId);
      if (secondsLeft > 0) {
         event.sendMessageWithCheckRound(event, AbstractFightEvent.MESSAGE_TYPES.SCREEN_BIG, true, secondsLeft);
      } else {
         event.startRound();
      }

      return true;
   }

   public static boolean endRoundTimer(int eventObjId, int secondsLeft) {
      AbstractFightEvent event = FightEventManager.getInstance().getEventById(eventObjId);
      if (secondsLeft > 0) {
         String msg = !event.isLastRound() ? "FightEvents.GOING_OVER_ROUND" : "FightEvents.GOING_OVER_MATCH";
         event.sendMessageToFighting(AbstractFightEvent.MESSAGE_TYPES.SCREEN_BIG, msg, false, secondsLeft);
      } else {
         event.endRound();
      }

      return true;
   }

   public static boolean shutDownTimer(int eventObjId, int secondsLeft) {
      AbstractFightEvent event = FightEventManager.getInstance().getEventById(eventObjId);
      if (!FightEventManager.getInstance().serverShuttingDown()) {
         event._dontLetAnyoneIn = false;
         return false;
      } else {
         if (secondsLeft < 180 && !event._dontLetAnyoneIn) {
            event.sendMessageToRegistered(AbstractFightEvent.MESSAGE_TYPES.CRITICAL, "FightEvents.NO_LONGER");

            for(FightEventPlayer player : event.getPlayers("registered_players")) {
               event.unregister(player.getPlayer());
            }

            event.getObjects("registered_players").clear();
            event._dontLetAnyoneIn = true;
         }

         if (secondsLeft < 60) {
            event._timer.cancel(false);
            event.sendMessageToFighting(AbstractFightEvent.MESSAGE_TYPES.CRITICAL, "FightEvents.EVENT_END", false);
            event.setState(AbstractFightEvent.EVENT_STATE.OVER);
            event.stopEvent();
            event._dontLetAnyoneIn = false;
            return false;
         } else {
            return true;
         }
      }
   }

   public static boolean teleportBackSinglePlayerTimer(int eventObjId, int secondsLeft, Player player) {
      AbstractFightEvent event = FightEventManager.getInstance().getEventById(eventObjId);
      if (player != null && player.isOnline()) {
         if (secondsLeft > 0) {
            ServerMessage msg = new ServerMessage("FightEvents.TELE_BACK", player.getLang());
            msg.add(getFixedTime(player, secondsLeft).toString());
            event.sendMessageToPlayer(player, AbstractFightEvent.MESSAGE_TYPES.SCREEN_BIG, msg);
         } else {
            event.teleportBackToTown(player);
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean ressurectionTimer(int eventObjId, int secondsLeft, FightEventPlayer fPlayer) {
      AbstractFightEvent event = FightEventManager.getInstance().getEventById(eventObjId);
      Player player = fPlayer.getPlayer();
      if (player != null && player.isOnline() && player.isDead()) {
         if (secondsLeft > 0) {
            ServerMessage msg = new ServerMessage("FightEvents.RESPAWN_IN", player.getLang());
            msg.add(getFixedTime(player, secondsLeft).toString());
            player.sendMessage(msg.toString());
         } else {
            event.hideScores(player);
            event.teleportSinglePlayer(fPlayer, false, true);
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean setInvisible(int eventObjId, int secondsLeft, FightEventPlayer fPlayer, boolean sendMessages) {
      AbstractFightEvent event = FightEventManager.getInstance().getEventById(eventObjId);
      if (fPlayer.getPlayer() != null && fPlayer.getPlayer().isOnline()) {
         if (secondsLeft > 0) {
            if (sendMessages) {
               ServerMessage msg = new ServerMessage("FightEvents.VISIBLE_IN", fPlayer.getPlayer().getLang());
               msg.add(getFixedTime(fPlayer.getPlayer(), secondsLeft).toString());
               event.sendMessageToPlayer(fPlayer.getPlayer(), AbstractFightEvent.MESSAGE_TYPES.SCREEN_BIG, msg);
            }
         } else {
            if (sendMessages && event.getState() == AbstractFightEvent.EVENT_STATE.STARTED) {
               event.sendMessageToPlayer(fPlayer.getPlayer(), AbstractFightEvent.MESSAGE_TYPES.SCREEN_BIG, new ServerMessage("FightEvents.FIGHT", true));
            }

            event.stopInvisibility(fPlayer.getPlayer());
         }

         return true;
      } else {
         return false;
      }
   }

   public void startNewTimer(boolean saveAsMainTimer, int firstWaitingTimeInMilis, String methodName, Object... args) {
      ScheduledFuture<?> timer = ThreadPoolManager.getInstance()
         .schedule(new AbstractFightEvent.SmartTimer(methodName, saveAsMainTimer, args), (long)firstWaitingTimeInMilis);
      if (saveAsMainTimer) {
         this._timer = timer;
      }
   }

   public void onAddEvent(GameObject o) {
      if (o.isPlayer()) {
         o.getActingPlayer().addEventListener(new FightEventListener(o.getActingPlayer()));
      }
   }

   public void onRemoveEvent(GameObject o) {
      if (o.isPlayer()) {
         o.getActingPlayer().removeEventListener(FightEventListener.class);
      }
   }

   public boolean isInProgress() {
      return this._state != AbstractFightEvent.EVENT_STATE.NOT_ACTIVE;
   }

   public <O extends Serializable> List<O> getObjects(String name) {
      List<Serializable> objects = this._objects.get(name);
      return objects == null ? Collections.emptyList() : objects;
   }

   public <O extends Serializable> O getFirstObject(String name) {
      List<O> objects = this.getObjects(name);
      return objects.size() > 0 ? objects.get(0) : null;
   }

   public void addObject(String name, Serializable object) {
      if (object != null) {
         List<Serializable> list = this._objects.get(name);
         if (list != null) {
            list.add(object);
         } else {
            List<Serializable> var4 = new CopyOnWriteArrayList();
            var4.add(object);
            this._objects.put(name, var4);
         }
      }
   }

   public void removeObject(String name, Serializable o) {
      if (o != null) {
         List<Serializable> list = this._objects.get(name);
         if (list != null) {
            list.remove(o);
         }
      }
   }

   public <O extends Serializable> List<O> removeObjects(String name) {
      List<Serializable> objects = this._objects.remove(name);
      return objects == null ? Collections.emptyList() : objects;
   }

   public void addObjects(String name, List<? extends Serializable> objects) {
      if (!objects.isEmpty()) {
         List<Serializable> list = this._objects.get(name);
         if (list != null) {
            list.addAll(objects);
         } else {
            this._objects.put(name, objects);
         }
      }
   }

   public Map<String, List<Serializable>> getObjects() {
      return this._objects;
   }

   public int getId() {
      return this._id;
   }

   public String getNameEn() {
      return this._nameEn;
   }

   public String getNameRu() {
      return this._nameRu;
   }

   public Map<String, ZoneType> getActiveZones() {
      return this._activeZones;
   }

   private static void fightEventListeners(ScriptListener.EventStage stage) {
      if (!_listeners.isEmpty()) {
         switch(stage) {
            case START:
               for(FightEventRegisterListener listener : _listeners) {
                  listener.onBegin();
               }
               break;
            case END:
               for(FightEventRegisterListener listener : _listeners) {
                  listener.onEnd();
               }
         }
      }
   }

   public static void addListener(FightEventRegisterListener listener) {
      if (!_listeners.contains(listener)) {
         _listeners.add(listener);
      }
   }

   public static void removeListener(FightEventRegisterListener listener) {
      _listeners.remove(listener);
   }

   private static class BestPlayerComparator implements Comparator<FightEventPlayer> {
      private final boolean _scoreNotKills;

      private BestPlayerComparator(boolean scoreNotKills) {
         this._scoreNotKills = scoreNotKills;
      }

      public int compare(FightEventPlayer arg0, FightEventPlayer arg1) {
         return this._scoreNotKills ? Integer.compare(arg1.getScore(), arg0.getScore()) : Integer.compare(arg1.getKills(), arg0.getKills());
      }
   }

   private class BestTeamComparator implements Comparator<FightEventTeam>, Serializable {
      private static final long serialVersionUID = -7744947898101934099L;
      private final boolean _scoreNotKills;

      private BestTeamComparator(boolean scoreNotKills) {
         this._scoreNotKills = scoreNotKills;
      }

      public int compare(FightEventTeam o1, FightEventTeam o2) {
         return this._scoreNotKills
            ? Integer.compare(o2.getScore(), o1.getScore())
            : Integer.compare(AbstractFightEvent.this.getTeamTotalKills(o2), AbstractFightEvent.this.getTeamTotalKills(o1));
      }
   }

   private class CheckAfkThread extends RunnableImpl {
      private CheckAfkThread() {
      }

      @Override
      public void runImpl() {
         long currentTime = System.currentTimeMillis();

         for(FightEventPlayer iFPlayer : AbstractFightEvent.this.getPlayers("fighting_players")) {
            Player player = iFPlayer.getPlayer();
            boolean isAfk = player.getLastNotAfkTime() + (long)(AbstractFightEvent.TIME_TO_BE_AFK * 1000) < currentTime;
            if (!AbstractFightEvent.this.isAfkTimerStopped(player)) {
               if (iFPlayer.isAfk()) {
                  if (!isAfk) {
                     AbstractFightEvent.this.handleAfk(iFPlayer, false);
                  } else if (AbstractFightEvent.this._state != AbstractFightEvent.EVENT_STATE.OVER) {
                     AbstractFightEvent.this.sendMessageToPlayer(
                        player, AbstractFightEvent.MESSAGE_TYPES.CRITICAL, new ServerMessage("FightEvents.AFK_MODE", player.getLang())
                     );
                  }
               } else if (AbstractFightEvent.this._state == AbstractFightEvent.EVENT_STATE.NOT_ACTIVE) {
                  AbstractFightEvent.this.handleAfk(iFPlayer, false);
               } else if (isAfk) {
                  AbstractFightEvent.this.handleAfk(iFPlayer, true);
               }
            }
         }

         if (AbstractFightEvent.this.getState() != AbstractFightEvent.EVENT_STATE.NOT_ACTIVE) {
            ThreadPoolManager.getInstance().schedule(this, 1000L);
         } else {
            for(FightEventPlayer iFPlayer : AbstractFightEvent.this.getPlayers("fighting_players")) {
               if (iFPlayer.isAfk()) {
                  AbstractFightEvent.this.handleAfk(iFPlayer, false);
               }
            }
         }
      }
   }

   public static enum EVENT_STATE {
      NOT_ACTIVE,
      COUNT_DOWN,
      PREPARATION,
      STARTED,
      OVER;
   }

   private class LeftZoneThread extends RunnableImpl {
      private LeftZoneThread() {
      }

      @Override
      public void runImpl() {
         List<FightEventPlayer> toDelete = new ArrayList<>();

         for(Entry<FightEventPlayer, ZoneType> entry : AbstractFightEvent.this._leftZone.entrySet()) {
            Player player = entry.getKey().getPlayer();
            if (player != null
               && player.isOnline()
               && AbstractFightEvent.this._state != AbstractFightEvent.EVENT_STATE.NOT_ACTIVE
               && !entry.getValue().isInsideZone((GameObject)player)
               && !player.isDead()
               && !player.isTeleporting()) {
               int power = (int)Math.max(400.0, entry.getValue().getDistanceToZone(player) - 4000.0);
               player.sendPacket(new EarthQuake(player.getX(), player.getY(), player.getZ(), power, 5));
               player.sendPacket(
                  new CreatureSay(
                     0,
                     15,
                     new ServerMessage("FightEvents.ERROR", player.getLang()).toString(),
                     new ServerMessage("FightEvents.BACK_TO_ZONE", player.getLang()).toString()
                  )
               );
               entry.getKey().increaseSecondsOutsideZone();
               if (entry.getKey().getSecondsOutsideZone() >= AbstractFightEvent.TIME_MAX_SECONDS_OUTSIDE_ZONE) {
                  player.doDie(null);
                  toDelete.add(entry.getKey());
                  entry.getKey().clearSecondsOutsideZone();
               }
            } else {
               toDelete.add(entry.getKey());
            }
         }

         for(FightEventPlayer playerToDelete : toDelete) {
            if (playerToDelete != null) {
               AbstractFightEvent.this._leftZone.remove(playerToDelete);
               playerToDelete.clearSecondsOutsideZone();
            }
         }

         if (AbstractFightEvent.this._state != AbstractFightEvent.EVENT_STATE.NOT_ACTIVE) {
            ThreadPoolManager.getInstance().schedule(this, 1000L);
         }
      }
   }

   public static enum MESSAGE_TYPES {
      GM,
      NORMAL_MESSAGE,
      SCREEN_BIG,
      SCREEN_SMALL,
      CRITICAL;
   }

   private class SmartTimer extends RunnableImpl {
      private final String _methodName;
      private final Object[] _args;
      private final boolean _saveAsMain;

      private SmartTimer(String methodName, boolean saveAsMainTimer, Object... args) {
         this._methodName = methodName;
         Object[] changedArgs = new Object[args.length + 1];
         changedArgs[0] = AbstractFightEvent.this.getId();

         for(int i = 0; i < args.length; ++i) {
            changedArgs[i + 1] = args[i];
         }

         this._args = changedArgs;
         this._saveAsMain = saveAsMainTimer;
      }

      @Override
      public void runImpl() {
         Class<?>[] parameterTypes = new Class[this._args.length];

         for(int i = 0; i < this._args.length; ++i) {
            parameterTypes[i] = this._args[i] != null ? this._args[i].getClass() : null;
         }

         int waitingTime = this._args[1];

         try {
            Object ret = MethodUtils.invokeMethod(AbstractFightEvent.this, this._methodName, this._args, parameterTypes);
            if (!(Boolean)ret) {
               return;
            }
         } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException var5) {
            var5.printStackTrace();
         }

         if (waitingTime > 0) {
            int toWait = AbstractFightEvent.this.getTimeToWait(waitingTime);
            waitingTime -= toWait;
            this._args[1] = waitingTime;
            ScheduledFuture<?> timer = ThreadPoolManager.getInstance().schedule(this, (long)(toWait * 1000));
            if (this._saveAsMain) {
               AbstractFightEvent.this._timer = timer;
            }
         }
      }
   }

   private class TimeSpentOnEventThread extends RunnableImpl {
      private TimeSpentOnEventThread() {
      }

      @Override
      public void runImpl() {
         if (AbstractFightEvent.this._state == AbstractFightEvent.EVENT_STATE.STARTED) {
            for(FightEventPlayer iFPlayer : AbstractFightEvent.this.getPlayers("fighting_players")) {
               if (iFPlayer.getPlayer() != null && iFPlayer.getPlayer().isOnline() && !iFPlayer.isAfk()) {
                  iFPlayer.incSecondsSpentOnEvent(10);
               }
            }
         }

         if (AbstractFightEvent.this._state != AbstractFightEvent.EVENT_STATE.NOT_ACTIVE) {
            ThreadPoolManager.getInstance().schedule(AbstractFightEvent.this.new TimeSpentOnEventThread(), 10000L);
         }
      }
   }

   private class ZoneListener implements OnZoneEnterLeaveListener {
      private ZoneListener() {
      }

      @Override
      public void onZoneEnter(ZoneType zone, Creature actor) {
         if (actor.isPlayer()) {
            FightEventPlayer fPlayer = AbstractFightEvent.this.getFightEventPlayer(actor);
            if (fPlayer != null) {
               actor.sendPacket(new EarthQuake(actor.getX(), actor.getY(), actor.getZ(), 0, 1));
               AbstractFightEvent.this._leftZone.remove(AbstractFightEvent.this.getFightEventPlayer(actor));
            }
         }
      }

      @Override
      public void onZoneLeave(ZoneType zone, Creature actor) {
         if (actor.isPlayer() && AbstractFightEvent.this._state != AbstractFightEvent.EVENT_STATE.NOT_ACTIVE) {
            FightEventPlayer fPlayer = AbstractFightEvent.this.getFightEventPlayer(actor);
            if (fPlayer != null) {
               AbstractFightEvent.this._leftZone.put(AbstractFightEvent.this.getFightEventPlayer(actor), zone);
            }
         }
      }
   }
}
