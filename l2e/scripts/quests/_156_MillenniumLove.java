package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _156_MillenniumLove extends Quest {
   private static final String qn = "_156_MillenniumLove";
   private static final int LILITH = 30368;
   private static final int BAENEDES = 30369;
   private static final int RYLITHS_LETTER = 1022;
   private static final int THEONS_DIARY = 1023;

   public _156_MillenniumLove(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30368);
      this.addTalkId(30368);
      this.addTalkId(30369);
      this.questItemIds = new int[]{1022, 1023};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_156_MillenniumLove");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30368-04.htm")) {
            st.set("cond", "1");
            st.giveItems(1022, 1L);
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30369-02.htm")) {
            st.set("cond", "2");
            st.takeItems(1022, -1L);
            st.giveItems(1023, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30369-03.htm")) {
            st.takeItems(1022, -1L);
            st.addExpAndSp(3000, 0);
            st.playSound("ItemSound.quest_finish");
            st.unset("cond");
            st.exitQuest(false);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_156_MillenniumLove");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 15 && player.getLevel() <= 19) {
                  htmltext = "30368-01.htm";
               } else {
                  htmltext = "30368-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30368:
                     if (st.getQuestItemsCount(1022) == 1L) {
                        htmltext = "30368-05.htm";
                     } else if (st.getQuestItemsCount(1023) == 1L) {
                        htmltext = "30368-06.htm";
                        st.takeItems(1023, -1L);
                        st.giveItems(5250, 1L);
                        st.addExpAndSp(3000, 0);
                        st.playSound("ItemSound.quest_finish");
                        st.unset("cond");
                        st.exitQuest(false);
                        return htmltext;
                     }

                     return htmltext;
                  case 30369:
                     if (st.getQuestItemsCount(1022) == 1L) {
                        htmltext = "30369-01.htm";
                     } else if (st.getQuestItemsCount(1023) == 1L) {
                        htmltext = "30369-04.htm";
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
   }

   public static void main(String[] args) {
      new _156_MillenniumLove(156, "_156_MillenniumLove", "");
   }
}
