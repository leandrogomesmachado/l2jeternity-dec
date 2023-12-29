package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _237_WindsOfChange extends Quest {
   private static String qn = "_237_WindsOfChange";
   private static final int Flauen = 30899;
   private static final int Iason = 30969;
   private static final int Roman = 30897;
   private static final int Morelyn = 30925;
   private static final int Helvetica = 32641;
   private static final int Athenia = 32643;
   private static final int[] _npcs = new int[]{30899, 30969, 30897, 30925, 32641, 32643};
   private static final int FlauensLetter = 14862;
   private static final int LetterToHelvetica = 14863;
   private static final int LetterToAthenia = 14864;
   private static final int VicinityOfTheFieldOfSilenceResearchCenter = 14865;
   private static final int CertificateOfSupport = 14866;

   public _237_WindsOfChange(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30899);

      for(int i : _npcs) {
         this.addTalkId(i);
      }

      this.questItemIds = new int[]{14862, 14863, 14864, 14865, 14866};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(qn);
      if (st == null) {
         return null;
      } else {
         if (event.equalsIgnoreCase("30899-06.htm")) {
            st.giveItems(14862, 1L);
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30969-05.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30897-03.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30925-03.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30969-09.htm")) {
            st.giveItems(14863, 1L);
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30969-10.htm")) {
            st.giveItems(14864, 1L);
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32641-02.htm")) {
            st.takeItems(14863, -1L);
            st.giveItems(57, 213876L);
            st.addExpAndSp(892773, 60012);
            st.giveItems(14865, 1L);
            st.playSound("ItemSound.quest_finish");
            st.setState((byte)2);
            st.exitQuest(false);
         } else if (event.equalsIgnoreCase("32643-02.htm")) {
            st.takeItems(14864, -1L);
            st.giveItems(57, 213876L);
            st.addExpAndSp(892773, 60012);
            st.giveItems(14866, 1L);
            st.setState((byte)2);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(qn);
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         int cond = st.getInt("cond");
         if (npcId == 30899) {
            if (id == 0) {
               return player.getLevel() < 82 ? "30899-00.htm" : "30899-01.htm";
            } else if (id == 2) {
               return "30899-09.htm";
            } else {
               return cond < 5 ? "30899-07.htm" : "30899-08.htm";
            }
         } else {
            if (npcId == 30969) {
               if (cond == 1) {
                  st.takeItems(14862, -1L);
                  return "30969-01.htm";
               }

               if (cond > 1 && cond < 4) {
                  return "30969-06.htm";
               }

               if (cond == 4) {
                  return "30969-07.htm";
               }

               if (cond > 4) {
                  return "30969-11.htm";
               }
            } else if (npcId == 30897) {
               if (cond == 2) {
                  return "30897-01.htm";
               }

               if (cond > 2) {
                  return "30897-04.htm";
               }
            } else if (npcId == 30925) {
               if (cond == 3) {
                  return "30925-01.htm";
               }

               if (cond > 3) {
                  return "30925-04.htm";
               }
            } else if (npcId == 32641) {
               if (cond == 5) {
                  return "32641-01.htm";
               }

               if (id == 2) {
                  return "32641-03.htm";
               }
            } else if (npcId == 32643) {
               if (cond == 6) {
                  return "32643-01.htm";
               }

               if (id == 2) {
                  return "32643-03.htm";
               }
            }

            return htmltext;
         }
      }
   }

   public static void main(String[] args) {
      new _237_WindsOfChange(237, qn, "");
   }
}
