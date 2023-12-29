package l2e.gameserver.taskmanager.tasks;

import java.util.Calendar;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.taskmanager.Task;
import l2e.gameserver.taskmanager.TaskManager;
import l2e.gameserver.taskmanager.TaskTypes;

public class TaskClanLeaderApply extends Task {
   private static final String NAME = "clanleaderapply";

   @Override
   public String getName() {
      return "clanleaderapply";
   }

   @Override
   public void onTimeElapsed(TaskManager.ExecutedTask task) {
      Calendar cal = Calendar.getInstance();
      if (cal.get(7) == Config.ALT_CLAN_LEADER_DATE_CHANGE) {
         for(Clan clan : ClanHolder.getInstance().getClans()) {
            if (clan.getNewLeaderId() != 0) {
               ClanMember member = clan.getClanMember(clan.getNewLeaderId());
               if (member != null) {
                  clan.setNewLeader(member);
               }
            }
         }

         this._log.info(this.getClass().getSimpleName() + ": launched.");
      }
   }

   @Override
   public void initializate() {
      TaskManager.addUniqueTask("clanleaderapply", TaskTypes.TYPE_GLOBAL_TASK, "1", Config.ALT_CLAN_LEADER_HOUR_CHANGE, "");
   }
}
