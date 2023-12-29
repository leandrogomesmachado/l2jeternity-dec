package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _027_ChestCaughtWithABaitOfWind extends Quest {
   public _027_ChestCaughtWithABaitOfWind(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31570);
      this.addTalkId(31570);
      this.addTalkId(31434);
      this.questItemIds = new int[]{7625};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31570-04.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31570-07.htm")) {
            if (st.getQuestItemsCount(6500) > 0L) {
               st.takeItems(6500, 1L);
               st.giveItems(7625, 1L);
               st.setCond(2, true);
            } else {
               htmltext = "31570-08.htm";
            }
         } else if (event.equalsIgnoreCase("31434-02.htm")) {
            if (st.getQuestItemsCount(7625) == 1L) {
               st.takeItems(7625, -1L);
               st.calcReward(this.getId());
               st.exitQuest(false, true);
            } else {
               htmltext = "31434-03.htm";
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
               QuestState qs = player.getQuestState("_050_LanoscosSpecialBait");
               if (qs != null && qs.isCompleted() && player.getLevel() >= 27) {
                  htmltext = "31570-01.htm";
               } else {
                  htmltext = "31570-02.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 31434:
                     switch(cond) {
                        case 2:
                           return "31434-01.htm";
                        default:
                           return htmltext;
                     }
                  case 31570:
                     switch(cond) {
                        case 1:
                           if (st.getQuestItemsCount(6500) == 0L) {
                              htmltext = "31570-06.htm";
                           } else {
                              htmltext = "31570-05.htm";
                           }

                           return htmltext;
                        case 2:
                           htmltext = "31570-09.htm";
                           return htmltext;
                        default:
                           return htmltext;
                     }
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
      new _027_ChestCaughtWithABaitOfWind(27, _027_ChestCaughtWithABaitOfWind.class.getSimpleName(), "");
   }
}
