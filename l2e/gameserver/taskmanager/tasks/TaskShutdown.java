package l2e.gameserver.taskmanager.tasks;

import l2e.gameserver.Shutdown;
import l2e.gameserver.taskmanager.Task;
import l2e.gameserver.taskmanager.TaskManager;

public class TaskShutdown extends Task {
   public static final String NAME = "shutdown";

   @Override
   public String getName() {
      return "shutdown";
   }

   @Override
   public void onTimeElapsed(TaskManager.ExecutedTask task) {
      Shutdown handler = new Shutdown(Integer.parseInt(task.getParams()[2]), false);
      handler.start();
   }
}
