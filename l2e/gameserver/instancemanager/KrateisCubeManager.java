package l2e.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.commons.util.ValueSortMap;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.serverpackets.ExPVPMatchCCMyRecord;
import l2e.gameserver.network.serverpackets.ExPVPMatchCCRecord;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.NpcSay;

public class KrateisCubeManager {
   protected static final Logger _log = Logger.getLogger(KrateisCubeManager.class.getName());
   protected static boolean _started = false;
   protected static boolean _canRegister = true;
   protected static int _rotation = 0;
   protected static int _level = 0;
   protected ScheduledFuture<?> _rotateRoomTask = null;
   protected static int _playersToReward = 0;
   protected static int _matchDuration = 20;
   protected static int _waitingTime = 3;
   protected static Npc _manager;
   protected final List<Npc> _watchers = new ArrayList<>();
   protected final List<Npc> _mobs = new ArrayList<>();
   protected static List<Integer> _tempPlayers = new ArrayList<>();
   protected static List<Integer> _players = new ArrayList<>();
   protected static Map<Integer, Integer> _killList = new HashMap<>();
   public static final KrateisCubeManager.CCPlayer[] krateisScore = new KrateisCubeManager.CCPlayer[25];
   public static String[] scoreboardnames = new String[]{"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
   public static Integer[][] scoreboardkills = new Integer[][]{
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0},
      {0, 0}
   };
   private final List<Spawner> _managers = new ArrayList<>();
   private static final int MANAGER = 32503;
   protected static int _redWatcher = 18601;
   protected static int _blueWatcher = 18602;
   private static int _fantasyCoin = 13067;
   private static int[] _doorlistA = new int[]{
      17150014,
      17150013,
      17150019,
      17150024,
      17150039,
      17150044,
      17150059,
      17150064,
      17150079,
      17150084,
      17150093,
      17150094,
      17150087,
      17150088,
      17150082,
      17150077,
      17150062,
      17150057,
      17150008,
      17150007,
      17150018,
      17150023,
      17150038,
      17150043,
      17150058,
      17150063,
      17150078,
      17150083,
      17150030,
      17150029,
      17150028,
      17150027,
      17150036,
      17150041,
      17150056,
      17150061
   };
   private static int[] _doorlistB = new int[]{
      17150020,
      17150025,
      17150034,
      17150033,
      17150032,
      17150031,
      17150012,
      17150011,
      17150010,
      17150009,
      17150017,
      17150022,
      17150016,
      17150021,
      17150037,
      17150042,
      17150054,
      17150053,
      17150052,
      17150051,
      17150050,
      17150049,
      17150048,
      17150047,
      17150085,
      17150080,
      17150074,
      17150073,
      17150072,
      17150071,
      17150070,
      17150069,
      17150068,
      17150067,
      17150076,
      17150081,
      17150092,
      17150091,
      17150090,
      17150089
   };
   protected static int[] _level70Mobs = new int[]{18587, 18580, 18581, 18584, 18591, 18589, 18583};
   protected static int[] _level76Mobs = new int[]{18590, 18591, 18589, 18585, 18586, 18583, 18592, 18582};
   protected static int[] _level80Mobs = new int[]{18595, 18597, 18596, 18598, 18593, 18600, 18594, 18599};
   protected static int[] _maxlevelMobs = new int[0];
   protected static int[][][] _spawnLocs = new int[][][]{
      {{-77663, -85716, -8365}, {-77701, -85948, -8365}, {-77940, -86090, -8365}, {-78142, -85934, -8365}, {-78180, -85659, -8365}},
      {{-79653, -85689, -8365}, {-79698, -86017, -8365}, {-80003, -86025, -8365}, {-80102, -85880, -8365}, {-80061, -85603, -8365}},
      {{-81556, -85765, -8365}, {-81794, -85528, -8365}, {-82111, -85645, -8365}, {-82044, -85928, -8364}, {-81966, -86116, -8365}},
      {{-83750, -85882, -8365}, {-84079, -86021, -8365}, {-84123, -85663, -8365}, {-83841, -85748, -8364}, {-83951, -86120, -8365}},
      {{-85785, -85943, -8364}, {-86088, -85626, -8365}, {-85698, -85678, -8365}, {-86154, -85879, -8365}, {-85934, -85961, -8365}},
      {{-85935, -84131, -8365}, {-86058, -83921, -8365}, {-85841, -83684, -8364}, {-86082, -83557, -8365}, {-85680, -83816, -8365}},
      {{-84128, -83747, -8365}, {-83877, -83597, -8365}, {-83609, -83946, -8365}, {-83911, -83955, -8364}, {-83817, -83888, -8364}},
      {{-82039, -83971, -8365}, {-81815, -83972, -8365}, {-81774, -83742, -8364}, {-81996, -83733, -8364}, {-82124, -83589, -8365}},
      {{-80098, -83862, -8365}, {-79973, -84058, -8365}, {-79660, -83848, -8365}, {-79915, -83570, -8365}, {-79803, -83832, -8364}},
      {{-78023, -84066, -8365}, {-77869, -83891, -8364}, {-77674, -83757, -8365}, {-77861, -83540, -8365}, {-78107, -83660, -8365}},
      {{-77876, -82141, -8365}, {-77674, -81822, -8365}, {-77885, -81607, -8365}, {-78078, -81779, -8365}, {-78071, -81874, -8365}},
      {{-79740, -81636, -8365}, {-80094, -81713, -8365}, {-80068, -82004, -8365}, {-79677, -81987, -8365}, {-79891, -81734, -8364}},
      {{-81703, -81748, -8365}, {-81857, -81661, -8364}, {-82058, -81863, -8365}, {-81816, -82011, -8365}, {-81600, -81809, -8365}},
      {{-83669, -82007, -8365}, {-83815, -81965, -8365}, {-84121, -81805, -8365}, {-83962, -81626, -8365}, {-83735, -81625, -8365}},
      {{-85708, -81838, -8365}, {-86062, -82009, -8365}, {-86129, -81814, -8365}, {-85957, -81634, -8365}, {-85929, -81460, -8365}},
      {{-86160, -79933, -8365}, {-85766, -80061, -8365}, {-85723, -79691, -8365}, {-85922, -79623, -8365}, {-85941, -79879, -8364}},
      {{-84082, -79638, -8365}, {-83923, -80082, -8365}, {-83687, -79778, -8365}, {-83863, -79619, -8365}, {-83725, -79942, -8365}},
      {{-81963, -80020, -8365}, {-81731, -79707, -8365}, {-81957, -79589, -8365}, {-82151, -79788, -8365}, {-81837, -79868, -8364}},
      {{-80093, -80020, -8365}, {-80160, -79716, -8365}, {-79727, -79699, -8365}, {-79790, -80049, -8365}, {-79942, -79594, -8365}},
      {{-78113, -79658, -8365}, {-77967, -80022, -8365}, {-77692, -79779, -8365}, {-77728, -79603, -8365}, {-78078, -79857, -8365}},
      {{-77648, -77923, -8365}, {-77714, -77742, -8365}, {-78109, -77640, -8365}, {-78114, -77904, -8365}, {-77850, -77816, -8364}},
      {{-79651, -77492, -8365}, {-79989, -77613, -8365}, {-80134, -77981, -8365}, {-79759, -78011, -8365}, {-79644, -77779, -8365}},
      {{-81672, -77966, -8365}, {-81867, -77536, -8365}, {-82129, -77926, -8365}, {-82057, -78064, -8365}, {-82114, -77608, -8365}},
      {{-83938, -77574, -8365}, {-84129, -77924, -8365}, {-83909, -78111, -8365}, {-83652, -78006, -8365}, {-83855, -77756, -8364}},
      {{-85660, -78078, -8365}, {-85842, -77649, -8365}, {-85989, -77556, -8365}, {-86075, -77783, -8365}, {-86074, -78132, -8365}}
   };
   protected static int[][] _teleports = new int[][]{
      {-77906, -85809, -8362},
      {-79903, -85807, -8364},
      {-81904, -85807, -8364},
      {-83901, -85806, -8364},
      {-85903, -85807, -8364},
      {-77904, -83808, -8364},
      {-79904, -83807, -8364},
      {-81905, -83810, -8364},
      {-83903, -83807, -8364},
      {-85899, -83807, -8364},
      {-77903, -81808, -8364},
      {-79906, -81807, -8364},
      {-81901, -81808, -8364},
      {-83905, -81805, -8364},
      {-85907, -81809, -8364},
      {-77904, -79807, -8364},
      {-79905, -79807, -8364},
      {-81908, -79808, -8364},
      {-83907, -79806, -8364},
      {-85912, -79806, -8364},
      {-77905, -77808, -8364},
      {-79902, -77805, -8364},
      {-81904, -77808, -8364},
      {-83904, -77808, -8364},
      {-85904, -77807, -8364}
   };

