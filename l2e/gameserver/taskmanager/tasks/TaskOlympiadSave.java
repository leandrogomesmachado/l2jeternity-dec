package l2e.gameserver.taskmanager.tasks;

import l2e.gameserver.model.olympiad.Olympiad;
import l2e.gameserver.taskmanager.Task;
import l2e.gameserver.taskmanager.TaskManager;
import l2e.gameserver.taskmanager.TaskTypes;

public class TaskOlympiadSave extends Task {
   public static final String NAME = "olympiad_save";

   @Override
   public String getName() {
      return "olympiad_save";
   }

   @Override
   public void onTimeElapsed(TaskManager.ExecutedTask task) {
      if (Olympiad.getInstance().inCompPeriod()) {
         Olympiad.getInstance().saveOlympiadStatus();
         this._log.info("Olympiad System: Data updated.");
      }
   }

   @Override
   public void initializate() {
      super.initializate();
      TaskManager.addUniqueTask("olympiad_save", TaskTypes.TYPE_FIXED_SHEDULED, "900000", "1800000", "");
   }
}
