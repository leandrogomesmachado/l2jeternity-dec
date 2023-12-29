package l2e.gameserver.taskmanager.tasks;

import java.util.Calendar;
import l2e.gameserver.instancemanager.SoIManager;
import l2e.gameserver.taskmanager.Task;
import l2e.gameserver.taskmanager.TaskManager;
import l2e.gameserver.taskmanager.TaskTypes;

public class SoIStageUpdater extends Task {
   private static final String NAME = "soi_update";

   @Override
   public String getName() {
      return "soi_update";
   }

   @Override
   public void onTimeElapsed(TaskManager.ExecutedTask task) {
      if (Calendar.getInstance().get(7) == 2) {
         SoIManager.setCurrentStage(1);
         this._log.info("Seed of Infinity update Task: Seed updated successfuly.");
      }
   }

   @Override
   public void initializate() {
      super.initializate();
      TaskManager.addUniqueTask(this.getName(), TaskTypes.TYPE_GLOBAL_TASK, "1", "12:00:00", "");
   }
}
