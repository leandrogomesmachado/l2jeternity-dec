package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.SocialAction;

public class _410_PathToPalusKnight extends Quest {
   private static final String qn = "_410_PathToPalusKnight";
   private static final int VIRGIL = 30329;
   private static final int KALINTA = 30422;
   private static final int[] TALKERS = new int[]{30329, 30422};
   private static final int VENOMOUS_SPIDER = 20038;
   private static final int ARACHNID_TRACKER = 20043;
   private static final int LYCANTHROPE = 20049;
   private static final int[] KILLS = new int[]{20038, 20043, 20049};
   private static final int PALLUS_TALISMAN = 1237;
   private static final int LYCANTHROPE_SKULL = 1238;
   private static final int VIRGILS_LETTER = 1239;
   private static final int MORTE_TALISMAN = 1240;
   private static final int PREDATOR_CARAPACE = 1241;
   private static final int TRIMDEN_SILK = 1242;
   private static final int COFFIN_ETERNAL_REST = 1243;
   private static final int[] QUESTITEMS = new int[]{1237, 1238, 1239, 1240, 1241, 1242, 1243};
   private static final int GAZE_OF_ABYSS = 1244;

   public _410_PathToPalusKnight(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30329);

      for(int talkId : TALKERS) {
         this.addTalkId(talkId);
      }

      for(int killId : KILLS) {
         this.addKillId(killId);
      }

      this.questItemIds = QUESTITEMS;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return super.onAdvEvent(event, npc, player);
      } else {
         int level = player.getLevel();
         int classId = player.getClassId().getId();
         if (event.equalsIgnoreCase("1")) {
            st.set("id", "0");
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1237, 1L);
            htmltext = "30329-06.htm";
         } else if (event.equalsIgnoreCase("410_1")) {
            if (level >= 18 && classId == 31 && st.getQuestItemsCount(1244) == 0L) {
               htmltext = "30329-05.htm";
            } else if (classId != 31) {
               htmltext = classId == 32 ? "30329-02a.htm" : "30329-03.htm";
            } else if (level < 18 && classId == 31) {
               htmltext = "30329-02.htm";
            } else if (level >= 18 && classId == 31 && st.getQuestItemsCount(1244) == 1L) {
               htmltext = "30329-04.htm";
            }
         } else if (event.equalsIgnoreCase("30329_2")) {
            st.takeItems(1237, 1L);
            st.takeItems(1238, st.getQuestItemsCount(1238));
            st.giveItems(1239, 1L);
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            htmltext = "30329-10.htm";
         } else if (event.equalsIgnoreCase("30422_1")) {
            st.takeItems(1239, 1L);
            st.giveItems(1240, 1L);
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            htmltext = "30422-02.htm";
         } else if (event.equalsIgnoreCase("30422_2")) {
            st.takeItems(1240, 1L);
            st.takeItems(1242, st.getQuestItemsCount(1242));
            st.takeItems(1241, st.getQuestItemsCount(1241));
            st.giveItems(1243, 1L);
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            htmltext = "30422-06.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      String htmltext = Quest.getNoQuestMsg(talker);
      QuestState st = talker.getQuestState("_410_PathToPalusKnight");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         if (npcId != 30329 && id != 1) {
            return htmltext;
         } else {
            if (npcId == 30329 && st.getInt("cond") == 0) {
               htmltext = "30329-01.htm";
            } else if (npcId == 30329 && st.getInt("cond") > 0) {
               if (st.getQuestItemsCount(1237) == 1L && st.getQuestItemsCount(1238) == 0L) {
                  htmltext = "30329-07.htm";
               } else if (st.getQuestItemsCount(1237) == 1L && st.getQuestItemsCount(1238) > 0L && st.getQuestItemsCount(1238) < 13L) {
                  htmltext = "30329-08.htm";
               } else if (st.getQuestItemsCount(1237) == 1L && st.getQuestItemsCount(1238) >= 13L) {
                  htmltext = "30329-09.htm";
               } else if (st.getQuestItemsCount(1243) == 1L) {
                  st.takeItems(1243, 1L);
                  String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
                  if (isFinished.equalsIgnoreCase("")) {
                     st.addExpAndSp(295862, 5050);
                  }

                  st.giveItems(1244, 1L);
                  st.giveItems(57, 163800L);
                  st.saveGlobalQuestVar("1ClassQuestFinished", "1");
                  st.set("cond", "0");
                  talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
                  st.exitQuest(false);
                  st.playSound("ItemSound.quest_finish");
                  htmltext = "30329-11.htm";
               } else if (st.getQuestItemsCount(1240) > 0L || st.getQuestItemsCount(1239) > 0L) {
                  htmltext = "30329-12.htm";
               }
            } else if (npcId == 30422 && st.getInt("cond") > 0) {
               if (st.getQuestItemsCount(1239) > 0L) {
                  htmltext = "30422-01.htm";
               } else if (st.getQuestItemsCount(1240) > 0L && st.getQuestItemsCount(1242) == 0L && st.getQuestItemsCount(1241) == 0L) {
                  htmltext = "30422-03.htm";
               } else if (st.getQuestItemsCount(1240) > 0L && st.getQuestItemsCount(1242) > 0L && st.getQuestItemsCount(1241) == 0L) {
                  htmltext = "30422-04.htm";
               } else if (st.getQuestItemsCount(1240) > 0L && st.getQuestItemsCount(1242) == 0L && st.getQuestItemsCount(1241) > 0L) {
                  htmltext = "30422-04.htm";
               } else if (st.getQuestItemsCount(1240) > 0L && st.getQuestItemsCount(1242) >= 5L && st.getQuestItemsCount(1241) > 0L) {
                  htmltext = "30422-05.htm";
               }
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_410_PathToPalusKnight");
      if (st == null) {
         return super.onKill(npc, killer, isSummon);
      } else if (st.getState() != 1) {
         return super.onKill(npc, killer, isSummon);
      } else {
         int npcId = npc.getId();
         if (npcId == 20049) {
            st.set("id", "0");
            if (st.getInt("cond") > 0 && st.getQuestItemsCount(1237) == 1L && st.getQuestItemsCount(1238) < 13L) {
               st.giveItems(1238, 1L);
               if (st.getQuestItemsCount(1238) == 13L) {
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "2");
               } else {
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20038) {
            st.set("id", "0");
            if (st.getInt("cond") > 0 && st.getQuestItemsCount(1240) == 1L && st.getQuestItemsCount(1241) < 1L) {
               st.giveItems(1241, 1L);
               st.playSound("ItemSound.quest_middle");
               if (st.getQuestItemsCount(1242) >= 5L && st.getQuestItemsCount(1241) > 0L) {
                  st.set("cond", "5");
               }
            }
         } else if (npcId == 20043) {
            st.set("id", "0");
            if (st.getInt("cond") > 0 && st.getQuestItemsCount(1240) == 1L && st.getQuestItemsCount(1242) < 5L) {
               st.giveItems(1242, 1L);
               if (st.getQuestItemsCount(1242) == 5L) {
                  st.playSound("ItemSound.quest_middle");
                  if (st.getQuestItemsCount(1242) >= 5L && st.getQuestItemsCount(1241) > 0L) {
                     st.set("cond", "5");
                  }
               } else {
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _410_PathToPalusKnight(410, "_410_PathToPalusKnight", "");
   }
}
