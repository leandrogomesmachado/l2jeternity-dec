package l2e.gameserver.taskmanager.tasks;

import l2e.gameserver.Shutdown;
import l2e.gameserver.taskmanager.Task;
import l2e.gameserver.taskmanager.TaskManager;

public final class TaskRestart extends Task {
   public static final String NAME = "restart";

   @Override
   public String getName() {
      return "restart";
   }

   @Override
   public void onTimeElapsed(TaskManager.ExecutedTask task) {
      Shutdown handler = new Shutdown(Integer.parseInt(task.getParams()[2]), true);
      handler.start();
   }
}