   public static final KrateisCubeManager getInstance() {
      return KrateisCubeManager.SingletonHolder._instance;
   }

   public void init() {
      Calendar cal = Calendar.getInstance();
      if (cal.get(12) >= 57) {
         cal.add(10, 1);
         cal.set(12, 27);
      } else if (cal.get(12) >= 0 && cal.get(12) <= 26) {
         cal.set(12, 27);
      } else {
         cal.set(12, 57);
      }

      cal.set(13, 0);
      ThreadPoolManager.getInstance()
         .scheduleAtFixedRate(new KrateisCubeManager.checkRegistered(), cal.getTimeInMillis() - System.currentTimeMillis(), 1800000L);
      Date date = new Date(cal.getTimeInMillis());
      this._managers.clear();

      for(Spawner spawn : SpawnParser.getInstance().getSpawnData()) {
         if (spawn != null && spawn.getId() == 32503) {
            this._managers.add(spawn);
         }
      }

      _log.info("Krateis Cube: Initialized, next match starting at " + date);
   }

   protected void ManagerSay(String msg) {
      for(Spawner spawn : this._managers) {
         Npc manager = spawn.getLastSpawn();
         if (manager != null) {
            NpcSay packet = new NpcSay(manager.getObjectId(), 0, manager.getId(), msg);
            manager.broadcastPacket(packet);
         }
      }
   }

