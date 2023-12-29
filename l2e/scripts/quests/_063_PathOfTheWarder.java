package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _063_PathOfTheWarder extends Quest {
   private static final String qn = "_063_PathOfTheWarder";
   private static final int SIONE = 32195;
   private static final int GOBIE = 32198;
   private static final int BATHIS = 30332;
   private static final int TOBIAS = 30297;
   private static final int OL_MAHUM_NOVICE = 20782;
   private static final int OL_MAHUM_PATROL = 20053;
   private static final int MAILLE_LIZARDMAN = 20919;
   private static final int OL_MAHUM_OFFICER_TAK = 27337;
   private static final int ORDERS = 9762;
   private static final int ORGANIZATION_CHART = 9763;
   private static final int GOBIES_ORDERS = 9764;
   private static final int LETTER_TO_HUMANS = 9765;
   private static final int REPLAY_HUMANS = 9766;
   private static final int LETTER_TO_DARKELVES = 9767;
   private static final int REPLAY_DARKELVES = 9768;
   private static final int REPORT_TO_SIONE = 9769;
   private static final int EMPTY_SOUL_CRYSTAL = 9770;
   private static final int TAKS_CAPTURED_SOUL = 9771;
   private static final int STEELRAZOR_EVALUTION = 9772;
   private static final int[] QUESTITEMS = new int[]{9762, 9763, 9764, 9765, 9766, 9767, 9768, 9769, 9770, 9771};

   public _063_PathOfTheWarder(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32195);
      this.addTalkId(32195);
      this.addTalkId(32198);
      this.addTalkId(30332);
      this.addTalkId(30297);
      this.addKillId(20782);
      this.addKillId(20053);
      this.addKillId(20919);
      this.addKillId(27337);
      this.questItemIds = QUESTITEMS;
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_063_PathOfTheWarder");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32195-02.htm")) {
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.setState((byte)1);
         } else if (event.equalsIgnoreCase("32195-04.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32198-02.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(9764, -1L);
            st.giveItems(9765, 1L);
         } else if (event.equalsIgnoreCase("30332-01.htm")) {
            st.giveItems(9766, 1L);
            st.takeItems(9765, -1L);
         } else if (event.equalsIgnoreCase("30332-03.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32198-06.htm")) {
            st.giveItems(9767, 1L);
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30297-04.htm")) {
            st.giveItems(9768, 1L);
            st.set("cond", "8");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32198-09.htm")) {
            st.giveItems(9769, 1L);
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32198-13.htm")) {
            st.giveItems(9770, 1L);
            st.set("cond", "11");
            st.playSound("ItemSound.quest_middle");
         }

         return event;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_063_PathOfTheWarder");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (st.getState() == 2) {
            if (npcId == 32198) {
               htmltext = "32198-16.htm";
            } else {
               htmltext = getAlreadyCompletedMsg(player);
            }
         } else if (npcId == 32195) {
            if (player.getClassId().getId() != 124 || player.getLevel() < 18) {
               htmltext = "32195-00.htm";
               st.exitQuest(true);
            } else if (st.getState() == 0) {
               htmltext = "32195-01.htm";
            } else if (cond == 1) {
               htmltext = "32195-03.htm";
            } else if (cond == 2) {
               htmltext = "32195-05.htm";
            } else if (cond == 3) {
               htmltext = "32195-06.htm";
               st.set("cond", "4");
               st.playSound("ItemSound.quest_middle");
               st.giveItems(9764, 1L);
               st.takeItems(9762, -1L);
               st.takeItems(9763, -1L);
            } else if (cond >= 4 && cond < 9) {
               htmltext = "32195-07.htm";
            } else if (cond == 9) {
               htmltext = "32195-08.htm";
               st.set("cond", "10");
               st.playSound("ItemSound.quest_middle");
               st.takeItems(9769, -1L);
            } else if (cond == 10) {
               htmltext = "32195-09.htm";
            }
         } else if (npcId == 32198) {
            if (cond == 4) {
               htmltext = "32198-01.htm";
            } else if (cond == 5) {
               htmltext = "32198-03.htm";
            } else if (cond == 6) {
               if (st.getQuestItemsCount(9766) == 1L) {
                  st.takeItems(9766, -1L);
                  htmltext = "32198-04.htm";
               } else {
                  htmltext = "32198-05.htm";
               }
            } else if (cond == 7) {
               htmltext = "32198-07.htm";
            } else if (cond == 8) {
               if (st.getQuestItemsCount(9767) == 1L) {
                  htmltext = "32198-08.htm";
                  st.takeItems(9768, -1L);
               } else {
                  htmltext = "32198-09.htm";
                  st.giveItems(9769, 1L);
                  st.set("cond", "9");
                  st.playSound("ItemSound.quest_middle");
               }
            } else if (cond == 9) {
               htmltext = "32198-10.htm";
            } else if (cond == 10) {
               htmltext = "32198-11.htm";
            } else if (cond == 11) {
               htmltext = "32198-14.htm";
            } else if (cond == 12) {
               htmltext = "32198-15.htm";
               st.takeItems(9771, -1L);
               st.giveItems(9772, 1L);
               String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
               if (isFinished.equalsIgnoreCase("")) {
                  st.addExpAndSp(160267, 2967);
               }

               st.giveItems(57, 163800L);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(false);
               st.saveGlobalQuestVar("1ClassQuestFinished", "1");
               st.unset("cond");
            }
         } else if (npcId == 30332) {
            if (cond == 5) {
               if (st.getQuestItemsCount(9766) == 1L) {
                  htmltext = "30332-02.htm";
               } else {
                  htmltext = "30332-00.htm";
               }
            } else if (cond > 5) {
               htmltext = "30332-04.htm";
            }
         } else if (npcId == 30297) {
            if (cond == 7) {
               if (st.getQuestItemsCount(9767) == 1L) {
                  htmltext = "30297-01.htm";
                  st.takeItems(9767, -1L);
               } else {
                  htmltext = "30297-04.htm";
                  st.giveItems(9768, 1L);
                  st.set("cond", "8");
                  st.playSound("ItemSound.quest_middle");
               }
            } else if (cond == 8) {
               htmltext = "30297-05.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (npcId == 20782) {
            if (st.getQuestItemsCount(9762) < 10L && cond == 2) {
               st.giveItems(9762, 1L);
               if (st.getQuestItemsCount(9762) == 10L) {
                  if (st.getQuestItemsCount(9763) == 5L) {
                     st.playSound("ItemSound.quest_middle");
                     st.set("cond", "3");
                  }
               } else {
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20053) {
            if (st.getQuestItemsCount(9763) < 5L && cond == 2) {
               st.giveItems(9763, 1L);
               if (st.getQuestItemsCount(9763) == 5L) {
                  if (st.getQuestItemsCount(9762) == 10L) {
                     st.playSound("ItemSound.quest_middle");
                     st.set("cond", "3");
                  }
               } else {
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20919) {
            if (st.getQuestItemsCount(9771) == 0L && st.getRandom(10) < 2 && cond == 11) {
               npc = st.addSpawn(27337, 180000);
            }
         } else if (npcId == 27337 && st.getQuestItemsCount(9771) == 0L && cond == 11) {
            st.playSound("ItemSound.quest_middle");
            st.takeItems(9770, -1L);
            st.giveItems(9771, 1L);
            st.set("cond", "12");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _063_PathOfTheWarder(63, "_063_PathOfTheWarder", "");
   }
}
