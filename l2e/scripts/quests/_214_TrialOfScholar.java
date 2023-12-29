package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _214_TrialOfScholar extends Quest {
   private static final String qn = "_214_TrialOfScholar";
   private static final int MIRIEN = 30461;
   private static final int SYLVAIN = 30070;
   private static final int LUCAS = 30071;
   private static final int VALKON = 30103;
   private static final int DIETER = 30111;
   private static final int JUREK = 30115;
   private static final int EDROC = 30230;
   private static final int RAUT = 30316;
   private static final int POITAN = 30458;
   private static final int MARIA = 30608;
   private static final int CRETA = 30609;
   private static final int CRONOS = 30610;
   private static final int TRIFF = 30611;
   private static final int CASIAN = 30612;
   private static final int[] TALKERS = new int[]{30461, 30070, 30071, 30103, 30111, 30115, 30230, 30316, 30458, 30608, 30609, 30610, 30611, 30612};
   private static final int MEDUSA = 20158;
   private static final int GHOUL = 20201;
   private static final int SHACKLE = 20235;
   private static final int BREKA_ORC_SHAMAN = 20269;
   private static final int FETTERED_SOUL = 20552;
   private static final int GRANDIS = 20554;
   private static final int ENCHANTED_GARGOYLE = 20567;
   private static final int LETO_LIZARDMAN_WARRIOR = 20580;
   private static final int MONSTER_EYE_DESTROYER = 20068;
   private static final int[] MOBS = new int[]{20158, 20201, 20235, 20269, 20552, 20554, 20567, 20580, 20068};
   private static final int MIRIENS_SIGIL1 = 2675;
   private static final int MIRIENS_SIGIL2 = 2676;
   private static final int MIRIENS_SIGIL3 = 2677;
   private static final int MIRIENS_INSTRUCTION = 2678;
   private static final int MARIAS_LETTER1 = 2679;
   private static final int MARIAS_LETTER2 = 2680;
   private static final int LUKAS_LETTER = 2681;
   private static final int LUCILLAS_HANDBAG = 2682;
   private static final int CRETAS_LETTER1 = 2683;
   private static final int CRETAS_PAINTING1 = 2684;
   private static final int CRETAS_PAINTING2 = 2685;
   private static final int CRETAS_PAINTING3 = 2686;
   private static final int BROWN_SCROLL_SCRAP = 2687;
   private static final int CRYSTAL_OF_PURITY1 = 2688;
   private static final int HIGHPRIESTS_SIGIL = 2689;
   private static final int GMAGISTERS_SIGIL = 2690;
   private static final int CRONOS_SIGIL = 2691;
   private static final int SYLVAINS_LETTER = 2692;
   private static final int SYMBOL_OF_SYLVAIN = 2693;
   private static final int JUREKS_LIST = 2694;
   private static final int MEYEDESTROYERS_SKIN = 2695;
   private static final int SHAMANS_NECKLACE = 2696;
   private static final int SHACKLES_SCALP = 2697;
   private static final int SYMBOL_OF_JUREK = 2698;
   private static final int CRONOS_LETTER = 2699;
   private static final int DIETERS_KEY = 2700;
   private static final int CRETAS_LETTER2 = 2701;
   private static final int DIETERS_LETTER = 2702;
   private static final int DIETERS_DIARY = 2703;
   private static final int RAUTS_LETTER_ENVELOPE = 2704;
   private static final int TRIFFS_RING = 2705;
   private static final int SCRIPTURE_CHAPTER_1 = 2706;
   private static final int SCRIPTURE_CHAPTER_2 = 2707;
   private static final int SCRIPTURE_CHAPTER_3 = 2708;
   private static final int SCRIPTURE_CHAPTER_4 = 2709;
   private static final int VALKONS_REQUEST = 2710;
   private static final int POITANS_NOTES = 2711;
   private static final int STRONG_LIQUOR = 2713;
   private static final int CRYSTAL_OF_PURITY2 = 2714;
   private static final int CASIANS_LIST = 2715;
   private static final int GHOULS_SKIN = 2716;
   private static final int MEDUSAS_BLOOD = 2717;
   private static final int FETTEREDSOULS_ICHOR = 2718;
   private static final int ENCHT_GARGOYLES_NAIL = 2719;
   private static final int SYMBOL_OF_CRONOS = 2720;
   private static final int[] QUESTITEMS = new int[]{2714, 2715, 2716, 2717, 2718, 2719, 2720};
   private static final int MARK_OF_SCHOLAR = 2674;
   private static final int[] CLASSES = new int[]{11, 26, 39};

   public _214_TrialOfScholar(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30461);

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
      QuestState st = player.getQuestState("_214_TrialOfScholar");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            htmltext = "30461-04.htm";
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(2675, 1L);
         } else if (event.equalsIgnoreCase("30461_1")) {
            if (player.getLevel() < 35) {
               htmltext = "30461-09.htm";
               st.takeItems(2698, -1L);
               st.takeItems(2676, -1L);
               st.giveItems(2678, 1L);
            } else {
               htmltext = "30461-10.htm";
               st.takeItems(2698, -1L);
               st.takeItems(2676, -1L);
               st.giveItems(2677, 1L);
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "19");
            }
         } else if (event.equalsIgnoreCase("30070_1")) {
            htmltext = "30070-02.htm";
            st.giveItems(2689, 1L);
            st.giveItems(2692, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "2");
         } else if (event.equalsIgnoreCase("30608_1")) {
            htmltext = "30608-02.htm";
            st.takeItems(2692, -1L);
            st.giveItems(2679, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "3");
         } else if (event.equalsIgnoreCase("30608_2")) {
            htmltext = "30608-07.htm";
         } else if (event.equalsIgnoreCase("30608_3")) {
            htmltext = "30608-08.htm";
            st.takeItems(2683, -1L);
            st.giveItems(2682, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "7");
         } else if (event.equalsIgnoreCase("30608_4")) {
            htmltext = "30608-14.htm";
            st.takeItems(2687, -1L);
            st.takeItems(2686, -1L);
            st.giveItems(2688, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "13");
         } else if (event.equalsIgnoreCase("30115_1")) {
            htmltext = "30115-02.htm";
         } else if (event.equalsIgnoreCase("30115_2")) {
            htmltext = "30115-03.htm";
            st.giveItems(2694, 1L);
            st.giveItems(2690, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "16");
         } else if (event.equalsIgnoreCase("30071_1")) {
            htmltext = "30071-04.htm";
            st.takeItems(2685, -1L);
            st.giveItems(2686, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "10");
         } else if (event.equalsIgnoreCase("30609_1")) {
            htmltext = "30609-02.htm";
         } else if (event.equalsIgnoreCase("30609_2")) {
            htmltext = "30609-03.htm";
         } else if (event.equalsIgnoreCase("30609_3")) {
            htmltext = "30609-04.htm";
         } else if (event.equalsIgnoreCase("30609_4")) {
            htmltext = "30609-05.htm";
            st.takeItems(2680, -1L);
            st.giveItems(2683, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "6");
         } else if (event.equalsIgnoreCase("30609_5")) {
            htmltext = "30609-08.htm";
         } else if (event.equalsIgnoreCase("30609_6")) {
            htmltext = "30609-09.htm";
            st.takeItems(2682, -1L);
            st.giveItems(2684, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "8");
         } else if (event.equalsIgnoreCase("30609_7")) {
            htmltext = "30609-13.htm";
         } else if (event.equalsIgnoreCase("30609_8")) {
            htmltext = "30609-14.htm";
            st.takeItems(2700, -1L);
            st.giveItems(2701, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "22");
         } else if (event.equalsIgnoreCase("30610_1")) {
            htmltext = "30610-02.htm";
         } else if (event.equalsIgnoreCase("30610_2")) {
            htmltext = "30610-03.htm";
         } else if (event.equalsIgnoreCase("30610_3")) {
            htmltext = "30610-04.htm";
         } else if (event.equalsIgnoreCase("30610_4")) {
            htmltext = "30610-05.htm";
         } else if (event.equalsIgnoreCase("30610_5")) {
            htmltext = "30610-06.htm";
         } else if (event.equalsIgnoreCase("30610_6")) {
            htmltext = "30610-07.htm";
         } else if (event.equalsIgnoreCase("30610_7")) {
            htmltext = "30610-08.htm";
         } else if (event.equalsIgnoreCase("30610_8")) {
            htmltext = "30610-09.htm";
         } else if (event.equalsIgnoreCase("30610_9")) {
            htmltext = "30610-10.htm";
            st.giveItems(2691, 1L);
            st.giveItems(2699, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "20");
         } else if (event.equalsIgnoreCase("30610_10")) {
            htmltext = "30610-13.htm";
         } else if (event.equalsIgnoreCase("30610_11")) {
            htmltext = "30610-14.htm";
            st.takeItems(2706, -1L);
            st.takeItems(2707, -1L);
            st.takeItems(2708, -1L);
            st.takeItems(2709, -1L);
            st.takeItems(2691, -1L);
            st.takeItems(2705, -1L);
            st.takeItems(2703, -1L);
            st.giveItems(2720, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "31");
         } else if (event.equalsIgnoreCase("30111_1")) {
            htmltext = "30111-02.htm";
         } else if (event.equalsIgnoreCase("30111_2")) {
            htmltext = "30111-03.htm";
         } else if (event.equalsIgnoreCase("30111_3")) {
            htmltext = "30111-04.htm";
         } else if (event.equalsIgnoreCase("30111_4")) {
            htmltext = "30111-05.htm";
            st.takeItems(2699, -1L);
            st.giveItems(2700, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "21");
         } else if (event.equalsIgnoreCase("30111_5")) {
            htmltext = "30111-08.htm";
         } else if (event.equalsIgnoreCase("30111_6")) {
            htmltext = "30111-09.htm";
            st.takeItems(2701, -1L);
            st.giveItems(2702, 1L);
            st.giveItems(2703, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "23");
         } else if (event.equalsIgnoreCase("30230_1")) {
            htmltext = "30230-02.htm";
            st.takeItems(2702, -1L);
            st.giveItems(2704, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "24");
         } else if (event.equalsIgnoreCase("30316_1")) {
            htmltext = "30316-02.htm";
            st.takeItems(2704, -1L);
            st.giveItems(2706, 1L);
            st.giveItems(2713, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "25");
         } else if (event.equalsIgnoreCase("30611_1")) {
            htmltext = "30611-02.htm";
         } else if (event.equalsIgnoreCase("30611_2")) {
            htmltext = "30611-03.htm";
         } else if (event.equalsIgnoreCase("30611_3")) {
            htmltext = "30611-04.htm";
            st.takeItems(2713, -1L);
            st.giveItems(2705, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "26");
         } else if (event.equalsIgnoreCase("30103_1")) {
            htmltext = "30103-02.htm";
         } else if (event.equalsIgnoreCase("30103_2")) {
            htmltext = "30103-03.htm";
         } else if (event.equalsIgnoreCase("30103_3")) {
            htmltext = "30103-04.htm";
            st.giveItems(2710, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30612_1")) {
            htmltext = "30612-03.htm";
         } else if (event.equalsIgnoreCase("30612_2")) {
            htmltext = "30612-04.htm";
            st.giveItems(2715, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "28");
         } else if (event.equalsIgnoreCase("30612_3")) {
            htmltext = "30612-07.htm";
            st.giveItems(2709, 1L);
            st.takeItems(2715, -1L);
            st.takeItems(2716, -1L);
            st.takeItems(2717, -1L);
            st.takeItems(2718, -1L);
            st.takeItems(2719, -1L);
            st.takeItems(2711, -1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "30");
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player talker) {
      String htmltext = getNoQuestMsg(talker);
      QuestState st = talker.getQuestState("_214_TrialOfScholar");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         int npcId = npc.getId();
         int id = st.getState();
         if (npcId != 30461 && id != 1) {
            return htmltext;
         } else {
            if (npcId == 30461 && cond == 0) {
               if (st.getInt("onlyone") == 0) {
                  if (Util.contains(CLASSES, talker.getClassId().getId())) {
                     if (talker.getLevel() >= 35) {
                        htmltext = "30461-03.htm";
                     } else {
                        htmltext = "30461-02.htm";
                        st.exitQuest(true);
                     }
                  } else {
                     htmltext = "30461-01.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = getAlreadyCompletedMsg(talker);
               }
            } else if (npcId == 30461 && cond == 1) {
               htmltext = "30461-05.htm";
            } else if (npcId == 30461 && cond == 14) {
               htmltext = "30461-06.htm";
               st.takeItems(2693, -1L);
               st.takeItems(2675, -1L);
               st.giveItems(2676, 1L);
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "15");
            } else if (npcId == 30461 && cond > 14 && cond < 18) {
               htmltext = "30461-07.htm";
            } else if (npcId == 30461 && cond == 18 && st.getQuestItemsCount(2678) >= 1L) {
               if (talker.getLevel() < 35) {
                  htmltext = "30461-11.htm";
               } else {
                  htmltext = "30461-12.htm";
                  st.giveItems(2677, 1L);
                  st.takeItems(2678, -1L);
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "19");
               }
            } else if (npcId == 30461 && cond == 18) {
               htmltext = "30461-08.htm";
            } else if (npcId == 30461 && cond == 19) {
               htmltext = "30461-13.htm";
            } else if (npcId == 30461 && cond == 31 && st.getQuestItemsCount(2720) >= 1L) {
               htmltext = "30461-14.htm";
               st.set("cond", "0");
               st.set("onlyone", "1");
               st.takeItems(2677, -1L);
               st.takeItems(2720, -1L);
               st.addExpAndSp(876963, 56877);
               st.giveItems(57, 159814L);
               if (talker.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
                  st.giveItems(7562, 168L);
                  talker.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
               }

               st.giveItems(2674, 1L);
               st.exitQuest(false);
               st.playSound("ItemSound.quest_finish");
            } else if (npcId == 30070 && cond == 1) {
               htmltext = "30070-01.htm";
            } else if (npcId == 30070 && cond == 2) {
               htmltext = "30070-03.htm";
            } else if (npcId == 30070 && cond == 13) {
               htmltext = "30070-04.htm";
               st.giveItems(2693, 1L);
               st.takeItems(2689, -1L);
               st.takeItems(2688, -1L);
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "14");
            } else if (npcId == 30070 && cond == 14) {
               htmltext = "30070-05.htm";
            } else if (npcId == 30070 && cond > 14) {
               htmltext = "30070-06.htm";
            } else if (npcId == 30608 && cond == 2) {
               htmltext = "30608-01.htm";
            } else if (npcId == 30608 && cond == 3) {
               htmltext = "30608-03.htm";
            } else if (npcId == 30608 && cond == 4) {
               htmltext = "30608-04.htm";
               st.giveItems(2680, 1L);
               st.takeItems(2681, -1L);
               st.set("cond", "5");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30608 && cond == 5) {
               htmltext = "30608-05.htm";
            } else if (npcId == 30608 && cond == 6) {
               htmltext = "30608-06.htm";
            } else if (npcId == 30608 && cond == 7) {
               htmltext = "30608-09.htm";
            } else if (npcId == 30608 && cond == 8) {
               htmltext = "30608-10.htm";
               st.giveItems(2685, 1L);
               st.takeItems(2684, -1L);
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "9");
            } else if (npcId == 30608 && cond == 9) {
               htmltext = "30608-11.htm";
            } else if (npcId == 30608 && cond == 10) {
               htmltext = "30608-12.htm";
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "11");
            } else if (npcId == 30608 && cond == 12) {
               htmltext = "30608-13.htm";
            } else if (npcId == 30608 && cond == 13) {
               htmltext = "30608-15.htm";
            } else if (npcId != 30608 || st.getQuestItemsCount(2693) <= 0L && st.getQuestItemsCount(2676) <= 0L) {
               if (npcId == 30608 && st.getQuestItemsCount(2677) >= 1L && st.getQuestItemsCount(2710) == 0L) {
                  htmltext = "30608-17.htm";
               } else if (npcId == 30608 && cond == 26 && st.getQuestItemsCount(2710) >= 1L) {
                  htmltext = "30608-18.htm";
                  st.giveItems(2714, 1L);
                  st.takeItems(2710, -1L);
                  st.playSound("ItemSound.quest_middle");
               } else if (npcId == 30115 && cond == 15) {
                  htmltext = "30115-01.htm";
               } else if (npcId == 30115 && cond == 16) {
                  htmltext = "30115-04.htm";
               } else if (npcId == 30115 && cond == 17) {
                  htmltext = "30115-05.htm";
                  st.takeItems(2694, -1L);
                  st.takeItems(2695, -1L);
                  st.takeItems(2696, -1L);
                  st.takeItems(2697, -1L);
                  st.takeItems(2690, -1L);
                  st.giveItems(2698, 1L);
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "18");
               } else if (npcId == 30115 && cond == 18) {
                  htmltext = "30115-06.htm";
               } else if (npcId == 30115 && cond > 18) {
                  htmltext = "30115-07.htm";
               } else if (npcId == 30071 && cond == 3) {
                  htmltext = "30071-01.htm";
                  st.set("cond", "4");
                  st.giveItems(2681, 1L);
                  st.takeItems(2679, -1L);
                  st.playSound("ItemSound.quest_middle");
               } else if (npcId == 30071 && cond == 4) {
                  htmltext = "30071-02.htm";
               } else if (npcId == 30071 && cond == 9) {
                  htmltext = "30071-03.htm";
               } else if (npcId == 30071 && cond == 10) {
                  htmltext = st.getQuestItemsCount(2687) < 5L ? "30071-05.htm" : "30071-06.htm";
               } else if (npcId == 30071 && cond < 10) {
                  htmltext = "30071-07.htm";
               } else if (npcId == 30609 && cond == 5) {
                  htmltext = "30609-01.htm";
               } else if (npcId == 30609 && cond == 6) {
                  htmltext = "30609-06.htm";
               } else if (npcId == 30609 && cond == 7) {
                  htmltext = "30609-07.htm";
               } else if (npcId == 30609 && cond == 8) {
                  htmltext = "30609-10.htm";
               } else if (npcId == 30609 && cond > 9 && cond < 21) {
                  htmltext = "30609-11.htm";
               } else if (npcId == 30609 && cond == 21) {
                  htmltext = "30609-12.htm";
               } else if (npcId == 30609 && cond == 22) {
                  htmltext = "30609-14.htm";
               } else if (npcId == 30609 && cond > 22) {
                  htmltext = "30609-15.htm";
               } else if (npcId == 30610 && cond == 19) {
                  htmltext = "30610-01.htm";
               } else if (npcId == 30610 && cond == 20) {
                  htmltext = "30610-11.htm";
               } else if (npcId == 30610 && cond == 30) {
                  htmltext = "30610-12.htm";
               } else if (npcId == 30610 && cond == 31) {
                  htmltext = "30610-15.htm";
               } else if (npcId == 30111 && cond == 20) {
                  htmltext = "30111-01.htm";
               } else if (npcId == 30111 && cond == 21) {
                  htmltext = "30111-06.htm";
               } else if (npcId == 30111 && cond == 22) {
                  htmltext = "30111-07.htm";
               } else if (npcId == 30111 && cond == 23) {
                  htmltext = "30111-10.htm";
               } else if (npcId == 30111
                  && st.getQuestItemsCount(2677) > 0L
                  && st.getQuestItemsCount(2691) > 0L
                  && st.getQuestItemsCount(2703) > 0L
                  && st.getQuestItemsCount(2704) > 0L) {
                  htmltext = "30111-11.htm";
               } else if (npcId == 30111
                  && st.getQuestItemsCount(2677) > 0L
                  && st.getQuestItemsCount(2691) > 0L
                  && st.getQuestItemsCount(2703) > 0L
                  && st.getQuestItemsCount(2702) == 0L
                  && st.getQuestItemsCount(2704) == 0L) {
                  htmltext = st.getQuestItemsCount(2706) > 0L
                        && st.getQuestItemsCount(2707) > 0L
                        && st.getQuestItemsCount(2708) > 0L
                        && st.getQuestItemsCount(2709) > 0L
                     ? "30111-13.htm"
                     : "30111-12.htm";
               } else if (npcId == 30111 && st.getQuestItemsCount(2720) >= 1L) {
                  htmltext = "30111-15.htm";
               } else if (npcId == 30230 && cond == 23) {
                  htmltext = "30230-01.htm";
               } else if (npcId == 30230 && cond == 24) {
                  htmltext = "30230-03.htm";
               } else if (npcId != 30230 || st.getQuestItemsCount(2703) < 1L || st.getQuestItemsCount(2713) <= 0L && st.getQuestItemsCount(2705) <= 0L) {
                  if (npcId == 30316 && cond == 24) {
                     htmltext = "30316-01.htm";
                  } else if (npcId == 30316 && cond == 25) {
                     htmltext = "30316-04.htm";
                  } else if (npcId == 30316 && st.getQuestItemsCount(2703) > 0L && st.getQuestItemsCount(2706) > 0L && st.getQuestItemsCount(2705) > 0L) {
                     htmltext = "30316-05.htm";
                  } else if (npcId == 30611 && cond == 25) {
                     htmltext = "30611-01.htm";
                  } else if (npcId == 30611 && cond > 25) {
                     htmltext = "30611-05.htm";
                  } else if (npcId == 30103 && cond == 26 && st.getQuestItemsCount(2714) >= 1L) {
                     htmltext = "30103-06.htm";
                     st.giveItems(2707, 1L);
                     st.takeItems(2714, -1L);
                     st.playSound("ItemSound.quest_middle");
                  } else if (npcId == 30103 && cond == 26 && st.getQuestItemsCount(2710) >= 1L) {
                     htmltext = "30103-05.htm";
                  } else if (npcId == 30103 && cond == 26 && st.getQuestItemsCount(2707) == 0L) {
                     htmltext = "30103-01.htm";
                  } else if (npcId == 30103 && st.getQuestItemsCount(2707) >= 1L) {
                     htmltext = "30103-07.htm";
                  } else if (npcId == 30458 && cond == 26 && st.getQuestItemsCount(2711) == 0L) {
                     htmltext = "30458-01.htm";
                     st.giveItems(2711, 1L);
                     st.playSound("ItemSound.quest_middle");
                  } else if (npcId == 30458 && cond == 26 && st.getQuestItemsCount(2711) > 0L && st.getQuestItemsCount(2715) == 0L) {
                     htmltext = "30458-02.htm";
                  } else if (npcId == 30458 && cond == 27 && st.getQuestItemsCount(2711) > 0L && st.getQuestItemsCount(2715) > 0L) {
                     htmltext = "30458-03.htm";
                  } else if (npcId == 30458 && cond >= 28) {
                     htmltext = "30458-04.htm";
                  } else if (npcId == 30612 && cond == 26) {
                     htmltext = st.getQuestItemsCount(2706) > 0L && st.getQuestItemsCount(2707) > 0L && st.getQuestItemsCount(2708) > 0L
                        ? "30612-02.htm"
                        : "30612-01.htm";
                  } else if (npcId == 30612 && cond == 28) {
                     if (st.getQuestItemsCount(2716) + st.getQuestItemsCount(2717) + st.getQuestItemsCount(2718) + st.getQuestItemsCount(2719) < 32L) {
                        htmltext = "30612-05.htm";
                     }
                  } else if (npcId == 30612 && cond == 29) {
                     htmltext = "30612-06.htm";
                  } else if (npcId == 30612 && cond >= 30) {
                     htmltext = "30612-08.htm";
                  }
               } else {
                  htmltext = "30230-04.htm";
               }
            } else {
               htmltext = "30608-16.htm";
            }

            return htmltext;
         }
      }
   }

   @Override
   public final String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_214_TrialOfScholar");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (npcId == 20580 && cond == 11 && st.getQuestItemsCount(2687) < 5L && st.getRandom(100) < 50) {
            st.giveItems(2687, 1L);
            if (st.getQuestItemsCount(2687) < 5L) {
               st.playSound("ItemSound.quest_itemget");
            } else {
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "12");
            }
         }

         if (cond == 16) {
            if (npcId == 20068) {
               if (st.getQuestItemsCount(2695) < 5L && st.getRandom(100) < 50) {
                  st.giveItems(2695, 1L);
                  if (st.getQuestItemsCount(2695) < 5L) {
                     st.playSound("ItemSound.quest_itemget");
                  } else {
                     st.playSound("ItemSound.quest_middle");
                     if (st.getQuestItemsCount(2697) == 2L && st.getQuestItemsCount(2696) == 5L && st.getQuestItemsCount(2695) == 5L) {
                        st.set("cond", "17");
                     }
                  }
               }
            } else if (npcId == 20269) {
               if (st.getQuestItemsCount(2696) < 5L && st.getRandom(100) < 50) {
                  st.giveItems(2696, 1L);
                  if (st.getQuestItemsCount(2696) < 5L) {
                     st.playSound("ItemSound.quest_itemget");
                  } else {
                     st.playSound("ItemSound.quest_middle");
                     if (st.getQuestItemsCount(2697) == 2L && st.getQuestItemsCount(2696) == 5L && st.getQuestItemsCount(2695) == 5L) {
                        st.set("cond", "17");
                     }
                  }
               }
            } else if (npcId == 20235 && st.getQuestItemsCount(2697) < 2L) {
               st.giveItems(2697, 1L);
               if (st.getQuestItemsCount(2697) < 2L) {
                  st.playSound("ItemSound.quest_itemget");
               } else {
                  st.playSound("ItemSound.quest_middle");
                  if (st.getQuestItemsCount(2697) == 2L && st.getQuestItemsCount(2696) == 5L && st.getQuestItemsCount(2695) == 5L) {
                     st.set("cond", "17");
                  }
               }
            }
         } else if (npcId == 20554 && (st.getInt("cond") == 26 || st.getInt("cond") == 27) && st.getQuestItemsCount(2708) == 0L) {
            if (st.getRandom(100) < 30) {
               st.giveItems(2708, 1L);
               st.playSound("ItemSound.quest_middle");
            }
         } else if (npcId == 20201) {
            if (cond == 28 && st.getQuestItemsCount(2716) < 10L) {
               st.giveItems(2716, 1L);
               if (st.getQuestItemsCount(2716) < 10L) {
                  st.playSound("ItemSound.quest_itemget");
               } else {
                  st.playSound("ItemSound.quest_middle");
                  if (st.getQuestItemsCount(2716) == 10L
                     && st.getQuestItemsCount(2717) == 12L
                     && st.getQuestItemsCount(2718) == 5L
                     && st.getQuestItemsCount(2719) == 5L) {
                     st.set("cond", "29");
                  }
               }
            }
         } else if (npcId == 20158) {
            if (cond == 28 && st.getQuestItemsCount(2717) < 12L) {
               st.giveItems(2717, 1L);
               if (st.getQuestItemsCount(2717) < 12L) {
                  st.playSound("ItemSound.quest_itemget");
               } else {
                  st.playSound("ItemSound.quest_middle");
                  if (st.getQuestItemsCount(2716) == 10L
                     && st.getQuestItemsCount(2717) == 12L
                     && st.getQuestItemsCount(2718) == 5L
                     && st.getQuestItemsCount(2719) == 5L) {
                     st.set("cond", "29");
                  }
               }
            }
         } else if (npcId == 20552) {
            if (cond == 28 && st.getQuestItemsCount(2718) < 5L) {
               st.giveItems(2718, 1L);
               if (st.getQuestItemsCount(2718) < 5L) {
                  st.playSound("ItemSound.quest_itemget");
               } else {
                  st.playSound("ItemSound.quest_middle");
                  if (st.getQuestItemsCount(2716) == 10L
                     && st.getQuestItemsCount(2717) == 12L
                     && st.getQuestItemsCount(2718) == 5L
                     && st.getQuestItemsCount(2719) == 5L) {
                     st.set("cond", "29");
                  }
               }
            }
         } else if (npcId == 20567 && cond == 28 && st.getQuestItemsCount(2719) < 5L) {
            st.giveItems(2719, 1L);
            if (st.getQuestItemsCount(2719) < 5L) {
               st.playSound("ItemSound.quest_itemget");
            } else {
               st.playSound("ItemSound.quest_middle");
               if (st.getQuestItemsCount(2716) == 10L
                  && st.getQuestItemsCount(2717) == 12L
                  && st.getQuestItemsCount(2718) == 5L
                  && st.getQuestItemsCount(2719) == 5L) {
                  st.set("cond", "29");
               }
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _214_TrialOfScholar(214, "_214_TrialOfScholar", "");
   }
}
