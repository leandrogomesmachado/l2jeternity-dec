package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _126_TheNameOfEvil2 extends Quest {
   private static final String qn = "_126_TheNameOfEvil2";
   private static int Mushika = 32114;
   private static int Asamah = 32115;
   private static int UluKaimu = 32119;
   private static int BaluKaimu = 32120;
   private static int ChutaKaimu = 32121;
   private static int WarriorGrave = 32122;
   private static int ShilenStoneStatue = 32109;
   private static final int[] NPCS = new int[]{Mushika, Asamah, UluKaimu, BaluKaimu, ChutaKaimu, WarriorGrave, ShilenStoneStatue};
   private static int BONEPOWDER = 8783;
   private static int EPITAPH = 8781;
   private static int EWA = 729;
   private static int ADENA = 57;

   public _126_TheNameOfEvil2(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(Asamah);

      for(int i : NPCS) {
         this.addTalkId(i);
      }
   }

   private final String getSongOne32122(QuestState st) {
      String htmltext = "32122-24.htm";
      if (st.getInt("cond") == 14 && st.getInt("DO") > 0 && st.getInt("MI") > 0 && st.getInt("FA") > 0 && st.getInt("SOL") > 0 && st.getInt("FA_2") > 0) {
         htmltext = "32122-42.htm";
         st.set("cond", "15");
         st.unset("DO");
         st.unset("MI");
         st.unset("FA");
         st.unset("SOL");
         st.unset("FA_2");
         st.playSound("ItemSound.quest_middle");
      }

      return htmltext;
   }

   private final String getSongTwo32122(QuestState st) {
      String htmltext = "32122-45.htm";
      if (st.getInt("cond") == 15 && st.getInt("FA") > 0 && st.getInt("SOL") > 0 && st.getInt("TI") > 0 && st.getInt("SOL_2") > 0 && st.getInt("FA_2") > 0) {
         htmltext = "32122-63.htm";
         st.set("cond", "16");
         st.unset("FA");
         st.unset("SOL");
         st.unset("TI");
         st.unset("SOL_2");
         st.unset("FA3_2");
         st.playSound("ItemSound.quest_middle");
      }

      return htmltext;
   }

   private final String getSongTri32122(QuestState st) {
      String htmltext = "32122-66.htm";
      if (st.getInt("cond") == 16 && st.getInt("SOL") > 0 && st.getInt("FA") > 0 && st.getInt("MI") > 0 && st.getInt("FA_2") > 0 && st.getInt("MI_2") > 0) {
         htmltext = "32122-84.htm";
         st.set("cond", "17");
         st.unset("SOL");
         st.unset("FA");
         st.unset("MI");
         st.unset("FA_2");
         st.unset("MI_2");
         st.playSound("ItemSound.quest_middle");
      }

      return htmltext;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_126_TheNameOfEvil2");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32115-05.htm")) {
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.set("cond", "1");
         }

         if (event.equalsIgnoreCase("32115-10.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
         }

         if (event.equalsIgnoreCase("32119-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
         }

         if (event.equalsIgnoreCase("32119-09.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
         }

         if (event.equalsIgnoreCase("32119-11.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
         }

         if (event.equalsIgnoreCase("32120-07.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
         }

         if (event.equalsIgnoreCase("32120-09.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
         }

         if (event.equalsIgnoreCase("32120-11.htm")) {
            st.set("cond", "8");
            st.playSound("ItemSound.quest_middle");
         }

         if (event.equalsIgnoreCase("32121-07.htm")) {
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
         }

         if (event.equalsIgnoreCase("32121-10.htm")) {
            st.set("cond", "10");
            st.playSound("ItemSound.quest_middle");
         }

         if (event.equalsIgnoreCase("32121-15.htm")) {
            st.set("cond", "11");
            st.playSound("ItemSound.quest_middle");
         }

         if (event.equalsIgnoreCase("32122-03.htm")) {
            st.set("cond", "12");
            st.playSound("ItemSound.quest_middle");
         }

         if (event.equalsIgnoreCase("32122-15.htm")) {
            st.set("cond", "13");
            st.playSound("ItemSound.quest_middle");
         }

         if (event.equalsIgnoreCase("32122-18.htm")) {
            st.set("cond", "14");
            st.playSound("ItemSound.quest_middle");
         }

         if (event.equalsIgnoreCase("32122-87.htm")) {
            htmltext = "32122-87.htm";
            st.giveItems(BONEPOWDER, 1L);
         }

         if (event.equalsIgnoreCase("32122-90.htm")) {
            st.set("cond", "18");
            st.playSound("ItemSound.quest_middle");
         }

         if (event.equalsIgnoreCase("32109-02.htm")) {
            st.set("cond", "19");
            st.playSound("ItemSound.quest_middle");
         }

         if (event.equalsIgnoreCase("32109-19.htm")) {
            st.set("cond", "20");
            st.takeItems(BONEPOWDER, 1L);
            st.playSound("ItemSound.quest_middle");
         }

         if (event.equalsIgnoreCase("32115-21.htm")) {
            st.set("cond", "21");
            st.playSound("ItemSound.quest_middle");
         }

         if (event.equalsIgnoreCase("32115-28.htm")) {
            st.set("cond", "22");
            st.playSound("ItemSound.quest_middle");
         }

         if (event.equalsIgnoreCase("32114-08.htm")) {
            st.set("cond", "23");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32114-09.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.giveItems(EWA, 1L);
            st.giveItems(ADENA, 298496L);
            st.addExpAndSp(670612, 0);
            st.unset("cond");
            st.setState((byte)2);
            st.exitQuest(false);
         }

         if (event.equalsIgnoreCase("DOOne")) {
            htmltext = "32122-26.htm";
            if (st.getInt("DO") < 1) {
               st.set("DO", "1");
            }
         } else if (event.equalsIgnoreCase("MIOne")) {
            htmltext = "32122-30.htm";
            if (st.getInt("MI") < 1) {
               st.set("MI", "1");
            }
         } else if (event.equalsIgnoreCase("FAOne")) {
            htmltext = "32122-34.htm";
            if (st.getInt("FA") < 1) {
               st.set("FA", "1");
            }
         } else if (event.equalsIgnoreCase("SOLOne")) {
            htmltext = "32122-38.htm";
            if (st.getInt("SOL") < 1) {
               st.set("SOL", "1");
            }
         } else if (event.equalsIgnoreCase("FA_2One")) {
            if (st.getInt("FA_2") < 1) {
               st.set("FA_2", "1");
            }

            htmltext = this.getSongOne32122(st);
         } else if (event.equalsIgnoreCase("FATwo")) {
            htmltext = "32122-47.htm";
            if (st.getInt("FA") < 1) {
               st.set("FA", "1");
            }
         } else if (event.equalsIgnoreCase("SOLTwo")) {
            htmltext = "32122-51.htm";
            if (st.getInt("SOL") < 1) {
               st.set("SOL", "1");
            }
         } else if (event.equalsIgnoreCase("TITwo")) {
            htmltext = "32122-55.htm";
            if (st.getInt("TI") < 1) {
               st.set("TI", "1");
            }
         } else if (event.equalsIgnoreCase("SOL_2Two")) {
            htmltext = "32122-59.htm";
            if (st.getInt("SOL_2") < 1) {
               st.set("SOL_2", "1");
            }
         } else if (event.equalsIgnoreCase("FA_2Two")) {
            if (st.getInt("FA_2") < 1) {
               st.set("FA_2", "1");
            }

            htmltext = this.getSongTwo32122(st);
         } else if (event.equalsIgnoreCase("SOLTri")) {
            htmltext = "32122-68.htm";
            if (st.getInt("SOL") < 1) {
               st.set("SOL", "1");
            }
         } else if (event.equalsIgnoreCase("FATri")) {
            htmltext = "32122-72.htm";
            if (st.getInt("FA") < 1) {
               st.set("FA", "1");
            }
         } else if (event.equalsIgnoreCase("MITri")) {
            htmltext = "32122-76.htm";
            if (st.getInt("MI") < 1) {
               st.set("MI", "1");
            }
         } else if (event.equalsIgnoreCase("FA_2Tri")) {
            htmltext = "32122-80.htm";
            if (st.getInt("FA_2") < 1) {
               st.set("FA_2", "1");
            }
         } else if (event.equalsIgnoreCase("MI_2Tri")) {
            if (st.getInt("MI_2") < 1) {
               st.set("MI_2", "1");
            }

            htmltext = this.getSongTri32122(st);
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_126_TheNameOfEvil2");
      int cond = st.getInt("cond");
      int npcId = npc.getId();
      if (npcId == Asamah) {
         QuestState _prev = player.getQuestState("_125_TheNameOfEvil1");
         if (cond == 0) {
            if (_prev != null && _prev.getState() == 2 && st.getQuestItemsCount(EPITAPH) >= 1L) {
               htmltext = "32115-01.htm";
               st.exitQuest(true);
            } else if (player.getLevel() < 77) {
               htmltext = "32115-02.htm";
               st.exitQuest(true);
            } else {
               htmltext = "32115-04.htm";
               st.exitQuest(true);
            }
         } else {
            if (cond == 1) {
               return "32115-11.htm";
            }

            if (cond > 1 && cond < 20) {
               return "32115-12.htm";
            }

            if (cond == 20) {
               return "32115-13.htm";
            }

            if (cond == 22) {
               return "32115-29.htm";
            }
         }
      } else if (npcId == UluKaimu) {
         if (cond == 1) {
            return "32119-01a.htm";
         }

         if (cond == 2) {
            return "32119-02.htm";
         }

         if (cond == 3) {
            return "32119-08.htm";
         }

         if (cond == 4) {
            return "32119-09.htm";
         }

         if (cond >= 5) {
            return "32119-12.htm";
         }
      } else if (npcId == BaluKaimu) {
         if (cond < 5) {
            return "32120-02.htm";
         }

         if (cond == 5) {
            return "32120-01.htm";
         }

         if (cond == 6) {
            return "32120-03.htm";
         }

         if (cond == 7) {
            return "32120-08.htm";
         }

         if (cond >= 8) {
            return "32120-12.htm";
         }
      } else if (npcId == ChutaKaimu) {
         if (cond < 8) {
            return "32121-02.htm";
         }

         if (cond == 8) {
            return "32121-01.htm";
         }

         if (cond == 9) {
            return "32121-03.htm";
         }

         if (cond == 10) {
            return "32121-10.htm";
         }

         if (cond >= 11) {
            return "32121-16.htm";
         }
      } else if (npcId == WarriorGrave) {
         if (cond < 11) {
            return "32122-02.htm";
         }

         if (cond == 11) {
            return "32122-01.htm";
         }

         if (cond == 12) {
            return "32122-15.htm";
         }

         if (cond == 13) {
            return "32122-18.htm";
         }

         if (cond == 14) {
            return "32122-24.htm";
         }

         if (cond == 15) {
            return "32122-45.htm";
         }

         if (cond == 16) {
            return "32122-66.htm";
         }

         if (cond == 17) {
            return "32122-84.htm";
         }

         if (cond == 18) {
            return "32122-91.htm";
         }
      } else if (npcId == ShilenStoneStatue) {
         if (cond < 18) {
            return "32109-03.htm";
         }

         if (cond == 18) {
            return "32109-02.htm";
         }

         if (cond == 19) {
            return "32109-05.htm";
         }

         if (cond > 19) {
            return "32109-04.htm";
         }
      } else {
         if (npcId != Mushika) {
            return getAlreadyCompletedMsg(player);
         }

         if (cond < 22) {
            return "32114-02.htm";
         }

         if (cond == 22) {
            return "32114-01.htm";
         }

         if (cond == 23) {
            return "32114-04.htm";
         }
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _126_TheNameOfEvil2(126, "_126_TheNameOfEvil2", "");
   }
}
