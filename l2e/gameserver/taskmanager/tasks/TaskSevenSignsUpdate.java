package l2e.gameserver.taskmanager.tasks;

import l2e.gameserver.SevenSigns;
import l2e.gameserver.SevenSignsFestival;
import l2e.gameserver.taskmanager.Task;
import l2e.gameserver.taskmanager.TaskManager;
import l2e.gameserver.taskmanager.TaskTypes;

public class TaskSevenSignsUpdate extends Task {
   private static final String NAME = "seven_signs_update";

   @Override
   public String getName() {
      return "seven_signs_update";
   }

   @Override
   public void onTimeElapsed(TaskManager.ExecutedTask task) {
      try {
         SevenSigns.getInstance().saveSevenSignsStatus();
         if (!SevenSigns.getInstance().isSealValidationPeriod()) {
            SevenSignsFestival.getInstance().saveFestivalData(false);
         }

         this._log.info("SevenSigns: Data updated successfully.");
      } catch (Exception var3) {
         this._log.warning(this.getClass().getSimpleName() + ": SevenSigns: Failed to save Seven Signs configuration: " + var3.getMessage());
      }
   }

   @Override
   public void initializate() {
      super.initializate();
      TaskManager.addUniqueTask("seven_signs_update", TaskTypes.TYPE_FIXED_SHEDULED, "1800000", "1800000", "");
   }
}
