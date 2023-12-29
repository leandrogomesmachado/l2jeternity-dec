package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _618_IntoTheFlame extends Quest {
   private static final String qn = "_618_IntoTheFlame";
   private static final int KLEIN = 31540;
   private static final int HILDA = 31271;
   private static final int VACUALITE_ORE = 7265;
   private static final int VACUALITE = 7266;
   private static final int FLOATING_STONE = 7267;

   public _618_IntoTheFlame(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31540);
      this.addTalkId(new int[]{31540, 31271});
      this.addKillId(new int[]{21274, 21275, 21276, 21277, 21282, 21283, 21284, 21285, 21290, 21291, 21292, 21293});
      this.questItemIds = new int[]{7265, 7266};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_618_IntoTheFlame");
      if (st == null) {
         return event;
      } else {
         int cond = st.getInt("cond");
         if (event.equalsIgnoreCase("31540-03.htm")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("31540-05.htm")) {
            if (cond == 4 && st.getQuestItemsCount(7266) > 0L) {
               st.takeItems(7266, 1L);
               st.giveItems(7267, 1L);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(true);
            } else {
               htmltext = "31540-03.htm";
            }
         } else if (event.equalsIgnoreCase("31271-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("31271-05.htm")) {
            if (cond == 3 && st.getQuestItemsCount(7265) == 50L) {
               st.takeItems(7265, -1L);
               st.giveItems(7266, 1L);
               st.set("cond", "4");
               st.playSound("ItemSound.quest_middle");
            } else {
               htmltext = "31271-03.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_618_IntoTheFlame");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < 60) {
                  htmltext = "31540-01.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "31540-02.htm";
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               switch(npc.getId()) {
                  case 31271:
                     if (cond == 1) {
                        htmltext = "31271-01.htm";
                     } else if (cond == 3 && st.getQuestItemsCount(7265) == 50L) {
                        htmltext = "31271-04.htm";
                     } else if (cond == 4) {
                        htmltext = "31271-06.htm";
                     } else {
                        htmltext = "31271-03.htm";
                     }
                     break;
                  case 31540:
                     if (cond == 4 && st.getQuestItemsCount(7266) > 0L) {
                        htmltext = "31540-04.htm";
                     } else {
                        htmltext = "31540-03.htm";
                     }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 2);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState("_618_IntoTheFlame");
         if (st.dropQuestItems(7265, 1, 50L, 500000, true)) {
            st.set("cond", "3");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _618_IntoTheFlame(618, "_618_IntoTheFlame", "");
   }
}
