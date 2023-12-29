package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.time.cron.SchedulingPattern;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.GrandBossInstance;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.zone.type.BossZone;
import l2e.gameserver.network.serverpackets.CreatureSay;

public class EpicBossManager {
   protected static Logger _log = Logger.getLogger(EpicBossManager.class.getName());
   private static final String DELETE_GRAND_BOSS_LIST = "DELETE FROM grandboss_list";
   private static final String INSERT_GRAND_BOSS_LIST = "INSERT INTO grandboss_list (player_id,zone) VALUES (?,?)";
   private static final String UPDATE_GRAND_BOSS_DATA = "UPDATE grandboss_data set loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? where boss_id = ?";
   private static final String UPDATE_GRAND_BOSS_DATA2 = "UPDATE grandboss_data set status = ?, respawn_time = ? where boss_id = ?";
   protected static Map<Integer, GrandBossInstance> _bosses = new ConcurrentHashMap<>();
   protected static Map<Integer, StatsSet> _storedInfo = new HashMap<>();
   private final Map<Integer, Integer> _bossStatus = new ConcurrentHashMap<>();
   private final Map<Integer, BossZone> _zones = new ConcurrentHashMap<>();

   public static EpicBossManager getInstance() {
      return EpicBossManager.SingletonHolder._instance;
   }

   protected EpicBossManager() {
      this.init();
   }

