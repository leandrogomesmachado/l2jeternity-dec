package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _362_BardsMandolin extends Quest {
   public _362_BardsMandolin(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30957);
      this.addTalkId(new int[]{30957, 30956, 30958, 30837});
      this.questItemIds = new int[]{4316, 4317};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30957-3.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("30957-7.htm") || event.equalsIgnoreCase("30957-8.htm")) {
            st.calcReward(this.getId());
            st.exitQuest(true, true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getCond();
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 15) {
                  htmltext = "30957-1.htm";
               } else {
                  htmltext = "30957-2.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30837:
                     if (cond == 1) {
                        st.setCond(2, true);
                        htmltext = "30837-1.htm";
                     } else if (cond == 2) {
                        htmltext = "30837-2.htm";
                     } else if (cond > 2) {
                        htmltext = "30837-3.htm";
                     }
                     break;
                  case 30956:
                     if (cond == 4) {
                        htmltext = "30956-1.htm";
                        st.setCond(5, true);
                        st.takeItems(4316, 1L);
                        st.takeItems(4317, 1L);
                     } else if (cond == 5) {
                        htmltext = "30956-2.htm";
                     }
                     break;
                  case 30957:
                     if (cond == 1 || cond == 2) {
                        htmltext = "30957-4.htm";
                     } else if (cond == 3) {
                        htmltext = "30957-5.htm";
                        st.setCond(4, true);
                        st.giveItems(4317, 1L);
                     } else if (cond == 4) {
                        htmltext = "30957-5a.htm";
                     } else if (cond == 5) {
                        htmltext = "30957-6.htm";
                     }
                     break;
                  case 30958:
                     if (cond == 2) {
                        htmltext = "30958-1.htm";
                        st.setCond(3, true);
                        st.giveItems(4316, 1L);
                     } else if (cond >= 3) {
                        htmltext = "30958-2.htm";
                     }
               }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _362_BardsMandolin(362, _362_BardsMandolin.class.getSimpleName(), "");
   }
}
