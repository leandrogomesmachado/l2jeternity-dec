package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _130_PathToHellbound extends Quest {
   private static final String qn = "_130_PathToHellbound";
   public static final int GALATE = 32292;
   public static final int CASIAN = 30612;
   public static final int CASIAN_BLUE_CRY = 12823;

   public _130_PathToHellbound(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30612);
      this.addTalkId(30612);
      this.addTalkId(32292);
      this.questItemIds = new int[]{12823};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_130_PathToHellbound");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30612-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
         } else if (event.equalsIgnoreCase("32292-03.htm")) {
            st.set("cond", "2");
         } else if (event.equalsIgnoreCase("30612-05.htm")) {
            st.set("cond", "3");
            st.giveItems(12823, 1L);
         } else if (event.equalsIgnoreCase("32292-06.htm")) {
            st.takeItems(12823, -1L);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_130_PathToHellbound");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (npcId == 30612) {
                  if (player.getLevel() >= 78) {
                     htmltext = "30612-01.htm";
                  } else {
                     st.exitQuest(true);
                     htmltext = "30612-00.htm";
                  }
               }
               break;
            case 1:
               switch(npcId) {
                  case 30612:
                     switch(cond) {
                        case 1:
                           return "30612-03a.htm";
                        case 2:
                           return "30612-04.htm";
                        case 3:
                           return "30612-05a.htm";
                        default:
                           return htmltext;
                     }
                  case 32292:
                     switch(cond) {
                        case 1:
                           return "32292-01.htm";
                        case 2:
                           return "32292-03a.htm";
                        case 3:
                           if (st.getQuestItemsCount(12823) == 1L) {
                              htmltext = "32292-04.htm";
                           } else {
                              htmltext = "Incorrect item count";
                           }

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
      new _130_PathToHellbound(130, "_130_PathToHellbound", "");
   }
}
