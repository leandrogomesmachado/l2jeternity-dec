package l2e.gameserver.taskmanager;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

public abstract class Task {
   protected final Logger _log = Logger.getLogger(this.getClass().getName());

   public void initializate() {
   }

   public ScheduledFuture<?> launchSpecial(TaskManager.ExecutedTask instance) {
      return null;
   }

   public abstract String getName();

   public abstract void onTimeElapsed(TaskManager.ExecutedTask var1);

   public void onDestroy() {
   }
}