   private void init() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         Statement s = con.createStatement();
         ResultSet rset = s.executeQuery("SELECT * from grandboss_data ORDER BY boss_id");
      ) {
         while(rset.next()) {
            StatsSet info = new StatsSet();
            int bossId = rset.getInt("boss_id");
            info.set("loc_x", rset.getInt("loc_x"));
            info.set("loc_y", rset.getInt("loc_y"));
            info.set("loc_z", rset.getInt("loc_z"));
            info.set("heading", rset.getInt("heading"));
            info.set("respawnTime", rset.getLong("respawn_time"));
            double HP = rset.getDouble("currentHP");
            int true_HP = (int)HP;
            info.set("currentHP", true_HP);
            double MP = rset.getDouble("currentMP");
            int true_MP = (int)MP;
            info.set("currentMP", true_MP);
            int status = rset.getInt("status");
            this._bossStatus.put(bossId, status);
            _storedInfo.put(bossId, info);
            String checkStatus;
            switch(status) {
               case 1:
                  checkStatus = "Wait";
                  break;
               case 2:
                  checkStatus = "Fight";
                  break;
               case 3:
                  checkStatus = "Dead";
                  break;
               default:
                  checkStatus = "Alive";
            }

            if (status > 0) {
               _log.info(
                  "EpicBossManager: "
                     + NpcsParser.getInstance().getTemplate(bossId).getName()
                     + "["
                     + bossId
                     + "] respawn date ["
                     + new Date(info.getLong("respawnTime"))
                     + "]."
               );
            } else {
               _log.info("EpicBossManager: " + NpcsParser.getInstance().getTemplate(bossId).getName() + "[" + bossId + "] status [" + checkStatus + "].");
            }
         }
      } catch (SQLException var71) {
         _log.log(Level.WARNING, "EpicBossManager: Could not load grandboss_data table: " + var71.getMessage(), (Throwable)var71);
      } catch (Exception var72) {
         _log.log(Level.WARNING, "Error while initializing EpicBossManager: " + var72.getMessage(), (Throwable)var72);
      }
   }

   public void initZones() {
      Map<Integer, List<Integer>> zones = new HashMap<>();

      for(Integer zoneId : this._zones.keySet()) {
         zones.put(zoneId, new ArrayList<>());
      }

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         Statement s = con.createStatement();
         ResultSet rset = s.executeQuery("SELECT * from grandboss_list ORDER BY player_id");
      ) {
         while(rset.next()) {
            int id = rset.getInt("player_id");
            int zone_id = rset.getInt("zone");
            zones.get(zone_id).add(id);
         }

         if (Config.DEBUG) {
            _log.info("EpicBossManager: Initialized " + this._zones.size() + " Grand Boss Zones");
         }
      } catch (SQLException var64) {
         _log.log(Level.WARNING, "EpicBossManager: Could not load grandboss_list table: " + var64.getMessage(), (Throwable)var64);
      } catch (Exception var65) {
         _log.log(Level.WARNING, "Error while initializing EpicBoss zones: " + var65.getMessage(), (Throwable)var65);
      }

      for(Entry<Integer, BossZone> e : this._zones.entrySet()) {
         e.getValue().setAllowedPlayers(zones.get(e.getKey()));
      }

      zones.clear();
   }

   public void addZone(BossZone zone) {
      this._zones.put(zone.getId(), zone);
   }

   public final BossZone getZone(int zoneId) {
      return this._zones.get(zoneId);
   }

   public final BossZone getZone(Creature character) {
      return this._zones.values().stream().filter(z -> z.isCharacterInZone(character)).findFirst().orElse(null);
   }

   public final BossZone getZone(Location loc) {
      return this.getZone(loc.getX(), loc.getY(), loc.getZ());
   }

   public final BossZone getZone(int x, int y, int z) {
      return this._zones.values().stream().filter(zone -> zone.isInsideZone(x, y, z)).findFirst().orElse(null);
   }

   public boolean checkIfInZone(String zoneType, GameObject obj) {
      BossZone temp = this.getZone(obj.getX(), obj.getY(), obj.getZ());
      return temp != null && temp.getName().equalsIgnoreCase(zoneType);
   }

   public boolean checkIfInZone(Player player) {
      return player != null && this.getZone(player.getX(), player.getY(), player.getZ()) != null;
   }

   public int getBossStatus(int bossId) {
      return this._bossStatus.get(bossId);
   }

   public void setBossStatus(int bossId, int status, boolean print) {
      if (status == 0 && Arrays.binarySearch(Config.GRANDBOSS_ANNOUNCE_LIST, bossId) >= 0) {
         for(Player player : World.getInstance().getAllPlayers()) {
            if (player.isOnline()) {
               ServerMessage msg = new ServerMessage("Announce.EPIC_RESPAWN", player.getLang());
               msg.add(player.getNpcName(NpcsParser.getInstance().getTemplate(bossId)));
               player.sendPacket(new CreatureSay(0, 10, "", msg.toString()));
            }
         }
      }

      this._bossStatus.put(bossId, status);
      if (print) {
         _log.info(
            this.getClass().getSimpleName() + ": Updated " + NpcsParser.getInstance().getTemplate(bossId).getName() + "(" + bossId + ") status to " + status
         );
      }

      this.updateDb(bossId, true);
   }

   public void addBoss(GrandBossInstance boss) {
      _bosses.put(boss.getId(), boss);
   }

   public GrandBossInstance getBoss(int bossId) {
      return _bosses.get(bossId);
   }

   public StatsSet getStatsSet(int bossId) {
      return _storedInfo.get(bossId);
   }

   public void setStatsSet(int bossId, StatsSet info) {
      _storedInfo.put(bossId, info);
      this.updateDb(bossId, false);
   }

   private void storeToDb() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement delete = con.prepareStatement("DELETE FROM grandboss_list");
      ) {
         delete.executeUpdate();

         try (PreparedStatement insert = con.prepareStatement("INSERT INTO grandboss_list (player_id,zone) VALUES (?,?)")) {
            for(Entry<Integer, BossZone> e : this._zones.entrySet()) {
               List<Integer> list = e.getValue().getAllowedPlayers();
               if (list != null && !list.isEmpty()) {
                  for(Integer player : list) {
                     insert.setInt(1, player);
                     insert.setInt(2, e.getKey());
                     insert.executeUpdate();
                     insert.clearParameters();
                  }
               }
            }
         }

         for(Entry<Integer, StatsSet> e : _storedInfo.entrySet()) {
            GrandBossInstance boss = _bosses.get(e.getKey());
            StatsSet info = e.getValue();
            if (boss != null && info != null) {
               try (PreparedStatement update = con.prepareStatement(
                     "UPDATE grandboss_data set loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? where boss_id = ?"
                  )) {
                  update.setInt(1, boss.getX());
                  update.setInt(2, boss.getY());
                  update.setInt(3, boss.getZ());
                  update.setInt(4, boss.getHeading());
                  update.setLong(5, info.getLong("respawnTime"));
                  double hp = boss.getCurrentHp();
                  double mp = boss.getCurrentMp();
                  if (boss.isDead()) {
                     hp = boss.getMaxHp();
                     mp = boss.getMaxMp();
                  }

                  update.setDouble(6, hp);
                  update.setDouble(7, mp);
                  update.setInt(8, this._bossStatus.get(e.getKey()));
                  update.setInt(9, e.getKey());
                  update.executeUpdate();
                  update.clearParameters();
               }
            } else {
               try (PreparedStatement update = con.prepareStatement("UPDATE grandboss_data set status = ?, respawn_time = ? where boss_id = ?")) {
                  update.setInt(1, this._bossStatus.get(e.getKey()));
                  update.setLong(2, info != null ? info.getLong("respawnTime", 0L) : 0L);
                  update.setInt(3, e.getKey());
                  update.executeUpdate();
                  update.clearParameters();
               }
            }
         }
      } catch (SQLException var132) {
         _log.log(Level.WARNING, "EpicBossManager: Couldn't store grandbosses to database:" + var132.getMessage(), (Throwable)var132);
      }
   }

   private void updateDb(int bossId, boolean statusOnly) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         GrandBossInstance boss = _bosses.get(bossId);
         StatsSet info = _storedInfo.get(bossId);
         if (!statusOnly && boss != null && info != null) {
            try (PreparedStatement statement = con.prepareStatement(
                  "UPDATE grandboss_data set loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? where boss_id = ?"
               )) {
               statement.setInt(1, boss.getX());
               statement.setInt(2, boss.getY());
               statement.setInt(3, boss.getZ());
               statement.setInt(4, boss.getHeading());
               statement.setLong(5, info.getLong("respawnTime"));
               double hp = boss.getCurrentHp();
               double mp = boss.getCurrentMp();
               if (boss.isDead()) {
                  hp = boss.getMaxHp();
                  mp = boss.getMaxMp();
               }

               statement.setDouble(6, hp);
               statement.setDouble(7, mp);
               statement.setInt(8, this._bossStatus.get(bossId));
               statement.setInt(9, bossId);
               statement.executeUpdate();
            }
         } else {
            try (PreparedStatement statement = con.prepareStatement("UPDATE grandboss_data set status = ?, respawn_time = ? where boss_id = ?")) {
               statement.setInt(1, this._bossStatus.get(bossId));
               statement.setLong(2, info != null ? info.getLong("respawnTime", 0L) : 0L);
               statement.setInt(3, bossId);
               statement.executeUpdate();
            }
         }
      } catch (SQLException var62) {
         _log.log(Level.WARNING, "EpicBossManager: Couldn't update grandbosses to database:" + var62.getMessage(), (Throwable)var62);
      }
   }

   public void cleanUp() {
      this.storeToDb();
      _bosses.clear();
      _storedInfo.clear();
      this._bossStatus.clear();
      this._zones.clear();
   }

   public Map<Integer, BossZone> getZones() {
      return this._zones;
   }

   public Map<Integer, StatsSet> getStoredInfo() {
      return _storedInfo;
   }

   public static String respawnTimeFormat(StatsSet info) {
      return Util.dateFormat(info.getLong("respawnTime"));
   }

   public long setRespawnTime(int npcId, String time) {
      SchedulingPattern cronTime;
      try {
         cronTime = new SchedulingPattern(time);
      } catch (SchedulingPattern.InvalidPatternException var10) {
         return 0L;
      }

      long respawnTime = cronTime.next(System.currentTimeMillis());
      Calendar date = Calendar.getInstance();
      date.setTimeInMillis(respawnTime);
      if (npcId != 29065) {
         this.getStatsSet(npcId).set("respawnTime", date.getTimeInMillis());
         this.setBossStatus(npcId, 3, false);
      }

      if (Arrays.binarySearch(Config.GRANDBOSS_DEAD_ANNOUNCE_LIST, npcId) >= 0) {
         for(Player player : World.getInstance().getAllPlayers()) {
            if (player.isOnline()) {
               ServerMessage msg = new ServerMessage("Announce.EPIC_DEATH_ANNOUNCE", player.getLang());
               msg.add(player.getNpcName(NpcsParser.getInstance().getTemplate(npcId)));
               player.sendPacket(new CreatureSay(0, 10, "", msg.toString()));
            }
         }
      }

      _log.info(
         "EpicBossManager: " + NpcsParser.getInstance().getTemplate(npcId).getName() + " Dead! Respawn date [" + new Date(date.getTimeInMillis()) + "]."
      );
      return date.getTimeInMillis();
   }

   private static class SingletonHolder {
      protected static final EpicBossManager _instance = new EpicBossManager();
   }
}
