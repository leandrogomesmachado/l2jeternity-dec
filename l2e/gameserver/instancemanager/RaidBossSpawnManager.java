package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.holder.SpawnHolder;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.RaidBossInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.RadarControl;
import l2e.gameserver.network.serverpackets.ShowMiniMap;

public class RaidBossSpawnManager {
   private static final Logger _log = Logger.getLogger(RaidBossSpawnManager.class.getName());
   protected static final Map<Integer, RaidBossInstance> _bosses = new ConcurrentHashMap<>();
   protected static final Map<Integer, Spawner> _spawns = new ConcurrentHashMap<>();
   protected static final Map<Integer, StatsSet> _storedInfo = new ConcurrentHashMap<>();
   protected static final Map<Integer, ScheduledFuture<?>> _schedules = new ConcurrentHashMap<>();
   protected static Map<Integer, Map<Integer, Integer>> _points = new ConcurrentHashMap<>();
   protected static Map<Integer, Integer> _clanPoints = new ConcurrentHashMap<>();
   public static final Integer KEY_RANK = new Integer(-1);
   public static final Integer KEY_TOTAL_POINTS = new Integer(0);
   private final Lock _pointsLock = new ReentrantLock();

   protected RaidBossSpawnManager() {
      this.load();
      this.restorePoints();
      this.calculateRanking();
   }

