package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _412_PathToDarkwizard extends Quest {
   private static final String qn = "_412_PathToDarkwizard";
   private static final int VARIKA = 30421;
   private static final int CHARKEREN = 30415;
   private static final int ANNIKA = 30418;
   private static final int ARKENIA = 30419;
   private static final int[] TALKERS = new int[]{30421, 30415, 30418, 30419};
   private static final int MARSH_ZOMBIE = 20015;
   private static final int MISERY_SKELETON = 20022;
   private static final int SKELETON_SCOUT = 20045;
   private static final int SKELETON_HUNTER = 20517;
   private static final int SKELETON_HUNTER_ARCHER = 20518;
   private static final int[] KILLS = new int[]{20015, 20022, 20045, 20517, 20518};
   private static final int SEEDS_OF_ANGER = 1253;
   private static final int SEEDS_OF_DESPAIR = 1254;
   private static final int SEEDS_OF_HORROR = 1255;
   private static final int SEEDS_OF_LUNACY = 1256;
   private static final int FAMILYS_ASHES = 1257;
   private static final int KNEE_BONE = 1259;
   private static final int HEART_OF_LUNACY = 1260;
   private static final int LUCKY_KEY = 1277;
   private static final int CANDLE = 1278;
   private static final int HUB_SCENT = 1279;
   private static final int[] QUESTITEMS = new int[]{1253, 1254, 1255, 1256, 1257, 1259, 1260, 1277, 1278, 1279};
   private static final int JEWEL_OF_DARKNESS = 1261;

   public _412_PathToDarkwizard(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30421);

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
            if (st.getInt("cond") == 0) {
               if (level >= 18 && classId == 38 && st.getQuestItemsCount(1261) == 0L) {
                  st.set("cond", "1");
                  st.setState((byte)1);
                  st.playSound("ItemSound.quest_accept");
                  st.giveItems(1254, 1L);
                  htmltext = "30421-05.htm";
               } else if (classId != 38) {
                  htmltext = classId == 39 ? "30421-02a.htm" : "30421-03.htm";
               } else if (level < 18 && classId == 38) {
                  htmltext = "30421-02.htm";
               } else if (level >= 18 && classId == 38 && st.getQuestItemsCount(1261) == 1L) {
                  htmltext = "30421-04.htm";
               }
            }
         } else if (event.equalsIgnoreCase("412_1")) {
            htmltext = st.getQuestItemsCount(1253) > 0L ? "30421-06.htm" : "30421-07.htm";
         } else if (event.equalsIgnoreCase("412_2")) {
            htmltext = st.getQuestItemsCount(1255) > 0L ? "30421-09.htm" : "30421-10.htm";
         } else if (event.equalsIgnoreCase("412_3")) {
            if (st.getQuestItemsCount(1256) > 0L) {
               htmltext = "30421-12.htm";
            } else if (st.getQuestItemsCount(1256) == 0L && st.getQuestItemsCount(1254) > 0L) {
               st.giveItems(1279, 1L);
               htmltext = "30421-13.htm";
            }
         } else if (event.equalsIgnoreCase("412_4")) {
            st.giveItems(1277, 1L);
            htmltext = "30415-03.htm";
         } else if (event.equalsIgnoreCase("30418_1")) {
            st.giveItems(1278, 1L);
            htmltext = "30418-02.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      String htmltext = Quest.getNoQuestMsg(talker);
      QuestState st = talker.getQuestState("_412_PathToDarkwizard");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         int cond = st.getInt("cond");
         if (npcId != 30421 && id != 1) {
            return htmltext;
         } else {
            if (npcId == 30421 && cond == 0) {
               htmltext = st.getQuestItemsCount(1261) == 0L ? "30421-01.htm" : "30421-04.htm";
            } else if (npcId == 30421 && cond == 1) {
               if (st.getQuestItemsCount(1254) > 0L
                  && st.getQuestItemsCount(1255) > 0L
                  && st.getQuestItemsCount(1256) > 0L
                  && st.getQuestItemsCount(1253) > 0L) {
                  st.takeItems(1255, 1L);
                  st.takeItems(1253, 1L);
                  st.takeItems(1256, 1L);
                  st.takeItems(1254, 1L);
                  String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
                  if (isFinished.equalsIgnoreCase("")) {
                     st.addExpAndSp(295862, 5210);
                  }

                  st.giveItems(1261, 1L);
                  st.giveItems(57, 163800L);
                  st.saveGlobalQuestVar("1ClassQuestFinished", "1");
                  st.set("cond", "0");
                  st.exitQuest(false);
                  st.playSound("ItemSound.quest_finish");
                  htmltext = "30421-16.htm";
               } else if (st.getQuestItemsCount(1254) == 1L
                  && st.getQuestItemsCount(1257) == 0L
                  && st.getQuestItemsCount(1277) == 0L
                  && st.getQuestItemsCount(1278) == 0L
                  && st.getQuestItemsCount(1279) == 0L
                  && st.getQuestItemsCount(1259) == 0L
                  && st.getQuestItemsCount(1260) == 0L) {
                  htmltext = "30421-17.htm";
               } else if (st.getQuestItemsCount(1254) == 1L && st.getInt("id") == 1 && st.getQuestItemsCount(1253) == 0L) {
                  htmltext = "30421-08.htm";
               } else if (st.getQuestItemsCount(1254) == 1L && st.getInt("id") == 2 && st.getQuestItemsCount(1255) > 0L) {
                  htmltext = "30421-19.htm";
               } else if (st.getQuestItemsCount(1254) == 1L && st.getInt("id") == 3 && st.getQuestItemsCount(1260) == 0L) {
                  htmltext = "30421-13.htm";
               }
            } else if (npcId == 30419 && cond == 1) {
               if (st.getQuestItemsCount(1279) == 0L && st.getQuestItemsCount(1260) == 0L) {
                  st.giveItems(1279, 1L);
                  htmltext = "30419-01.htm";
               } else if (st.getQuestItemsCount(1279) > 0L && st.getQuestItemsCount(1260) < 3L) {
                  htmltext = "30419-02.htm";
               } else if (st.getQuestItemsCount(1279) > 0L && st.getQuestItemsCount(1260) >= 3L) {
                  st.giveItems(1256, 1L);
                  st.takeItems(1260, 3L);
                  st.takeItems(1279, 1L);
                  htmltext = "30419-03.htm";
               }
            } else if (npcId == 30415 && cond == 1 && st.getQuestItemsCount(1253) == 0L) {
               if (st.getQuestItemsCount(1254) == 1L && st.getQuestItemsCount(1257) == 0L && st.getQuestItemsCount(1277) == 0L) {
                  htmltext = "30415-01.htm";
               } else if (st.getQuestItemsCount(1254) == 1L && st.getQuestItemsCount(1257) < 3L && st.getQuestItemsCount(1277) == 1L) {
                  htmltext = "30415-04.htm";
               } else if (st.getQuestItemsCount(1254) == 1L && st.getQuestItemsCount(1257) >= 3L && st.getQuestItemsCount(1277) == 1L) {
                  st.giveItems(1253, 1L);
                  st.takeItems(1257, 3L);
                  st.takeItems(1277, 1L);
                  htmltext = "30415-05.htm";
               }
            } else if (npcId == 30415 && cond == 1 && st.getQuestItemsCount(1253) == 1L) {
               htmltext = "30415-06.htm";
            } else if (npcId == 30418 && cond > 0 && st.getQuestItemsCount(1255) == 0L) {
               if (st.getQuestItemsCount(1254) == 1L && st.getQuestItemsCount(1278) == 0L && st.getQuestItemsCount(1259) == 0L) {
                  htmltext = "30418-01.htm";
               } else if (st.getQuestItemsCount(1254) == 1L && st.getQuestItemsCount(1278) == 1L && st.getQuestItemsCount(1259) < 2L) {
                  htmltext = "30418-03.htm";
               } else if (st.getQuestItemsCount(1254) == 1L && st.getQuestItemsCount(1278) == 1L && st.getQuestItemsCount(1259) >= 2L) {
                  st.giveItems(1255, 1L);
                  st.takeItems(1278, 1L);
                  st.takeItems(1259, 2L);
                  htmltext = "30418-04.htm";
               }
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_412_PathToDarkwizard");
      if (st == null) {
         return super.onKill(npc, killer, isSummon);
      } else if (st.getState() != 1) {
         return super.onKill(npc, killer, isSummon);
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (npcId == 20015) {
            st.set("id", "0");
            if (cond == 1 && st.getQuestItemsCount(1277) == 1L && st.getQuestItemsCount(1257) < 3L && st.getRandom(2) == 0) {
               st.giveItems(1257, 1L);
               st.playSound(st.getQuestItemsCount(1257) == 3L ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
            }
         } else if (npcId == 20517) {
            st.set("id", "0");
            if (cond == 1 && st.getQuestItemsCount(1278) == 1L && st.getQuestItemsCount(1259) < 2L && st.getRandom(2) == 0) {
               st.giveItems(1259, 1L);
               st.playSound(st.getQuestItemsCount(1259) == 2L ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
            }
         } else if (npcId == 20518) {
            st.set("id", "0");
            if (cond == 1 && st.getQuestItemsCount(1278) == 1L && st.getQuestItemsCount(1259) < 2L && st.getRandom(2) == 0) {
               st.giveItems(1259, 1L);
               st.playSound(st.getQuestItemsCount(1259) == 2L ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
            }
         } else if (npcId == 20022) {
            st.set("id", "0");
            if (cond == 1 && st.getQuestItemsCount(1278) == 1L && st.getQuestItemsCount(1259) < 2L && st.getRandom(2) == 0) {
               st.giveItems(1259, 1L);
               st.playSound(st.getQuestItemsCount(1259) == 2L ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
            }
         } else if (npcId == 20045) {
            st.set("id", "0");
            if (cond == 1 && st.getQuestItemsCount(1279) == 1L && st.getQuestItemsCount(1260) < 3L && st.getRandom(2) == 0) {
               st.giveItems(1260, 1L);
               st.playSound(st.getQuestItemsCount(1260) == 3L ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
            }
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _412_PathToDarkwizard(412, "_412_PathToDarkwizard", "");
   }
}
