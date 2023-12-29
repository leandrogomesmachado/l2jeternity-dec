package l2e.gameserver.taskmanager.tasks;

import l2e.gameserver.instancemanager.GlobalVariablesManager;
import l2e.gameserver.taskmanager.Task;
import l2e.gameserver.taskmanager.TaskManager;
import l2e.gameserver.taskmanager.TaskTypes;

public class TaskGlobalVariablesSave extends Task {
   public static final String NAME = "global_varibales_save";

   @Override
   public String getName() {
      return "global_varibales_save";
   }

   @Override
   public void onTimeElapsed(TaskManager.ExecutedTask task) {
      GlobalVariablesManager.getInstance().storeMe();
   }

   @Override
   public void initializate() {
      super.initializate();
      TaskManager.addUniqueTask("global_varibales_save", TaskTypes.TYPE_FIXED_SHEDULED, "500000", "1800000", "");
   }
}