   public boolean teleportToWaitRoom() {
      if (_tempPlayers.size() < 1) {
         return false;
      } else {
         for(int i = 0; i <= 24; ++i) {
            scoreboardkills[i][0] = 0;
            scoreboardkills[i][1] = 0;
            scoreboardnames[i] = "";
         }

         int i = 0;

         for(int objectId : _tempPlayers) {
            _players.add(objectId);
            Player player = World.getInstance().getPlayer(objectId);
            if (player != null) {
               this.doTeleport(player, -87028, -81780, -8365);
               player.setIsInKrateisCube(true);
               player.sendPacket(new ExShowScreenMessage("The match will start in 3 minutes.", 15000));
               scoreboardkills[i][0] = objectId;
               scoreboardnames[i] = player.getName();
               ++i;
            }
         }

         _tempPlayers.clear();
         _started = true;
         ThreadPoolManager.getInstance().schedule(new KrateisCubeManager.startKrateisCube(), (long)(_waitingTime * 60000));
         return true;
      }
   }

   protected void rewardPlayers() {
      int kills = 0;
      int i = 0;
      double amount = 0.0;
      _playersToReward = this.getNumberPlayerToReward();
      _killList = ValueSortMap.sortMapByValue(_killList, false);

      for(int objectId : _killList.keySet()) {
         Player player = World.getInstance().getPlayer(objectId);
         if (player != null) {
            kills = _killList.get(objectId);
            if (kills >= 10) {
               amount = this.getRewardAmount(player, i);
               int coinAmount = (int)amount;
               player.addItem("Krateis Cube Reward", _fantasyCoin, (long)coinAmount, player, true);
               player.getInventory().updateDatabase();
               ++i;
            }
         }
      }

      _playersToReward = 0;
   }

