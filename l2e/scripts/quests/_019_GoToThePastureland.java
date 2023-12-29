package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _019_GoToThePastureland extends Quest {
   public _019_GoToThePastureland(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31302);
      this.addTalkId(31302);
      this.addTalkId(31537);
      this.questItemIds = new int[]{7547};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31302-1.htm")) {
            st.giveItems(7547, 1L);
            st.startQuest();
         } else if (event.equalsIgnoreCase("31537-1.htm")) {
            st.takeItems(7547, 1L);
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
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 63) {
                  htmltext = "31302-0.htm";
               } else {
                  htmltext = "31302-0a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 31302:
                     return "31302-2.htm";
                  case 31537:
                     if (st.getQuestItemsCount(7547) >= 1L) {
                        htmltext = "31537-0.htm";
                     } else {
                        htmltext = "31537-1.htm";
                        st.exitQuest(true);
                     }

                     return htmltext;
                  default:
                     return htmltext;
               }
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _019_GoToThePastureland(19, _019_GoToThePastureland.class.getSimpleName(), "");
   }
}
