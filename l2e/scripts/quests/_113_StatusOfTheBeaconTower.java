package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _113_StatusOfTheBeaconTower extends Quest {
   public _113_StatusOfTheBeaconTower(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31979);
      this.addTalkId(new int[]{31979, 32016});
      this.questItemIds = new int[]{8086};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31979-02.htm")) {
            st.startQuest();
            st.giveItems(8086, 1L);
         } else if (event.equalsIgnoreCase("32016-02.htm")) {
            st.takeItems(8086, 1L);
            st.calcExpAndSp(this.getId());
            st.calcReward(this.getId());
            st.exitQuest(false, true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         switch(st.getState()) {
            case 0:
               if (npcId == 31979) {
                  if (player.getLevel() >= 80) {
                     htmltext = "31979-01.htm";
                  } else {
                     htmltext = "31979-00.htm";
                     st.exitQuest(true);
                  }
               }
               break;
            case 1:
               if (npcId == 31979) {
                  htmltext = "31979-03.htm";
               } else if (npcId == 32016 && st.getQuestItemsCount(8086) == 1L) {
                  htmltext = "32016-01.htm";
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _113_StatusOfTheBeaconTower(113, _113_StatusOfTheBeaconTower.class.getSimpleName(), "");
   }
}
