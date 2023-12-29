package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _225_TestOfTheSearcher extends Quest {
   private static final String qn = "_225_TestOfTheSearcher";
   private static final int Luther = 30690;
   private static final int Alex = 30291;
   private static final int Tyra = 30420;
   private static final int Chest = 30628;
   private static final int Leirynn = 30728;
   private static final int Borys = 30729;
   private static final int Jax = 30730;
   private static final int Tree = 30627;
   private static final int LuthersLetter = 2784;
   private static final int AlexsWarrant = 2785;
   private static final int Leirynns1stOrder = 2786;
   private static final int DeluTotem = 2787;
   private static final int Leirynns2ndOrder = 2788;
   private static final int ChiefKalkisFang = 2789;
   private static final int AlexsRecommend = 2808;
   private static final int LambertsMap = 2792;
   private static final int LeirynnsReport = 2790;
   private static final int AlexsLetter = 2793;
   private static final int StrangeMap = 2791;
   private static final int AlexsOrder = 2794;
   private static final int CombinedMap = 2805;
   private static final int GoldBar = 2807;
   private static final int WineCatalog = 2795;
   private static final int OldOrder = 2799;
   private static final int MalrukianWine = 2798;
   private static final int TyrasContract = 2796;
   private static final int RedSporeDust = 2797;
   private static final int JaxsDiary = 2800;
   private static final int SoltsMap = 2803;
   private static final int MakelsMap = 2804;
   private static final int RustedKey = 2806;
   private static final int TornMapPiece1st = 2801;
   private static final int TornMapPiece2st = 2802;
   private static final int MarkOfSearcher = 2809;
   private static final int DeluLizardmanShaman = 20781;
   private static final int DeluLizardmanAssassin = 27094;
   private static final int DeluChiefKalkis = 27093;
   private static final int GiantFungus = 20555;
   private static final int RoadScavenger = 20551;
   private static final int HangmanTree = 20144;
   private static final int[][] DROPLIST_COND = new int[][]{
      {3, 4, 20781, 0, 2787, 10, 100, 1}, {3, 4, 27094, 0, 2787, 10, 100, 1}, {10, 11, 20555, 0, 2797, 10, 100, 1}
   };

   public _225_TestOfTheSearcher(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30690);
      this.addTalkId(30690);
      this.addTalkId(30291);
      this.addTalkId(30728);
      this.addTalkId(30729);
      this.addTalkId(30420);
      this.addTalkId(30730);
      this.addTalkId(30627);
      this.addTalkId(30628);
      this.addKillId(27093);
      this.addKillId(20551);
      this.addKillId(20144);

      for(int[] element : DROPLIST_COND) {
         this.addKillId(element[2]);
      }

      this.questItemIds = new int[]{
         2787, 2797, 2784, 2785, 2786, 2788, 2790, 2789, 2791, 2792, 2793, 2794, 2795, 2796, 2799, 2798, 2800, 2801, 2802, 2803, 2804, 2806, 2805
      };
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_225_TestOfTheSearcher");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30690-05.htm")) {
            st.giveItems(2784, 1L);
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30291-07.htm")) {
            st.takeItems(2790, -1L);
            st.takeItems(2791, -1L);
            st.giveItems(2792, 1L);
            st.giveItems(2793, 1L);
            st.giveItems(2794, 1L);
            st.set("cond", "8");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30420-01a.htm")) {
            st.takeItems(2795, -1L);
            st.giveItems(2796, 1L);
            st.set("cond", "10");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30730-01d.htm")) {
            st.takeItems(2799, -1L);
            st.giveItems(2800, 1L);
            st.set("cond", "14");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30627-01a.htm")) {
            if (st.getQuestItemsCount(2806) == 0L) {
               st.giveItems(2806, 1L);
            }

            st.addSpawn(30628, 10098, 157287, -2406, 300000);
            st.set("cond", "17");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30628-01a.htm")) {
            st.takeItems(2806, -1L);
            st.giveItems(2807, 20L);
            st.set("cond", "18");
            st.playSound("ItemSound.quest_middle");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_225_TestOfTheSearcher");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (npcId == 30690) {
                  if (player.getClassId().getId() != 7
                     && player.getClassId().getId() != 22
                     && player.getClassId().getId() != 35
                     && player.getClassId().getId() != 54) {
                     htmltext = "30690-01.htm";
                     st.exitQuest(true);
                  } else if (player.getLevel() >= 39) {
                     if (player.getClassId().getId() == 54) {
                        htmltext = "30690-04.htm";
                     } else {
                        htmltext = "30690-03.htm";
                     }
                  } else {
                     htmltext = "30690-02.htm";
                     st.exitQuest(true);
                  }
               }
               break;
            case 1:
               if (npcId == 30690) {
                  if (cond == 1) {
                     htmltext = "30690-06.htm";
                  } else if (cond > 1 && cond < 16) {
                     htmltext = "30623-17.htm";
                  } else if (cond == 19) {
                     htmltext = "30690-08.htm";
                     st.addExpAndSp(894888, 61408);
                     st.giveItems(57, 161806L);
                     if (player.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
                        st.giveItems(7562, 82L);
                        player.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
                     }

                     st.takeItems(2808, -1L);
                     st.giveItems(2809, 1L);
                     st.set("cond", "0");
                     st.playSound("ItemSound.quest_finish");
                     st.exitQuest(false);
                  }
               } else if (npcId == 30291) {
                  if (cond == 1) {
                     htmltext = "30291-01.htm";
                     st.takeItems(2784, -1L);
                     st.giveItems(2785, 1L);
                     st.set("cond", "2");
                     st.playSound("ItemSound.quest_middle");
                  } else if (cond == 2) {
                     htmltext = "30291-02.htm";
                  } else if (cond > 2 && cond < 7) {
                     htmltext = "30291-03.htm";
                  } else if (cond == 7) {
                     htmltext = "30291-04.htm";
                  } else if (cond == 8) {
                     htmltext = "30291-08.htm";
                  } else if (cond == 13 || cond == 14) {
                     htmltext = "30291-09.htm";
                  } else if (cond == 18) {
                     st.takeItems(2794, -1L);
                     st.takeItems(2805, -1L);
                     st.takeItems(2807, -1L);
                     st.giveItems(2808, 1L);
                     htmltext = "30291-11.htm";
                     st.set("cond", "19");
                     st.playSound("ItemSound.quest_middle");
                  } else if (cond == 19) {
                     htmltext = "30291-12.htm";
                  }
               } else if (npcId == 30728) {
                  if (cond == 2) {
                     htmltext = "30728-01.htm";
                     st.takeItems(2785, -1L);
                     st.giveItems(2786, 1L);
                     st.set("cond", "3");
                     st.playSound("ItemSound.quest_middle");
                  } else if (cond == 3) {
                     htmltext = "30728-02.htm";
                  } else if (cond == 4) {
                     htmltext = "30728-03.htm";
                     st.takeItems(2787, -1L);
                     st.takeItems(2786, -1L);
                     st.giveItems(2788, 1L);
                     st.set("cond", "5");
                     st.playSound("ItemSound.quest_middle");
                  } else if (cond == 5) {
                     htmltext = "30728-04.htm";
                  } else if (cond == 6) {
                     st.takeItems(2789, -1L);
                     st.takeItems(2788, -1L);
                     st.giveItems(2790, 1L);
                     htmltext = "30728-05.htm";
                     st.set("cond", "7");
                     st.playSound("ItemSound.quest_middle");
                  } else if (cond == 7) {
                     htmltext = "30728-06.htm";
                  } else if (cond == 8) {
                     htmltext = "30728-07.htm";
                  }
               } else if (npcId == 30729) {
                  if (cond == 8) {
                     st.takeItems(2793, -1L);
                     st.giveItems(2795, 1L);
                     htmltext = "30729-01.htm";
                     st.set("cond", "9");
                     st.playSound("ItemSound.quest_middle");
                  } else if (cond == 9) {
                     htmltext = "30729-02.htm";
                  } else if (cond == 12) {
                     st.takeItems(2795, -1L);
                     st.takeItems(2798, -1L);
                     st.giveItems(2799, 1L);
                     htmltext = "30729-03.htm";
                     st.set("cond", "13");
                     st.playSound("ItemSound.quest_middle");
                  } else if (cond == 13) {
                     htmltext = "30729-04.htm";
                  } else if (cond >= 8 && cond <= 14) {
                     htmltext = "30729-05.htm";
                  }
               } else if (npcId == 30420) {
                  if (cond == 9) {
                     htmltext = "30420-01.htm";
                  } else if (cond == 10) {
                     htmltext = "30420-02.htm";
                  } else if (cond == 11) {
                     st.takeItems(2796, -1L);
                     st.takeItems(2797, -1L);
                     st.giveItems(2798, 1L);
                     htmltext = "30420-03.htm";
                     st.set("cond", "12");
                     st.playSound("ItemSound.quest_middle");
                  } else if (cond == 12 || cond == 13) {
                     htmltext = "30420-04.htm";
                  }
               } else if (npcId == 30730) {
                  if (cond == 13) {
                     htmltext = "30730-01.htm";
                  } else if (cond == 14) {
                     htmltext = "30730-02.htm";
                  } else if (cond == 15) {
                     st.takeItems(2803, -1L);
                     st.takeItems(2804, -1L);
                     st.takeItems(2792, -1L);
                     st.takeItems(2800, -1L);
                     st.giveItems(2805, 1L);
                     htmltext = "30730-03.htm";
                     st.set("cond", "16");
                  } else if (cond == 16) {
                     htmltext = "30730-04.htm";
                  }
               } else if (npcId == 30627) {
                  if (cond == 16 || cond == 17) {
                     htmltext = "30627-01.htm";
                  }
               } else if (npcId == 30628) {
                  if (cond == 17) {
                     htmltext = "30628-01.htm";
                  } else {
                     htmltext = "30628-02.htm";
                  }
               }
               break;
            case 2:
               if (npcId == 30690) {
                  htmltext = getAlreadyCompletedMsg(player);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_225_TestOfTheSearcher");
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
                  st.playSound("Itemsound.quest_itemget");
               }
            }
         }

         if (cond == 5 && npcId == 27093) {
            if (st.getQuestItemsCount(2791) == 0L) {
               st.giveItems(2791, 1L);
            }

            if (st.getQuestItemsCount(2789) == 0L) {
               st.giveItems(2789, 1L);
            }

            st.playSound("ItemSound.quest_middle");
            st.set("cond", "6");
         } else if (cond == 14) {
            if (npcId == 20551 && st.getQuestItemsCount(2803) == 0L) {
               st.giveItems(2801, 1L);
               if (st.getQuestItemsCount(2801) >= 4L) {
                  st.takeItems(2801, -1L);
                  st.giveItems(2803, 1L);
               }
            } else if (npcId == 20144 && st.getQuestItemsCount(2804) == 0L) {
               st.giveItems(2802, 1L);
               if (st.getQuestItemsCount(2802) >= 4L) {
                  st.takeItems(2802, -1L);
                  st.giveItems(2804, 1L);
               }
            }

            if (st.getQuestItemsCount(2803) != 0L && st.getQuestItemsCount(2804) != 0L) {
               st.set("cond", "15");
               st.playSound("ItemSound.quest_middle");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _225_TestOfTheSearcher(225, "_225_TestOfTheSearcher", "");
   }
}
