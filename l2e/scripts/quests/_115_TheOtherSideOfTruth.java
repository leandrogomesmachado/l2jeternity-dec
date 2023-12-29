package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _115_TheOtherSideOfTruth extends Quest {
   private static final String qn = "_115_TheOtherSideOfTruth";
   private static int RAFFORTY = 32020;
   private static int MISA = 32018;
   private static int KIERRE = 32022;
   private static int ICE_SCULPTURE1 = 32021;
   private static int ICE_SCULPTURE2 = 32077;
   private static int ICE_SCULPTURE3 = 32078;
   private static int ICE_SCULPTURE4 = 32079;
   private static int ADENA = 57;
   private static int MISAS_LETTER = 8079;
   private static int RAFFORTYS_LETTER = 8080;
   private static int PIECE_OF_TABLET = 8081;
   private static int REPORT_PIECE = 8082;

   public _115_TheOtherSideOfTruth(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(RAFFORTY);
      this.addTalkId(RAFFORTY);
      this.addTalkId(MISA);
      this.addTalkId(KIERRE);
      this.addTalkId(ICE_SCULPTURE1);
      this.addTalkId(ICE_SCULPTURE2);
      this.addTalkId(ICE_SCULPTURE3);
      this.addTalkId(ICE_SCULPTURE4);
      this.questItemIds = new int[]{MISAS_LETTER, RAFFORTYS_LETTER, PIECE_OF_TABLET, REPORT_PIECE};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_115_TheOtherSideOfTruth");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32020-02.htm") && st.getState() == 0) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         }

         if (event.equalsIgnoreCase("32020-06.htm") || event.equalsIgnoreCase("32020-08a.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         } else if (event.equalsIgnoreCase("32020-05.htm")) {
            st.set("cond", "3");
            st.takeItems(MISAS_LETTER, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32020-08.htm") || event.equalsIgnoreCase("32020-07a.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32020-12.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32018-04.htm")) {
            st.set("cond", "7");
            st.takeItems(RAFFORTYS_LETTER, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("Sculpture-04a")) {
            st.set("cond", "8");
            st.playSound("ItemSound.quest_middle");
            if (st.getInt("32021") == 0 && st.getInt("32077") == 0) {
               st.giveItems(PIECE_OF_TABLET, 1L);
            }

            htmltext = "Sculpture-04.htm";
         } else if (event.equalsIgnoreCase("32022-02.htm")) {
            st.set("cond", "9");
            st.giveItems(REPORT_PIECE, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32020-16.htm")) {
            st.set("cond", "10");
            st.takeItems(REPORT_PIECE, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32020-18.htm")) {
            if (st.hasQuestItems(PIECE_OF_TABLET)) {
               st.giveItems(ADENA, 115673L);
               st.addExpAndSp(493595, 40442);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(false);
            } else {
               st.set("cond", "11");
               st.playSound("ItemSound.quest_middle");
               htmltext = "32020-19.htm";
            }
         } else if (event.equalsIgnoreCase("32020-19.htm")) {
            st.set("cond", "11");
            st.playSound("ItemSound.quest_middle");
         } else if (event.startsWith("32021") || event.startsWith("32077")) {
            if (event.contains("-pick")) {
               st.set("talk", "1");
               event = event.replace("-pick", "");
            }

            st.set("talk", "1");
            st.set(event, "1");
            htmltext = "Sculpture-05.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_115_TheOtherSideOfTruth");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         if (npcId == RAFFORTY) {
            switch(st.getState()) {
               case 0:
                  if (player.getLevel() >= 53) {
                     htmltext = "32020-01.htm";
                  } else {
                     htmltext = "32020-00.htm";
                     st.exitQuest(true);
                  }
                  break;
               case 1:
                  if (st.getInt("cond") == 1) {
                     htmltext = "32020-03.htm";
                  } else if (st.getInt("cond") == 2) {
                     htmltext = "32020-04.htm";
                  } else if (st.getInt("cond") == 3) {
                     htmltext = "32020-05.htm";
                  } else if (st.getInt("cond") == 4) {
                     htmltext = "32020-11.htm";
                  } else if (st.getInt("cond") == 5) {
                     htmltext = "32020-13.htm";
                     st.giveItems(RAFFORTYS_LETTER, 1L);
                     st.playSound("ItemSound.quest_middle");
                     st.set("cond", "6");
                  } else if (st.getInt("cond") == 6) {
                     htmltext = "32020-14.htm";
                  } else if (st.getInt("cond") == 7 || st.getInt("cond") == 8) {
                     htmltext = "32020-14a.htm";
                  } else if (st.getInt("cond") == 9) {
                     htmltext = "32020-15.htm";
                  } else if (st.getInt("cond") == 10) {
                     htmltext = "32020-17.htm";
                  } else if (st.getInt("cond") == 11) {
                     htmltext = "32020-20.htm";
                  } else if (st.getInt("cond") == 12) {
                     htmltext = "32020-18.htm";
                     st.giveItems(ADENA, 115673L);
                     st.addExpAndSp(493595, 40442);
                     st.playSound("ItemSound.quest_finish");
                     st.exitQuest(false);
                  }
                  break;
               case 2:
                  htmltext = getAlreadyCompletedMsg(player);
            }
         } else if (npcId == MISA && st.getState() == 1) {
            if (st.getInt("cond") == 1) {
               htmltext = "32018-01.htm";
               st.set("cond", "2");
               st.giveItems(MISAS_LETTER, 1L);
               st.playSound("ItemSound.quest_middle");
            } else if (st.getInt("cond") == 2) {
               htmltext = "32018-02.htm";
            } else if (st.getInt("cond") == 6) {
               htmltext = "32018-03.htm";
            } else if (st.getInt("cond") == 7) {
               htmltext = "32018-05.htm";
            }
         } else if (npcId == KIERRE && st.getState() == 1) {
            if (st.getInt("cond") == 8) {
               htmltext = "32022-02.htm";
               st.set("cond", "9");
               st.giveItems(REPORT_PIECE, 1L);
               st.playSound("ItemSound.quest_middle");
            } else if (st.getInt("cond") >= 9) {
               htmltext = "";
            }
         } else if ((npcId == ICE_SCULPTURE1 || npcId == ICE_SCULPTURE2 || npcId == ICE_SCULPTURE3 || npcId == ICE_SCULPTURE4) && st.getState() == 1) {
            if (st.getInt("cond") == 7) {
               String _npcId = String.valueOf(npcId);
               int npcId_flag = st.getInt(_npcId);
               if (npcId == ICE_SCULPTURE1 || npcId == ICE_SCULPTURE2) {
                  int talk_flag = st.getInt("talk");
                  return npcId_flag == 1 ? "Sculpture-02.htm" : (talk_flag == 1 ? "Sculpture-06.htm" : "Sculpture-03-" + _npcId + ".htm");
               }

               if (npcId_flag == 1) {
                  htmltext = "Sculpture-02.htm";
               } else {
                  st.set(_npcId, "1");
                  htmltext = "Sculpture-01.htm";
               }
            } else if (st.getInt("cond") == 8) {
               htmltext = "Sculpture-04.htm";
            } else if (st.getInt("cond") == 11) {
               htmltext = "Sculpture-07.htm";
               st.set("cond", "12");
               st.giveItems(PIECE_OF_TABLET, 1L);
               st.playSound("ItemSound.quest_middle");
            } else if (st.getInt("cond") == 12) {
               htmltext = "Sculpture-08.htm";
            }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _115_TheOtherSideOfTruth(115, "_115_TheOtherSideOfTruth", "");
   }
}
