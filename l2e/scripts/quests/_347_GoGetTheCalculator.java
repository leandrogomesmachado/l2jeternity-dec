package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _347_GoGetTheCalculator extends Quest {
   private static final String qn = "_347_GoGetTheCalculator";
   private static final int BRUNON = 30526;
   private static final int SILVERA = 30527;
   private static final int SPIRON = 30532;
   private static final int BALANKI = 30533;
   private static final int GEMSTONE_BEAST_CRYSTAL = 4286;
   private static final int CALCULATOR_Q = 4285;
   private static final int CALCULATOR_REAL = 4393;

   public _347_GoGetTheCalculator(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30526);
      this.addTalkId(new int[]{30526, 30527, 30532, 30533});
      this.addKillId(20540);
      this.questItemIds = new int[]{4286};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_347_GoGetTheCalculator");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30526-05.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30533-03.htm")) {
            if (st.getQuestItemsCount(57) >= 100L) {
               htmltext = "30533-02.htm";
               st.takeItems(57, 100L);
               if (st.getInt("cond") == 3) {
                  st.set("cond", "4");
               } else {
                  st.set("cond", "2");
               }

               st.playSound("ItemSound.quest_middle");
            }
         } else if (event.equalsIgnoreCase("30532-02.htm")) {
            if (st.getInt("cond") == 2) {
               st.set("cond", "4");
            } else {
               st.set("cond", "3");
            }

            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30526-08.htm")) {
            st.takeItems(4285, -1L);
            st.giveItems(4393, 1L);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         } else if (event.equalsIgnoreCase("30526-09.htm")) {
            st.takeItems(4285, -1L);
            st.rewardItems(57, 1000L);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_347_GoGetTheCalculator");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 12) {
                  htmltext = "30526-01.htm";
               } else {
                  htmltext = "30526-00.htm";
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30526:
                     if (st.getQuestItemsCount(4285) == 0L) {
                        htmltext = "30526-06.htm";
                     } else {
                        htmltext = "30526-07.htm";
                     }
                     break;
                  case 30527:
                     if (cond < 4) {
                        htmltext = "30527-00.htm";
                     } else if (cond == 4) {
                        htmltext = "30527-01.htm";
                        st.set("cond", "5");
                        st.playSound("ItemSound.quest_middle");
                     } else if (cond == 5) {
                        if (st.getQuestItemsCount(4286) >= 10L) {
                           htmltext = "30527-03.htm";
                           st.set("cond", "6");
                           st.takeItems(4286, -1L);
                           st.giveItems(4285, 1L);
                           st.playSound("ItemSound.quest_middle");
                        } else {
                           htmltext = "30527-02.htm";
                        }
                     } else if (cond == 6) {
                        htmltext = "30527-04.htm";
                     }
                  case 30528:
                  case 30529:
                  case 30530:
                  case 30531:
                  default:
                     break;
                  case 30532:
                     if (cond >= 1 && cond <= 3) {
                        htmltext = "30532-01.htm";
                     } else if (cond >= 4) {
                        htmltext = "30532-05.htm";
                     }
                     break;
                  case 30533:
                     if (cond >= 1 && cond <= 3) {
                        htmltext = "30533-01.htm";
                     } else if (cond >= 4) {
                        htmltext = "30533-04.htm";
                     }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_347_GoGetTheCalculator");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 5) {
            st.dropQuestItems(4286, 1, 10L, 500000, true);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _347_GoGetTheCalculator(347, "_347_GoGetTheCalculator", "");
   }
}