   public void load() {
      _bosses.clear();
      _spawns.clear();
      _storedInfo.clear();
      _schedules.clear();
      _points.clear();
      _clanPoints.clear();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM raidboss_status ORDER BY boss_id");
         ResultSet rset = statement.executeQuery();
      ) {
         while(rset.next()) {
            NpcTemplate template = this.getValidTemplate(rset.getInt("boss_id"));
            if (template != null) {
               int id = rset.getInt("boss_id");
               StatsSet info = new StatsSet();
               info.set("currentHp", rset.getDouble("currentHp"));
               info.set("currentMp", rset.getDouble("currentMp"));
               info.set("respawnTime", rset.getLong("respawn_time"));
               _storedInfo.put(id, info);
            } else {
               _log.warning(this.getClass().getSimpleName() + ": Could not load raidboss #" + rset.getInt("boss_id") + " from DataBase");
            }
         }

         _log.info(this.getClass().getSimpleName() + ": Loaded " + _storedInfo.size() + " statuses.");
      } catch (SQLException var64) {
         _log.warning(this.getClass().getSimpleName() + ": Couldnt load raidboss_status table");
      } catch (Exception var65) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Error while initializing RaidBossSpawnManager: " + var65.getMessage(), (Throwable)var65);
      }
   }

   public void updateStatus(RaidBossInstance boss, boolean isBossDead) {
      if (_storedInfo.containsKey(boss.getId())) {
         StatsSet info = _storedInfo.get(boss.getId());
         if (isBossDead) {
            boss.setRaidStatus(RaidBossSpawnManager.StatusEnum.DEAD);
            int respawnDelay = 0;
            long respawnTime = 0L;
            int respawnMinDelay = (int)((float)boss.getSpawn().getRespawnMinDelay() * Config.RAID_MIN_RESPAWN_MULTIPLIER);
            int respawnMaxDelay = (int)((float)boss.getSpawn().getRespawnMaxDelay() * Config.RAID_MAX_RESPAWN_MULTIPLIER);
            if (boss.getSpawn().getRespawnPattern() != null) {
               respawnTime = boss.getSpawn().getRespawnPattern().next(System.currentTimeMillis());
               respawnDelay = (int)(respawnTime - System.currentTimeMillis());
            } else {
               respawnDelay = Rnd.get(respawnMinDelay, respawnMaxDelay);
               respawnTime = Calendar.getInstance().getTimeInMillis() + (long)respawnDelay;
            }

            info.set("currentHP", boss.getMaxHp());
            info.set("currentMP", boss.getMaxMp());
            info.set("respawnTime", respawnTime);
            if (!_schedules.containsKey(boss.getId()) && (respawnMinDelay > 0 || respawnMaxDelay > 0 || boss.getSpawn().getRespawnPattern() != null)) {
               Calendar time = Calendar.getInstance();
               time.setTimeInMillis(respawnTime);
               if (Arrays.binarySearch(Config.RAIDBOSS_DEAD_ANNOUNCE_LIST, boss.getId()) >= 0) {
                  for(Player player : World.getInstance().getAllPlayers()) {
                     if (player.isOnline()) {
                        ServerMessage msg = new ServerMessage("Announce.RAID_DEATH_ANNOUNCE", player.getLang());
                        msg.add(player.getNpcName(boss.getTemplate()));
                        player.sendPacket(new CreatureSay(0, 10, "", msg.toString()));
                     }
                  }
               }

               _log.info(this.getClass().getSimpleName() + ": Updated " + boss.getName() + " respawn time to " + time.getTime());
               _schedules.put(boss.getId(), ThreadPoolManager.getInstance().schedule(new RaidBossSpawnManager.SpawnSchedule(boss.getId()), (long)respawnDelay));
               this.updateDb();
            }
         } else {
            boss.setRaidStatus(RaidBossSpawnManager.StatusEnum.ALIVE);
            info.set("currentHP", boss.getCurrentHp());
            info.set("currentMP", boss.getCurrentMp());
            info.set("respawnTime", 0L);
         }

         _storedInfo.put(boss.getId(), info);
      }
   }

   private void restorePoints() {
      this._pointsLock.lock();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT charId, boss_id, points FROM `raidboss_points` ORDER BY charId ASC");
         ResultSet rset = statement.executeQuery();
      ) {
         int currentOwner = 0;
         Map<Integer, Integer> score = null;

         while(rset.next()) {
            if (currentOwner != rset.getInt("charId")) {
               currentOwner = rset.getInt("charId");
               score = new HashMap<>();
               _points.put(currentOwner, score);
            }

            assert score != null;

            int bossId = rset.getInt("boss_id");
            NpcTemplate template = NpcsParser.getInstance().getTemplate(bossId);
            if (bossId != KEY_RANK && bossId != KEY_TOTAL_POINTS && template != null && template.getRewardRp() > 0) {
               score.put(bossId, rset.getInt("points"));
            }
         }
      } catch (SQLException var62) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Couldnt load raidboss points", (Throwable)var62);
      }

      for(Entry<Integer, Map<Integer, Integer>> charPoints : _points.entrySet()) {
         Map<Integer, Integer> tmpPoint = charPoints.getValue();
         int totalPoints = 0;

         for(Entry<Integer, Integer> e : tmpPoint.entrySet()) {
            totalPoints += e.getValue();
         }

         if (totalPoints != 0) {
            int clanId = ClanHolder.getInstance().getClanId(charPoints.getKey());
            if (clanId != 0) {
               if (_clanPoints.containsKey(clanId)) {
                  int clanPoints = _clanPoints.get(clanId);
                  _clanPoints.put(clanId, clanPoints + totalPoints);
               } else {
                  _clanPoints.put(clanId, totalPoints);
               }
            }
         }
      }

      this._pointsLock.unlock();
   }

   public void updatePointsDb() {
      this._pointsLock.lock();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("TRUNCATE raidboss_points");
      ) {
         statement.execute();
      } catch (SQLException var93) {
         _log.warning(this.getClass().getSimpleName() + ": Couldnt empty raidboss_points table");
      }

      if (_points.isEmpty()) {
         this._pointsLock.unlock();
      } else {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("INSERT INTO `raidboss_points` (charId, boss_id, points) VALUES(?,?,?)");
         ) {
            for(Entry<Integer, Map<Integer, Integer>> pointEntry : _points.entrySet()) {
               Map<Integer, Integer> tmpPoint = pointEntry.getValue();
               if (tmpPoint != null && !tmpPoint.isEmpty()) {
                  for(Entry<Integer, Integer> pointListEntry : tmpPoint.entrySet()) {
                     if (!KEY_RANK.equals(pointListEntry.getKey())
                        && !KEY_TOTAL_POINTS.equals(pointListEntry.getKey())
                        && pointListEntry.getValue() != null
                        && pointListEntry.getValue() != 0) {
                        statement.setInt(1, pointEntry.getKey());
                        statement.setInt(2, pointListEntry.getKey());
                        statement.setInt(3, pointListEntry.getValue());
                        statement.execute();
                     }
                  }
               }
            }

            statement.close();
         } catch (SQLException var89) {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Couldnt update raidboss_points table", (Throwable)var89);
         }

         this._pointsLock.unlock();
      }
   }

   public void addPoints(Player player, int bossId, int points) {
      if (points > 0 && player.getObjectId() > 0 && bossId > 0) {
         this._pointsLock.lock();
         Map<Integer, Integer> pointsTable = _points.get(player.getObjectId());
         if (pointsTable == null) {
            pointsTable = new HashMap<>();
            _points.put(player.getObjectId(), pointsTable);
         }

         if (player.getClan() != null) {
            if (_clanPoints.containsKey(player.getClan().getId())) {
               int clanPoints = _clanPoints.get(player.getClan().getId());
               _clanPoints.put(player.getClan().getId(), clanPoints + points);
            } else {
               _clanPoints.put(player.getClan().getId(), points);
            }
         }

         if (pointsTable.isEmpty()) {
            pointsTable.put(bossId, points);
         } else {
            Integer currentPoins = pointsTable.get(bossId);
            pointsTable.put(bossId, currentPoins == null ? points : currentPoins + points);
         }

         this._pointsLock.unlock();
      }
   }

   public TreeMap<Integer, Integer> calculateRanking() {
      TreeMap<Integer, Integer> tmpRanking = new TreeMap<>();
      this._pointsLock.lock();

      for(Entry<Integer, Map<Integer, Integer>> point : _points.entrySet()) {
         Map<Integer, Integer> tmpPoint = point.getValue();
         tmpPoint.remove(KEY_RANK);
         tmpPoint.remove(KEY_TOTAL_POINTS);
         int totalPoints = 0;

         for(Entry<Integer, Integer> e : tmpPoint.entrySet()) {
            totalPoints += e.getValue();
         }

         if (totalPoints != 0) {
            tmpPoint.put(KEY_TOTAL_POINTS, totalPoints);
            tmpRanking.put(totalPoints, point.getKey());
         }
      }

      int ranking = 1;

      for(Entry<Integer, Integer> entry : tmpRanking.descendingMap().entrySet()) {
         Map<Integer, Integer> tmpPoint = _points.get(entry.getValue());
         tmpPoint.put(KEY_RANK, ranking);
         ++ranking;
      }

      this._pointsLock.unlock();
      return tmpRanking;
   }

   public void distributeRewards() {
      this._pointsLock.lock();
      TreeMap<Integer, Integer> ranking = this.calculateRanking();
      Iterator<Integer> e = ranking.descendingMap().values().iterator();

      for(int counter = 1; e.hasNext() && counter <= 100; ++counter) {
         int reward = 0;
         int playerId = e.next();
         switch(counter) {
            case 1:
               reward = Config.RAID_RANKING_1ST;
               break;
            case 2:
               reward = Config.RAID_RANKING_2ND;
               break;
            case 3:
               reward = Config.RAID_RANKING_3RD;
               break;
            case 4:
               reward = Config.RAID_RANKING_4TH;
               break;
            case 5:
               reward = Config.RAID_RANKING_5TH;
               break;
            case 6:
               reward = Config.RAID_RANKING_6TH;
               break;
            case 7:
               reward = Config.RAID_RANKING_7TH;
               break;
            case 8:
               reward = Config.RAID_RANKING_8TH;
               break;
            case 9:
               reward = Config.RAID_RANKING_9TH;
               break;
            case 10:
               reward = Config.RAID_RANKING_10TH;
               break;
            default:
               if (counter <= 50) {
                  reward = Config.RAID_RANKING_UP_TO_50TH;
               } else {
                  reward = Config.RAID_RANKING_UP_TO_100TH;
               }
         }

         Clan clan = null;
         Player player = World.getInstance().getPlayer(playerId);
         if (player != null) {
            clan = player.getClan();
         } else {
            int res = ClanHolder.getInstance().getClanId(playerId);
            if (res != 0) {
               clan = ClanHolder.getInstance().getClan(res);
            }
         }

         if (clan != null) {
            clan.addReputationScore(reward, true);
         }
      }

      _points.clear();
      this.updatePointsDb();
      this._pointsLock.unlock();
   }

   public void addNewSpawn(Spawner spawnDat, boolean storeInDb) {
      if (spawnDat != null) {
         if (!_spawns.containsKey(spawnDat.getId())) {
            StatsSet info = _storedInfo.get(spawnDat.getId());
            if (info != null) {
               int bossId = spawnDat.getId();
               long time = Calendar.getInstance().getTimeInMillis();
               long respawnTime = info.getLong("respawnTime");
               int currentHP = info.getInteger("currentHp");
               int currentMP = info.getInteger("currentMp");
               if (respawnTime != 0L && time <= respawnTime) {
                  long spawnTime = respawnTime - Calendar.getInstance().getTimeInMillis();
                  _schedules.put(bossId, ThreadPoolManager.getInstance().schedule(new RaidBossSpawnManager.SpawnSchedule(bossId), spawnTime));
               } else {
                  RaidBossInstance raidboss = null;
                  if (bossId == 25328) {
                     raidboss = DayNightSpawnManager.getInstance().handleBoss(spawnDat);
                  } else {
                     raidboss = (RaidBossInstance)spawnDat.doSpawn();
                  }

                  if (raidboss != null) {
                     if (currentHP == 0) {
                        raidboss.setCurrentHp(raidboss.getMaxHp());
                     } else {
                        raidboss.setCurrentHp((double)currentHP);
                     }

                     if (currentMP == 0) {
                        raidboss.setCurrentMp(raidboss.getMaxMp());
                     } else {
                        raidboss.setCurrentMp((double)currentMP);
                     }

                     raidboss.setRaidStatus(RaidBossSpawnManager.StatusEnum.ALIVE);
                     _bosses.put(bossId, raidboss);
                     info.set("currentHP", raidboss.getCurrentHp());
                     info.set("currentMP", raidboss.getCurrentMp());
                     info.set("respawnTime", 0L);
                  }
               }

               _spawns.put(bossId, spawnDat);
            } else if (!spawnDat.getTemplate().getParameter("isDestructionBoss", false) && spawnDat.getId() != 25665 && spawnDat.getId() != 25666) {
               _log.info(this.getClass().getSimpleName() + ": Could not load raidboss #" + spawnDat.getId() + " status in database.");
            }

            if (storeInDb) {
               RaidBossInstance raidboss = (RaidBossInstance)spawnDat.doSpawn();
               raidboss.setCurrentHp(raidboss.getMaxHp());
               raidboss.setCurrentMp(raidboss.getMaxMp());

               try (
                  Connection con = DatabaseFactory.getInstance().getConnection();
                  PreparedStatement statement = con.prepareStatement("INSERT INTO raidboss_status (boss_id,respawn_time,currentHp,currentMp) VALUES(?,?,?,?)");
               ) {
                  statement.setInt(1, spawnDat.getId());
                  statement.setLong(2, 0L);
                  statement.setDouble(3, raidboss.getMaxHp());
                  statement.setDouble(4, raidboss.getMaxMp());
                  statement.execute();
                  StatsSet inf = new StatsSet();
                  inf.set("currentHP", raidboss.getMaxHp());
                  inf.set("currentMP", raidboss.getMaxMp());
                  inf.set("respawnTime", 0L);
                  _storedInfo.put(spawnDat.getId(), inf);
               } catch (Exception var40) {
                  _log.log(
                     Level.WARNING,
                     this.getClass().getSimpleName() + ": Could not store raidboss #" + spawnDat.getId() + " in the DB:" + var40.getMessage(),
                     (Throwable)var40
                  );
               }
            }
         }
      }
   }

   public void deleteSpawn(Spawner spawnDat, boolean updateDb) {
      if (spawnDat != null) {
         int bossId = spawnDat.getId();
         if (_spawns.containsKey(bossId)) {
            SpawnHolder.getInstance().deleteSpawn(spawnDat, false);
            _spawns.remove(bossId);
            if (_bosses.containsKey(bossId)) {
               _bosses.remove(bossId);
            }

            if (_schedules.containsKey(bossId)) {
               ScheduledFuture<?> f = _schedules.remove(bossId);
               f.cancel(true);
            }

            if (_storedInfo.containsKey(bossId)) {
               _storedInfo.remove(bossId);
            }

            if (updateDb) {
               try (
                  Connection con = DatabaseFactory.getInstance().getConnection();
                  PreparedStatement statement = con.prepareStatement("DELETE FROM raidboss_status WHERE boss_id=?");
               ) {
                  statement.setInt(1, bossId);
                  statement.execute();
               } catch (Exception var36) {
                  _log.log(
                     Level.WARNING,
                     this.getClass().getSimpleName() + ": Could not remove raidboss #" + bossId + " from DB: " + var36.getMessage(),
                     (Throwable)var36
                  );
               }
            }
         }
      }
   }

   private void updateDb() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE raidboss_status SET respawn_time = ?, currentHP = ?, currentMP = ? WHERE boss_id = ?");
      ) {
         for(Integer bossId : _storedInfo.keySet()) {
            if (bossId != null) {
               RaidBossInstance boss = _bosses.get(bossId);
               if (boss != null) {
                  if (boss.getRaidStatus().equals(RaidBossSpawnManager.StatusEnum.ALIVE)) {
                     this.updateStatus(boss, false);
                  }

                  StatsSet info = _storedInfo.get(bossId);
                  if (info != null) {
                     try {
                        statement.setLong(1, info.getLong("respawnTime"));
                        statement.setDouble(2, info.getDouble("currentHP"));
                        statement.setDouble(3, info.getDouble("currentMP"));
                        statement.setInt(4, bossId);
                        statement.executeUpdate();
                        statement.clearParameters();
                     } catch (SQLException var35) {
                        _log.log(
                           Level.WARNING, this.getClass().getSimpleName() + ": Couldnt update raidboss_status table " + var35.getMessage(), (Throwable)var35
                        );
                     }
                  }
               }
            }
         }
      } catch (SQLException var40) {
         _log.log(
            Level.WARNING, this.getClass().getSimpleName() + ": SQL error while updating RaidBoss spawn to database: " + var40.getMessage(), (Throwable)var40
         );
      }
   }

   public String[] getAllRaidBossStatus() {
      String[] msg = new String[_bosses == null ? 0 : _bosses.size()];
      if (_bosses == null) {
         msg[0] = "None";
         return msg;
      } else {
         int index = 0;

         for(int i : _bosses.keySet()) {
            RaidBossInstance boss = _bosses.get(i);
            msg[index++] = boss.getName() + ": " + boss.getRaidStatus().name();
         }

         return msg;
      }
   }

   public String getRaidBossStatus(int bossId) {
      String msg = "RaidBoss Status..." + Config.EOL;
      if (_bosses == null) {
         return msg + "None";
      } else {
         if (_bosses.containsKey(bossId)) {
            RaidBossInstance boss = _bosses.get(bossId);
            msg = msg + boss.getName() + ": " + boss.getRaidStatus().name();
         }

         return msg;
      }
   }

   public RaidBossSpawnManager.StatusEnum getRaidBossStatusId(int bossId) {
      if (_bosses.containsKey(bossId)) {
         return _bosses.get(bossId).getRaidStatus();
      } else {
         return _schedules.containsKey(bossId) ? RaidBossSpawnManager.StatusEnum.DEAD : RaidBossSpawnManager.StatusEnum.UNDEFINED;
      }
   }

   public NpcTemplate getValidTemplate(int bossId) {
      NpcTemplate template = NpcsParser.getInstance().getTemplate(bossId);
      if (template == null) {
         return null;
      } else {
         return !template.isType("RaidBoss") && !template.isType("FlyRaidBoss") ? null : template;
      }
   }

   public void notifySpawnNightBoss(RaidBossInstance raidboss) {
      StatsSet info = new StatsSet();
      info.set("currentHP", raidboss.getCurrentHp());
      info.set("currentMP", raidboss.getCurrentMp());
      info.set("respawnTime", 0L);
      raidboss.setRaidStatus(RaidBossSpawnManager.StatusEnum.ALIVE);
      _storedInfo.put(raidboss.getId(), info);
      _log.info(this.getClass().getSimpleName() + ": Spawning Night Raid Boss " + raidboss.getName());
      _bosses.put(raidboss.getId(), raidboss);
   }

   public boolean isDefined(int bossId) {
      return _spawns.containsKey(bossId);
   }

   public Map<Integer, RaidBossInstance> getBosses() {
      return _bosses;
   }

   public Map<Integer, Spawner> getSpawns() {
      return _spawns;
   }

   public Map<Integer, StatsSet> getStoredInfo() {
      return _storedInfo;
   }

   public void cleanUp() {
      this.updateDb();
      _bosses.clear();
      if (_schedules != null) {
         for(Integer bossId : _schedules.keySet()) {
            ScheduledFuture<?> f = _schedules.get(bossId);
            f.cancel(true);
         }

         _schedules.clear();
      }

      _storedInfo.clear();
      _spawns.clear();
   }

   public void showBossLocation(final Player player, int bossId) {
      for(int id : Config.BLOCKED_RAID_LIST) {
         if (id == bossId) {
            return;
         }
      }

      switch(getInstance().getRaidBossStatusId(bossId)) {
         case ALIVE:
         case DEAD:
            Spawner spawn = getInstance().getSpawns().get(bossId);
            final Location loc = spawn.calcSpawnRangeLoc(spawn.getGeoIndex(), spawn.getTemplate());
            new Timer().schedule(new TimerTask() {
               @Override
               public void run() {
                  player.sendPacket(new RadarControl(2, 2, loc.getX(), loc.getY(), loc.getZ()));
                  player.sendPacket(new RadarControl(0, 1, loc.getX(), loc.getY(), loc.getZ()));
               }
            }, 500L);
            player.sendPacket(new ShowMiniMap(0));
            break;
         case UNDEFINED:
            ServerMessage msg = new ServerMessage("BossesBBS.BOSS_NOT_INGAME", player.getLang());
            msg.add(bossId);
            player.sendMessage(msg.toString());
      }
   }

   public RaidBossInstance getBossStatus(int bossId) {
      return _bosses.containsKey(bossId) && _bosses.get(bossId).getRaidStatus() == RaidBossSpawnManager.StatusEnum.ALIVE ? _bosses.get(bossId) : null;
   }

   public Map<Integer, Map<Integer, Integer>> getPoints() {
      return _points;
   }

   public Map<Integer, Integer> getClanPoints() {
      return _clanPoints;
   }

   public Map<Integer, Integer> getPointsForOwnerId(int ownerId) {
      return _points.get(ownerId);
   }

   public static final Map<Integer, Integer> getList(Player player) {
      return _points.get(player.getObjectId());
   }

   public static RaidBossSpawnManager getInstance() {
      return RaidBossSpawnManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final RaidBossSpawnManager _instance = new RaidBossSpawnManager();
   }

   private static class SpawnSchedule implements Runnable {
      private static final Logger _log = Logger.getLogger(RaidBossSpawnManager.SpawnSchedule.class.getName());
      private final int bossId;

      public SpawnSchedule(int npcId) {
         this.bossId = npcId;
      }

      @Override
      public void run() {
         RaidBossInstance raidboss = null;
         if (this.bossId == 25328) {
            raidboss = DayNightSpawnManager.getInstance().handleBoss(RaidBossSpawnManager._spawns.get(this.bossId));
         } else {
            raidboss = (RaidBossInstance)RaidBossSpawnManager._spawns.get(this.bossId).doSpawn();
         }

         if (raidboss != null) {
            if (Arrays.binarySearch(Config.RAIDBOSS_ANNOUNCE_LIST, raidboss.getId()) >= 0) {
               for(Player player : World.getInstance().getAllPlayers()) {
                  if (player.isOnline()) {
                     ServerMessage msg = new ServerMessage("Announce.RAID_RESPAWN", player.getLang());
                     msg.add(player.getNpcName(raidboss.getTemplate()));
                     player.sendPacket(new CreatureSay(0, 10, "", msg.toString()));
                  }
               }
            }

            raidboss.setRaidStatus(RaidBossSpawnManager.StatusEnum.ALIVE);
            StatsSet info = new StatsSet();
            info.set("currentHP", raidboss.getCurrentHp());
            info.set("currentMP", raidboss.getCurrentMp());
            info.set("respawnTime", 0L);
            RaidBossSpawnManager._storedInfo.put(this.bossId, info);
            _log.info(this.getClass().getSimpleName() + ": Spawning Raid Boss " + raidboss.getName());
            RaidBossSpawnManager._bosses.put(this.bossId, raidboss);
         }

         RaidBossSpawnManager._schedules.remove(this.bossId);
      }
   }

   public static enum StatusEnum {
      ALIVE,
      DEAD,
      UNDEFINED;
   }
}