   private double getRewardAmount(Player player, int place) {
      int n = Math.round((float)(_playersToReward / 10));
      double reward;
      switch(place) {
         case 0:
            reward = Math.floor((double)(40 + n * 2));
            if (reward > 50.0) {
               reward = 50.0;
            }
            break;
         case 1:
            reward = Math.floor((double)(18 + n * 2));
            if (reward > 20.0) {
               reward = 20.0;
            }
            break;
         default:
            reward = Math.floor((double)(11 + n));
            if (reward > 5.0) {
               reward = 5.0;
            }
      }

      return reward;
   }

   private int getNumberPlayerToReward() {
      int number = 0;
      int kills = 0;

      for(int objectId : _killList.keySet()) {
         kills = _killList.get(objectId);
         if (kills >= 10) {
            ++number;
         }
      }

      return number;
   }

   protected void globalDespawn() {
      for(Npc npc : this._mobs) {
         npc.getSpawn().stopRespawn();
         npc.deleteMe();
      }

      this._mobs.clear();

      for(Npc npc : this._watchers) {
         npc.getSpawn().stopRespawn();
         npc.deleteMe();
      }

      _manager.getSpawn().stopRespawn();
      _manager.deleteMe();
      _manager = null;
      this._watchers.clear();
   }

   protected void openDoors() {
      int[] doorToOpen = _rotation == 1 ? _doorlistB : _doorlistA;
      this.closeAllDoors();

      for(int doorId : doorToOpen) {
         DoorParser.getInstance().getDoor(doorId).openMe();
      }

      ThreadPoolManager.getInstance().schedule(new KrateisCubeManager.CloseDoors(), 25000L);
   }

   protected void closeAllDoors() {
      for(int doorId = 17150001; doorId <= 17150103; ++doorId) {
         DoorParser.getInstance().getDoor(doorId).closeMe();
      }
   }

   protected void doTeleport(Player player, int x, int y, int z) {
      if (player.isOnline()) {
         player.teleToLocation(x, y, z, false);
         Summon pet = player.getSummon();
         if (pet != null) {
            pet.teleToLocation(x, y, z, false);
         }
      }
   }

   protected Spawner spawnNpc(int npcId, int x, int y, int z, int heading, int respawnTime, int instanceId) {
      NpcTemplate npcTemplate = NpcsParser.getInstance().getTemplate(npcId);
      Spawner spawnDat = null;

      try {
         spawnDat = new Spawner(npcTemplate);
         spawnDat.setAmount(1);
         spawnDat.setX(x);
         spawnDat.setY(y);
         spawnDat.setZ(z);
         spawnDat.setHeading(heading);
         spawnDat.setRespawnDelay(respawnTime);
         spawnDat.setReflectionId(instanceId);
         SpawnParser.getInstance().addNewSpawn(spawnDat);
         spawnDat.init();
         spawnDat.startRespawn();
         if (respawnTime == 0) {
            spawnDat.stopRespawn();
         }
      } catch (Exception var11) {
         var11.printStackTrace();
      }

      return spawnDat;
   }

   public boolean registerPlayer(Player player) {
      int objectId = player.getObjectId();
      if (!_tempPlayers.contains(objectId) && _tempPlayers.size() < 25) {
         _tempPlayers.add(objectId);
         return true;
      } else {
         return false;
      }
   }

   public boolean removePlayer(Player player) {
      int objectId = player.getObjectId();
      if (_tempPlayers.contains(objectId)) {
         _tempPlayers.remove(_tempPlayers.indexOf(objectId));
         return true;
      } else {
         return false;
      }
   }

   public void addKill(Player player) {
      this.addKills(player, 1);
      player.sendPacket(new ExPVPMatchCCRecord(1, krateisScore));
   }

   public boolean addKills(Player player, int value) {
      int objectId = player.getObjectId();
      int kills = 0;
      if (_players.contains(objectId)) {
         if (_killList.containsKey(objectId)) {
            kills = _killList.get(objectId);
         }

         kills += value;
         _killList.put(objectId, kills);

         for(int i = 0; i <= 24; ++i) {
            if (scoreboardkills[i][0] == objectId) {
               scoreboardkills[i][1] = kills;
            }
         }

         player.sendPacket(new ExPVPMatchCCMyRecord(kills));
         return true;
      } else {
         return false;
      }
   }

