package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _013_ParcelDelivery extends Quest {
   public _013_ParcelDelivery(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31274);
      this.addTalkId(31274);
      this.addTalkId(31539);
      this.questItemIds = new int[]{7263};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31274-2.htm")) {
            st.giveItems(7263, 1L);
            st.startQuest();
         } else if (event.equalsIgnoreCase("31539-1.htm")) {
            st.takeItems(7263, 1L);
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
                  htmltext = "31274-0.htm";
               } else {
                  htmltext = "31274-1.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 31274:
                     if (st.getCond() == 1) {
                        htmltext = "31274-2.htm";
                     }

                     return htmltext;
                  case 31539:
                     if (st.getCond() == 1 && st.getQuestItemsCount(7263) == 1L) {
                        htmltext = "31539-0.htm";
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
      new _013_ParcelDelivery(13, _013_ParcelDelivery.class.getSimpleName(), "");
   }
}
