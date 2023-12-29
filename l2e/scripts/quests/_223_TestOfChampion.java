package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _223_TestOfChampion extends Quest {
   private static final String qn = "_223_TestOfChampion";
   private static final int MARK_OF_CHAMPION = 3276;
   private static final int ASCALONS_LETTER1 = 3277;
   private static final int MASONS_LETTER = 3278;
   private static final int IRON_ROSE_RING = 3279;
   private static final int ASCALONS_LETTER2 = 3280;
   private static final int WHITE_ROSE_INSIGNIA = 3281;
   private static final int GROOTS_LETTER = 3282;
   private static final int ASCALONS_LETTER3 = 3283;
   private static final int MOUENS_ORDER1 = 3284;
   private static final int MOUENS_ORDER2 = 3285;
   private static final int MOUENS_LETTER = 3286;
   private static final int HARPYS_EGG = 3287;
   private static final int MEDUSA_VENOM = 3288;
   private static final int WINDSUS_BILE = 3289;
   private static final int BLOODY_AXE_HEAD = 3290;
   private static final int ROAD_RATMAN_HEAD = 3291;
   private static final int LETO_LIZARDMAN_FANG = 3292;
   private static final int Ascalon = 30624;
   private static final int Groot = 30093;
   private static final int Mouen = 30196;
   private static final int Mason = 30625;
   private static final int Harpy = 20145;
   private static final int HarpyMatriarch = 27088;
   private static final int Medusa = 20158;
   private static final int Windsus = 20553;
   private static final int RoadScavenger = 20551;
   private static final int LetoLizardman = 20577;
   private static final int LetoLizardmanArcher = 20578;
   private static final int LetoLizardmanSoldier = 20579;
   private static final int LetoLizardmanWarrior = 20580;
   private static final int LetoLizardmanShaman = 20581;
   private static final int LetoLizardmanOverlord = 20582;
   private static final int BloodyAxeElite = 20780;
   private static final int[][] DROPLIST = new int[][]{
      {2, 3, 20780, 3290, 20, 10},
      {6, 7, 20145, 3287, 100, 30},
      {6, 7, 27088, 3287, 100, 30},
      {6, 7, 20158, 3288, 50, 30},
      {6, 7, 20553, 3289, 50, 30},
      {10, 11, 20551, 3291, 20, 10},
      {12, 13, 20577, 3292, 20, 10},
      {12, 13, 20578, 3292, 22, 10},
      {12, 13, 20579, 3292, 24, 10},
      {12, 13, 20580, 3292, 26, 10},
      {12, 13, 20581, 3292, 28, 10},
      {12, 13, 20582, 3292, 30, 10}
   };

   public _223_TestOfChampion(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30624);
      this.addTalkId(30624);
      this.addTalkId(30093);
      this.addTalkId(30196);
      this.addTalkId(30625);
      this.addKillId(new int[]{20145, 20158, 27088, 20551, 20553, 20577, 20578, 20579, 20580, 20581, 20582, 20780});
      this.questItemIds = new int[]{3278, 3288, 3289, 3281, 3287, 3282, 3286, 3277, 3279, 3290, 3280, 3283, 3284, 3291, 3285, 3292};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_223_TestOfChampion");
      if (st == null) {
         return event;
      } else {
         if (event.equals("1")) {
            htmltext = "30624-06.htm";
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(3277, 1L);
         } else if (event.equals("30624_1")) {
            htmltext = "30624-05.htm";
         } else if (event.equals("30624_2")) {
            htmltext = "30624-10.htm";
            st.playSound("Itemsound.quest_middle");
            st.set("cond", "5");
            st.takeItems(3278, -1L);
            st.giveItems(3280, 1L);
         } else if (event.equals("30624_3")) {
            htmltext = "30624-14.htm";
            st.playSound("Itemsound.quest_middle");
            st.set("cond", "9");
            st.takeItems(3282, -1L);
            st.giveItems(3283, 1L);
         } else if (event.equals("30625_1")) {
            htmltext = "30625-02.htm";
         } else if (event.equals("30625_2")) {
            htmltext = "30625-03.htm";
            st.playSound("Itemsound.quest_middle");
            st.set("cond", "2");
            st.takeItems(3277, -1L);
            st.giveItems(3279, 1L);
         } else if (event.equals("30093_1")) {
            htmltext = "30093-02.htm";
            st.playSound("Itemsound.quest_middle");
            st.set("cond", "6");
            st.takeItems(3280, -1L);
            st.giveItems(3281, 1L);
         } else if (event.equals("30196_1")) {
            htmltext = "30196-02.htm";
         } else if (event.equals("30196_2")) {
            htmltext = "30196-03.htm";
            st.playSound("Itemsound.quest_middle");
            st.set("cond", "10");
            st.takeItems(3283, -1L);
            st.giveItems(3284, 1L);
         } else if (event.equals("30196_3")) {
            htmltext = "30196-06.htm";
            st.playSound("Itemsound.quest_middle");
            st.set("cond", "12");
            st.takeItems(3284, -1L);
            st.takeItems(3291, -1L);
            st.giveItems(3285, 1L);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_223_TestOfChampion");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (npcId == 30624) {
                  int class_id = player.getClassId().getId();
                  if (class_id != 1 && class_id != 45) {
                     st.exitQuest(true);
                     return "30624-01.htm";
                  }

                  if (st.getPlayer().getLevel() < 39) {
                     st.exitQuest(true);
                     return "30624-02.htm";
                  }

                  return class_id == 1 ? "30624-03.htm" : "30624-04.htm";
               }
               break;
            case 1:
               if (npcId == 30624) {
                  if (cond == 1) {
                     htmltext = "30624-07.htm";
                  } else if (cond == 2 || cond == 3) {
                     htmltext = "30624-08.htm";
                  } else if (cond == 4) {
                     htmltext = "30624-09.htm";
                  } else if (cond == 5) {
                     htmltext = "30624-11.htm";
                  } else if (cond == 6 || cond == 7) {
                     htmltext = "30624-12.htm";
                  } else if (cond == 8) {
                     htmltext = "30624-13.htm";
                  } else if (cond == 9) {
                     htmltext = "30624-15.htm";
                  } else if (cond > 9 && cond < 14) {
                     htmltext = "30624-16.htm";
                  } else if (cond == 14) {
                     htmltext = "30624-17.htm";
                     st.takeItems(3286, -1L);
                     st.giveItems(3276, 1L);
                     st.addExpAndSp(1270742, 87200);
                     if (player.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
                        st.giveItems(7562, 72L);
                        st.giveItems(8870, 15L);
                        player.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
                     }

                     st.giveItems(57, 229764L);
                     st.set("cond", "0");
                     st.playSound("ItemSound.quest_finish");
                     st.exitQuest(false);
                  }
               } else if (npcId == 30625) {
                  if (cond == 1) {
                     htmltext = "30625-01.htm";
                  } else if (cond == 2) {
                     htmltext = "30625-04.htm";
                  } else if (cond == 3) {
                     htmltext = "30625-05.htm";
                     st.takeItems(3290, -1L);
                     st.takeItems(3279, -1L);
                     st.giveItems(3278, 1L);
                     st.playSound("Itemsound.quest_middle");
                     st.set("cond", "4");
                  } else if (cond == 4) {
                     htmltext = "30625-06.htm";
                  } else {
                     htmltext = "30625-07.htm";
                  }
               } else if (npcId == 30093) {
                  if (cond == 5) {
                     htmltext = "30093-01.htm";
                  } else if (cond == 6) {
                     htmltext = "30093-03.htm";
                  } else if (cond == 7) {
                     htmltext = "30093-04.htm";
                     st.takeItems(3281, -1L);
                     st.takeItems(3287, -1L);
                     st.takeItems(3288, -1L);
                     st.takeItems(3289, -1L);
                     st.giveItems(3282, 1L);
                     st.playSound("Itemsound.quest_middle");
                     st.set("cond", "8");
                  } else if (cond == 8) {
                     htmltext = "30093-05.htm";
                  } else if (cond > 8) {
                     htmltext = "30093-06.htm";
                  }
               } else if (npcId == 30196) {
                  if (cond == 9) {
                     htmltext = "30196-01.htm";
                  } else if (cond == 10) {
                     htmltext = "30196-04.htm";
                  } else if (cond == 11) {
                     htmltext = "30196-05.htm";
                  } else if (cond == 12) {
                     htmltext = "30196-07.htm";
                  } else if (cond == 13) {
                     htmltext = "30196-08.htm";
                     st.takeItems(3285, -1L);
                     st.takeItems(3292, -1L);
                     st.giveItems(3286, 1L);
                     st.playSound("Itemsound.quest_middle");
                     st.set("cond", "14");
                  } else if (cond == 14) {
                     htmltext = "30196-09.htm";
                  }
               }
               break;
            case 2:
               if (npcId == 30624) {
                  htmltext = getAlreadyCompletedMsg(player);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_223_TestOfChampion");
      if (st == null) {
         return null;
      } else {
         int cond = st.getInt("cond");
         int npcId = npc.getId();

         for(int[] drop : DROPLIST) {
            if (drop[2] == npcId && drop[0] == cond) {
               st.rollAndGive(drop[3], 1, 1, drop[5], (double)drop[4]);

               for(int[] drop2 : DROPLIST) {
                  if (drop2[0] == cond && st.getQuestItemsCount(drop2[3]) < (long)drop2[5]) {
                     return null;
                  }
               }

               st.setCond(cond + 1);
               return null;
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _223_TestOfChampion(223, "_223_TestOfChampion", "");
   }
}
