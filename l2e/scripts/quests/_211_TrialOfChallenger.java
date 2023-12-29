package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _211_TrialOfChallenger extends Quest {
   private static final String qn = "_211_TrialOfChallenger";
   private static final int FILAUR = 30535;
   private static final int KASH = 30644;
   private static final int MARTIEN = 30645;
   private static final int RALDO = 30646;
   private static final int CHEST_OF_SHYSLASSYS = 30647;
   private static final int[] TALKERS = new int[]{30535, 30644, 30645, 30646, 30647};
   private static final int SHYSLASSYS = 27110;
   private static final int GORR = 27112;
   private static final int BARAHAM = 27113;
   private static final int SUCCUBUS_QUEEN = 27114;
   private static final int[] MOBS = new int[]{27110, 27112, 27113, 27114};
   private static final int LETTER_OF_KASH = 2628;
   private static final int SCROLL_OF_SHYSLASSY = 2631;
   private static final int WATCHERS_EYE1 = 2629;
   private static final int BROKEN_KEY = 2632;
   private static final int MITHRIL_SCALE_GAITERS_MATERIAL = 2918;
   private static final int BRIGANDINE_GAUNTLET_PATTERN = 2927;
   private static final int MANTICOR_SKIN_GAITERS_PATTERN = 1943;
   private static final int GAUNTLET_OF_REPOSE_OF_THE_SOUL_PATTERN = 1946;
   private static final int IRON_BOOTS_DESIGN = 1940;
   private static final int TOME_OF_BLOOD_PAGE = 2030;
   private static final int ELVEN_NECKLACE_BEADS = 1904;
   private static final int WHITE_TUNIC_PATTERN = 1936;
   private static final int WATCHERS_EYE2 = 2630;
   private static final int[] QUESTITEMS = new int[]{2631, 2628, 2629, 2632, 2630};
   private static final int MARK_OF_CHALLENGER = 2627;
   private static final int[] CLASSES = new int[]{1, 19, 32, 45, 47};

   public _211_TrialOfChallenger(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30644);

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
      QuestState st = player.getQuestState("_211_TrialOfChallenger");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            htmltext = "30644-05.htm";
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30644_1")) {
            htmltext = "30644-04.htm";
         } else if (event.equalsIgnoreCase("30645_1")) {
            htmltext = "30645-02.htm";
            st.takeItems(2628, 1L);
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30647_1")) {
            if (st.getQuestItemsCount(2632) == 1L) {
               st.giveItems(2631, 1L);
               st.playSound("ItemSound.quest_middle");
               if (st.getRandom(10) < 2) {
                  htmltext = "30647-03.htm";
                  st.takeItems(2632, 1L);
                  st.playSound("ItemSound.quest_jackpot");
                  int n = st.getRandom(100);
                  if (n > 90) {
                     st.giveItems(2918, 1L);
                     st.giveItems(2927, 1L);
                     st.giveItems(1943, 1L);
                     st.giveItems(1946, 1L);
                     st.giveItems(1940, 1L);
                     st.playSound("ItemSound.quest_middle");
                  } else if (n > 70) {
                     st.giveItems(2030, 1L);
                     st.giveItems(1904, 1L);
                     st.playSound("ItemSound.quest_middle");
                  } else if (n > 40) {
                     st.giveItems(1936, 1L);
                     st.playSound("ItemSound.quest_middle");
                  } else {
                     st.giveItems(1940, 1L);
                     st.playSound("ItemSound.quest_middle");
                  }
               } else {
                  htmltext = "30647-02.htm";
                  st.takeItems(2632, 1L);
                  st.giveItems(57, (long)(st.getRandom(1000) + 1));
                  st.playSound("ItemSound.quest_middle");
               }
            } else {
               htmltext = "30647-04.htm";
               st.takeItems(2632, 1L);
            }
         } else if (event.equalsIgnoreCase("30646_1")) {
            htmltext = "30646-02.htm";
         } else if (event.equalsIgnoreCase("30646_2")) {
            htmltext = "30646-03.htm";
         } else if (event.equalsIgnoreCase("30646_3")) {
            htmltext = "30646-04.htm";
            st.set("cond", "8");
            st.takeItems(2630, 1L);
         } else if (event.equalsIgnoreCase("30646_4")) {
            htmltext = "30646-06.htm";
            st.set("cond", "8");
            st.takeItems(2630, 1L);
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player talker) {
      String htmltext = getNoQuestMsg(talker);
      QuestState st = talker.getQuestState("_211_TrialOfChallenger");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         if (npcId != 30644 && id != 1) {
            return htmltext;
         } else {
            int cond = st.getInt("cond");
            if (id == 0) {
               if (npcId == 30644) {
                  if (Util.contains(CLASSES, talker.getClassId().ordinal())) {
                     if (talker.getLevel() >= 35) {
                        htmltext = "30644-03.htm";
                     } else {
                        htmltext = "30644-01.htm";
                        st.exitQuest(true);
                     }
                  } else {
                     htmltext = "30644-02.htm";
                     st.exitQuest(true);
                  }
               }
            } else if (npcId == 30644 && id == 2) {
               htmltext = Quest.getAlreadyCompletedMsg(talker);
            } else if (npcId == 30644 && cond == 1) {
               htmltext = "30644-06.htm";
            } else if (npcId == 30644 && cond == 2 && st.getQuestItemsCount(2631) == 1L) {
               htmltext = "30644-07.htm";
               st.takeItems(2631, 1L);
               st.giveItems(2628, 1L);
               st.set("cond", "3");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30644 && cond == 1 && st.getQuestItemsCount(2628) == 1L) {
               htmltext = "30644-08.htm";
            } else if (npcId == 30644 && cond >= 7) {
               htmltext = "30644-09.htm";
            } else if (npcId == 30645 && cond == 3 && st.getQuestItemsCount(2628) == 1L) {
               htmltext = "30645-01.htm";
            } else if (npcId == 30645 && cond == 4 && st.getQuestItemsCount(2629) == 0L) {
               htmltext = "30645-03.htm";
            } else if (npcId == 30645 && cond == 5 && st.getQuestItemsCount(2629) > 0L) {
               htmltext = "30645-04.htm";
               st.takeItems(2629, 1L);
               st.set("cond", "6");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30645 && cond == 6) {
               htmltext = "30645-05.htm";
            } else if (npcId == 30645 && cond >= 7) {
               htmltext = "30645-06.htm";
            } else if (npcId == 30647 && cond == 2) {
               htmltext = "30647-01.htm";
            } else if (npcId == 30646 && cond == 7 && st.getQuestItemsCount(2630) > 0L) {
               htmltext = "30646-01.htm";
            } else if (npcId == 30646 && cond == 7) {
               htmltext = "30646-06a.htm";
            } else if (npcId == 30646 && cond == 10) {
               htmltext = "30646-07.htm";
               st.set("cond", "0");
               st.takeItems(2632, 1L);
               st.addExpAndSp(533803, 34621);
               st.giveItems(57, 97278L);
               if (talker.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
                  st.giveItems(7562, 61L);
                  talker.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
               }

               st.giveItems(2627, 1L);
               st.exitQuest(false);
               st.playSound("ItemSound.quest_finish");
            } else if (npcId == 30535 && cond == 7) {
               if (talker.getLevel() >= 35) {
                  htmltext = "30535-01.htm";
                  st.addRadar(176560, -184969, -3729);
                  st.set("cond", "8");
                  st.playSound("ItemSound.quest_middle");
               } else {
                  htmltext = "30535-03.htm";
               }
            } else if (npcId == 30535 && cond == 8) {
               htmltext = "30535-02.htm";
               st.addRadar(176560, -184969, -3729);
               st.set("cond", "9");
               st.playSound("ItemSound.quest_middle");
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_211_TrialOfChallenger");
      if (st == null) {
         return null;
      } else {
         int cond = st.getInt("cond");
         int npcId = npc.getId();
         if (npcId == 27110 && cond == 1 && st.getQuestItemsCount(2632) == 0L) {
            st.giveItems(2632, 1L);
            st.addSpawn(30647, npc, true, 0);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "2");
         } else if (npcId == 27112 && cond == 4 && st.getQuestItemsCount(2629) == 0L) {
            st.giveItems(2629, 1L);
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
         } else if (npcId == 27113 && cond == 6 && st.getQuestItemsCount(2630) == 0L) {
            st.giveItems(2630, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "7");
            st.addSpawn(30646, npc, false, 300000);
         } else if (npcId == 27114 && cond == 9) {
            st.set("cond", "10");
            st.playSound("ItemSound.quest_middle");
            st.addSpawn(30646, npc, false, 300000);
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _211_TrialOfChallenger(211, "_211_TrialOfChallenger", "");
   }
}
