package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _029_ChestCaughtWithABaitOfEarth extends Quest {
   public _029_ChestCaughtWithABaitOfEarth(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31574);
      this.addTalkId(31574);
      this.addTalkId(30909);
      this.questItemIds = new int[]{7627};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31574-04.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31574-07.htm")) {
            if (st.getQuestItemsCount(6507) > 0L) {
               st.setCond(2, true);
               st.takeItems(6507, 1L);
               st.giveItems(7627, 1L);
            } else {
               htmltext = "31574-08.htm";
            }
         } else if (event.equalsIgnoreCase("30909-02.htm")) {
            if (st.getQuestItemsCount(7627) == 1L) {
               htmltext = "30909-02.htm";
               st.takeItems(7627, -1L);
               st.calcReward(this.getId());
               st.exitQuest(false, true);
            } else {
               htmltext = "30909-03.htm";
               st.exitQuest(true);
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getCond();
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < 48) {
                  QuestState qs = player.getQuestState("_052_WilliesSpecialBait");
                  if (qs == null || !qs.isCompleted()) {
                     htmltext = "31574-03.htm";
                     st.exitQuest(true);
                  } else if (qs.isCompleted()) {
                     htmltext = "31574-01.htm";
                  } else {
                     htmltext = "31574-02.htm";
                     st.exitQuest(true);
                  }
               }
               break;
            case 1:
               if (npc.getId() == 31574) {
                  if (cond == 1) {
                     htmltext = "31574-05.htm";
                     if (st.getQuestItemsCount(6507) == 0L) {
                        htmltext = "31574-06.htm";
                     }
                  } else if (cond == 2) {
                     htmltext = "31574-09.htm";
                  }
               } else if (npc.getId() == 30909 && cond == 2) {
                  htmltext = "30909-01.htm";
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _029_ChestCaughtWithABaitOfEarth(29, _029_ChestCaughtWithABaitOfEarth.class.getSimpleName(), "");
   }
}
