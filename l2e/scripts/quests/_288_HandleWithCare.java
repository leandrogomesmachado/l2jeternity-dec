package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _288_HandleWithCare extends Quest {
   public _288_HandleWithCare(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32741);
      this.addTalkId(32741);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (npc.getId() == 32741) {
            if (event.equalsIgnoreCase("32741-03.htm")) {
               st.startQuest();
            } else if (event.equalsIgnoreCase("32741-07.htm")) {
               if (st.hasQuestItems(15498)) {
                  st.takeItems(15498, 1L);
                  st.calcReward(this.getId(), 1, true);
               } else if (st.hasQuestItems(15497)) {
                  st.takeItems(15497, 1L);
                  st.calcReward(this.getId(), 1, true);
                  st.calcReward(this.getId(), 2);
               }

               st.exitQuest(true, true);
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
         if (npc.getId() == 32741) {
            switch(st.getState()) {
               case 0:
                  if (player.getLevel() >= 82) {
                     htmltext = "32741-01.htm";
                  } else {
                     htmltext = "32741-00.htm";
                  }
                  break;
               case 1:
                  if (st.isCond(2) && st.hasQuestItems(15498)) {
                     htmltext = "32741-05.htm";
                  } else if (st.isCond(3) && st.hasQuestItems(15497)) {
                     htmltext = "32741-06.htm";
                  } else {
                     htmltext = "32741-04.htm";
                  }
            }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _288_HandleWithCare(288, _288_HandleWithCare.class.getSimpleName(), "");
   }
}
