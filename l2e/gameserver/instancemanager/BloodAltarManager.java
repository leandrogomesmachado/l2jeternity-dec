package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.instance.RaidBossInstance;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.stats.StatsSet;

public class BloodAltarManager {
   private static final Logger _log = Logger.getLogger(BloodAltarManager.class.getName());
   protected static final Map<Integer, StatsSet> _bosses = new ConcurrentHashMap<>();
   protected static final Map<Integer, RaidBossInstance> _spawns = new ConcurrentHashMap<>();
   protected static final Map<String, StatsSet> _altars = new ConcurrentHashMap<>();

   protected BloodAltarManager() {
      _bosses.clear();
      _altars.clear();
      _spawns.clear();
      this.loadAltars();
      this.loadBosses();
      _log.info(this.getClass().getSimpleName() + ": Loaded " + _altars.size() + " blood altars and " + _bosses.size() + " destruction bosses.");
   }

   private void loadAltars() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM blood_altars ORDER BY altar_name");
         ResultSet rset = statement.executeQuery();
      ) {
         while(rset.next()) {
            String altar_name = rset.getString("altar_name");
            StatsSet info = new StatsSet();
            info.set("status", rset.getInt("status"));
            info.set("progress", rset.getInt("progress"));
            info.set("changeTime", rset.getLong("changeTime"));
            _altars.put(altar_name, info);
         }
      } catch (SQLException var63) {
         _log.warning(this.getClass().getSimpleName() + ": Couldnt load blood_altars table");
      } catch (Exception var64) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Error while initializing BloodAltarManager: " + var64.getMessage(), (Throwable)var64);
      }
   }

   private void loadBosses() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM destruction_bosses ORDER BY bossId");
         ResultSet rs = statement.executeQuery();
      ) {
         while(rs.next()) {
            int bossId = rs.getInt("bossId");
            StatsSet info = new StatsSet();
            info.set("altar_name", rs.getString("altar_name"));
            info.set("status", rs.getInt("status"));
            info.set("currentHp", rs.getDouble("currentHp"));
            info.set("currentMp", rs.getDouble("currentMp"));
            _bosses.put(bossId, info);
         }
      } catch (SQLException var63) {
         _log.warning(this.getClass().getSimpleName() + ": Couldnt load destruction_bosses table");
      } catch (Exception var64) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Error while initializing BloodAltarManager: " + var64.getMessage(), (Throwable)var64);
      }
   }

   public void addBossSpawn(Spawner spawnDat) {
      if (spawnDat != null) {
         if (!_spawns.containsKey(spawnDat.getId())) {
            StatsSet info = _bosses.get(spawnDat.getId());
            if (info != null) {
               int bossId = spawnDat.getId();
               double currentHP = info.getDouble("currentHp");
               double currentMP = info.getDouble("currentMp");
               spawnDat.stopRespawn();
               RaidBossInstance raidboss = (RaidBossInstance)spawnDat.doSpawn();
               if (raidboss != null) {
                  if (currentHP == 0.0) {
                     raidboss.setCurrentHp(raidboss.getMaxHp());
                  } else {
                     raidboss.setCurrentHp(currentHP);
                  }

                  if (currentMP == 0.0) {
                     raidboss.setCurrentMp(raidboss.getMaxMp());
                  } else {
                     raidboss.setCurrentMp(currentMP);
                  }

                  info.set("currentHp", raidboss.getCurrentHp());
                  info.set("currentMp", raidboss.getCurrentMp());
                  _spawns.put(bossId, raidboss);
               }
            } else {
               _log.info(this.getClass().getSimpleName() + ": Could not load destruction boss #" + spawnDat.getId() + " status in database.");
            }
         }
      }
   }

   public void removeBossSpawn(Spawner spawnDat) {
      if (spawnDat != null && _spawns.containsKey(spawnDat.getId())) {
         _spawns.remove(spawnDat.getId());
      }
   }

   public StatsSet getAltarInfo(String altar) {
      return !_altars.containsKey(altar) ? null : _altars.get(altar);
   }

   public List<Integer> getDeadBossList(String altar) {
      List<Integer> bossList = new ArrayList<>();

      for(int i : _bosses.keySet()) {
         StatsSet info = _bosses.get(i);
         if (info.getString("altar_name").equalsIgnoreCase(altar) && info.getInteger("status") == 1) {
            bossList.add(i);
         }
      }

      return bossList;
   }

   public List<Integer> getBossList(String altar) {
      List<Integer> altar_bosses = new ArrayList<>();

      for(int i : _bosses.keySet()) {
         StatsSet info = _bosses.get(i);
         if (info.getString("altar_name").equalsIgnoreCase(altar)) {
            altar_bosses.add(i);
         }
      }

      return altar_bosses;
   }

   public void cleanBossStatus(String altar) {
      for(int i : _bosses.keySet()) {
         StatsSet info = _bosses.get(i);
         if (info.getString("altar_name").equalsIgnoreCase(altar)) {
            info.set("status", 0);
         }

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement stmt = con.prepareStatement("UPDATE destruction_bosses SET status = ? WHERE bossId = ?");
            stmt.setInt(1, info.getInteger("status"));
            stmt.setInt(2, i);
            stmt.execute();
            stmt.close();
         } catch (Exception var18) {
            _log.warning("Warning: could not clean status for destruction bossId: " + i + " in database!");
         }
      }
   }

   public void updateBossStatus(String altar, RaidBossInstance boss, int status) {
      for(int i : _bosses.keySet()) {
         StatsSet info = _bosses.get(i);
         if (info.getString("altar_name").equalsIgnoreCase(altar) && i == boss.getId()) {
            double currentHP = 0.0;
            double currentMP = 0.0;
            if (status == 1) {
               currentHP = boss.getMaxHp();
               currentMP = boss.getMaxMp();
            } else {
               currentHP = boss.getCurrentHp();
               currentMP = boss.getCurrentMp();
               info.set("status", 0);
            }

            info.set("currentHP", currentHP);
            info.set("currentMP", currentMP);
            info.set("status", status);

            try (Connection con = DatabaseFactory.getInstance().getConnection()) {
               PreparedStatement stmt = con.prepareStatement("UPDATE destruction_bosses SET currentHP = ?, currentMP = ?, status = ? WHERE bossId = ?");
               stmt.setDouble(1, currentHP);
               stmt.setDouble(2, currentMP);
               stmt.setInt(3, status);
               stmt.setInt(4, boss.getId());
               stmt.execute();
               stmt.close();
            } catch (Exception var24) {
               _log.warning("Warning: could not update destruction bossId: " + boss.getId() + " in database!");
            }
         }
      }
   }

   public void updateStatusTime(String altar, long time) {
      if (_altars.containsKey(altar)) {
         StatsSet info = _altars.get(altar);
         info.set("changeTime", time);

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement stmt = con.prepareStatement("UPDATE blood_altars SET changeTime = ? WHERE altar_name = ?");
            stmt.setLong(1, time);
            stmt.setString(2, altar);
            stmt.execute();
            stmt.close();
         } catch (Exception var18) {
            _log.warning("Warning: could not update: " + altar + " time in database!");
         }
      }
   }

   public void updateProgress(String altar, int progress) {
      if (_altars.containsKey(altar)) {
         StatsSet info = _altars.get(altar);
         info.set("progress", progress);

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement stmt = con.prepareStatement("UPDATE blood_altars SET progress = ? WHERE altar_name = ?");
            stmt.setInt(1, progress);
            stmt.setString(2, altar);
            stmt.execute();
            stmt.close();
         } catch (Exception var17) {
            _log.warning("Warning: could not update: " + altar + " in database!");
         }
      }
   }

   public void updateStatus(String altar, int status) {
      if (_altars.containsKey(altar)) {
         StatsSet info = _altars.get(altar);
         info.set("status", status);

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement stmt = con.prepareStatement("UPDATE blood_altars SET status = ? WHERE altar_name = ?");
            stmt.setInt(1, status);
            stmt.setString(2, altar);
            stmt.execute();
            stmt.close();
         } catch (Exception var17) {
            _log.warning("Warning: could not update: " + altar + " in database!");
         }
      }
   }

   public void saveDb() {
      for(int i : _bosses.keySet()) {
         RaidBossInstance boss = _spawns.get(i);
         if (boss != null) {
            StatsSet info = _bosses.get(i);
            if (info != null) {
               this.updateBossStatus(info.getString("altar_name"), boss, 0);
            }
         }
      }

      _spawns.clear();
   }

   public static BloodAltarManager getInstance() {
      return BloodAltarManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final BloodAltarManager _instance = new BloodAltarManager();
   }
}
