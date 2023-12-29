package l2e.gameserver.taskmanager.tasks;

import l2e.gameserver.taskmanager.Task;
import l2e.gameserver.taskmanager.TaskManager;

public final class TaskCleanUp extends Task {
   public static final String NAME = "clean_up";

   @Override
   public String getName() {
      return "clean_up";
   }

   @Override
   public void onTimeElapsed(TaskManager.ExecutedTask task) {
      System.runFinalization();
      System.gc();
   }
}
