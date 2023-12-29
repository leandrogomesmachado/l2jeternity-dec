package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _418_PathToArtisan extends Quest {
   private static final String qn = "_418_PathToArtisan";
   private static final int SILVERA = 30527;
   private static final int PINTER = 30298;
   private static final int KLUTO = 30317;
   private static final int VUKU_FIGHTER = 20017;
   private static final int BOOGLE_RATMAN = 20389;
   private static final int BOOGLE_RATMAN_LEADER = 20390;
   private static final int SILVERYS_RING = 1632;
   private static final int PASS_1ST = 1633;
   private static final int PASS_2ND = 1634;
   private static final int PASS_FINAL = 1635;
   private static final int RATMAN_TOOTH = 1636;
   private static final int BIG_RATMAN_TOOTH = 1637;
   private static final int KLUTOS_LETTER = 1638;
   private static final int FOOTPRINT = 1639;
   private static final int SECRET_BOX1 = 1640;
   private static final int SECRET_BOX2 = 1641;
   private static final int TOTEM_SPIRIT_CLAW = 1622;
   private static final int TATARUS_LETTER = 1623;
   private static final int[] QUESTITEMS = new int[]{1632, 1633, 1634, 1636, 1637, 1638, 1639, 1640, 1641, 1622, 1623};

   public _418_PathToArtisan(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30527);
      this.addTalkId(30527);
      this.addTalkId(30298);
      this.addTalkId(30317);
      this.addKillId(20017);
      this.addKillId(20389);
      this.addKillId(20390);
      this.questItemIds = QUESTITEMS;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return super.onAdvEvent(event, npc, player);
      } else {
         if (event.equalsIgnoreCase("30527_1")) {
            if (player.getClassId().getId() != 53) {
               htmltext = player.getClassId().getId() == 56 ? "30527-02a.htm" : "30527-02.htm";
            } else if (player.getLevel() < 18) {
               htmltext = "30527-03.htm";
            } else {
               htmltext = st.getQuestItemsCount(1635) != 0L ? "30527-04.htm" : "30527-05.htm";
            }
         } else if (event.equalsIgnoreCase("30527_2")) {
            st.takeItems(1622, 1L);
            st.giveItems(1623, 1L);
            htmltext = "30527-11.htm";
         } else if (event.equalsIgnoreCase("1")) {
            st.set("id", "0");
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1632, 1L);
            htmltext = "30527-06.htm";
         } else if (event.equalsIgnoreCase("30317_1")) {
            htmltext = "30317-02.htm";
         } else if (event.equalsIgnoreCase("30317_2")) {
            htmltext = "30317-05.htm";
         } else if (event.equalsIgnoreCase("30317_3")) {
            htmltext = "30317-03.htm";
         } else if (event.equalsIgnoreCase("30317_4")) {
            st.giveItems(1638, 1L);
            st.set("cond", "4");
            htmltext = "30317-04.htm";
         } else if (event.equalsIgnoreCase("30317_5")) {
            htmltext = "30317-06.htm";
         } else if (event.equalsIgnoreCase("30317_6")) {
            st.giveItems(1638, 1L);
            st.set("cond", "4");
            htmltext = "30317-07.htm";
         } else if (event.equalsIgnoreCase("30317_7")) {
            if (st.getQuestItemsCount(1633) > 0L && st.getQuestItemsCount(1634) > 0L && st.getQuestItemsCount(1641) > 0L) {
               st.takeItems(1633, 1L);
               st.takeItems(1634, 1L);
               st.takeItems(1641, 1L);
               String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
               if (isFinished.equalsIgnoreCase("")) {
                  st.addExpAndSp(160267, 3670);
               }

               st.giveItems(1635, 1L);
               st.giveItems(57, 163800L);
               st.saveGlobalQuestVar("1ClassQuestFinished", "1");
               st.set("cond", "0");
               st.exitQuest(false);
               st.playSound("ItemSound.quest_finish");
               htmltext = "30317-10.htm";
            } else {
               htmltext = "30317-08.htm";
            }
         } else if (event.equalsIgnoreCase("30317_8")) {
            htmltext = "30317-11.htm";
         } else if (event.equalsIgnoreCase("30317_9")) {
            if (st.getQuestItemsCount(1633) > 0L && st.getQuestItemsCount(1634) > 0L && st.getQuestItemsCount(1641) > 0L) {
               st.set("cond", "0");
               st.takeItems(1633, 1L);
               st.takeItems(1634, 1L);
               st.takeItems(1641, 1L);
               st.addExpAndSp(228064, 3670);
               st.giveItems(1635, 1L);
               st.exitQuest(false);
               st.playSound("ItemSound.quest_finish");
               htmltext = "30317-12.htm";
            } else {
               htmltext = "30317-08.htm";
            }
         } else if (event.equalsIgnoreCase("30298_1")) {
            htmltext = "30298-02.htm";
         } else if (event.equalsIgnoreCase("30298_2")) {
            st.takeItems(1638, 1L);
            st.giveItems(1639, 1L);
            st.set("cond", "5");
            htmltext = "30298-03.htm";
         } else if (event.equalsIgnoreCase("30298_3")) {
            st.takeItems(1640, 1L);
            st.takeItems(1639, 1L);
            st.giveItems(1641, 1L);
            st.giveItems(1634, 1L);
            st.set("cond", "7");
            htmltext = "30298-06.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      String htmltext = Quest.getNoQuestMsg(talker);
      QuestState st = talker.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         int cond = st.getInt("cond");
         if (npcId != 30527 && id != 1) {
            return htmltext;
         } else {
            if (npcId == 30527 && cond == 0) {
               htmltext = "30527-01.htm";
            } else if (npcId == 30527 && cond > 0 && st.getQuestItemsCount(1632) == 1L && st.getQuestItemsCount(1636) + st.getQuestItemsCount(1637) < 12L) {
               htmltext = "30527-07.htm";
            } else if (npcId == 30527
               && cond > 0
               && st.getQuestItemsCount(1632) == 1L
               && st.getQuestItemsCount(1636) >= 10L
               && st.getQuestItemsCount(1637) >= 2L) {
               st.takeItems(1632, st.getQuestItemsCount(1632));
               st.takeItems(1636, st.getQuestItemsCount(1636));
               st.takeItems(1637, st.getQuestItemsCount(1637));
               st.giveItems(1633, 1L);
               st.set("cond", "3");
               htmltext = "30527-08.htm";
            } else if (npcId == 30527 && cond > 0 && st.getQuestItemsCount(1633) == 1L) {
               htmltext = "30527-09.htm";
            } else if (npcId == 30317
               && cond > 0
               && st.getQuestItemsCount(1638) == 0L
               && st.getQuestItemsCount(1639) == 0L
               && st.getQuestItemsCount(1633) > 0L
               && st.getQuestItemsCount(1634) == 0L
               && st.getQuestItemsCount(1641) == 0L) {
               htmltext = "30317-01.htm";
            } else if (npcId != 30317
               || cond <= 0
               || st.getQuestItemsCount(1633) <= 0L
               || st.getQuestItemsCount(1638) <= 0L && st.getQuestItemsCount(1639) <= 0L) {
               if (npcId == 30317 && cond > 0 && st.getQuestItemsCount(1633) > 0L && st.getQuestItemsCount(1634) > 0L && st.getQuestItemsCount(1641) > 0L) {
                  htmltext = "30317-09.htm";
               } else if (npcId == 30298 && cond > 0 && st.getQuestItemsCount(1633) > 0L && st.getQuestItemsCount(1638) > 0L) {
                  htmltext = "30298-01.htm";
               } else if (npcId == 30298
                  && cond > 0
                  && st.getQuestItemsCount(1633) > 0L
                  && st.getQuestItemsCount(1639) > 0L
                  && st.getQuestItemsCount(1640) == 0L) {
                  htmltext = "30298-04.htm";
               } else if (npcId == 30298
                  && cond > 0
                  && st.getQuestItemsCount(1633) > 0L
                  && st.getQuestItemsCount(1639) > 0L
                  && st.getQuestItemsCount(1640) > 0L) {
                  htmltext = "30298-05.htm";
               } else if (npcId == 30298
                  && cond > 0
                  && st.getQuestItemsCount(1633) > 0L
                  && st.getQuestItemsCount(1634) > 0L
                  && st.getQuestItemsCount(1641) > 0L) {
                  htmltext = "30298-07.htm";
               }
            } else {
               htmltext = "30317-08.htm";
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState(this.getName());
      if (st == null) {
         return super.onKill(npc, killer, isSummon);
      } else if (st.getState() != 1) {
         return super.onKill(npc, killer, isSummon);
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (npcId == 20389) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1632) == 1L && st.getQuestItemsCount(1636) < 10L && st.getRandom(10) < 7) {
               if (st.getQuestItemsCount(1636) == 9L && st.getQuestItemsCount(1637) == 2L) {
                  st.giveItems(1636, 1L);
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "2");
               } else {
                  st.giveItems(1636, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20390) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1632) == 1L && st.getQuestItemsCount(1637) < 2L && st.getRandom(10) < 5) {
               if (st.getQuestItemsCount(1637) == 1L && st.getQuestItemsCount(1636) == 10L) {
                  st.giveItems(1637, 1L);
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "2");
               } else {
                  st.giveItems(1637, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20017) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1639) == 1L && st.getQuestItemsCount(1640) < 1L && st.getRandom(10) < 2) {
               st.giveItems(1640, 1L);
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "6");
            }
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _418_PathToArtisan(418, "_418_PathToArtisan", "");
   }
}
