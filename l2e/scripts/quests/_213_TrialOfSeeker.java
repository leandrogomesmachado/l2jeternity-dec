package l2e.scripts.quests;

import java.util.HashMap;
import java.util.Map;
import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _213_TrialOfSeeker extends Quest {
   private static final String qn = "_213_TrialOfSeeker";
   private static final int DUFNER = 30106;
   private static final int TERRY = 30064;
   private static final int BRUNON = 30526;
   private static final int VIKTOR = 30684;
   private static final int MARINA = 30715;
   private static final int[] TALKERS = new int[]{30106, 30064, 30526, 30684, 30715};
   private static final int NEER_GHOUL_BERSERKER = 20198;
   private static final int OL_MAHUM_CAPTAIN = 20211;
   private static final int TUREK_ORC_WARLORD = 20495;
   private static final int ANT_CAPTAIN = 20080;
   private static final int TURAK_BUGBEAR_WARRIOR = 20249;
   private static final int MARSH_STAKATO_DRONE = 20234;
   private static final int BREKA_ORC_OVERLORD = 20270;
   private static final int ANT_WARRIOR_CAPTAIN = 20088;
   private static final int LETO_LIZARDMAN_WARRIOR = 20580;
   private static final int MEDUSA = 20158;
   private static final int[] MOBS = new int[]{20198, 20211, 20495, 20080, 20249, 20234, 20270, 20088, 20580, 20158};
   private static final int DUFNERS_LETTER = 2647;
   private static final int TERYS_ORDER1 = 2648;
   private static final int TERYS_ORDER2 = 2649;
   private static final int TERYS_LETTER = 2650;
   private static final int VIKTORS_LETTER = 2651;
   private static final int HAWKEYES_LETTER = 2652;
   private static final int MYSTERIOUS_RUNESTONE = 2653;
   private static final int OL_MAHUM_RUNESTONE = 2654;
   private static final int TUREK_RUNESTONE = 2655;
   private static final int ANT_RUNESTONE = 2656;
   private static final int TURAK_BUGBEAR_RUNESTONE = 2657;
   private static final int TERYS_BOX = 2658;
   private static final int VIKTORS_REQUEST = 2659;
   private static final int MEDUSAS_SCALES = 2660;
   private static final int SILENS_RUNESTONE = 2661;
   private static final int ANALYSIS_REQUEST = 2662;
   private static final int MARINAS_LETTER = 2663;
   private static final int EXPERIMENT_TOOLS = 2664;
   private static final int ANALYSIS_RESULT = 2665;
   private static final int TERYS_ORDER3 = 2666;
   private static final int LIST_OF_HOST = 2667;
   private static final int ABYSS_RUNESTONE1 = 2668;
   private static final int ABYSS_RUNESTONE2 = 2669;
   private static final int ABYSS_RUNESTONE3 = 2670;
   private static final int ABYSS_RUNESTONE4 = 2671;
   private static final int TERYS_REPORT = 2672;
   private static final int[] QUESTITEMS = new int[]{
      2647,
      2648,
      2649,
      2650,
      2651,
      2652,
      2653,
      2654,
      2655,
      2656,
      2657,
      2658,
      2659,
      2660,
      2661,
      2662,
      2663,
      2664,
      2665,
      2666,
      2667,
      2668,
      2669,
      2670,
      2671,
      2672
   };
   private static final int MARK_OF_SEEKER = 2673;
   private static Map<Integer, int[]> DROPLIST = new HashMap<>();
   private static final int[] CLASSES = new int[]{7, 22, 35};

   public _213_TrialOfSeeker(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30106);

      for(int talkId : TALKERS) {
         this.addTalkId(talkId);
      }

      for(int mobId : MOBS) {
         this.addKillId(mobId);
      }

      this.questItemIds = QUESTITEMS;
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_213_TrialOfSeeker");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30106-05.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(2647, 1L);
         } else if (event.equalsIgnoreCase("30064-03.htm")) {
            st.takeItems(2647, 1L);
            st.giveItems(2648, 1L);
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30064-06.htm")) {
            st.takeItems(2653, 1L);
            st.takeItems(2648, 1L);
            st.giveItems(2649, 1L);
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30064-10.htm")) {
            st.takeItems(2654, 1L);
            st.takeItems(2655, 1L);
            st.takeItems(2656, 1L);
            st.takeItems(2657, 1L);
            st.takeItems(2649, 1L);
            st.giveItems(2650, 1L);
            st.giveItems(2658, 1L);
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30064-18.htm")) {
            if (player.getLevel() < 35) {
               htmltext = "30064-17.htm";
               st.giveItems(2666, 1L);
               st.takeItems(2665, 1L);
            } else {
               st.giveItems(2667, 1L);
               st.takeItems(2665, 1L);
               st.set("cond", "16");
            }
         } else if (event.equalsIgnoreCase("30684-05.htm")) {
            st.giveItems(2651, 1L);
            st.takeItems(2650, 1L);
            st.set("cond", "7");
         } else if (event.equalsIgnoreCase("30684-11.htm")) {
            st.takeItems(2650, 1L);
            st.takeItems(2658, 1L);
            st.takeItems(2652, 1L);
            st.takeItems(2651, st.getQuestItemsCount(2651));
            st.giveItems(2659, 1L);
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30684-15.htm")) {
            st.takeItems(2659, 1L);
            st.takeItems(2660, st.getQuestItemsCount(2660));
            st.giveItems(2661, 1L);
            st.giveItems(2662, 1L);
            st.set("cond", "11");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30715-02.htm")) {
            st.takeItems(2661, 1L);
            st.takeItems(2662, 1L);
            st.giveItems(2663, 1L);
            st.set("cond", "12");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30715-05.htm")) {
            st.takeItems(2664, 1L);
            st.giveItems(2665, 1L);
            st.set("cond", "14");
            st.playSound("ItemSound.quest_middle");
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player talker) {
      String htmltext = getNoQuestMsg(talker);
      QuestState st = talker.getQuestState("_213_TrialOfSeeker");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         int npcId = npc.getId();
         int id = st.getState();
         if (npcId != 30106 && id != 1) {
            return htmltext;
         } else {
            if (id == 2) {
               htmltext = Quest.getAlreadyCompletedMsg(talker);
            } else if (id == 0) {
               st.set("cond", "0");
               st.set("id", "0");
               st.set("onlyone", "0");
            }

            if (npcId == 30106 && st.getInt("cond") == 0 && st.getInt("onlyone") == 0) {
               if (Util.contains(CLASSES, talker.getClassId().getId())) {
                  if (talker.getLevel() >= 35) {
                     htmltext = "30106-03.htm";
                  } else {
                     htmltext = "30106-02.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30106-00.htm";
                  st.exitQuest(true);
               }
            } else if (npcId == 30106) {
               if (cond == 1) {
                  htmltext = "30106-06.htm";
               } else if (cond >= 1 && st.getInt("id") != 18) {
                  htmltext = "30106-07.htm";
               } else if (cond == 17 && st.getInt("id") == 18) {
                  htmltext = "30106-08.htm";
                  st.set("cond", "0");
                  st.set("onlyone", "1");
                  st.set("id", "0");
                  st.takeItems(2672, 1L);
                  st.addExpAndSp(514739, 33384);
                  st.giveItems(57, 93803L);
                  if (talker.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
                     st.giveItems(7562, 128L);
                     talker.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
                  }

                  st.giveItems(2673, 1L);
                  st.exitQuest(false);
                  st.playSound("ItemSound.quest_finish");
               }
            } else if (npcId == 30064 && st.getQuestItemsCount(2666) == 1L) {
               if (talker.getLevel() < 35) {
                  htmltext = "30064-20.htm";
               } else {
                  htmltext = "30064-21.htm";
                  st.giveItems(2667, 1L);
                  st.takeItems(2666, 1L);
                  st.set("cond", "16");
                  st.playSound("ItemSound.quest_middle");
               }
            } else if (npcId == 30064 && cond == 1) {
               htmltext = "30064-01.htm";
            } else if (npcId == 30064 && cond == 2) {
               htmltext = "30064-04.htm";
            } else if (npcId == 30064 && cond == 3) {
               htmltext = "30064-05.htm";
            } else if (npcId == 30064 && cond == 4) {
               htmltext = "30064-08.htm";
            } else if (npcId == 30064 && cond == 5) {
               htmltext = "30064-09.htm";
            } else if (npcId == 30064 && cond == 6) {
               htmltext = "30064-11.htm";
            } else if (npcId == 30064 && cond == 7) {
               htmltext = "30064-12.htm";
               st.takeItems(2651, 1L);
               st.giveItems(2652, 1L);
               st.set("cond", "8");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30064 && cond == 8) {
               htmltext = "30064-13.htm";
            } else if (npcId == 30064 && cond > 8 && cond < 14) {
               htmltext = "30064-14.htm";
            } else if (npcId == 30064 && cond == 14) {
               htmltext = "30064-15.htm";
            } else if (npcId == 30064 && cond == 16) {
               htmltext = "30064-22.htm";
            } else if (npcId == 30064 && cond == 17 && st.getInt("id") != 18) {
               htmltext = "30064-23.htm";
               st.takeItems(2667, 1L);
               st.takeItems(2668, 1L);
               st.takeItems(2669, 1L);
               st.takeItems(2670, 1L);
               st.takeItems(2671, 1L);
               st.giveItems(2672, 1L);
               st.set("id", "18");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30064 && cond == 17 && st.getInt("id") == 18) {
               htmltext = "30064-24.htm";
            } else if (npcId == 30684 && cond == 6) {
               htmltext = "30684-01.htm";
            } else if (npcId == 30684 && cond == 7) {
               htmltext = "30684-05.htm";
            } else if (npcId == 30684 && cond == 8) {
               htmltext = "30684-12.htm";
            } else if (npcId == 30684 && cond == 9) {
               htmltext = "30684-13.htm";
            } else if (npcId == 30684 && cond == 10) {
               htmltext = "30684-14.htm";
            } else if (npcId == 30684 && cond == 11) {
               htmltext = "30684-16.htm";
            } else if (npcId == 30684 && cond == 14) {
               htmltext = "30684-17.htm";
            } else if (npcId == 30715 && cond == 11) {
               htmltext = "30715-01.htm";
            } else if (npcId == 30715 && cond == 12) {
               htmltext = "30715-03.htm";
            } else if (npcId == 30715 && cond == 13) {
               htmltext = "30715-04.htm";
            } else if (npcId == 30715 && cond == 14) {
               htmltext = "30715-06.htm";
            } else if (npcId == 30526 && cond == 12) {
               htmltext = "30526-01.htm";
               st.takeItems(2663, 1L);
               st.giveItems(2664, 1L);
               st.set("cond", "13");
            } else if (npcId == 30526 && cond == 13) {
               htmltext = "30526-02.htm";
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_213_TrialOfSeeker");
      if (st == null) {
         return null;
      } else {
         int cond = st.getInt("cond");
         int npcId = npc.getId();
         int required = DROPLIST.get(npcId)[0];
         int item = DROPLIST.get(npcId)[1];
         int chance = DROPLIST.get(npcId)[2];
         int maxqty = DROPLIST.get(npcId)[3];
         long count = st.getQuestItemsCount(item);
         if (st.getQuestItemsCount(required) > 0L && count < (long)maxqty && st.getRandom(100) < chance) {
            st.giveItems(item, 1L);
            if (count + 1L == (long)maxqty) {
               st.playSound("ItemSound.quest_middle");
               if (cond == 4) {
                  if (st.getQuestItemsCount(2654) + st.getQuestItemsCount(2655) + st.getQuestItemsCount(2656) + st.getQuestItemsCount(2657) == 4L) {
                     st.set("cond", String.valueOf(cond + 1));
                  }
               } else if (cond == 16) {
                  if (st.getQuestItemsCount(2668) + st.getQuestItemsCount(2669) + st.getQuestItemsCount(2670) + st.getQuestItemsCount(2671) == 4L) {
                     st.set("cond", String.valueOf(cond + 1));
                  }
               } else {
                  st.set("cond", String.valueOf(cond + 1));
               }
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _213_TrialOfSeeker(213, "_213_TrialOfSeeker", "");
   }

   static {
      DROPLIST.put(20198, new int[]{2648, 2653, 10, 1});
      DROPLIST.put(20211, new int[]{2649, 2654, 25, 1});
      DROPLIST.put(20495, new int[]{2649, 2655, 25, 1});
      DROPLIST.put(20080, new int[]{2649, 2656, 25, 1});
      DROPLIST.put(20249, new int[]{2649, 2657, 25, 1});
      DROPLIST.put(20234, new int[]{2667, 2668, 25, 1});
      DROPLIST.put(20270, new int[]{2667, 2669, 25, 1});
      DROPLIST.put(20088, new int[]{2667, 2670, 25, 1});
      DROPLIST.put(20580, new int[]{2667, 2671, 25, 1});
      DROPLIST.put(20158, new int[]{2659, 2660, 30, 10});
   }
}
