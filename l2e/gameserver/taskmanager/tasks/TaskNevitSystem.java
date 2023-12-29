package l2e.gameserver.taskmanager.tasks;

import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.taskmanager.Task;
import l2e.gameserver.taskmanager.TaskManager;
import l2e.gameserver.taskmanager.TaskTypes;

public class TaskNevitSystem extends Task {
   private static final String NAME = "sp_navitsystem";

   @Override
   public String getName() {
      return "sp_navitsystem";
   }

   @Override
   public void onTimeElapsed(TaskManager.ExecutedTask task) {
      this._log.info("Navit System Global Task: launched.");

      for(Player player : World.getInstance().getAllPlayers()) {
         player.getNevitSystem().restartSystem();
      }

      this._log.info("Navit System Task: completed.");
   }

   @Override
   public void initializate() {
      TaskManager.addUniqueTask("sp_navitsystem", TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
   }
}