   public int getKills(Player player) {
      int objectId = player.getObjectId();
      int kills = 0;
      if (_players.contains(objectId) && _killList.containsKey(objectId)) {
         kills = _killList.get(objectId);
      }

      return kills;
   }

   public boolean isTimeToRegister() {
      return _canRegister;
   }

   public boolean isRegistered(Player player) {
      int objectId = player.getObjectId();
      return _players.contains(objectId);
   }

   public void teleportPlayerIn(Player player) {
      int i = Rnd.get(_teleports.length);
      this.doTeleport(player, _teleports[i][0], _teleports[i][1], _teleports[i][2]);
   }

   public class CCPlayer {
      private final String _name;
      private int _killPoints;

      public CCPlayer(Player player) {
         this(player.getName());
      }

      private CCPlayer(String name) {
         this._name = name;
         this._killPoints = 0;
      }

      public final String getName() {
         return this._name;
      }

      public final int getPoints() {
         return this._killPoints;
      }

      public final void setPoints(int killPoints) {
         this._killPoints = killPoints;
      }
   }

   protected class CloseDoors implements Runnable {
      @Override
      public void run() {
         KrateisCubeManager.this.closeAllDoors();

         for(Npc npc : KrateisCubeManager.this._watchers) {
            npc.getSpawn().stopRespawn();
            npc.deleteMe();
         }

         KrateisCubeManager.this._watchers.clear();
      }
   }

   private static class SingletonHolder {
      protected static final KrateisCubeManager _instance = new KrateisCubeManager();
   }

   protected class checkRegistered implements Runnable {
      @Override
      public void run() {
         if (!KrateisCubeManager._started) {
            if (KrateisCubeManager._tempPlayers.isEmpty()) {
               KrateisCubeManager._log.info("Krateis Cube: Match canceled due to lack of participant, next round in 30 minutes.");
               KrateisCubeManager.this.ManagerSay("Match canceled due to lack of participants, the next round will start in 30 minutes.");
            } else {
               KrateisCubeManager._log.info("Krateis Cube: Match started.");
               KrateisCubeManager.this.ManagerSay("The match is about to start. Teleporting all participants to the waiting room.");
               KrateisCubeManager._canRegister = false;
               KrateisCubeManager.this.teleportToWaitRoom();
            }
         }
      }
   }

   protected class finishCube implements Runnable {
      @Override
      public void run() {
         KrateisCubeManager._log.info("Krateis Cube match ended.");
         KrateisCubeManager.this.ManagerSay("The match has ended.");
         if (KrateisCubeManager.this._rotateRoomTask != null) {
            KrateisCubeManager.this._rotateRoomTask.cancel(true);
         }

         KrateisCubeManager.this.closeAllDoors();
         KrateisCubeManager.this.globalDespawn();
         KrateisCubeManager.this.rewardPlayers();

         for(int objectId : KrateisCubeManager._players) {
            Player player = World.getInstance().getPlayer(objectId);
            if (player != null) {
               KrateisCubeManager.this.doTeleport(player, -70381, -70937, -1428);
               player.setIsInKrateisCube(false);
               player.sendPacket(new ExPVPMatchCCRecord(2, KrateisCubeManager.krateisScore));
            }
         }

         KrateisCubeManager._killList.clear();
         KrateisCubeManager._players.clear();
         KrateisCubeManager._started = false;
      }
   }

