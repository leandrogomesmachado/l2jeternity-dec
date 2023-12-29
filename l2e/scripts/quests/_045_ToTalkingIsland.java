package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _045_ToTalkingIsland extends Quest {
   public _045_ToTalkingIsland(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30097);
      this.addTalkId(30097);
      this.addTalkId(30094);
      this.addTalkId(30090);
      this.addTalkId(30116);
      this.questItemIds = new int[]{7563, 7564, 7565, 7568, 7567, 7566};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            st.startQuest();
            st.giveItems(7563, 1L);
            htmltext = "30097-03.htm";
         } else if (event.equalsIgnoreCase("2")) {
            st.takeItems(7563, 1L);
            st.giveItems(7568, 1L);
            htmltext = "30094-02.htm";
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("3")) {
            st.takeItems(7568, 1L);
            st.giveItems(7564, 1L);
            htmltext = "30097-06.htm";
            st.setCond(3, true);
         } else if (event.equalsIgnoreCase("4")) {
            st.takeItems(7564, 1L);
            st.giveItems(7567, 1L);
            htmltext = "30090-02.htm";
            st.setCond(4, true);
         } else if (event.equalsIgnoreCase("5")) {
            st.takeItems(7567, 1L);
            st.giveItems(7565, 1L);
            htmltext = "30097-09.htm";
            st.setCond(5, true);
         } else if (event.equalsIgnoreCase("6")) {
            st.takeItems(7565, 1L);
            st.giveItems(7566, 1L);
            htmltext = "30116-02.htm";
            st.setCond(6, true);
         } else if (event.equalsIgnoreCase("7")) {
            st.takeItems(7566, 1L);
            htmltext = "30097-12.htm";
            st.calcReward(this.getId());
            st.exitQuest(false, true);
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
               if (player.getRace().ordinal() == 0 && st.getQuestItemsCount(7570) > 0L) {
                  htmltext = "30097-02.htm";
               } else {
                  htmltext = "30097-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30090:
                     switch(cond) {
                        case 3:
                           return "30090-01.htm";
                        case 4:
                           return "30090-03.htm";
                        default:
                           return htmltext;
                     }
                  case 30094:
                     switch(cond) {
                        case 1:
                           return "30094-01.htm";
                        case 2:
                           return "30094-03.htm";
                        default:
                           return htmltext;
                     }
                  case 30097:
                     switch(cond) {
                        case 1:
                           return "30097-04.htm";
                        case 2:
                           return "30097-05.htm";
                        case 3:
                           return "30097-07.htm";
                        case 4:
                           return "30097-08.htm";
                        case 5:
                           return "30097-10.htm";
                        case 6:
                           return "30097-11.htm";
                        default:
                           return htmltext;
                     }
                  case 30116:
                     switch(cond) {
                        case 5:
                           return "30116-01.htm";
                        case 6:
                           htmltext = "30116-03.htm";
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
      new _045_ToTalkingIsland(45, _045_ToTalkingIsland.class.getSimpleName(), "");
   }
}
