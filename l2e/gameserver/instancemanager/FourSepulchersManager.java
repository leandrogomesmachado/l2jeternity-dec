package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.tasks.FourSepulchersChangeAttackTimeTask;
import l2e.gameserver.instancemanager.tasks.FourSepulchersChangeCoolDownTimeTask;
import l2e.gameserver.instancemanager.tasks.FourSepulchersChangeEntryTimeTask;
import l2e.gameserver.instancemanager.tasks.FourSepulchersChangeWarmUpTimeTask;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.instance.SepulcherMonsterInstance;
import l2e.gameserver.model.actor.instance.SepulcherNpcInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public final class FourSepulchersManager {
   private static final Logger _log = Logger.getLogger(FourSepulchersManager.class.getName());
   private static final int QUEST_ID = 620;
   private static final int ENTRANCE_PASS = 7075;
   private static final int USED_PASS = 7261;
   private static final int CHAPEL_KEY = 7260;
   private static final int ANTIQUE_BROOCH = 7262;
   private boolean _firstTimeRun;
   private boolean _inEntryTime = false;
   private boolean _inWarmUpTime = false;
   private boolean _inAttackTime = false;
   private boolean _inCoolDownTime = false;
   private ScheduledFuture<?> _changeCoolDownTimeTask = null;
   private ScheduledFuture<?> _changeEntryTimeTask = null;
   private ScheduledFuture<?> _changeWarmUpTimeTask = null;
   private ScheduledFuture<?> _changeAttackTimeTask = null;
   private final int[][] _startHallSpawn = new int[][]{{181632, -85587, -7218}, {179963, -88978, -7218}, {173217, -86132, -7218}, {175608, -82296, -7218}};
   private final int[][][] _shadowSpawnLoc = new int[][][]{
      {
            {25339, 191231, -85574, -7216, 33380},
            {25349, 189534, -88969, -7216, 32768},
            {25346, 173195, -76560, -7215, 49277},
            {25342, 175591, -72744, -7215, 49317}
      },
      {
            {25342, 191231, -85574, -7216, 33380},
            {25339, 189534, -88969, -7216, 32768},
            {25349, 173195, -76560, -7215, 49277},
            {25346, 175591, -72744, -7215, 49317}
      },
      {
            {25346, 191231, -85574, -7216, 33380},
            {25342, 189534, -88969, -7216, 32768},
            {25339, 173195, -76560, -7215, 49277},
            {25349, 175591, -72744, -7215, 49317}
      },
      {
            {25349, 191231, -85574, -7216, 33380},
            {25346, 189534, -88969, -7216, 32768},
            {25342, 173195, -76560, -7215, 49277},
            {25339, 175591, -72744, -7215, 49317}
      }
   };
   protected Map<Integer, Boolean> _archonSpawned = new ConcurrentHashMap<>();
   protected Map<Integer, Boolean> _hallInUse = new ConcurrentHashMap<>();
   protected Map<Integer, Player> _challengers = new ConcurrentHashMap<>();
   protected Map<Integer, int[]> _startHallSpawns = new HashMap<>();
   protected Map<Integer, Integer> _hallGateKeepers = new HashMap<>();
   protected Map<Integer, Integer> _keyBoxNpc = new HashMap<>();
   protected Map<Integer, Integer> _victim = new HashMap<>();
   protected Map<Integer, Spawner> _executionerSpawns = new HashMap<>();
   protected Map<Integer, Spawner> _keyBoxSpawns = new HashMap<>();
   protected Map<Integer, Spawner> _mysteriousBoxSpawns = new HashMap<>();
   protected Map<Integer, Spawner> _shadowSpawns = new HashMap<>();
   protected Map<Integer, List<Spawner>> _dukeFinalMobs = new HashMap<>();
   protected Map<Integer, List<SepulcherMonsterInstance>> _dukeMobs = new HashMap<>();
   protected Map<Integer, List<Spawner>> _emperorsGraveNpcs = new HashMap<>();
   protected Map<Integer, List<Spawner>> _magicalMonsters = new HashMap<>();
   protected Map<Integer, List<Spawner>> _physicalMonsters = new HashMap<>();
   protected Map<Integer, List<SepulcherMonsterInstance>> _viscountMobs = new HashMap<>();
   protected List<Spawner> _physicalSpawns;
   protected List<Spawner> _magicalSpawns;
   protected List<Spawner> _managers = new CopyOnWriteArrayList<>();
   protected List<Spawner> _dukeFinalSpawns;
   protected List<Spawner> _emperorsGraveSpawns;
   protected List<Npc> _allMobs = new CopyOnWriteArrayList<>();
   private long _attackTimeEnd = 0L;
   private long _coolDownTimeEnd = 0L;
   private long _entryTimeEnd = 0L;
   private long _warmUpTimeEnd = 0L;
   private final byte _newCycleMin = 55;

   public void init() {
      if (this._changeCoolDownTimeTask != null) {
         this._changeCoolDownTimeTask.cancel(true);
      }

      if (this._changeEntryTimeTask != null) {
         this._changeEntryTimeTask.cancel(true);
      }

      if (this._changeWarmUpTimeTask != null) {
         this._changeWarmUpTimeTask.cancel(true);
      }

      if (this._changeAttackTimeTask != null) {
         this._changeAttackTimeTask.cancel(true);
      }

      this._changeCoolDownTimeTask = null;
      this._changeEntryTimeTask = null;
      this._changeWarmUpTimeTask = null;
      this._changeAttackTimeTask = null;
      this._inEntryTime = false;
      this._inWarmUpTime = false;
      this._inAttackTime = false;
      this._inCoolDownTime = false;
      this._firstTimeRun = true;
      this.initFixedInfo();
      this.loadMysteriousBox();
      this.initKeyBoxSpawns();
      this.loadPhysicalMonsters();
      this.loadMagicalMonsters();
      this.initLocationShadowSpawns();
      this.initExecutionerSpawns();
      this.loadDukeMonsters();
      this.loadEmperorsGraveMonsters();
      this.timeSelector();
      this.spawnManagers();
      _log.info(this.getClass().getSimpleName() + ": Loaded all functions.");
   }

   protected void timeSelector() {
      this.timeCalculator();
      long currentTime = Calendar.getInstance().getTimeInMillis();
      if (currentTime >= this._coolDownTimeEnd && currentTime < this._entryTimeEnd) {
         this.clean();
         this._changeEntryTimeTask = ThreadPoolManager.getInstance().schedule(new FourSepulchersChangeEntryTimeTask(), 0L);
         if (Config.DEBUG) {
            _log.info(this.getClass().getSimpleName() + ": Beginning in Entry time");
         }
      } else if (currentTime >= this._entryTimeEnd && currentTime < this._warmUpTimeEnd) {
         this.clean();
         this._changeWarmUpTimeTask = ThreadPoolManager.getInstance().schedule(new FourSepulchersChangeWarmUpTimeTask(), 0L);
         if (Config.DEBUG) {
            _log.info(this.getClass().getSimpleName() + ": Beginning in WarmUp time");
         }
      } else if (currentTime >= this._warmUpTimeEnd && currentTime < this._attackTimeEnd) {
         this.clean();
         this._changeAttackTimeTask = ThreadPoolManager.getInstance().schedule(new FourSepulchersChangeAttackTimeTask(), 0L);
         if (Config.DEBUG) {
            _log.info(this.getClass().getSimpleName() + ": Beginning in Attack time");
         }
      } else {
         this._changeCoolDownTimeTask = ThreadPoolManager.getInstance().schedule(new FourSepulchersChangeCoolDownTimeTask(), 0L);
         if (Config.DEBUG) {
            _log.info(this.getClass().getSimpleName() + ": Beginning in Cooldown time");
         }
      }
   }

   protected void timeCalculator() {
      Calendar tmp = Calendar.getInstance();
      if (tmp.get(12) < 55) {
         tmp.set(10, Calendar.getInstance().get(10) - 1);
      }

      tmp.set(12, 55);
      this._coolDownTimeEnd = tmp.getTimeInMillis();
      this._entryTimeEnd = this._coolDownTimeEnd + (long)Config.FS_TIME_ENTRY * 60000L;
      this._warmUpTimeEnd = this._entryTimeEnd + (long)Config.FS_TIME_WARMUP * 60000L;
      this._attackTimeEnd = this._warmUpTimeEnd + (long)Config.FS_TIME_ATTACK * 60000L;
   }

   public void clean() {
      for(int i = 31921; i < 31925; ++i) {
         int[] Location = (int[])this._startHallSpawns.get(i);
         EpicBossManager.getInstance().getZone(Location[0], Location[1], Location[2]).oustAllPlayers();
      }

      this.deleteAllMobs();
      this.closeAllDoors();
      this._hallInUse.clear();
      this._hallInUse.put(31921, false);
      this._hallInUse.put(31922, false);
      this._hallInUse.put(31923, false);
      this._hallInUse.put(31924, false);
      if (this._archonSpawned.size() != 0) {
         for(int npcId : this._archonSpawned.keySet()) {
            this._archonSpawned.put(npcId, false);
         }
      }
   }

   protected void spawnManagers() {
      for(int i = 31921; i <= 31924; ++i) {
         if (i >= 31921 && i <= 31924) {
            NpcTemplate template1 = NpcsParser.getInstance().getTemplate(i);
            if (template1 != null) {
               try {
                  Spawner spawnDat = new Spawner(template1);
                  spawnDat.setAmount(1);
                  spawnDat.setRespawnDelay(60);
                  switch(i) {
                     case 31921:
                        spawnDat.setX(181061);
                        spawnDat.setY(-85595);
                        spawnDat.setZ(-7200);
                        spawnDat.setHeading(-32584);
                        break;
                     case 31922:
                        spawnDat.setX(179292);
                        spawnDat.setY(-88981);
                        spawnDat.setZ(-7200);
                        spawnDat.setHeading(-33272);
                        break;
                     case 31923:
                        spawnDat.setX(173202);
                        spawnDat.setY(-87004);
                        spawnDat.setZ(-7200);
                        spawnDat.setHeading(-16248);
                        break;
                     case 31924:
                        spawnDat.setX(175606);
                        spawnDat.setY(-82853);
                        spawnDat.setZ(-7200);
                        spawnDat.setHeading(-16248);
                  }

                  this._managers.add(spawnDat);
                  SpawnParser.getInstance().addNewSpawn(spawnDat);
                  spawnDat.doSpawn();
                  spawnDat.startRespawn();
                  if (Config.DEBUG) {
                     _log.info(this.getClass().getSimpleName() + ": Spawned " + spawnDat.getTemplate().getName());
                  }
               } catch (Exception var5) {
                  _log.log(Level.WARNING, "Error while spawning managers: " + var5.getMessage(), (Throwable)var5);
               }
            }
         }
      }
   }

   protected void initFixedInfo() {
      this._startHallSpawns.put(31921, this._startHallSpawn[0]);
      this._startHallSpawns.put(31922, this._startHallSpawn[1]);
      this._startHallSpawns.put(31923, this._startHallSpawn[2]);
      this._startHallSpawns.put(31924, this._startHallSpawn[3]);
      this._hallInUse.put(31921, false);
      this._hallInUse.put(31922, false);
      this._hallInUse.put(31923, false);
      this._hallInUse.put(31924, false);
      this._hallGateKeepers.put(31925, 25150012);
      this._hallGateKeepers.put(31926, 25150013);
      this._hallGateKeepers.put(31927, 25150014);
      this._hallGateKeepers.put(31928, 25150015);
      this._hallGateKeepers.put(31929, 25150016);
      this._hallGateKeepers.put(31930, 25150002);
      this._hallGateKeepers.put(31931, 25150003);
      this._hallGateKeepers.put(31932, 25150004);
      this._hallGateKeepers.put(31933, 25150005);
      this._hallGateKeepers.put(31934, 25150006);
      this._hallGateKeepers.put(31935, 25150032);
      this._hallGateKeepers.put(31936, 25150033);
      this._hallGateKeepers.put(31937, 25150034);
      this._hallGateKeepers.put(31938, 25150035);
      this._hallGateKeepers.put(31939, 25150036);
      this._hallGateKeepers.put(31940, 25150022);
      this._hallGateKeepers.put(31941, 25150023);
      this._hallGateKeepers.put(31942, 25150024);
      this._hallGateKeepers.put(31943, 25150025);
      this._hallGateKeepers.put(31944, 25150026);
      this._keyBoxNpc.put(18120, 31455);
      this._keyBoxNpc.put(18121, 31455);
      this._keyBoxNpc.put(18122, 31455);
      this._keyBoxNpc.put(18123, 31455);
      this._keyBoxNpc.put(18124, 31456);
      this._keyBoxNpc.put(18125, 31456);
      this._keyBoxNpc.put(18126, 31456);
      this._keyBoxNpc.put(18127, 31456);
      this._keyBoxNpc.put(18128, 31457);
      this._keyBoxNpc.put(18129, 31457);
      this._keyBoxNpc.put(18130, 31457);
      this._keyBoxNpc.put(18131, 31457);
      this._keyBoxNpc.put(18149, 31458);
      this._keyBoxNpc.put(18150, 31459);
      this._keyBoxNpc.put(18151, 31459);
      this._keyBoxNpc.put(18152, 31459);
      this._keyBoxNpc.put(18153, 31459);
      this._keyBoxNpc.put(18154, 31460);
      this._keyBoxNpc.put(18155, 31460);
      this._keyBoxNpc.put(18156, 31460);
      this._keyBoxNpc.put(18157, 31460);
      this._keyBoxNpc.put(18158, 31461);
      this._keyBoxNpc.put(18159, 31461);
      this._keyBoxNpc.put(18160, 31461);
      this._keyBoxNpc.put(18161, 31461);
      this._keyBoxNpc.put(18162, 31462);
      this._keyBoxNpc.put(18163, 31462);
      this._keyBoxNpc.put(18164, 31462);
      this._keyBoxNpc.put(18165, 31462);
      this._keyBoxNpc.put(18183, 31463);
      this._keyBoxNpc.put(18184, 31464);
      this._keyBoxNpc.put(18212, 31465);
      this._keyBoxNpc.put(18213, 31465);
      this._keyBoxNpc.put(18214, 31465);
      this._keyBoxNpc.put(18215, 31465);
      this._keyBoxNpc.put(18216, 31466);
      this._keyBoxNpc.put(18217, 31466);
      this._keyBoxNpc.put(18218, 31466);
      this._keyBoxNpc.put(18219, 31466);
      this._victim.put(18150, 18158);
      this._victim.put(18151, 18159);
      this._victim.put(18152, 18160);
      this._victim.put(18153, 18161);
      this._victim.put(18154, 18162);
      this._victim.put(18155, 18163);
      this._victim.put(18156, 18164);
      this._victim.put(18157, 18165);
   }

   private void loadMysteriousBox() {
      this._mysteriousBoxSpawns.clear();

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement(
            "SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY id"
         );
         statement.setInt(1, 0);
         ResultSet rset = statement.executeQuery();

         while(rset.next()) {
            NpcTemplate template1 = NpcsParser.getInstance().getTemplate(rset.getInt("npc_templateid"));
            if (template1 != null) {
               Spawner spawnDat = new Spawner(template1);
               spawnDat.setAmount(rset.getInt("count"));
               spawnDat.setX(rset.getInt("locx"));
               spawnDat.setY(rset.getInt("locy"));
               spawnDat.setZ(rset.getInt("locz"));
               spawnDat.setHeading(rset.getInt("heading"));
               spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
               SpawnParser.getInstance().addNewSpawn(spawnDat);
               int keyNpcId = rset.getInt("key_npc_id");
               this._mysteriousBoxSpawns.put(keyNpcId, spawnDat);
            } else {
               _log.warning(this.getClass().getSimpleName() + ": Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
            }
         }

         rset.close();
         statement.close();
         if (Config.DEBUG) {
            _log.info(this.getClass().getSimpleName() + ": Loaded " + this._mysteriousBoxSpawns.size() + " Mysterious-Box spawns.");
         }
      } catch (Exception var18) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Spawn could not be initialized: " + var18.getMessage(), (Throwable)var18);
      }
   }

   private void initKeyBoxSpawns() {
      for(Entry<Integer, Integer> keyNpc : this._keyBoxNpc.entrySet()) {
         try {
            NpcTemplate template = NpcsParser.getInstance().getTemplate(keyNpc.getValue());
            if (template != null) {
               Spawner spawnDat = new Spawner(template);
               spawnDat.setAmount(1);
               spawnDat.setX(0);
               spawnDat.setY(0);
               spawnDat.setZ(0);
               spawnDat.setHeading(0);
               spawnDat.setRespawnDelay(3600);
               SpawnParser.getInstance().addNewSpawn(spawnDat);
               this._keyBoxSpawns.put(keyNpc.getKey(), spawnDat);
            } else {
               _log.warning(this.getClass().getSimpleName() + ": Data missing in NPC table for ID: " + keyNpc.getValue() + ".");
            }
         } catch (Exception var6) {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Spawn could not be initialized: " + var6.getMessage(), (Throwable)var6);
         }
      }
   }

   private void loadPhysicalMonsters() {
      this._physicalMonsters.clear();
      int loaded = 0;

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement1 = con.prepareStatement(
            "SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id"
         );
         statement1.setInt(1, 1);
         ResultSet rset1 = statement1.executeQuery();
         PreparedStatement statement2 = con.prepareStatement(
            "SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id"
         );

         while(rset1.next()) {
            int keyNpcId = rset1.getInt("key_npc_id");
            statement2.setInt(1, keyNpcId);
            statement2.setInt(2, 1);
            ResultSet rset2 = statement2.executeQuery();
            statement2.clearParameters();
            this._physicalSpawns = new ArrayList<>();

            while(rset2.next()) {
               NpcTemplate template1 = NpcsParser.getInstance().getTemplate(rset2.getInt("npc_templateid"));
               if (template1 != null) {
                  Spawner spawnDat = new Spawner(template1);
                  spawnDat.setAmount(rset2.getInt("count"));
                  spawnDat.setX(rset2.getInt("locx"));
                  spawnDat.setY(rset2.getInt("locy"));
                  spawnDat.setZ(rset2.getInt("locz"));
                  spawnDat.setHeading(rset2.getInt("heading"));
                  spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
                  SpawnParser.getInstance().addNewSpawn(spawnDat);
                  this._physicalSpawns.add(spawnDat);
                  ++loaded;
               } else {
                  _log.warning(this.getClass().getSimpleName() + ": Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
               }
            }

            rset2.close();
            this._physicalMonsters.put(keyNpcId, this._physicalSpawns);
         }

         rset1.close();
         statement1.close();
         statement2.close();
         if (Config.DEBUG) {
            _log.info(this.getClass().getSimpleName() + ": Loaded " + loaded + " Physical type monsters spawns.");
         }
      } catch (Exception var21) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Spawn could not be initialized: " + var21.getMessage(), (Throwable)var21);
      }
   }

   private void loadMagicalMonsters() {
      this._magicalMonsters.clear();
      int loaded = 0;

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement1 = con.prepareStatement(
            "SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id"
         );
         statement1.setInt(1, 2);
         ResultSet rset1 = statement1.executeQuery();
         PreparedStatement statement2 = con.prepareStatement(
            "SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist WHERE key_npc_id = ? AND spawntype = ? ORDER BY id"
         );

         while(rset1.next()) {
            int keyNpcId = rset1.getInt("key_npc_id");
            statement2.setInt(1, keyNpcId);
            statement2.setInt(2, 2);
            ResultSet rset2 = statement2.executeQuery();
            statement2.clearParameters();
            this._magicalSpawns = new ArrayList<>();

            while(rset2.next()) {
               NpcTemplate template1 = NpcsParser.getInstance().getTemplate(rset2.getInt("npc_templateid"));
               if (template1 != null) {
                  Spawner spawnDat = new Spawner(template1);
                  spawnDat.setAmount(rset2.getInt("count"));
                  spawnDat.setX(rset2.getInt("locx"));
                  spawnDat.setY(rset2.getInt("locy"));
                  spawnDat.setZ(rset2.getInt("locz"));
                  spawnDat.setHeading(rset2.getInt("heading"));
                  spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
                  SpawnParser.getInstance().addNewSpawn(spawnDat);
                  this._magicalSpawns.add(spawnDat);
                  ++loaded;
               } else {
                  _log.warning(this.getClass().getSimpleName() + ": Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
               }
            }

            rset2.close();
            this._magicalMonsters.put(keyNpcId, this._magicalSpawns);
         }

         rset1.close();
         statement1.close();
         statement2.close();
         if (Config.DEBUG) {
            _log.info(this.getClass().getSimpleName() + ": Loaded " + loaded + " Magical type monsters spawns.");
         }
      } catch (Exception var21) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Spawn could not be initialized: " + var21.getMessage(), (Throwable)var21);
      }
   }

   private void loadDukeMonsters() {
      this._dukeFinalMobs.clear();
      this._archonSpawned.clear();
      int loaded = 0;

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement1 = con.prepareStatement(
            "SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id"
         );
         statement1.setInt(1, 5);
         ResultSet rset1 = statement1.executeQuery();
         PreparedStatement statement2 = con.prepareStatement(
            "SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist WHERE key_npc_id = ? AND spawntype = ? ORDER BY id"
         );

         while(rset1.next()) {
            int keyNpcId = rset1.getInt("key_npc_id");
            statement2.setInt(1, keyNpcId);
            statement2.setInt(2, 5);
            ResultSet rset2 = statement2.executeQuery();
            statement2.clearParameters();
            this._dukeFinalSpawns = new ArrayList<>();

            while(rset2.next()) {
               NpcTemplate template1 = NpcsParser.getInstance().getTemplate(rset2.getInt("npc_templateid"));
               if (template1 != null) {
                  Spawner spawnDat = new Spawner(template1);
                  spawnDat.setAmount(rset2.getInt("count"));
                  spawnDat.setX(rset2.getInt("locx"));
                  spawnDat.setY(rset2.getInt("locy"));
                  spawnDat.setZ(rset2.getInt("locz"));
                  spawnDat.setHeading(rset2.getInt("heading"));
                  spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
                  SpawnParser.getInstance().addNewSpawn(spawnDat);
                  this._dukeFinalSpawns.add(spawnDat);
                  ++loaded;
               } else {
                  _log.warning(this.getClass().getSimpleName() + ": Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
               }
            }

            rset2.close();
            this._dukeFinalMobs.put(keyNpcId, this._dukeFinalSpawns);
            this._archonSpawned.put(keyNpcId, false);
         }

         rset1.close();
         statement1.close();
         statement2.close();
         if (Config.DEBUG) {
            _log.info(this.getClass().getSimpleName() + ": Loaded " + loaded + " Church of duke monsters spawns.");
         }
      } catch (Exception var21) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Spawn could not be initialized: " + var21.getMessage(), (Throwable)var21);
      }
   }

   private void loadEmperorsGraveMonsters() {
      this._emperorsGraveNpcs.clear();
      int loaded = 0;

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement1 = con.prepareStatement(
            "SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id"
         );
         statement1.setInt(1, 6);
         ResultSet rset1 = statement1.executeQuery();
         PreparedStatement statement2 = con.prepareStatement(
            "SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist WHERE key_npc_id = ? and spawntype = ? ORDER BY id"
         );

         while(rset1.next()) {
            int keyNpcId = rset1.getInt("key_npc_id");
            statement2.setInt(1, keyNpcId);
            statement2.setInt(2, 6);
            ResultSet rset2 = statement2.executeQuery();
            statement2.clearParameters();
            this._emperorsGraveSpawns = new ArrayList<>();

            while(rset2.next()) {
               NpcTemplate template1 = NpcsParser.getInstance().getTemplate(rset2.getInt("npc_templateid"));
               if (template1 != null) {
                  Spawner spawnDat = new Spawner(template1);
                  spawnDat.setAmount(rset2.getInt("count"));
                  spawnDat.setX(rset2.getInt("locx"));
                  spawnDat.setY(rset2.getInt("locy"));
                  spawnDat.setZ(rset2.getInt("locz"));
                  spawnDat.setHeading(rset2.getInt("heading"));
                  spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
                  SpawnParser.getInstance().addNewSpawn(spawnDat);
                  this._emperorsGraveSpawns.add(spawnDat);
                  ++loaded;
               } else {
                  _log.warning(this.getClass().getSimpleName() + ": Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
               }
            }

            rset2.close();
            this._emperorsGraveNpcs.put(keyNpcId, this._emperorsGraveSpawns);
         }

         rset1.close();
         statement1.close();
         statement2.close();
         if (Config.DEBUG) {
            _log.info(this.getClass().getSimpleName() + ": Loaded " + loaded + " Emperor's grave NPC spawns.");
         }
      } catch (Exception var21) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Spawn could not be initialized: " + var21.getMessage(), (Throwable)var21);
      }
   }

   protected void initLocationShadowSpawns() {
      int locNo = Rnd.get(4);
      int[] gateKeeper = new int[]{31929, 31934, 31939, 31944};
      this._shadowSpawns.clear();

      for(int i = 0; i <= 3; ++i) {
         NpcTemplate template = NpcsParser.getInstance().getTemplate(this._shadowSpawnLoc[locNo][i][0]);
         if (template != null) {
            try {
               Spawner spawnDat = new Spawner(template);
               spawnDat.setAmount(1);
               spawnDat.setX(this._shadowSpawnLoc[locNo][i][1]);
               spawnDat.setY(this._shadowSpawnLoc[locNo][i][2]);
               spawnDat.setZ(this._shadowSpawnLoc[locNo][i][3]);
               spawnDat.setHeading(this._shadowSpawnLoc[locNo][i][4]);
               SpawnParser.getInstance().addNewSpawn(spawnDat);
               int keyNpcId = gateKeeper[i];
               this._shadowSpawns.put(keyNpcId, spawnDat);
            } catch (Exception var7) {
               _log.log(Level.SEVERE, "Error on InitLocationShadowSpawns", (Throwable)var7);
            }
         } else {
            _log.warning(this.getClass().getSimpleName() + ": Data missing in NPC table for ID: " + this._shadowSpawnLoc[locNo][i][0] + ".");
         }
      }
   }

   protected void initExecutionerSpawns() {
      for(int keyNpcId : this._victim.keySet()) {
         try {
            NpcTemplate template = NpcsParser.getInstance().getTemplate(this._victim.get(keyNpcId));
            if (template != null) {
               Spawner spawnDat = new Spawner(template);
               spawnDat.setAmount(1);
               spawnDat.setX(0);
               spawnDat.setY(0);
               spawnDat.setZ(0);
               spawnDat.setHeading(0);
               spawnDat.setRespawnDelay(3600);
               SpawnParser.getInstance().addNewSpawn(spawnDat);
               this._executionerSpawns.put(keyNpcId, spawnDat);
            } else {
               _log.warning(this.getClass().getSimpleName() + ": Data missing in NPC table for ID: " + this._victim.get(keyNpcId) + ".");
            }
         } catch (Exception var6) {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Spawn could not be initialized: " + var6.getMessage(), (Throwable)var6);
         }
      }
   }

   public ScheduledFuture<?> getChangeAttackTimeTask() {
      return this._changeAttackTimeTask;
   }

   public void setChangeAttackTimeTask(ScheduledFuture<?> task) {
      this._changeAttackTimeTask = task;
   }

   public ScheduledFuture<?> getChangeCoolDownTimeTask() {
      return this._changeCoolDownTimeTask;
   }

   public void setChangeCoolDownTimeTask(ScheduledFuture<?> task) {
      this._changeCoolDownTimeTask = task;
   }

   public ScheduledFuture<?> getChangeEntryTimeTask() {
      return this._changeEntryTimeTask;
   }

   public void setChangeEntryTimeTask(ScheduledFuture<?> task) {
      this._changeEntryTimeTask = task;
   }

   public ScheduledFuture<?> getChangeWarmUpTimeTask() {
      return this._changeWarmUpTimeTask;
   }

   public void setChangeWarmUpTimeTask(ScheduledFuture<?> task) {
      this._changeWarmUpTimeTask = task;
   }

   public long getAttackTimeEnd() {
      return this._attackTimeEnd;
   }

   public void setAttackTimeEnd(long attackTimeEnd) {
      this._attackTimeEnd = attackTimeEnd;
   }

   public byte getCycleMin() {
      return 55;
   }

   public long getEntrytTimeEnd() {
      return this._entryTimeEnd;
   }

   public void setEntryTimeEnd(long entryTimeEnd) {
      this._entryTimeEnd = entryTimeEnd;
   }

   public long getWarmUpTimeEnd() {
      return this._warmUpTimeEnd;
   }

   public void setWarmUpTimeEnd(long warmUpTimeEnd) {
      this._warmUpTimeEnd = warmUpTimeEnd;
   }

   public boolean isAttackTime() {
      return this._inAttackTime;
   }

   public void setIsAttackTime(boolean attackTime) {
      this._inAttackTime = attackTime;
   }

   public boolean isCoolDownTime() {
      return this._inCoolDownTime;
   }

   public void setIsCoolDownTime(boolean isCoolDownTime) {
      this._inCoolDownTime = isCoolDownTime;
   }

   public boolean isEntryTime() {
      return this._inEntryTime;
   }

   public void setIsEntryTime(boolean entryTime) {
      this._inEntryTime = entryTime;
   }

   public boolean isFirstTimeRun() {
      return this._firstTimeRun;
   }

   public void setIsFirstTimeRun(boolean isFirstTimeRun) {
      this._firstTimeRun = isFirstTimeRun;
   }

   public boolean isWarmUpTime() {
      return this._inWarmUpTime;
   }

   public void setIsWarmUpTime(boolean isWarmUpTime) {
      this._inWarmUpTime = isWarmUpTime;
   }

   public synchronized void tryEntry(Npc npc, Player player) {
      Quest hostQuest = QuestManager.getInstance().getQuest(620);
      if (hostQuest == null) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Couldn't find quest: " + 620);
      } else {
         int npcId = npc.getId();
         switch(npcId) {
            case 31921:
            case 31922:
            case 31923:
            case 31924:
               if (this._hallInUse.get(npcId)) {
                  this.showHtmlFile(player, npcId + "-FULL.htm", npc, null);
                  return;
               } else {
                  if (Config.FS_PARTY_MEMBER_COUNT > 1) {
                     if (!player.isInParty() || player.getParty().getMemberCount() < Config.FS_PARTY_MEMBER_COUNT) {
                        this.showHtmlFile(player, npcId + "-SP.htm", npc, null);
                        return;
                     }

                     if (!player.getParty().isLeader(player)) {
                        this.showHtmlFile(player, npcId + "-NL.htm", npc, null);
                        return;
                     }

                     for(Player mem : player.getParty().getMembers()) {
                        QuestState qs = mem.getQuestState(hostQuest.getName());
                        if (qs == null || !qs.isStarted() && !qs.isCompleted()) {
                           this.showHtmlFile(player, npcId + "-NS.htm", npc, mem);
                           return;
                        }

                        if (mem.getInventory().getItemByItemId(7075) == null) {
                           this.showHtmlFile(player, npcId + "-SE.htm", npc, mem);
                           return;
                        }

                        if (player.getWeightPenalty() >= 3) {
                           mem.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
                           return;
                        }
                     }
                  } else if (Config.FS_PARTY_MEMBER_COUNT <= 1 && player.isInParty()) {
                     if (!player.getParty().isLeader(player)) {
                        this.showHtmlFile(player, npcId + "-NL.htm", npc, null);
                        return;
                     }

                     for(Player mem : player.getParty().getMembers()) {
                        QuestState qs = mem.getQuestState(hostQuest.getName());
                        if (qs == null || !qs.isStarted() && !qs.isCompleted()) {
                           this.showHtmlFile(player, npcId + "-NS.htm", npc, mem);
                           return;
                        }

                        if (mem.getInventory().getItemByItemId(7075) == null) {
                           this.showHtmlFile(player, npcId + "-SE.htm", npc, mem);
                           return;
                        }

                        if (player.getWeightPenalty() >= 3) {
                           mem.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
                           return;
                        }
                     }
                  } else {
                     QuestState qs = player.getQuestState(hostQuest.getName());
                     if (qs == null || !qs.isStarted() && !qs.isCompleted()) {
                        this.showHtmlFile(player, npcId + "-NS.htm", npc, player);
                        return;
                     }

                     if (player.getInventory().getItemByItemId(7075) == null) {
                        this.showHtmlFile(player, npcId + "-SE.htm", npc, player);
                        return;
                     }

                     if (player.getWeightPenalty() >= 3) {
                        player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
                        return;
                     }
                  }

                  if (!this.isEntryTime()) {
                     this.showHtmlFile(player, npcId + "-NE.htm", npc, null);
                     return;
                  }

                  this.showHtmlFile(player, npcId + "-OK.htm", npc, null);
                  this.entry(npcId, player);
                  return;
               }
            default:
               if (!player.isGM()) {
                  Util.handleIllegalPlayerAction(player, "" + player.getName() + " tried to enter four sepulchers with invalid npc id.");
               }
         }
      }
   }

   private void entry(int npcId, Player player) {
      int[] Location = (int[])this._startHallSpawns.get(npcId);
      if (Config.FS_PARTY_MEMBER_COUNT > 1) {
         List<Player> members = new LinkedList<>();

         for(Player mem : player.getParty().getMembers()) {
            if (!mem.isDead() && Util.checkIfInRange(700, player, mem, true)) {
               members.add(mem);
            }
         }

         for(Player mem : members) {
            EpicBossManager.getInstance().getZone(Location[0], Location[1], Location[2]).allowPlayerEntry(mem, 30);
            int driftx = Rnd.get(-80, 80);
            int drifty = Rnd.get(-80, 80);
            mem.teleToLocation(Location[0] + driftx, Location[1] + drifty, Location[2], true);
            mem.destroyItemByItemId("Quest", 7075, 1L, mem, true);
            if (mem.getInventory().getItemByItemId(7262) == null) {
               mem.addItem("Quest", 7261, 1L, mem, true);
            }

            ItemInstance hallsKey = mem.getInventory().getItemByItemId(7260);
            if (hallsKey != null) {
               mem.destroyItemByItemId("Quest", 7260, hallsKey.getCount(), mem, true);
            }
         }

         this._challengers.remove(npcId);
         this._challengers.put(npcId, player);
         this._hallInUse.remove(npcId);
         this._hallInUse.put(npcId, true);
      }

      if (Config.FS_PARTY_MEMBER_COUNT <= 1 && player.isInParty()) {
         List<Player> members = new LinkedList<>();

         for(Player mem : player.getParty().getMembers()) {
            if (!mem.isDead() && Util.checkIfInRange(700, player, mem, true)) {
               members.add(mem);
            }
         }

         for(Player mem : members) {
            EpicBossManager.getInstance().getZone(Location[0], Location[1], Location[2]).allowPlayerEntry(mem, 30);
            int driftx = Rnd.get(-80, 80);
            int drifty = Rnd.get(-80, 80);
            mem.teleToLocation(Location[0] + driftx, Location[1] + drifty, Location[2], true);
            mem.destroyItemByItemId("Quest", 7075, 1L, mem, true);
            if (mem.getInventory().getItemByItemId(7262) == null) {
               mem.addItem("Quest", 7261, 1L, mem, true);
            }

            ItemInstance hallsKey = mem.getInventory().getItemByItemId(7260);
            if (hallsKey != null) {
               mem.destroyItemByItemId("Quest", 7260, hallsKey.getCount(), mem, true);
            }
         }

         this._challengers.remove(npcId);
         this._challengers.put(npcId, player);
         this._hallInUse.remove(npcId);
         this._hallInUse.put(npcId, true);
      } else {
         EpicBossManager.getInstance().getZone(Location[0], Location[1], Location[2]).allowPlayerEntry(player, 30);
         int driftx = Rnd.get(-80, 80);
         int drifty = Rnd.get(-80, 80);
         player.teleToLocation(Location[0] + driftx, Location[1] + drifty, Location[2], true);
         player.destroyItemByItemId("Quest", 7075, 1L, player, true);
         if (player.getInventory().getItemByItemId(7262) == null) {
            player.addItem("Quest", 7261, 1L, player, true);
         }

         ItemInstance hallsKey = player.getInventory().getItemByItemId(7260);
         if (hallsKey != null) {
            player.destroyItemByItemId("Quest", 7260, hallsKey.getCount(), player, true);
         }

         this._challengers.remove(npcId);
         this._challengers.put(npcId, player);
         this._hallInUse.remove(npcId);
         this._hallInUse.put(npcId, true);
      }
   }

   public void spawnMysteriousBox(int npcId) {
      if (this.isAttackTime()) {
         Spawner spawnDat = this._mysteriousBoxSpawns.get(npcId);
         if (spawnDat != null) {
            this._allMobs.add(spawnDat.doSpawn());
            spawnDat.stopRespawn();
         }
      }
   }

   public void spawnMonster(int npcId) {
      if (this.isAttackTime()) {
         List<SepulcherMonsterInstance> mobs = new CopyOnWriteArrayList<>();
         List<Spawner> monsterList;
         if (Rnd.get(2) == 0) {
            monsterList = this._physicalMonsters.get(npcId);
         } else {
            monsterList = this._magicalMonsters.get(npcId);
         }

         if (monsterList != null) {
            boolean spawnKeyBoxMob = false;
            boolean spawnedKeyBoxMob = false;

            for(Spawner spawnDat : monsterList) {
               if (spawnedKeyBoxMob) {
                  spawnKeyBoxMob = false;
               } else {
                  switch(npcId) {
                     case 31469:
                     case 31474:
                     case 31479:
                     case 31484:
                        if (Rnd.get(48) == 0) {
                           spawnKeyBoxMob = true;
                        }
                        break;
                     default:
                        spawnKeyBoxMob = false;
                  }
               }

               SepulcherMonsterInstance mob = null;
               if (spawnKeyBoxMob) {
                  try {
                     NpcTemplate template = NpcsParser.getInstance().getTemplate(18149);
                     if (template != null) {
                        Spawner keyBoxMobSpawn = new Spawner(template);
                        keyBoxMobSpawn.setAmount(1);
                        keyBoxMobSpawn.setX(spawnDat.getX());
                        keyBoxMobSpawn.setY(spawnDat.getY());
                        keyBoxMobSpawn.setZ(spawnDat.getZ());
                        keyBoxMobSpawn.setHeading(spawnDat.getHeading());
                        keyBoxMobSpawn.setRespawnDelay(3600);
                        SpawnParser.getInstance().addNewSpawn(keyBoxMobSpawn);
                        mob = (SepulcherMonsterInstance)keyBoxMobSpawn.doSpawn();
                        keyBoxMobSpawn.stopRespawn();
                     } else {
                        _log.warning(this.getClass().getSimpleName() + ": Data missing in NPC table for ID: 18149");
                     }
                  } catch (Exception var11) {
                     _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Spawn could not be initialized: " + var11.getMessage(), (Throwable)var11);
                  }

                  spawnedKeyBoxMob = true;
               } else {
                  mob = (SepulcherMonsterInstance)spawnDat.doSpawn();
                  spawnDat.stopRespawn();
               }

               if (mob != null) {
                  mob.mysteriousBoxId = npcId;
                  switch(npcId) {
                     case 31469:
                     case 31472:
                     case 31474:
                     case 31477:
                     case 31479:
                     case 31482:
                     case 31484:
                     case 31487:
                        mobs.add(mob);
                     case 31470:
                     case 31471:
                     case 31473:
                     case 31475:
                     case 31476:
                     case 31478:
                     case 31480:
                     case 31481:
                     case 31483:
                     case 31485:
                     case 31486:
                     default:
                        this._allMobs.add(mob);
                  }
               }
            }

            switch(npcId) {
               case 31469:
               case 31474:
               case 31479:
               case 31484:
                  this._viscountMobs.put(npcId, mobs);
               case 31470:
               case 31471:
               case 31473:
               case 31475:
               case 31476:
               case 31478:
               case 31480:
               case 31481:
               case 31483:
               case 31485:
               case 31486:
               default:
                  break;
               case 31472:
               case 31477:
               case 31482:
               case 31487:
                  this._dukeMobs.put(npcId, mobs);
            }
         }
      }
   }

   public synchronized boolean isViscountMobsAnnihilated(int npcId) {
      List<SepulcherMonsterInstance> mobs = this._viscountMobs.get(npcId);
      if (mobs == null) {
         return true;
      } else {
         for(SepulcherMonsterInstance mob : mobs) {
            if (!mob.isDead()) {
               return false;
            }
         }

         return true;
      }
   }

   public synchronized boolean isDukeMobsAnnihilated(int npcId) {
      List<SepulcherMonsterInstance> mobs = this._dukeMobs.get(npcId);
      if (mobs == null) {
         return true;
      } else {
         for(SepulcherMonsterInstance mob : mobs) {
            if (!mob.isDead()) {
               return false;
            }
         }

         return true;
      }
   }

   public void spawnKeyBox(Npc activeChar) {
      if (this.isAttackTime()) {
         Spawner spawnDat = this._keyBoxSpawns.get(activeChar.getId());
         if (spawnDat != null) {
            spawnDat.setAmount(1);
            spawnDat.setX(activeChar.getX());
            spawnDat.setY(activeChar.getY());
            spawnDat.setZ(activeChar.getZ());
            spawnDat.setHeading(activeChar.getHeading());
            spawnDat.setRespawnDelay(3600);
            this._allMobs.add(spawnDat.doSpawn());
            spawnDat.stopRespawn();
         }
      }
   }

   public void spawnExecutionerOfHalisha(Npc activeChar) {
      if (this.isAttackTime()) {
         Spawner spawnDat = this._executionerSpawns.get(activeChar.getId());
         if (spawnDat != null) {
            spawnDat.setAmount(1);
            spawnDat.setX(activeChar.getX());
            spawnDat.setY(activeChar.getY());
            spawnDat.setZ(activeChar.getZ());
            spawnDat.setHeading(activeChar.getHeading());
            spawnDat.setRespawnDelay(3600);
            this._allMobs.add(spawnDat.doSpawn());
            spawnDat.stopRespawn();
         }
      }
   }

   public void spawnArchonOfHalisha(int npcId) {
      if (this.isAttackTime()) {
         if (!this._archonSpawned.get(npcId)) {
            List<Spawner> monsterList = this._dukeFinalMobs.get(npcId);
            if (monsterList != null) {
               for(Spawner spawnDat : monsterList) {
                  SepulcherMonsterInstance mob = (SepulcherMonsterInstance)spawnDat.doSpawn();
                  spawnDat.stopRespawn();
                  if (mob != null) {
                     mob.mysteriousBoxId = npcId;
                     this._allMobs.add(mob);
                  }
               }

               this._archonSpawned.put(npcId, true);
            }
         }
      }
   }

   public void spawnEmperorsGraveNpc(int npcId) {
      if (this.isAttackTime()) {
         List<Spawner> monsterList = this._emperorsGraveNpcs.get(npcId);
         if (monsterList != null) {
            for(Spawner spawnDat : monsterList) {
               this._allMobs.add(spawnDat.doSpawn());
               spawnDat.stopRespawn();
            }
         }
      }
   }

   public void locationShadowSpawns() {
      int locNo = Rnd.get(4);
      int[] gateKeeper = new int[]{31929, 31934, 31939, 31944};

      for(int i = 0; i <= 3; ++i) {
         int keyNpcId = gateKeeper[i];
         Spawner spawnDat = this._shadowSpawns.get(keyNpcId);
         spawnDat.setX(this._shadowSpawnLoc[locNo][i][1]);
         spawnDat.setY(this._shadowSpawnLoc[locNo][i][2]);
         spawnDat.setZ(this._shadowSpawnLoc[locNo][i][3]);
         spawnDat.setHeading(this._shadowSpawnLoc[locNo][i][4]);
         this._shadowSpawns.put(keyNpcId, spawnDat);
      }
   }

   public void spawnShadow(int npcId) {
      if (this.isAttackTime()) {
         Spawner spawnDat = this._shadowSpawns.get(npcId);
         if (spawnDat != null) {
            SepulcherMonsterInstance mob = (SepulcherMonsterInstance)spawnDat.doSpawn();
            spawnDat.stopRespawn();
            if (mob != null) {
               mob.mysteriousBoxId = npcId;
               this._allMobs.add(mob);
            }
         }
      }
   }

   public void deleteAllMobs() {
      for(Npc mob : this._allMobs) {
         if (mob != null) {
            try {
               if (mob.getSpawn() != null) {
                  mob.getSpawn().stopRespawn();
               }

               mob.deleteMe();
            } catch (Exception var4) {
               _log.log(Level.SEVERE, this.getClass().getSimpleName() + ": Failed deleting mob.", (Throwable)var4);
            }
         }
      }

      this._allMobs.clear();
   }

   protected void closeAllDoors() {
      for(int doorId : this._hallGateKeepers.values()) {
         try {
            DoorInstance door = DoorParser.getInstance().getDoor(doorId);
            if (door != null) {
               door.closeMe();
            } else {
               _log.warning(this.getClass().getSimpleName() + ": Attempted to close undefined door. doorId: " + doorId);
            }
         } catch (Exception var4) {
            _log.log(Level.SEVERE, this.getClass().getSimpleName() + ": Failed closing door", (Throwable)var4);
         }
      }
   }

   protected byte minuteSelect(byte min) {
      if ((double)min % 5.0 != 0.0) {
         switch(min) {
            case 6:
            case 7:
               min = 5;
               break;
            case 8:
            case 9:
            case 11:
            case 12:
               min = 10;
            case 10:
            case 15:
            case 20:
            case 25:
            case 30:
            case 35:
            case 40:
            case 45:
            case 50:
            case 55:
            default:
               break;
            case 13:
            case 14:
            case 16:
            case 17:
               min = 15;
               break;
            case 18:
            case 19:
            case 21:
            case 22:
               min = 20;
               break;
            case 23:
            case 24:
            case 26:
            case 27:
               min = 25;
               break;
            case 28:
            case 29:
            case 31:
            case 32:
               min = 30;
               break;
            case 33:
            case 34:
            case 36:
            case 37:
               min = 35;
               break;
            case 38:
            case 39:
            case 41:
            case 42:
               min = 40;
               break;
            case 43:
            case 44:
            case 46:
            case 47:
               min = 45;
               break;
            case 48:
            case 49:
            case 51:
            case 52:
               min = 50;
               break;
            case 53:
            case 54:
            case 56:
            case 57:
               min = 55;
         }
      }

      return min;
   }

   public void managerSay(byte min) {
      if (this._inAttackTime) {
         if (min < 5) {
            return;
         }

         min = this.minuteSelect(min);
         NpcStringId msg = NpcStringId.MINUTES_HAVE_PASSED;
         if (min == 90) {
            msg = NpcStringId.GAME_OVER_THE_TELEPORT_WILL_APPEAR_MOMENTARILY;
         }

         if (!this._managers.isEmpty()) {
            for(Spawner temp : this._managers) {
               if (temp == null) {
                  _log.warning(this.getClass().getSimpleName() + ": managerSay(): manager is null");
               } else if (!(temp.getLastSpawn() instanceof SepulcherNpcInstance)) {
                  _log.warning(this.getClass().getSimpleName() + ": managerSay(): manager is not Sepulcher instance");
               } else if (this._hallInUse.get(temp.getId())) {
                  ((SepulcherNpcInstance)temp.getLastSpawn()).sayInShout(msg, min);
               }
            }
         }
      } else if (this._inEntryTime) {
         NpcStringId msg1 = NpcStringId.YOU_MAY_NOW_ENTER_THE_SEPULCHER;
         NpcStringId msg2 = NpcStringId.IF_YOU_PLACE_YOUR_HAND_ON_THE_STONE_STATUE_IN_FRONT_OF_EACH_SEPULCHER_YOU_WILL_BE_ABLE_TO_ENTER;
         if (!this._managers.isEmpty()) {
            for(Spawner temp : this._managers) {
               if (temp == null) {
                  _log.warning(this.getClass().getSimpleName() + ": Something goes wrong in managerSay()...");
               } else if (!(temp.getLastSpawn() instanceof SepulcherNpcInstance)) {
                  _log.warning(this.getClass().getSimpleName() + ": Something goes wrong in managerSay()...");
               } else {
                  ((SepulcherNpcInstance)temp.getLastSpawn()).sayInShout(msg1, 0);
                  ((SepulcherNpcInstance)temp.getLastSpawn()).sayInShout(msg2, 0);
               }
            }
         }
      }
   }

   public Map<Integer, Integer> getHallGateKeepers() {
      return this._hallGateKeepers;
   }

   public void showHtmlFile(Player player, String file, Npc npc, Player member) {
      NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
      html.setFile(player, player.getLang(), "data/html/SepulcherNpc/" + file);
      if (member != null) {
         html.replace("%member%", member.getName());
      }

      player.sendPacket(html);
   }

   public static final FourSepulchersManager getInstance() {
      return FourSepulchersManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final FourSepulchersManager _instance = new FourSepulchersManager();
   }
}
