package l2e.gameserver.taskmanager.tasks;

import l2e.gameserver.instancemanager.RevengeManager;
import l2e.gameserver.instancemanager.ServerVariables;
import l2e.gameserver.taskmanager.Task;
import l2e.gameserver.taskmanager.TaskManager;
import l2e.gameserver.taskmanager.TaskTypes;

public class TaskRevenges extends Task {
   private static final String NAME = "revenge_task";

   @Override
   public String getName() {
      return "revenge_task";
   }

   @Override
   public void onTimeElapsed(TaskManager.ExecutedTask task) {
      ServerVariables.set("Revenge_Task", 0);
      RevengeManager.getInstance().cleanUpDatas();
   }

   @Override
   public void initializate() {
      TaskManager.addUniqueTask("revenge_task", TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
   }
}
