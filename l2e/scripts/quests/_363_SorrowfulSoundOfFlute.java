package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _363_SorrowfulSoundOfFlute extends Quest {
   private static final String qn = "_363_SorrowfulSoundOfFlute";
   private static final int NANARIN = 30956;
   private static final int OPIX = 30595;
   private static final int ALDO = 30057;
   private static final int RANSPO = 30594;
   private static final int HOLVAS = 30058;
   private static final int BARBADO = 30959;
   private static final int POITAN = 30458;
   private static final int NANARINS_FLUTE = 4319;
   private static final int BLACK_BEER = 4320;
   private static final int CLOTHES = 4318;
   private static final int THEME_OF_SOLITUDE = 4420;

   public _363_SorrowfulSoundOfFlute(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30956);
      this.addTalkId(new int[]{30956, 30595, 30057, 30594, 30058, 30959, 30458});
      this.questItemIds = new int[]{4319, 4320, 4318};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_363_SorrowfulSoundOfFlute");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30956-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30956-05.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(4318, 1L);
         } else if (event.equalsIgnoreCase("30956-06.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(4319, 1L);
         } else if (event.equalsIgnoreCase("30956-07.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(4320, 1L);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_363_SorrowfulSoundOfFlute");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < 15) {
                  htmltext = "30956-03.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "30956-01.htm";
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               switch(npc.getId()) {
                  case 30057:
                  case 30058:
                  case 30458:
                  case 30594:
                  case 30595:
                     htmltext = npc.getId() + "-01.htm";
                     if (cond == 1) {
                        st.set("cond", "2");
                        st.playSound("ItemSound.quest_middle");
                     }
                     break;
                  case 30956:
                     if (cond == 1) {
                        htmltext = "30956-02.htm";
                     } else if (cond == 2) {
                        htmltext = "30956-04.htm";
                     } else if (cond == 3) {
                        htmltext = "30956-08.htm";
                     } else if (cond == 4) {
                        if (st.getInt("success") == 1) {
                           htmltext = "30956-09.htm";
                           st.giveItems(4420, 1L);
                           st.playSound("ItemSound.quest_finish");
                        } else {
                           htmltext = "30956-10.htm";
                           st.playSound("ItemSound.quest_giveup");
                        }

                        st.exitQuest(true);
                     }
                     break;
                  case 30959:
                     if (cond == 3) {
                        st.set("cond", "4");
                        st.playSound("ItemSound.quest_middle");
                        if (st.hasQuestItems(4319)) {
                           htmltext = "30959-02.htm";
                           st.set("success", "1");
                        } else {
                           htmltext = "30959-01.htm";
                        }

                        st.takeItems(4320, -1L);
                        st.takeItems(4318, -1L);
                        st.takeItems(4319, -1L);
                     } else if (cond == 4) {
                        htmltext = "30959-03.htm";
                     }
               }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _363_SorrowfulSoundOfFlute(363, "_363_SorrowfulSoundOfFlute", "");
   }
}
