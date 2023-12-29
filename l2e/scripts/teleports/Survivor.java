package l2e.scripts.teleports;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.BotFunctions;

public class Survivor extends Quest {
   public Survivor(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32632);
      this.addTalkId(32632);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (!event.isEmpty()) {
         if (player.getLevel() < 75) {
            event = "32632-3.htm";
         } else if (st.getQuestItemsCount(57) >= 150000L) {
            if (BotFunctions.getInstance().isAutoTpByIdEnable(player)) {
               BotFunctions.getInstance().getAutoTeleportById(player, player.getLocation(), new Location(-149406, 255247, -80), 1000);
               st.takeItems(57, 150000L);
               return null;
            }

            st.takeItems(57, 150000L);
            player.teleToLocation(-149406, 255247, -80, true);
         }
      }

      return event;
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      return st == null ? null : "32632-1.htm";
   }

   public static void main(String[] args) {
      new Survivor(-1, Survivor.class.getSimpleName(), "teleports");
   }
}
