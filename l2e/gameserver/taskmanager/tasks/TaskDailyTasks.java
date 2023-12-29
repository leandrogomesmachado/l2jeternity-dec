package l2e.gameserver.taskmanager.tasks;

import l2e.gameserver.instancemanager.DailyTaskManager;
import l2e.gameserver.instancemanager.ServerVariables;
import l2e.gameserver.taskmanager.Task;
import l2e.gameserver.taskmanager.TaskManager;
import l2e.gameserver.taskmanager.TaskTypes;

public class TaskDailyTasks extends Task {
   private static final String NAME = "daily_tasks";

   @Override
   public String getName() {
      return "daily_tasks";
   }

   @Override
   public void onTimeElapsed(TaskManager.ExecutedTask task) {
      ServerVariables.set("Daily_Tasks", 0);
      DailyTaskManager.getInstance().checkDailyTimeTask();
   }

   @Override
   public void initializate() {
      TaskManager.addUniqueTask("daily_tasks", TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
   }
}