   protected class rotateRooms implements Runnable {
      @Override
      public void run() {
         int instanceId = 0;
         int watcherA = KrateisCubeManager._rotation == 0 ? KrateisCubeManager._blueWatcher : KrateisCubeManager._redWatcher;
         int watcherB = KrateisCubeManager._rotation == 0 ? KrateisCubeManager._redWatcher : KrateisCubeManager._blueWatcher;
         Spawner spawnDat = KrateisCubeManager.this.spawnNpc(watcherA, -77906, -85809, -8362, 34826, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherA, -79903, -85807, -8364, 32652, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherB, -81904, -85807, -8364, 32839, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherA, -83901, -85806, -8364, 33336, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherA, -85903, -85807, -8364, 32571, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherB, -77904, -83808, -8364, 32933, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherA, -79904, -83807, -8364, 33055, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherB, -81905, -83810, -8364, 32767, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherB, -83903, -83807, -8364, 32676, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherB, -85899, -83807, -8364, 33005, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherB, -77903, -81808, -8364, 32664, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherA, -79906, -81807, -8364, 32647, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherB, -81901, -81808, -8364, 33724, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherA, -83905, -81805, -8364, 32926, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherB, -85907, -81809, -8364, 34248, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherB, -77904, -79807, -8364, 32905, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherA, -79905, -79807, -8364, 32767, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherB, -81908, -79808, -8364, 32767, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherA, -83907, -79806, -8364, 32767, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherB, -85912, -79806, -8364, 29025, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherA, -77905, -77808, -8364, 32767, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherA, -79902, -77805, -8364, 32767, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherB, -81904, -77808, -8364, 32478, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherA, -83904, -77808, -8364, 32698, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         spawnDat = KrateisCubeManager.this.spawnNpc(watcherA, -85904, -77807, -8364, 32612, 60, 0);
         KrateisCubeManager.this._watchers.add(spawnDat.doSpawn());
         KrateisCubeManager.this.openDoors();
         KrateisCubeManager._rotation = KrateisCubeManager._rotation == 0 ? 1 : 0;
      }
   }

   protected class spawnMobs implements Runnable {
      @Override
      public void run() {
         int _instanceId = 0;

         for(int i = 0; i <= 24; ++i) {
            for(int j = 0; j <= 4; ++j) {
               int npcId = KrateisCubeManager._maxlevelMobs[Rnd.get(KrateisCubeManager._maxlevelMobs.length)];
               Spawner spawnDat = KrateisCubeManager.this.spawnNpc(
                  npcId, KrateisCubeManager._spawnLocs[i][j][0], KrateisCubeManager._spawnLocs[i][j][1], KrateisCubeManager._spawnLocs[i][j][2], 0, 60, 0
               );
               KrateisCubeManager.this._mobs.add(spawnDat.doSpawn());
            }
         }
      }
   }

   protected class startKrateisCube implements Runnable {
      @Override
      public void run() {
         KrateisCubeManager._canRegister = true;
         KrateisCubeManager.this.closeAllDoors();
         int i = 0;
         int temp = 0;

         for(int objectId : KrateisCubeManager._players) {
            Player player = World.getInstance().getPlayer(objectId);
            if (player != null) {
               KrateisCubeManager.this.doTeleport(
                  player, KrateisCubeManager._teleports[i][0], KrateisCubeManager._teleports[i][1], KrateisCubeManager._teleports[i][2]
               );
               temp = player.getLevel();
               if (KrateisCubeManager._level < temp) {
                  KrateisCubeManager._level = temp;
               }

               ++i;
               player.sendPacket(new ExPVPMatchCCMyRecord(0));
               player.sendPacket(new ExPVPMatchCCRecord(1, KrateisCubeManager.krateisScore));
            }
         }

         if (KrateisCubeManager._level < 75) {
            KrateisCubeManager._maxlevelMobs = KrateisCubeManager._level70Mobs;
         }

         if (KrateisCubeManager._level > 74) {
            KrateisCubeManager._maxlevelMobs = KrateisCubeManager._level76Mobs;
         }

         if (KrateisCubeManager._level > 79) {
            KrateisCubeManager._maxlevelMobs = KrateisCubeManager._level80Mobs;
         }

         Spawner spawnDat = KrateisCubeManager.this.spawnNpc(32504, -86804, -81974, -8361, 34826, 60, 0);
         KrateisCubeManager._manager = spawnDat.doSpawn();
         KrateisCubeManager.this._rotateRoomTask = ThreadPoolManager.getInstance()
            .scheduleAtFixedRate(KrateisCubeManager.this.new rotateRooms(), 10000L, 50000L);
         ThreadPoolManager.getInstance().schedule(KrateisCubeManager.this.new spawnMobs(), 10000L);
         ThreadPoolManager.getInstance().schedule(KrateisCubeManager.this.new finishCube(), (long)(KrateisCubeManager._matchDuration * 60000));
      }
   }
}
