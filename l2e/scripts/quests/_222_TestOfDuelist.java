package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _222_TestOfDuelist extends Quest {
   private static final String qn = "_222_TestOfDuelist";
   private static final int Kaien = 30623;
   private static final int OrderGludio = 2763;
   private static final int OrderDion = 2764;
   private static final int OrderGiran = 2765;
   private static final int OrderOren = 2766;
   private static final int OrderAden = 2767;
   private static final int PunchersShard = 2768;
   private static final int NobleAntsFeeler = 2769;
   private static final int DronesChitin = 2770;
   private static final int DeadSeekerFang = 2771;
   private static final int OverlordNecklace = 2772;
   private static final int FetteredSoulsChain = 2773;
   private static final int ChiefsAmulet = 2774;
   private static final int EnchantedEyeMeat = 2775;
   private static final int TamrinOrcsRing = 2776;
   private static final int TamrinOrcsArrow = 2777;
   private static final int FinalOrder = 2778;
   private static final int ExcurosSkin = 2779;
   private static final int KratorsShard = 2780;
   private static final int GrandisSkin = 2781;
   private static final int TimakOrcsBelt = 2782;
   private static final int LakinsMace = 2783;
   private static final int MarkOfDuelist = 2762;
   private static final int Puncher = 20085;
   private static final int NobleAntLeader = 20090;
   private static final int MarshStakatoDrone = 20234;
   private static final int DeadSeeker = 20202;
   private static final int BrekaOrcOverlord = 20270;
   private static final int FetteredSoul = 20552;
   private static final int LetoLizardmanOverlord = 20582;
   private static final int EnchantedMonstereye = 20564;
   private static final int TamlinOrc = 20601;
   private static final int TamlinOrcArcher = 20602;
   private static final int Excuro = 20214;
   private static final int Krator = 20217;
   private static final int Grandis = 20554;
   private static final int TimakOrcOverlord = 20588;
   private static final int Lakin = 20604;
   private static final int[][] DROPLIST_COND = new int[][]{
      {2, 0, 20085, 0, 2768, 10, 70, 1},
      {2, 0, 20090, 0, 2769, 10, 70, 1},
      {2, 0, 20234, 0, 2770, 10, 70, 1},
      {2, 0, 20202, 0, 2771, 10, 70, 1},
      {2, 0, 20270, 0, 2772, 10, 70, 1},
      {2, 0, 20552, 0, 2773, 10, 70, 1},
      {2, 0, 20582, 0, 2774, 10, 70, 1},
      {2, 0, 20564, 0, 2775, 10, 70, 1},
      {2, 0, 20601, 0, 2776, 10, 70, 1},
      {2, 0, 20602, 0, 2777, 10, 70, 1},
      {4, 0, 20214, 0, 2779, 3, 70, 1},
      {4, 0, 20217, 0, 2780, 3, 70, 1},
      {4, 0, 20554, 0, 2781, 3, 70, 1},
      {4, 0, 20588, 0, 2782, 3, 70, 1},
      {4, 0, 20604, 0, 2783, 3, 70, 1}
   };

   public _222_TestOfDuelist(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30623);
      this.addTalkId(30623);

      for(int[] element : DROPLIST_COND) {
         this.addKillId(element[2]);
         this.registerQuestItems(new int[]{element[4]});
      }

      this.questItemIds = new int[]{2763, 2764, 2765, 2766, 2767, 2778};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_222_TestOfDuelist");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30623-04.htm")) {
            if (player.getRace().ordinal() == 3) {
               htmltext = "30623-05.htm";
            }
         } else if (event.equalsIgnoreCase("30623-07.htm")) {
            st.set("cond", "2");
            st.setState((byte)1);
            st.giveItems(2763, 1L);
            st.giveItems(2764, 1L);
            st.giveItems(2765, 1L);
            st.giveItems(2766, 1L);
            st.giveItems(2767, 1L);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30623-16.htm")) {
            st.takeItems(2768, -1L);
            st.takeItems(2769, -1L);
            st.takeItems(2770, -1L);
            st.takeItems(2771, -1L);
            st.takeItems(2772, -1L);
            st.takeItems(2773, -1L);
            st.takeItems(2774, -1L);
            st.takeItems(2775, -1L);
            st.takeItems(2776, -1L);
            st.takeItems(2777, -1L);
            st.takeItems(2763, -1L);
            st.takeItems(2764, -1L);
            st.takeItems(2765, -1L);
            st.takeItems(2766, -1L);
            st.takeItems(2767, -1L);
            st.giveItems(2778, 1L);
            st.set("cond", "4");
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_222_TestOfDuelist");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getClassId().getId() != 1
                  && player.getClassId().getId() != 47
                  && player.getClassId().getId() != 19
                  && player.getClassId().getId() != 32) {
                  htmltext = "30623-02.htm";
                  st.exitQuest(true);
               } else if (player.getLevel() >= 39) {
                  htmltext = "30623-03.htm";
               } else {
                  htmltext = "30623-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (cond == 2) {
                  htmltext = "30623-14.htm";
               } else if (cond == 3) {
                  htmltext = "30623-13.htm";
               } else if (cond == 4) {
                  htmltext = "30623-17.htm";
               } else if (cond == 5) {
                  st.giveItems(2762, 1L);
                  st.addExpAndSp(594888, 61408);
                  st.giveItems(57, 161806L);
                  if (player.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
                     st.giveItems(7562, 72L);
                     st.giveItems(8870, 15L);
                     player.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
                  }

                  htmltext = "30623-18.htm";
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(false);
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_222_TestOfDuelist");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");

         for(int[] element : DROPLIST_COND) {
            if (cond == element[0] && npcId == element[2] && (element[3] == 0 || st.getQuestItemsCount(element[3]) > 0L)) {
               if (element[5] == 0) {
                  st.rollAndGive(element[4], element[7], (double)element[6]);
               } else if (st.rollAndGive(element[4], element[7], element[7], element[5], (double)element[6]) && element[1] != cond && element[1] != 0) {
                  st.setCond(Integer.valueOf(element[1]));
               }
            }
         }

         if (cond == 2
            && st.getQuestItemsCount(2768) >= 10L
            && st.getQuestItemsCount(2769) >= 10L
            && st.getQuestItemsCount(2770) >= 10L
            && st.getQuestItemsCount(2771) >= 10L
            && st.getQuestItemsCount(2772) >= 10L
            && st.getQuestItemsCount(2773) >= 10L
            && st.getQuestItemsCount(2774) >= 10L
            && st.getQuestItemsCount(2775) >= 10L
            && st.getQuestItemsCount(2776) >= 10L
            && st.getQuestItemsCount(2777) >= 10L) {
            st.set("cond", "3");
         } else if (cond == 4
            && st.getQuestItemsCount(2779) >= 3L
            && st.getQuestItemsCount(2780) >= 3L
            && st.getQuestItemsCount(2783) >= 3L
            && st.getQuestItemsCount(2781) >= 3L
            && st.getQuestItemsCount(2782) >= 3L) {
            st.set("cond", "5");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _222_TestOfDuelist(222, "_222_TestOfDuelist", "");
   }
}
