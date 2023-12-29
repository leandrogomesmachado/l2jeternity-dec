package l2e.scripts.teleports;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.BotFunctions;

public class TeleportCube extends Quest {
   public TeleportCube(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32107);
      this.addTalkId(32107);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = "";
      QuestState st = player.getQuestState(this.getName());
      int npcId = npc.getId();
      if (npcId == 32107) {
         if (BotFunctions.getInstance().isAutoTpByIdEnable(player)) {
            BotFunctions.getInstance().getAutoTeleportById(player, player.getLocation(), new Location(10468, -24569, -3650), 1000);
            return null;
         } else {
            player.teleToLocation(10468, -24569, -3650, true);
            return null;
         }
      } else {
         st.exitQuest(true);
         return "";
      }
   }

   public static void main(String[] args) {
      new TeleportCube(-1, TeleportCube.class.getSimpleName(), "teleports");
   }
}
