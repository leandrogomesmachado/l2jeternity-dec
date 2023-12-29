package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _007_ATripBegins extends Quest {
   public _007_ATripBegins(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30146);
      this.addTalkId(new int[]{30146, 30148, 30154});
      this.questItemIds = new int[]{7572};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30146-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("30148-02.htm")) {
            st.giveItems(7572, 1L);
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("30154-02.htm")) {
            st.takeItems(7572, 1L);
            st.setCond(3, true);
         } else if (event.equalsIgnoreCase("30146-06.htm")) {
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
            if (npcId == 30146) {
               if (player.getRace().ordinal() == 1 && player.getLevel() >= 3) {
                  htmltext = "30146-02.htm";
               } else {
                  htmltext = "30146-01.htm";
                  st.exitQuest(true);
               }
            }
            break;
         case 1:
            switch(cond) {
               case 1:
                  if (npcId == 30146) {
                     htmltext = "30146-04.htm";
                  } else if (npcId == 30148 && st.getQuestItemsCount(7572) == 0L) {
                     return "30148-01.htm";
                  }

                  return htmltext;
               case 2:
                  if (npcId == 30148) {
                     htmltext = "30148-03.htm";
                  } else if (npcId == 30154 && st.getQuestItemsCount(7572) > 0L) {
                     return "30154-01.htm";
                  }

                  return htmltext;
               case 3:
                  if (npcId == 30146) {
                     htmltext = "30146-05.htm";
                  } else if (npcId == 30154) {
                     htmltext = "30154-03.htm";
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
      new _007_ATripBegins(7, _007_ATripBegins.class.getSimpleName(), "");
   }
}
