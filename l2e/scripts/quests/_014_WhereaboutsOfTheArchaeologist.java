package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _014_WhereaboutsOfTheArchaeologist extends Quest {
   public _014_WhereaboutsOfTheArchaeologist(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31263);
      this.addTalkId(31263);
      this.addTalkId(31538);
      this.questItemIds = new int[]{7253};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31263-2.htm")) {
            st.giveItems(7253, 1L);
            st.startQuest();
         } else if (event.equalsIgnoreCase("31538-1.htm")) {
            st.takeItems(7253, 1L);
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
               if (player.getLevel() >= 74) {
                  htmltext = "31263-0.htm";
               } else {
                  htmltext = "31263-1.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 31263:
                     if (st.getCond() == 1) {
                        htmltext = "31263-2.htm";
                     }

                     return htmltext;
                  case 31538:
                     if (st.getCond() == 1 && st.getQuestItemsCount(7253) == 1L) {
                        htmltext = "31538-0.htm";
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
      new _014_WhereaboutsOfTheArchaeologist(14, _014_WhereaboutsOfTheArchaeologist.class.getSimpleName(), "");
   }
}
