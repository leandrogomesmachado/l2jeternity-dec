package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.instancemanager.DailyTaskManager;
import l2e.gameserver.instancemanager.ServerVariables;
import l2e.gameserver.model.actor.Player;

public class DailyTasks implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_daily", "admin_weekly", "admin_month"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.startsWith("admin_daily")) {
         ServerVariables.set("Daily_Tasks", 0);
         DailyTaskManager.getInstance().checkDailyTimeTask();
         activeChar.sendMessage("Daily Tasks Clean Up!");
      } else if (command.startsWith("admin_weekly")) {
         ServerVariables.set("Weekly_Tasks", 0);
         DailyTaskManager.getInstance().checkWeeklyTimeTask();
         activeChar.sendMessage("Weekly Tasks Clean Up!");
      } else if (command.startsWith("admin_month")) {
         ServerVariables.set("Month_Tasks", 0);
         DailyTaskManager.getInstance().checkMonthTimeTask();
         activeChar.sendMessage("Month Tasks Clean Up!");
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
