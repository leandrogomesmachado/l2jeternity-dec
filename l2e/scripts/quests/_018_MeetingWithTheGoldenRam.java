package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _018_MeetingWithTheGoldenRam extends Quest {
   public _018_MeetingWithTheGoldenRam(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31314);
      this.addTalkId(31314);
      this.addTalkId(31315);
      this.addTalkId(31555);
      this.questItemIds = new int[]{7245};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31314-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31315-02.htm")) {
            st.giveItems(7245, 1L);
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("31555-02.htm")) {
            st.takeItems(7245, 1L);
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
         int cond = st.getCond();
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 66) {
                  htmltext = "31314-01.htm";
               } else {
                  htmltext = "31314-02.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 31314:
                     if (cond == 1) {
                        htmltext = "31314-04.htm";
                     }

                     return htmltext;
                  case 31315:
                     switch(cond) {
                        case 1:
                           return "31315-01.htm";
                        case 2:
                           return "31315-03.htm";
                        default:
                           return htmltext;
                     }
                  case 31555:
                     if (cond == 2 && st.getQuestItemsCount(7245) == 1L) {
                        htmltext = "31555-01.htm";
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
      new _018_MeetingWithTheGoldenRam(18, _018_MeetingWithTheGoldenRam.class.getSimpleName(), "");
   }
}
