package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _026_TiredOfWaiting extends Quest {
   public _026_TiredOfWaiting(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30655);
      this.addTalkId(new int[]{30655, 31045});
      this.questItemIds = new int[]{17281};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         int npcId = npc.getId();
         switch(npcId) {
            case 30655:
               if (event.equalsIgnoreCase("30655-04.html")) {
                  st.giveItems(17281, 1L);
                  st.startQuest();
               }
               break;
            case 31045:
               if (event.equalsIgnoreCase("31045-04.html")) {
                  st.takeItems(17281, 1L);
               } else if (event.equalsIgnoreCase("31045-10.html")) {
                  if (st.getCond() == 1) {
                     st.calcReward(this.getId(), 1);
                     st.exitQuest(false, true);
                  }
               } else if (event.equalsIgnoreCase("31045-11.html")) {
                  if (st.getCond() == 1) {
                     st.calcReward(this.getId(), 2);
                     st.exitQuest(false, true);
                  }
               } else if (event.equalsIgnoreCase("31045-12.html") && st.getCond() == 1) {
                  st.calcReward(this.getId(), 3);
                  st.exitQuest(false, true);
               }
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
               if (npcId == 30655) {
                  htmltext = player.getLevel() >= 80 ? "30655-01.htm" : "30655-00.html";
               }
               break;
            case 1:
               if (st.getCond() == 1) {
                  switch(npcId) {
                     case 30655:
                        htmltext = "30655-07.html";
                        break;
                     case 31045:
                        htmltext = st.hasQuestItems(17281) ? "31045-01.html" : "31045-09.html";
                  }
               }
               break;
            case 2:
               if (npcId == 30655) {
                  htmltext = "30655-08.html";
               }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _026_TiredOfWaiting(26, _026_TiredOfWaiting.class.getSimpleName(), "");
   }
}
