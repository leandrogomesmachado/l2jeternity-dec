package l2e.gameserver;

import java.util.Date;
import java.util.logging.Logger;
import l2e.commons.time.cron.SchedulingPattern;

public class AutoRestart {
   protected static final Logger _log = Logger.getLogger(AutoRestart.class.getName());
   private long _nextRestart;
   private static AutoRestart _instance;

   protected AutoRestart() {
      this.startCalculationOfNextRestartTime();
      if (this.getRestartNextTime() <= 0L) {
         _log.info("[Auto Restart]: System is disabled.");
      }
   }

   public long getRestartNextTime() {
      return this._nextRestart > System.currentTimeMillis() ? (this._nextRestart - System.currentTimeMillis()) / 1000L : 0L;
   }

   private void startCalculationOfNextRestartTime() {
      try {
         SchedulingPattern cronTime;
         try {
            cronTime = new SchedulingPattern(Config.AUTO_RESTART_PATTERN);
         } catch (SchedulingPattern.InvalidPatternException var4) {
            return;
         }

         long nextRestart = cronTime.next(System.currentTimeMillis());
         if (nextRestart > System.currentTimeMillis()) {
            this._nextRestart = nextRestart + (long)Config.AUTO_RESTART_TIME * 1000L;
            _log.info("[Auto Restart]: System activated.");
            _log.info("[Auto Restart]: Next restart - " + new Date(this._nextRestart));
            ThreadPoolManager.getInstance().schedule(new AutoRestart.RestartTask(), nextRestart - System.currentTimeMillis());
         }
      } catch (Exception var5) {
         _log.warning("[Auto Restart]: Has problem with the config file, please, check and correct it.!");
      }
   }

   public static AutoRestart getInstance() {
      if (_instance == null) {
         _instance = new AutoRestart();
      }

      return _instance;
   }

   private class RestartTask implements Runnable {
      private RestartTask() {
      }

      @Override
      public void run() {
         AutoRestart._log.info("[Auto Restart]: Auto restart started.");
         Shutdown.getInstance().autoRestart(Config.AUTO_RESTART_TIME);
      }
   }
}
