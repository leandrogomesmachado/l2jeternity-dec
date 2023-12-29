package l2e.gameserver.taskmanager.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.taskmanager.Task;
import l2e.gameserver.taskmanager.TaskManager;
import l2e.gameserver.taskmanager.TaskTypes;

public class TaskDailySkillReuseClean extends Task {
   private static final String NAME = "daily_skill_clean";
   private static final int[] _daily_skills = new int[]{2510, 22180};

   @Override
   public String getName() {
      return "daily_skill_clean";
   }

   @Override
   public void onTimeElapsed(TaskManager.ExecutedTask task) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         for(int skill_id : _daily_skills) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_skills_save WHERE skill_id=?;")) {
               ps.setInt(1, skill_id);
               ps.execute();
            }
         }
      } catch (Exception var38) {
         this._log.severe(this.getClass().getSimpleName() + ": Could not reset daily skill reuse: " + var38);
      }

      this._log.info("Daily skill reuse cleaned.");
   }

   @Override
   public void initializate() {
      super.initializate();
      TaskManager.addUniqueTask("daily_skill_clean", TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
   }
}
