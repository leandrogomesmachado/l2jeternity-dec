package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _231_TestOfTheMaestro extends Quest {
   private static final String qn = "_231_TestOfTheMaestro";
   private static final int Lockirin = 30531;
   private static final int Balanki = 30533;
   private static final int Arin = 30536;
   private static final int Filaur = 30535;
   private static final int Spiron = 30532;
   private static final int Croto = 30671;
   private static final int Kamur = 30675;
   private static final int Dubabah = 30672;
   private static final int Toma = 30556;
   private static final int Lorain = 30673;
   private static final int RecommendationOfBalanki = 2864;
   private static final int RecommendationOfFilaur = 2865;
   private static final int RecommendationOfArin = 2866;
   private static final int LetterOfSolderDetachment = 2868;
   private static final int PaintOfKamuru = 2869;
   private static final int NecklaceOfKamuru = 2870;
   private static final int PaintOfTeleportDevice = 2871;
   private static final int TeleportDevice = 2872;
   private static final int ArchitectureOfCruma = 2873;
   private static final int ReportOfCruma = 2874;
   private static final int IngredientsOfAntidote = 2875;
   private static final int StingerWaspNeedle = 2876;
   private static final int MarshSpidersWeb = 2877;
   private static final int BloodOfLeech = 2878;
   private static final int BrokenTeleportDevice = 2916;
   private static final int MarkOfMaestro = 2867;
   private static final int QuestMonsterEvilEyeLord = 27133;
   private static final int GiantMistLeech = 20225;
   private static final int StingerWasp = 20229;
   private static final int MarshSpider = 20233;
   private static final int[][] DROPLIST_COND = new int[][]{
      {4, 5, 27133, 0, 2870, 1, 100, 1}, {13, 0, 20225, 0, 2878, 10, 100, 1}, {13, 0, 20229, 0, 2876, 10, 100, 1}, {13, 0, 20233, 0, 2877, 10, 100, 1}
   };

   public _231_TestOfTheMaestro(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30531);
      this.addTalkId(30531);
      this.addTalkId(30533);
      this.addTalkId(30536);
      this.addTalkId(30535);
      this.addTalkId(30532);
      this.addTalkId(30671);
      this.addTalkId(30675);
      this.addTalkId(30672);
      this.addTalkId(30556);
      this.addTalkId(30673);

      for(int[] element : DROPLIST_COND) {
         this.addKillId(element[2]);
         this.registerQuestItems(new int[]{element[4]});
      }

      this.questItemIds = new int[]{2869, 2868, 2871, 2916, 2872, 2873, 2875, 2864, 2865, 2866, 2874};
   }

   public void recommendationCount(QuestState st) {
      if (st.getQuestItemsCount(2866) != 0L && st.getQuestItemsCount(2865) != 0L && st.getQuestItemsCount(2864) != 0L) {
         st.setCond(17);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_231_TestOfTheMaestro");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            htmltext = "30531-04.htm";
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.set("cond", "1");
         } else if (event.equalsIgnoreCase("30533_1")) {
            htmltext = "30533-02.htm";
            st.set("cond", "2");
         } else if (event.equalsIgnoreCase("30671_1")) {
            htmltext = "30671-02.htm";
            st.giveItems(2869, 1L);
            st.set("cond", "3");
         } else if (event.equalsIgnoreCase("30556_1")) {
            htmltext = "30556-02.htm";
         } else if (event.equalsIgnoreCase("30556_2")) {
            htmltext = "30556-03.htm";
         } else if (event.equalsIgnoreCase("30556_3")) {
            htmltext = "30556-05.htm";
            st.takeItems(2871, -1L);
            st.giveItems(2916, 1L);
            st.set("cond", "9");
            st.getPlayer().teleToLocation(140352, -194133, -2028, true);
         } else if (event.equalsIgnoreCase("30556_4")) {
            htmltext = "30556-04.htm";
         } else if (event.equalsIgnoreCase("30673_1")) {
            htmltext = "30673-04.htm";
            st.takeItems(2878, -1L);
            st.takeItems(2876, -1L);
            st.takeItems(2877, -1L);
            st.takeItems(2875, -1L);
            st.giveItems(2874, 1L);
            st.set("cond", "15");
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_231_TestOfTheMaestro");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (npcId == 30531) {
                  if (player.getClassId().getId() == 56) {
                     if (player.getLevel() > 38) {
                        htmltext = "30531-03.htm";
                     } else {
                        htmltext = "30531-01.htm";
                        st.exitQuest(true);
                     }
                  } else {
                     htmltext = "30531-02.htm";
                     st.exitQuest(true);
                  }
               }
               break;
            case 1:
               if (npcId == 30531) {
                  if (cond >= 1 && cond <= 16) {
                     htmltext = "30531-05.htm";
                  } else if (cond == 17) {
                     st.addExpAndSp(2058244, 141240);
                     st.giveItems(57, 372154L);
                     if (player.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
                        st.giveItems(7562, 23L);
                        player.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
                     }

                     htmltext = "30531-06.htm";
                     st.takeItems(2864, -1L);
                     st.takeItems(2865, -1L);
                     st.takeItems(2866, -1L);
                     st.giveItems(2867, 1L);
                     st.unset("cond");
                     st.playSound("ItemSound.quest_finish");
                     st.exitQuest(false);
                  }
               } else if (npcId == 30533) {
                  if ((cond == 1 || cond == 11 || cond == 16) && st.getQuestItemsCount(2864) == 0L) {
                     htmltext = "30533-01.htm";
                  } else if (cond == 2) {
                     htmltext = "30533-03.htm";
                  } else if (cond == 6) {
                     st.takeItems(2868, -1L);
                     st.giveItems(2864, 1L);
                     htmltext = "30533-04.htm";
                     st.set("cond", "7");
                     this.recommendationCount(st);
                  } else if (cond == 7 || cond == 17) {
                     htmltext = "30533-05.htm";
                  }
               } else if (npcId == 30536) {
                  if ((cond == 1 || cond == 7 || cond == 16) && st.getQuestItemsCount(2866) == 0L) {
                     st.giveItems(2871, 1L);
                     htmltext = "30536-01.htm";
                     st.set("cond", "8");
                  } else if (cond == 8) {
                     htmltext = "30536-02.htm";
                  } else if (cond == 10) {
                     st.takeItems(2872, -1L);
                     st.giveItems(2866, 1L);
                     htmltext = "30536-03.htm";
                     st.set("cond", "11");
                     this.recommendationCount(st);
                  } else if (cond == 11 || cond == 17) {
                     htmltext = "30536-04.htm";
                  }
               } else if (npcId == 30535) {
                  if ((cond == 1 || cond == 7 || cond == 11) && st.getQuestItemsCount(2865) == 0L) {
                     st.giveItems(2873, 1L);
                     htmltext = "30535-01.htm";
                     st.set("cond", "12");
                  } else if (cond == 12) {
                     htmltext = "30535-02.htm";
                  } else if (cond == 15) {
                     st.takeItems(2874, 1L);
                     st.giveItems(2865, 1L);
                     st.set("cond", "16");
                     htmltext = "30535-03.htm";
                     this.recommendationCount(st);
                  } else if (cond > 15) {
                     htmltext = "30535-04.htm";
                  }
               } else if (npcId == 30671) {
                  if (cond == 2) {
                     htmltext = "30671-01.htm";
                  } else if (cond == 3) {
                     htmltext = "30671-03.htm";
                  } else if (cond == 5) {
                     st.takeItems(2870, -1L);
                     st.takeItems(2869, -1L);
                     st.giveItems(2868, 1L);
                     htmltext = "30671-04.htm";
                     st.set("cond", "6");
                  } else if (cond == 6) {
                     htmltext = "30671-05.htm";
                  }
               } else if (npcId == 30672 && cond == 3) {
                  htmltext = "30672-01.htm";
               } else if (npcId == 30675 && cond == 3) {
                  htmltext = "30675-01.htm";
                  st.set("cond", "4");
               } else if (npcId == 30556) {
                  if (cond == 8) {
                     htmltext = "30556-01.htm";
                  } else if (cond == 9) {
                     st.takeItems(2916, -1L);
                     st.giveItems(2872, 5L);
                     htmltext = "30556-06.htm";
                     st.set("cond", "10");
                  } else if (cond == 10) {
                     htmltext = "30556-07.htm";
                  }
               } else if (npcId == 30673) {
                  if (cond == 12) {
                     st.takeItems(2873, -1L);
                     st.giveItems(2875, 1L);
                     st.set("cond", "13");
                     htmltext = "30673-01.htm";
                  } else if (cond == 13) {
                     htmltext = "30673-02.htm";
                  } else if (cond == 14) {
                     htmltext = "30673-03.htm";
                  } else if (cond == 15) {
                     htmltext = "30673-05.htm";
                  }
               } else if (npcId == 30532 && (cond == 1 || cond == 7 || cond == 11 || cond == 16)) {
                  htmltext = "30532-01.htm";
               }
               break;
            case 2:
               if (npcId == 30531) {
                  htmltext = getAlreadyCompletedMsg(player);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_231_TestOfTheMaestro");
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

         if (cond == 13 && st.getQuestItemsCount(2878) >= 10L && st.getQuestItemsCount(2876) >= 10L && st.getQuestItemsCount(2877) >= 10L) {
            st.set("cond", "14");
            st.playSound("Itemsound.quest_middle");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _231_TestOfTheMaestro(231, "_231_TestOfTheMaestro", "");
   }
}
