package l2e.gameserver.model.entity;

import java.util.Calendar;
import java.util.List;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.instancemanager.BloodAltarManager;
import l2e.gameserver.model.actor.instance.RaidBossInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.stats.StatsSet;

public abstract class BloodAltarsEngine extends Quest {
   public BloodAltarsEngine(String name, String descr) {
      super(-1, name, descr);
   }

   public abstract boolean changeSpawnInterval(long var1, int var3, int var4);

   public abstract int getStatus();

   public abstract int getProgress();

   protected void updateStatus(String altar, int status) {
      BloodAltarManager.getInstance().updateStatus(altar, status);
   }

   protected void updateProgress(String altar, int progress) {
      BloodAltarManager.getInstance().updateProgress(altar, progress);
   }

   private void updateStatusTime(String altar, long time) {
      long changeStatus = Calendar.getInstance().getTimeInMillis() + time;
      BloodAltarManager.getInstance().updateStatusTime(altar, changeStatus);
   }

   private void cleanBossStatus(String altar) {
      BloodAltarManager.getInstance().cleanBossStatus(altar);
   }

   protected List<Integer> getBossList(String altar) {
      return BloodAltarManager.getInstance().getBossList(altar);
   }

   protected void restoreStatus(String altar) {
      StatsSet info = BloodAltarManager.getInstance().getAltarInfo(altar);
      int status = info.getInteger("status");
      int progress = info.getInteger("progress");
      long time = info.getLong("changeTime");
      if (time > Calendar.getInstance().getTimeInMillis()) {
         long changeStatus = time - Calendar.getInstance().getTimeInMillis();
         if (Config.DEBUG) {
            Calendar timer = Calendar.getInstance();
            timer.setTimeInMillis(time);
            _log.info(this.getClass().getSimpleName() + ": " + this.getName() + " blood altar change status at " + timer.getTime());
         }

         this.changeSpawnInterval(changeStatus, status, progress);
         switch(status) {
            case 0:
               manageBosses(altar, false);
               manageNpcs(altar, true);
               break;
            case 1:
               manageNpcs(altar, false);
               manageBosses(altar, true);
               break;
            case 2:
               manageNpcs(altar, false);
               this.restoreActive(altar);
         }
      } else {
         this.changeStatus(altar, this.getChangeTime(), 0);
      }
   }

   private void restoreActive(String altar) {
      SpawnParser.getInstance().spawnCkeckGroup(altar + "_bloodaltar_bosses", BloodAltarManager.getInstance().getDeadBossList(altar));
   }

   protected void updateBossStatus(String altar, RaidBossInstance boss, int status) {
      BloodAltarManager.getInstance().updateBossStatus(altar, boss, status);
   }

   public void changeStatus(String altar, long time, int status) {
      int newStatus;
      if (Rnd.chance(Config.CHANCE_SPAWN) && status == 0) {
         newStatus = 1;
         manageNpcs(altar, false);
         manageBosses(altar, true);
         this.updateStatus(altar, newStatus);
      } else {
         newStatus = 0;
         manageBosses(altar, false);
         manageNpcs(altar, true);
         this.updateStatus(altar, newStatus);
      }

      this.updateProgress(altar, 0);
      this.changeSpawnInterval(time, newStatus, 0);
      this.updateStatusTime(altar, time);
      this.cleanBossStatus(altar);
   }

   private static void manageNpcs(String altar, boolean spawnAlive) {
      if (spawnAlive) {
         SpawnParser.getInstance().despawnGroup(altar + "_bloodaltar_dead_npc");
         SpawnParser.getInstance().spawnGroup(altar + "_bloodaltar_alive_npc");
      } else {
         SpawnParser.getInstance().despawnGroup(altar + "_bloodaltar_alive_npc");
         SpawnParser.getInstance().spawnGroup(altar + "_bloodaltar_dead_npc");
      }
   }

   private static void manageBosses(String altar, boolean spawn) {
      if (spawn) {
         SpawnParser.getInstance().spawnGroup(altar + "_bloodaltar_bosses");
      } else {
         SpawnParser.getInstance().despawnGroup(altar + "_bloodaltar_bosses");
      }
   }

   public long getChangeTime() {
      long changeStatus = Calendar.getInstance().getTimeInMillis() + (long)(Config.RESPAWN_TIME * 3600) * 1000L;
      Calendar time = Calendar.getInstance();
      time.setTimeInMillis(changeStatus);
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": " + this.getName() + " blood altar change status at " + time.getTime());
      }

      return time.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
   }
}
