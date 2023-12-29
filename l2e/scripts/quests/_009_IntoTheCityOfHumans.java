package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _009_IntoTheCityOfHumans extends Quest {
   public _009_IntoTheCityOfHumans(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30583);
      this.addTalkId(30583);
      this.addTalkId(30571);
      this.addTalkId(30576);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30583-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("30571-02.htm")) {
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("30576-02.htm")) {
            st.calcReward(this.getId());
            st.exitQuest(false, true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      String htmltext = getNoQuestMsg(player);
      int cond = st.getCond();
      int npcId = npc.getId();
      switch(st.getState()) {
         case 0:
            if (npcId == 30583) {
               if (player.getRace().ordinal() == 3 && player.getLevel() >= 3) {
                  htmltext = "30583-02.htm";
               } else {
                  htmltext = "30583-01.htm";
                  st.exitQuest(true);
               }
            }
            break;
         case 1:
            switch(cond) {
               case 1:
                  if (npcId == 30583) {
                     htmltext = "30583-04.htm";
                  } else if (npcId == 30571) {
                     return "30571-01.htm";
                  }

                  return htmltext;
               case 2:
                  if (npcId == 30571) {
                     htmltext = "30571-03.htm";
                  } else if (npcId == 30576) {
                     htmltext = "30576-01.htm";
                     return htmltext;
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

   public static void main(String[] args) {
      new _009_IntoTheCityOfHumans(9, _009_IntoTheCityOfHumans.class.getSimpleName(), "");
   }
}
