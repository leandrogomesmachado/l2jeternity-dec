package l2e.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.instance.RaidBossInstance;
import l2e.gameserver.model.spawn.Spawner;

public class DayNightSpawnManager {
   private static Logger _log = Logger.getLogger(DayNightSpawnManager.class.getName());
   private final List<Spawner> _dayCreatures = new ArrayList<>();
   private final List<Spawner> _nightCreatures = new ArrayList<>();
   private final Map<Spawner, RaidBossInstance> _bosses = new HashMap<>();

   public static DayNightSpawnManager getInstance() {
      return DayNightSpawnManager.SingletonHolder._instance;
   }

   protected DayNightSpawnManager() {
   }

   public void addDayCreature(Spawner spawnDat) {
      this._dayCreatures.add(spawnDat);
   }

   public void addNightCreature(Spawner spawnDat) {
      this._nightCreatures.add(spawnDat);
   }

   public void spawnDayCreatures() {
      this.spawnCreatures(this._nightCreatures, this._dayCreatures, "night", "day");
   }

   public void spawnNightCreatures() {
      this.spawnCreatures(this._dayCreatures, this._nightCreatures, "day", "night");
   }

   private void spawnCreatures(List<Spawner> unSpawnCreatures, List<Spawner> spawnCreatures, String UnspawnLogInfo, String SpawnLogInfo) {
      try {
         if (!unSpawnCreatures.isEmpty()) {
            int i = 0;

            for(Spawner spawn : unSpawnCreatures) {
               if (spawn != null) {
                  spawn.stopRespawn();
                  Npc last = spawn.getLastSpawn();
                  if (last != null) {
                     last.deleteMe();
                     ++i;
                  }
               }
            }

            _log.info("DayNightSpawnManager: Removed " + i + " " + UnspawnLogInfo + " creatures");
         }

         int i = 0;

         for(Spawner spawnDat : spawnCreatures) {
            if (spawnDat != null) {
               spawnDat.startRespawn();
               spawnDat.doSpawn();
               ++i;
            }
         }

         _log.info("DayNightSpawnManager: Spawned " + i + " " + SpawnLogInfo + " creatures");
      } catch (Exception var9) {
         _log.log(Level.WARNING, "Error while spawning creatures: " + var9.getMessage(), (Throwable)var9);
      }
   }

   private void changeMode(int mode) {
      if (!this._nightCreatures.isEmpty() || !this._dayCreatures.isEmpty()) {
         switch(mode) {
            case 0:
               this.spawnDayCreatures();
               this.specialNightBoss(0);
               break;
            case 1:
               this.spawnNightCreatures();
               this.specialNightBoss(1);
               break;
            default:
               _log.warning("DayNightSpawnManager: Wrong mode sent");
         }
      }
   }

   public DayNightSpawnManager trim() {
      ((ArrayList)this._nightCreatures).trimToSize();
      ((ArrayList)this._dayCreatures).trimToSize();
      return this;
   }

   public void notifyChangeMode() {
      try {
         if (GameTimeController.getInstance().isNight()) {
            this.changeMode(1);
         } else {
            this.changeMode(0);
         }
      } catch (Exception var2) {
         _log.log(Level.WARNING, "Error while notifyChangeMode(): " + var2.getMessage(), (Throwable)var2);
      }
   }

   public void cleanUp() {
      this._nightCreatures.clear();
      this._dayCreatures.clear();
      this._bosses.clear();
   }

   private void specialNightBoss(int mode) {
      try {
         for(Spawner spawn : this._bosses.keySet()) {
            RaidBossInstance boss = this._bosses.get(spawn);
            if (boss != null || mode != 1) {
               if (boss != null || mode != 0) {
                  if (boss != null && boss.getId() == 25328 && boss.getRaidStatus() == RaidBossSpawnManager.StatusEnum.ALIVE) {
                     this.handleHellmans(boss, mode);
                  }

                  return;
               }
            } else {
               boss = (RaidBossInstance)spawn.doSpawn();
               RaidBossSpawnManager.getInstance().notifySpawnNightBoss(boss);
               this._bosses.remove(spawn);
               this._bosses.put(spawn, boss);
            }
         }
      } catch (Exception var5) {
         _log.log(Level.WARNING, "Error while specialNoghtBoss(): " + var5.getMessage(), (Throwable)var5);
      }
   }

   private void handleHellmans(RaidBossInstance boss, int mode) {
      switch(mode) {
         case 0:
            boss.deleteMe();
            _log.info(this.getClass().getSimpleName() + ": Deleting Hellman raidboss");
            break;
         case 1:
            boss.spawnMe();
            boss.setDecayed(false);
            _log.info(this.getClass().getSimpleName() + ": Spawning Hellman raidboss");
      }
   }

   public RaidBossInstance handleBoss(Spawner spawnDat) {
      if (this._bosses.containsKey(spawnDat)) {
         return this._bosses.get(spawnDat);
      } else if (GameTimeController.getInstance().isNight()) {
         RaidBossInstance raidboss = (RaidBossInstance)spawnDat.doSpawn();
         this._bosses.put(spawnDat, raidboss);
         return raidboss;
      } else {
         this._bosses.put(spawnDat, null);
         return null;
      }
   }

   private static class SingletonHolder {
      protected static final DayNightSpawnManager _instance = new DayNightSpawnManager();
   }
}
