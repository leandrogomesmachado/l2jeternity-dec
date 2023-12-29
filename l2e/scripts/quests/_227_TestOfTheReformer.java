package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _227_TestOfTheReformer extends Quest {
   private static final String qn = "_227_TestOfTheReformer";
   private static final int Pupina = 30118;
   private static final int Sla = 30666;
   private static final int Katari = 30668;
   private static final int OlMahumPilgrimNPC = 30732;
   private static final int Kakan = 30669;
   private static final int Nyakuri = 30670;
   private static final int Ramus = 30667;
   private static final int BookOfReform = 2822;
   private static final int LetterOfIntroduction = 2823;
   private static final int SlasLetter = 2824;
   private static final int Greetings = 2825;
   private static final int OlMahumMoney = 2826;
   private static final int KatarisLetter = 2827;
   private static final int NyakurisLetter = 2828;
   private static final int KakansLetter = 3037;
   private static final int UndeadList = 2829;
   private static final int RamussLetter = 2830;
   private static final int RippedDiary = 2831;
   private static final int HugeNail = 2832;
   private static final int LetterOfBetrayer = 2833;
   private static final int BoneFragment1 = 2834;
   private static final int BoneFragment2 = 2835;
   private static final int BoneFragment3 = 2836;
   private static final int BoneFragment4 = 2837;
   private static final int BoneFragment5 = 2838;
   private static final int MarkOfReformer = 2821;
   private static final int NamelessRevenant = 27099;
   private static final int Aruraune = 27128;
   private static final int OlMahumInspector = 27129;
   private static final int OlMahumBetrayer = 27130;
   private static final int CrimsonWerewolf = 27131;
   private static final int KrudelLizardman = 27132;
   private static final int SilentHorror = 20404;
   private static final int SkeletonLord = 20104;
   private static final int SkeletonMarksman = 20102;
   private static final int MiserySkeleton = 20022;
   private static final int SkeletonArcher = 20100;
   public final int[][] DROPLIST_COND = new int[][]{
      {18, 0, 20404, 0, 2834, 1, 70, 1},
      {18, 0, 20104, 0, 2835, 1, 70, 1},
      {18, 0, 20102, 0, 2836, 1, 70, 1},
      {18, 0, 20022, 0, 2837, 1, 70, 1},
      {18, 0, 20100, 0, 2838, 1, 70, 1}
   };

   public _227_TestOfTheReformer(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30118);
      this.addTalkId(30118);
      this.addTalkId(30666);
      this.addTalkId(30668);
      this.addTalkId(30732);
      this.addTalkId(30669);
      this.addTalkId(30670);
      this.addTalkId(30667);
      this.addKillId(27099);
      this.addKillId(27128);
      this.addKillId(27129);
      this.addKillId(27130);
      this.addKillId(27131);
      this.addKillId(27132);

      for(int[] element : this.DROPLIST_COND) {
         this.addKillId(element[2]);
         this.registerQuestItems(new int[]{element[4]});
      }

      this.questItemIds = new int[]{2822, 2832, 2823, 2824, 2827, 2833, 2826, 2828, 2829, 2825, 3037, 2830, 2831};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_227_TestOfTheReformer");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            htmltext = "30118-04.htm";
            st.giveItems(2822, 1L);
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30118_1")) {
            htmltext = "30118-06.htm";
            st.takeItems(2832, -1L);
            st.takeItems(2822, -1L);
            st.giveItems(2823, 1L);
            st.set("cond", "4");
         } else if (event.equalsIgnoreCase("30666_2")) {
            htmltext = "30666-02.htm";
         } else if (event.equalsIgnoreCase("30666_3")) {
            htmltext = "30666-04.htm";
            st.takeItems(2823, -1L);
            st.giveItems(2824, 1L);
            st.set("cond", "5");
         } else if (event.equalsIgnoreCase("30666_4")) {
            htmltext = "30666-02.htm";
         } else if (event.equalsIgnoreCase("30669_1")) {
            htmltext = "30669-02.htm";
         } else if (event.equalsIgnoreCase("30669_2")) {
            htmltext = "30669-03.htm";
            st.addSpawn(27131, -9382, -89852, -2333);
            st.set("cond", "12");
         } else if (event.equalsIgnoreCase("30669_3")) {
            htmltext = "30669-05.htm";
         } else if (event.equalsIgnoreCase("30670_1")) {
            htmltext = "30670-03.htm";
            st.addSpawn(27132, 126019, -179983, -1781);
            st.set("cond", "15");
         } else if (event.equalsIgnoreCase("30670_2")) {
            htmltext = "30670-02.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_227_TestOfTheReformer");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (npcId == 30118) {
                  if (player.getClassId().getId() != 15 && player.getClassId().getId() != 42) {
                     htmltext = "30118-02.htm";
                     st.exitQuest(true);
                  } else if (player.getLevel() >= 39) {
                     htmltext = "30118-03.htm";
                  } else {
                     htmltext = "30118-01.htm";
                     st.exitQuest(true);
                  }
               }
               break;
            case 1:
               if (npcId == 30118) {
                  if (cond == 3) {
                     htmltext = "30118-05.htm";
                  } else if (cond >= 4) {
                     htmltext = "30118-07.htm";
                  }
               } else if (npcId == 30666) {
                  if (cond == 4) {
                     htmltext = "30666-01.htm";
                  } else if (cond == 5) {
                     htmltext = "30666-05.htm";
                  } else if (cond == 10) {
                     st.takeItems(2826, -1L);
                     st.giveItems(2825, 3L);
                     htmltext = "30666-06.htm";
                     st.set("cond", "11");
                  } else if (cond == 20) {
                     st.takeItems(2827, -1L);
                     st.takeItems(3037, -1L);
                     st.takeItems(2828, -1L);
                     st.takeItems(2830, -1L);
                     st.giveItems(2821, 1L);
                     st.addExpAndSp(1252844, 85972);
                     st.giveItems(57, 226528L);
                     if (player.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
                        st.giveItems(7562, 60L);
                        player.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
                     }

                     htmltext = "30666-07.htm";
                     st.set("cond", "0");
                     st.exitQuest(false);
                     st.playSound("ItemSound.quest_finish");
                  }
               } else if (npcId == 30668) {
                  if (cond == 5 || cond == 6) {
                     st.takeItems(2824, -1L);
                     htmltext = "30668-01.htm";
                     st.set("cond", "6");
                     st.addSpawn(30732, -4015, 40141, -3664);
                     st.addSpawn(27129, -4034, 40201, -3665);
                  } else if (cond == 8) {
                     htmltext = "30668-02.htm";
                     st.addSpawn(27130, -4106, 40174, -3660);
                  } else if (cond == 9) {
                     st.takeItems(2833, -1L);
                     st.giveItems(2827, 1L);
                     htmltext = "30668-03.htm";
                     st.set("cond", "10");
                  }
               } else if (npcId == 30732) {
                  if (cond == 7) {
                     st.giveItems(2826, 1L);
                     htmltext = "30732-01.htm";
                     st.set("cond", "8");
                  }
               } else if (npcId == 30669) {
                  if (cond == 11 || cond == 12) {
                     htmltext = "30669-01.htm";
                  } else if (cond == 13) {
                     st.takeItems(2825, 1L);
                     st.giveItems(3037, 1L);
                     htmltext = "30669-04.htm";
                     st.set("cond", "14");
                  }
               } else if (npcId == 30670) {
                  if (cond == 14 || cond == 15) {
                     htmltext = "30670-01.htm";
                  } else if (cond == 16) {
                     st.takeItems(2825, 1L);
                     st.giveItems(2828, 1L);
                     htmltext = "30670-04.htm";
                     st.set("cond", "17");
                  }
               } else if (npcId == 30667) {
                  if (cond == 17) {
                     st.takeItems(2825, -1L);
                     st.giveItems(2829, 1L);
                     htmltext = "30667-01.htm";
                     st.set("cond", "18");
                  } else if (cond == 19) {
                     st.takeItems(2834, -1L);
                     st.takeItems(2835, -1L);
                     st.takeItems(2836, -1L);
                     st.takeItems(2837, -1L);
                     st.takeItems(2838, -1L);
                     st.takeItems(2829, -1L);
                     st.giveItems(2830, 1L);
                     htmltext = "30667-03.htm";
                     st.set("cond", "20");
                  }
               }
               break;
            case 2:
               if (npcId == 30118) {
                  htmltext = getAlreadyCompletedMsg(player);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_227_TestOfTheReformer");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");

         for(int[] element : this.DROPLIST_COND) {
            if (cond == element[0] && npcId == element[2] && (element[3] == 0 || st.getQuestItemsCount(element[3]) > 0L)) {
               if (element[5] == 0) {
                  st.rollAndGive(element[4], element[7], (double)element[6]);
               } else if (st.rollAndGive(element[4], element[7], element[7], element[5], (double)element[6]) && element[1] != cond && element[1] != 0) {
                  st.setCond(Integer.valueOf(element[1]));
                  st.playSound("ItemSound.quest_middle");
               }
            }
         }

         if (cond == 18
            && st.getQuestItemsCount(2834) != 0L
            && st.getQuestItemsCount(2835) != 0L
            && st.getQuestItemsCount(2836) != 0L
            && st.getQuestItemsCount(2837) != 0L
            && st.getQuestItemsCount(2838) != 0L) {
            st.setCond(19);
            st.playSound("ItemSound.quest_middle");
         } else if (npcId == 27099 && (cond == 1 || cond == 2)) {
            if (st.getQuestItemsCount(2831) < 6L) {
               st.giveItems(2831, 1L);
               st.playSound("ItemSound.quest_itemget");
            } else {
               st.takeItems(2831, -1L);
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
               st.addSpawn(27128);
            }
         } else if (npcId == 27128) {
            if (cond == 2) {
               if (st.getQuestItemsCount(2832) == 0L) {
                  st.giveItems(2832, 1L);
               }

               st.playSound("ItemSound.quest_middle");
               st.set("cond", "3");
            }
         } else if (npcId == 27129) {
            if (cond == 6) {
               st.set("cond", "7");
            }
         } else if (npcId == 27130) {
            if (cond == 8) {
               if (st.getQuestItemsCount(2833) == 0L) {
                  st.giveItems(2833, 1L);
               }

               st.set("cond", "9");
            }
         } else if (npcId == 27131) {
            if (cond == 12) {
               st.set("cond", "13");
            }
         } else if (npcId == 27132 && cond == 15) {
            st.set("cond", "16");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _227_TestOfTheReformer(227, "_227_TestOfTheReformer", "");
   }
}
