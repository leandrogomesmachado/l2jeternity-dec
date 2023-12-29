package l2e.gameserver.taskmanager.tasks;

import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.taskmanager.Task;
import l2e.gameserver.taskmanager.TaskManager;
import l2e.gameserver.taskmanager.TaskTypes;

public class TaskQuestHwidMap extends Task {
   private static final String NAME = "quest_hwids";

   @Override
   public String getName() {
      return "quest_hwids";
   }

   @Override
   public void onTimeElapsed(TaskManager.ExecutedTask task) {
      QuestManager.getInstance().cleanHwidList();
   }

   @Override
   public void initializate() {
      TaskManager.addUniqueTask("quest_hwids", TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
   }
}
