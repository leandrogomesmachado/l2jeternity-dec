package l2e.gameserver.taskmanager.actionrunner.tasks;

import l2e.gameserver.taskmanager.actionrunner.ActionRunner;
import l2e.gameserver.taskmanager.actionrunner.ActionWrapper;

public abstract class AutomaticTask extends ActionWrapper {
   public static final String TASKS = "automatic_tasks";

   public AutomaticTask() {
      super("automatic_tasks");
   }

   public abstract void doTask() throws Exception;

   public abstract long reCalcTime(boolean var1);

   @Override
   public void runImpl0() throws Exception {
      try {
         this.doTask();
      } finally {
         ActionRunner.getInstance().register(this.reCalcTime(false), this);
      }
   }
}
