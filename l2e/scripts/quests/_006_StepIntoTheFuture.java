package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _006_StepIntoTheFuture extends Quest {
   public _006_StepIntoTheFuture(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30006);
      this.addTalkId(30006);
      this.addTalkId(30033);
      this.addTalkId(30311);
      this.questItemIds = new int[]{7571};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30006-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("30033-02.htm")) {
            st.giveItems(7571, 1L);
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("30311-03.htm")) {
            st.takeItems(7571, 1L);
            st.setCond(3, true);
         } else if (event.equalsIgnoreCase("30006-06.htm")) {
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
            if (npcId == 30006) {
               if (player.getRace().ordinal() == 0 && player.getLevel() >= 3) {
                  htmltext = "30006-02.htm";
               } else {
                  htmltext = "30006-01.htm";
                  st.exitQuest(true);
               }
            }
            break;
         case 1:
            switch(cond) {
               case 1:
                  if (npcId == 30006) {
                     htmltext = "30006-04.htm";
                  } else if (npcId == 30033) {
                     htmltext = "30033-01.htm";
                     return htmltext;
                  }

                  return htmltext;
               case 2:
                  if (npcId == 30033) {
                     if (st.getQuestItemsCount(7571) > 0L) {
                        htmltext = "30033-03.htm";
                        return htmltext;
                     }
                  } else if (npcId == 30311 && st.getQuestItemsCount(7571) > 0L) {
                     htmltext = "30311-02.htm";
                     return htmltext;
                  }

                  return htmltext;
               case 3:
                  if (npcId == 30006) {
                     htmltext = "30006-05.htm";
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
      new _006_StepIntoTheFuture(6, _006_StepIntoTheFuture.class.getSimpleName(), "");
   }
}
