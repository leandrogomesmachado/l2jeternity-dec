package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Util;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.service.autofarm.FarmSettings;

public final class AutoFarmManager {
   private static final Logger _log = Logger.getLogger(AutoFarmManager.class.getName());
   private final Map<String, Integer> _activeFarms = new HashMap<>();
   private final List<Integer> _nonCheckPlayers = new ArrayList<>();
   private ScheduledFuture<?> _farmTask = null;

   protected AutoFarmManager() {
      this._activeFarms.clear();
      this._nonCheckPlayers.clear();
      this.checkTimeTask();
   }

   public int getActiveFarms(String hwid) {
      if (FarmSettings.FARM_ACTIVE_LIMITS < 0) {
         return Integer.MAX_VALUE;
      } else {
         return this._activeFarms.containsKey(hwid) ? FarmSettings.FARM_ACTIVE_LIMITS - this._activeFarms.get(hwid) : FarmSettings.FARM_ACTIVE_LIMITS;
      }
   }

   public void addActiveFarm(String hwid, int charId) {
      if (FarmSettings.FARM_ACTIVE_LIMITS >= 0) {
         if (this._activeFarms.containsKey(hwid)) {
            if (!this.isNonCheckPlayer(charId)) {
               int activeHwids = this._activeFarms.get(hwid) + 1;
               this._activeFarms.put(hwid, activeHwids);
            }
         } else {
            this._activeFarms.put(hwid, 1);
         }
      }
   }

   public void removeActiveFarm(String hwid, int charId) {
      if (FarmSettings.FARM_ACTIVE_LIMITS >= 0) {
         if (this._activeFarms.containsKey(hwid)) {
            if (this.isNonCheckPlayer(charId)) {
               return;
            }

            int activeHwids = this._activeFarms.get(hwid) - 1;
            this._activeFarms.put(hwid, activeHwids);
         }
      }
   }

   public void addNonCheckPlayer(int charId) {
      if (FarmSettings.FARM_ACTIVE_LIMITS >= 0) {
         if (!this._nonCheckPlayers.contains(charId)) {
            this._nonCheckPlayers.add(charId);
         }
      }
   }

   public boolean isNonCheckPlayer(int charId) {
      return this._nonCheckPlayers.contains(charId);
   }

   private void checkTimeTask() {
      if (FarmSettings.REFRESH_FARM_TIME) {
         long lastUpdate = ServerVariables.getLong("Farm_FreeTime", 0L);
         if (System.currentTimeMillis() > lastUpdate) {
            this.cleanFarmFreeTime();
         } else {
            this._farmTask = ThreadPoolManager.getInstance().schedule(new AutoFarmManager.ClearFarmFreeTime(), lastUpdate - System.currentTimeMillis());
         }
      }
   }

   private void cleanFarmFreeTime() {
      if (FarmSettings.REFRESH_FARM_TIME) {
         Calendar newTime = Calendar.getInstance();
         newTime.setLenient(true);
         newTime.set(11, 6);
         newTime.set(12, 30);
         newTime.add(5, 1);
         ServerVariables.set("Farm_FreeTime", newTime.getTimeInMillis());

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("DELETE FROM character_variables WHERE name = ?");
         ) {
            statement.setString(1, "farmFreeTime");
            statement.execute();
         } catch (Exception var34) {
            _log.log(Level.SEVERE, "Failed to clean up autoFarm free times datas.", (Throwable)var34);
         }

         for(Player player : World.getInstance().getAllPlayers()) {
            if (player != null && !player.isFakePlayer()) {
               player.unsetVar("farmFreeTime");
            }
         }

         if (this._farmTask != null) {
            this._farmTask.cancel(false);
            this._farmTask = null;
         }

         this._farmTask = ThreadPoolManager.getInstance()
            .schedule(new AutoFarmManager.ClearFarmFreeTime(), newTime.getTimeInMillis() - System.currentTimeMillis());
         _log.info("AutoFarmManager: AutoFarm free times reshresh completed.");
         _log.info(
            "AutoFarmManager: Next autoFarm free times reshresh at: " + Util.formatTime((int)(newTime.getTimeInMillis() - System.currentTimeMillis()) / 1000)
         );
      }
   }

   public static final AutoFarmManager getInstance() {
      return AutoFarmManager.SingletonHolder._instance;
   }

   private class ClearFarmFreeTime extends RunnableImpl {
      private ClearFarmFreeTime() {
      }

      @Override
      public void runImpl() {
         AutoFarmManager.this.cleanFarmFreeTime();
      }
   }

   private static class SingletonHolder {
      protected static final AutoFarmManager _instance = new AutoFarmManager();
   }
}
