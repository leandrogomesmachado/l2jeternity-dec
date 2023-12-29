package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10274_CollectingInTheAir extends Quest {
   public _10274_CollectingInTheAir(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32557);
      this.addTalkId(32557);
      this.questItemIds = new int[]{13844, 13858, 13859, 13860};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32557-03.htm")) {
            st.startQuest();
            st.giveItems(13844, 8L);
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
               QuestState qs = player.getQuestState("_10273_GoodDayToFly");
               if (qs != null) {
                  htmltext = qs.isCompleted() && player.getLevel() >= 75 ? "32557-01.htm" : "32557-00.htm";
               } else {
                  htmltext = "32557-00.htm";
               }
               break;
            case 1:
               if (st.getQuestItemsCount(13858) + st.getQuestItemsCount(13859) + st.getQuestItemsCount(13860) >= 8L) {
                  htmltext = "32557-05.htm";
                  st.calcExpAndSp(this.getId());
                  st.calcReward(this.getId());
                  st.exitQuest(false, true);
               } else {
                  htmltext = "32557-04.htm";
               }
               break;
            case 2:
               htmltext = "32557-0a.htm";
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _10274_CollectingInTheAir(10274, _10274_CollectingInTheAir.class.getSimpleName(), "");
   }
}
