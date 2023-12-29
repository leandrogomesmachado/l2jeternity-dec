package l2e.gameserver;

import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.StackTrace;
import l2e.gameserver.instancemanager.DayNightSpawnManager;
import l2e.gameserver.model.actor.Creature;

public final class GameTimeController extends Thread {
   private static final Logger _log = Logger.getLogger(GameTimeController.class.getName());
   public static final int TICKS_PER_SECOND = 10;
   public static final int MILLIS_IN_TICK = 100;
   public static final int IG_DAYS_PER_DAY = 6;
   public static final int MILLIS_PER_IG_DAY = 14400000;
   public static final int SECONDS_PER_IG_DAY = 14400;
   public static final int MINUTES_PER_IG_DAY = 240;
   public static final int TICKS_PER_IG_DAY = 144000;
   public static final int TICKS_SUN_STATE_CHANGE = 36000;
   private static GameTimeController _instance;
   private final Set<Creature> _movingObjects = ConcurrentHashMap.newKeySet();
   private final long _referenceTime;

   private GameTimeController() {
      super("GameTimeController");
      super.setDaemon(true);
      super.setPriority(10);
      Calendar c = Calendar.getInstance();
      c.set(11, 0);
      c.set(12, 0);
      c.set(13, 0);
      c.set(14, 0);
      this._referenceTime = c.getTimeInMillis();
      super.start();
   }

   public static final void init() {
      _instance = new GameTimeController();
   }

   public boolean isNowNight() {
      return this.isNight();
   }

   public final int getGameTime() {
      return this.getGameTicks() % 144000 / 100;
   }

   public final int getGameHour() {
      return this.getGameTime() / 60;
   }

   public final int getGameMinute() {
      return this.getGameTime() % 60;
   }

   public final boolean isNight() {
      return this.getGameHour() < 6;
   }

   public final int getGameTicks() {
      return (int)((System.currentTimeMillis() - this._referenceTime) / 100L);
   }

   public final void registerMovingObject(Creature cha) {
      if (cha != null) {
         this._movingObjects.add(cha);
      }
   }

   private final void moveObjects() {
      this._movingObjects.removeIf(Creature::updatePosition);
   }

   public final void stopTimer() {
      super.interrupt();
      _log.log(Level.INFO, "Stopping " + this.getClass().getSimpleName());
   }

   @Override
   public final void run() {
      _log.log(Level.CONFIG, this.getClass().getSimpleName() + ": Started.");
      boolean isNight = this.isNight();
      if (isNight) {
         ThreadPoolManager.getInstance().execute(new Runnable() {
            @Override
            public final void run() {
               DayNightSpawnManager.getInstance().notifyChangeMode();
            }
         });
      }

      while(true) {
         long nextTickTime = System.currentTimeMillis() / 100L * 100L + 100L;

         try {
            this.moveObjects();
         } catch (Throwable var8) {
            StackTrace.displayStackTraceInformation(var8);
         }

         long sleepTime = nextTickTime - System.currentTimeMillis();
         if (sleepTime > 0L) {
            try {
               Thread.sleep(sleepTime);
            } catch (InterruptedException var7) {
            }
         }

         if (this.isNight() != isNight) {
            isNight = !isNight;
            ThreadPoolManager.getInstance().execute(new Runnable() {
               @Override
               public final void run() {
                  DayNightSpawnManager.getInstance().notifyChangeMode();
               }
            });
         }
      }
   }

   public static final GameTimeController getInstance() {
      return _instance;
   }
}
