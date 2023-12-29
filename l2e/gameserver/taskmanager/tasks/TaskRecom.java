package l2e.gameserver.taskmanager.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.taskmanager.Task;
import l2e.gameserver.taskmanager.TaskManager;
import l2e.gameserver.taskmanager.TaskTypes;

public class TaskRecom extends Task {
   private static final String NAME = "recommendations";

   @Override
   public String getName() {
      return "recommendations";
   }

   @Override
   public void onTimeElapsed(TaskManager.ExecutedTask task) {
      this._log.info("Recommendation Global Task: launched.");

      for(Player player : World.getInstance().getAllPlayers()) {
         player.getRecommendation().restartRecom();
      }

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE `characters` SET `rec_bonus_time`=3600");
      ) {
         statement.execute();
      } catch (Exception var34) {
         this._log.log(Level.WARNING, "Could not update chararacters recommendations!", (Throwable)var34);
      }

      this._log.info("Recommendation Global Task: completed.");
   }

   @Override
   public void initializate() {
      super.initializate();
      TaskManager.addUniqueTask("recommendations", TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
   }
}
