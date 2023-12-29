package l2e.gameserver.taskmanager.tasks;

import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.taskmanager.Task;
import l2e.gameserver.taskmanager.TaskManager;
import l2e.gameserver.taskmanager.TaskTypes;

public class TaskChatMessage extends Task {
   private static final String NAME = "chatMessages";

   @Override
   public String getName() {
      return "chatMessages";
   }

   @Override
   public void onTimeElapsed(TaskManager.ExecutedTask task) {
      this._log.info("Chat Messages Global Task: launched.");

      for(Player player : World.getInstance().getAllPlayers()) {
         player.restartChatMessages();
      }

      this._log.info("Chat Messages Global Task: completed.");
   }

   @Override
   public void initializate() {
      super.initializate();
      TaskManager.addUniqueTask("chatMessages", TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
   }
}
