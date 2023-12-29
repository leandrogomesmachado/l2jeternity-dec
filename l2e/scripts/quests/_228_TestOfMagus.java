package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _228_TestOfMagus extends Quest {
   private static final String qn = "_228_TestOfMagus";
   private static final int Rukal = 30629;
   private static final int Parina = 30391;
   private static final int Casian = 30612;
   private static final int Salamander = 30411;
   private static final int Sylph = 30412;
   private static final int Undine = 30413;
   private static final int Snake = 30409;
   private static final int RukalsLetter = 2841;
   private static final int ParinasLetter = 2842;
   private static final int LilacCharm = 2843;
   private static final int GoldenSeed1st = 2844;
   private static final int GoldenSeed2st = 2845;
   private static final int GoldenSeed3st = 2846;
   private static final int ScoreOfElements = 2847;
   private static final int ToneOfWater = 2856;
   private static final int ToneOfFire = 2857;
   private static final int ToneOfWind = 2858;
   private static final int ToneOfEarth = 2859;
   private static final int UndineCharm = 2862;
   private static final int DazzlingDrop = 2848;
   private static final int SalamanderCharm = 2860;
   private static final int FlameCrystal = 2849;
   private static final int SylphCharm = 2861;
   private static final int HarpysFeather = 2850;
   private static final int WyrmsWingbone = 2851;
   private static final int WindsusMane = 2852;
   private static final int SerpentCharm = 2863;
   private static final int EnchantedMonsterEyeShell = 2853;
   private static final int EnchantedStoneGolemPowder = 2854;
   private static final int EnchantedIronGolemScrap = 2855;
   private static final int MarkOfMagus = 2840;
   private static final int SingingFlowerPhantasm = 27095;
   private static final int SingingFlowerNightmare = 27096;
   private static final int SingingFlowerDarkling = 27097;
   private static final int Harpy = 20145;
   private static final int Wyrm = 20176;
   private static final int Windsus = 20553;
   private static final int EnchantedMonstereye = 20564;
   private static final int EnchantedStoneGolem = 20565;
   private static final int EnchantedIronGolem = 20566;
   private static final int QuestMonsterGhostFire = 27098;
   private static final int MarshStakatoWorker = 20230;
   private static final int ToadLord = 20231;
   private static final int MarshStakato = 20157;
   private static final int MarshStakatoSoldier = 20232;
   private static final int MarshStakatoDrone = 20234;
   private static final int[][] DROPLIST_COND = new int[][]{
      {3, 0, 27095, 2843, 2844, 10, 100, 1},
      {3, 0, 27096, 2843, 2845, 10, 100, 1},
      {3, 0, 27097, 2843, 2846, 10, 100, 1},
      {7, 0, 20145, 2861, 2850, 20, 50, 2},
      {7, 0, 20176, 2861, 2851, 10, 50, 2},
      {7, 0, 20553, 2861, 2852, 10, 50, 2},
      {7, 0, 20564, 2863, 2853, 10, 100, 2},
      {7, 0, 20565, 2863, 2854, 10, 100, 2},
      {7, 0, 20566, 2863, 2855, 10, 100, 2},
      {7, 0, 27098, 2860, 2849, 5, 50, 1},
      {7, 0, 20230, 2862, 2848, 20, 30, 2},
      {7, 0, 20231, 2862, 2848, 20, 30, 2},
      {7, 0, 20157, 2862, 2848, 20, 30, 2},
      {7, 0, 20232, 2862, 2848, 20, 40, 2},
      {7, 0, 20234, 2862, 2848, 20, 50, 2}
   };

   public _228_TestOfMagus(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30629);
      this.addTalkId(30629);
      this.addTalkId(30391);
      this.addTalkId(30612);
      this.addTalkId(30412);
      this.addTalkId(30409);
      this.addTalkId(30413);
      this.addTalkId(30411);

      for(int[] element : DROPLIST_COND) {
         this.addKillId(element[2]);
      }

      this.questItemIds = new int[]{
         2841, 2842, 2843, 2858, 2861, 2863, 2859, 2862, 2857, 2860, 2856, 2847, 2844, 2845, 2846, 2850, 2851, 2852, 2853, 2854, 2855, 2849, 2848
      };
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_228_TestOfMagus");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            htmltext = "30629-04.htm";
            st.giveItems(2841, 1L);
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30629_1")) {
            htmltext = "30629-09.htm";
         } else if (event.equalsIgnoreCase("30629_2")) {
            htmltext = "30629-10.htm";
            st.takeItems(2843, -1L);
            st.takeItems(2844, -1L);
            st.takeItems(2845, -1L);
            st.takeItems(2846, -1L);
            st.giveItems(2847, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "6");
         } else if (event.equalsIgnoreCase("30391_1")) {
            htmltext = "30391-02.htm";
            st.takeItems(2841, -1L);
            st.giveItems(2842, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "2");
         } else if (event.equalsIgnoreCase("30612_1")) {
            htmltext = "30612-02.htm";
            st.takeItems(2842, -1L);
            st.giveItems(2843, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "3");
         } else if (event.equalsIgnoreCase("30412_1")) {
            htmltext = "30412-02.htm";
            st.giveItems(2861, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "7");
         } else if (event.equalsIgnoreCase("30409_1")) {
            htmltext = "30409-02.htm";
         } else if (event.equalsIgnoreCase("30409_2")) {
            htmltext = "30409-03.htm";
            st.giveItems(2863, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "7");
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_228_TestOfMagus");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (npcId == 30629) {
                  if (player.getClassId().getId() != 11 && player.getClassId().getId() != 26 && player.getClassId().getId() != 39) {
                     htmltext = "30629-01.htm";
                     st.exitQuest(true);
                  } else if (player.getLevel() >= 39) {
                     htmltext = "30629-03.htm";
                  } else {
                     htmltext = "30629-02.htm";
                     st.exitQuest(true);
                  }
               }
               break;
            case 1:
               if (npcId == 30629) {
                  if (cond == 1) {
                     htmltext = "30629-05.htm";
                  } else if (cond == 2) {
                     htmltext = "30629-06.htm";
                  } else if (cond == 3) {
                     htmltext = "30629-07.htm";
                  } else if (cond == 4) {
                     htmltext = "30629-11.htm";
                  } else if (cond == 5) {
                     htmltext = "30629-08.htm";
                  } else if (cond == 6) {
                     st.takeItems(2847, -1L);
                     st.takeItems(2856, -1L);
                     st.takeItems(2857, -1L);
                     st.takeItems(2858, -1L);
                     st.takeItems(2859, -1L);
                     st.giveItems(2840, 1L);
                     htmltext = "30629-12.htm";
                     st.addExpAndSp(2058244, 141240);
                     st.giveItems(57, 372154L);
                     if (player.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
                        st.giveItems(7562, 122L);
                        st.giveItems(8870, 15L);
                        player.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
                     }

                     st.playSound("ItemSound.quest_finish");
                     st.set("cond", "0");
                     st.exitQuest(false);
                  }
               } else if (npcId == 30391) {
                  if (cond == 1) {
                     htmltext = "30391-01.htm";
                  } else if (cond == 2) {
                     htmltext = "30391-03.htm";
                  } else if (cond == 3 || cond == 4) {
                     htmltext = "30391-04.htm";
                  } else if (cond >= 5) {
                     htmltext = "30391-05.htm";
                  }
               } else if (npcId == 30612) {
                  if (cond == 2) {
                     htmltext = "30612-01.htm";
                  } else if (cond == 3) {
                     htmltext = "30612-03.htm";
                  } else if (cond == 4) {
                     htmltext = "30612-04.htm";
                  } else if (cond >= 5) {
                     htmltext = "30612-05.htm";
                  }
               } else if (npcId == 30411 && cond == 5) {
                  if (st.getQuestItemsCount(2857) == 0L) {
                     if (st.getQuestItemsCount(2860) == 0L) {
                        htmltext = "30411-01.htm";
                        st.giveItems(2860, 1L);
                        st.playSound("ItemSound.quest_middle");
                        st.set("cond", "7");
                     } else if (st.getQuestItemsCount(2849) < 5L) {
                        htmltext = "30411-02.htm";
                     } else {
                        st.takeItems(2860, -1L);
                        st.takeItems(2849, -1L);
                        st.giveItems(2857, 1L);
                        htmltext = "30411-03.htm";
                        st.playSound("ItemSound.quest_middle");
                     }
                  } else {
                     htmltext = "30411-04.htm";
                  }
               } else if (npcId == 30412 && cond == 5) {
                  if (st.getQuestItemsCount(2858) == 0L) {
                     if (st.getQuestItemsCount(2861) == 0L) {
                        htmltext = "30412-01.htm";
                     } else if (st.getQuestItemsCount(2850) >= 20L && st.getQuestItemsCount(2851) >= 10L && st.getQuestItemsCount(2852) >= 10L) {
                        st.takeItems(2861, -1L);
                        st.takeItems(2850, -1L);
                        st.takeItems(2851, -1L);
                        st.takeItems(2852, -1L);
                        st.giveItems(2858, 1L);
                        htmltext = "30412-04.htm";
                        st.playSound("ItemSound.quest_middle");
                     } else {
                        htmltext = "30412-03.htm";
                     }
                  } else {
                     htmltext = "30412-05.htm";
                  }
               } else if (npcId == 30409 && cond == 5) {
                  if (st.getQuestItemsCount(2859) == 0L) {
                     if (st.getQuestItemsCount(2863) == 0L) {
                        htmltext = "30409-01.htm";
                     } else if (st.getQuestItemsCount(2853) >= 10L && st.getQuestItemsCount(2854) >= 10L && st.getQuestItemsCount(2855) >= 10L) {
                        st.takeItems(2863, -1L);
                        st.takeItems(20564, -1L);
                        st.takeItems(2854, -1L);
                        st.takeItems(2855, -1L);
                        st.giveItems(2859, 1L);
                        htmltext = "30409-05.htm";
                        st.playSound("ItemSound.quest_middle");
                     } else {
                        htmltext = "30409-04.htm";
                     }
                  } else {
                     htmltext = "30409-06.htm";
                  }
               } else if (npcId == 30413 && cond == 5) {
                  if (st.getQuestItemsCount(2856) == 0L) {
                     if (st.getQuestItemsCount(2862) == 0L) {
                        htmltext = "30413-01.htm";
                        st.giveItems(2862, 1L);
                        st.set("cond", "7");
                     } else if (st.getQuestItemsCount(2848) < 20L) {
                        htmltext = "30413-02.htm";
                     } else {
                        st.takeItems(2862, -1L);
                        st.takeItems(2848, -1L);
                        st.giveItems(2856, 1L);
                        htmltext = "30413-03.htm";
                        st.playSound("ItemSound.quest_middle");
                     }
                  } else {
                     htmltext = "30413-04.htm";
                  }
               }
               break;
            case 2:
               if (npcId == 30629) {
                  htmltext = getAlreadyCompletedMsg(player);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_228_TestOfMagus");
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

         if (st.getQuestItemsCount(2844) != 0L && st.getQuestItemsCount(2845) != 0L && st.getQuestItemsCount(2846) != 0L) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _228_TestOfMagus(228, "_228_TestOfMagus", "");
   }
}
