package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _404_PathToWizard extends Quest {
   private static final String qn = "_404_PathToWizard";
   private static final int PARINA = 30391;
   private static final int EARTH_SNAKE = 30409;
   private static final int WASTELAND_LIZARDMAN = 30410;
   private static final int FLAME_SALAMANDER = 30411;
   private static final int WIND_SYLPH = 30412;
   private static final int WATER_UNDINE = 30413;
   private static final int[] TALKERS = new int[]{30391, 30409, 30410, 30411, 30412, 30413};
   private static final int RED_BEAR = 20021;
   private static final int RATMAN_WARRIOR = 20359;
   private static final int WATER_SEER = 27030;
   private static final int[] MOBS = new int[]{20021, 20359, 27030};
   private static final int MAP_OF_LUSTER = 1280;
   private static final int KEY_OF_FLAME = 1281;
   private static final int FLAME_EARING = 1282;
   private static final int BROKEN_BRONZE_MIRROR = 1283;
   private static final int WIND_FEATHER = 1284;
   private static final int WIND_BANGEL = 1285;
   private static final int RAMAS_DIARY = 1286;
   private static final int SPARKLE_PEBBLE = 1287;
   private static final int WATER_NECKLACE = 1288;
   private static final int RUST_GOLD_COIN = 1289;
   private static final int RED_SOIL = 1290;
   private static final int EARTH_RING = 1291;
   private static final int[] QUESTITEMS = new int[]{1280, 1281, 1282, 1283, 1284, 1285, 1286, 1287, 1288, 1289, 1290, 1291};
   private static final int BEAD_OF_SEASON = 1292;

   public _404_PathToWizard(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30391);

      for(int talkId : TALKERS) {
         this.addTalkId(talkId);
      }

      for(int mobId : MOBS) {
         this.addKillId(mobId);
      }

      this.questItemIds = QUESTITEMS;
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_404_PathToWizard");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            st.set("id", "0");
            if (player.getClassId().getId() == 10) {
               if (player.getLevel() >= 18) {
                  if (st.getQuestItemsCount(1292) > 0L) {
                     htmltext = "30391-03.htm";
                  } else {
                     st.set("cond", "1");
                     st.setState((byte)1);
                     st.playSound("ItemSound.quest_accept");
                     htmltext = "30391-08.htm";
                  }
               } else {
                  htmltext = "30391-02.htm";
               }
            } else {
               htmltext = player.getClassId().getId() == 11 ? "30391-02a.htm" : "30391-01.htm";
            }
         } else if (event.equalsIgnoreCase("30410_1") && st.getQuestItemsCount(1284) == 0L) {
            st.giveItems(1284, 1L);
            st.set("cond", "6");
            htmltext = "30410-03.htm";
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player talker) {
      String htmltext = getNoQuestMsg(talker);
      QuestState st = talker.getQuestState("_404_PathToWizard");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         if (npcId != 30391 && id != 1) {
            return htmltext;
         } else {
            if (npcId == 30391 && st.getInt("cond") == 0) {
               htmltext = "30391-04.htm";
            } else if (npcId != 30391
               || st.getInt("cond") == 0
               || st.getQuestItemsCount(1282) != 0L
                  && st.getQuestItemsCount(1285) != 0L
                  && st.getQuestItemsCount(1288) != 0L
                  && st.getQuestItemsCount(1291) != 0L) {
               if (npcId == 30411 && st.getInt("cond") != 0 && st.getQuestItemsCount(1280) == 0L && st.getQuestItemsCount(1282) == 0L) {
                  if (st.getQuestItemsCount(1280) == 0L) {
                     st.giveItems(1280, 1L);
                  }

                  st.set("cond", "2");
                  htmltext = "30411-01.htm";
               } else if (npcId == 30411 && st.getInt("cond") != 0 && st.getQuestItemsCount(1280) != 0L && st.getQuestItemsCount(1281) == 0L) {
                  htmltext = "30411-02.htm";
               } else if (npcId == 30411 && st.getInt("cond") != 0 && st.getQuestItemsCount(1280) != 0L && st.getQuestItemsCount(1281) != 0L) {
                  st.takeItems(1281, st.getQuestItemsCount(1281));
                  st.takeItems(1280, st.getQuestItemsCount(1280));
                  if (st.getQuestItemsCount(1282) == 0L) {
                     st.giveItems(1282, 1L);
                  }

                  st.set("cond", "4");
                  htmltext = "30411-03.htm";
               } else if (npcId == 30411 && st.getInt("cond") != 0 && st.getQuestItemsCount(1282) != 0L) {
                  htmltext = "30411-04.htm";
               } else if (npcId == 30412
                  && st.getInt("cond") != 0
                  && st.getQuestItemsCount(1282) != 0L
                  && st.getQuestItemsCount(1283) == 0L
                  && st.getQuestItemsCount(1285) == 0L) {
                  if (st.getQuestItemsCount(1283) == 0L) {
                     st.giveItems(1283, 1L);
                  }

                  st.set("cond", "5");
                  htmltext = "30412-01.htm";
               } else if (npcId == 30412 && st.getInt("cond") != 0 && st.getQuestItemsCount(1283) != 0L && st.getQuestItemsCount(1284) == 0L) {
                  htmltext = "30412-02.htm";
               } else if (npcId == 30412 && st.getInt("cond") != 0 && st.getQuestItemsCount(1283) != 0L && st.getQuestItemsCount(1284) != 0L) {
                  st.takeItems(1284, st.getQuestItemsCount(1284));
                  st.takeItems(1283, st.getQuestItemsCount(1283));
                  if (st.getQuestItemsCount(1285) == 0L) {
                     st.giveItems(1285, 1L);
                  }

                  st.set("cond", "7");
                  htmltext = "30412-03.htm";
               } else if (npcId == 30412 && st.getInt("cond") != 0 && st.getQuestItemsCount(1285) != 0L) {
                  htmltext = "30412-04.htm";
               } else if (npcId == 30410 && st.getInt("cond") != 0 && st.getQuestItemsCount(1283) != 0L && st.getQuestItemsCount(1284) == 0L) {
                  htmltext = "30410-01.htm";
               } else if (npcId == 30410 && st.getInt("cond") != 0 && st.getQuestItemsCount(1283) != 0L && st.getQuestItemsCount(1284) != 0L) {
                  htmltext = "30410-04.htm";
               } else if (npcId == 30413
                  && st.getInt("cond") != 0
                  && st.getQuestItemsCount(1285) != 0L
                  && st.getQuestItemsCount(1286) == 0L
                  && st.getQuestItemsCount(1288) == 0L) {
                  if (st.getQuestItemsCount(1286) == 0L) {
                     st.giveItems(1286, 1L);
                  }

                  st.set("cond", "8");
                  htmltext = "30413-01.htm";
               } else if (npcId == 30413 && st.getInt("cond") != 0 && st.getQuestItemsCount(1286) != 0L && st.getQuestItemsCount(1287) < 2L) {
                  htmltext = "30413-02.htm";
               } else if (npcId == 30413 && st.getInt("cond") != 0 && st.getQuestItemsCount(1286) != 0L && st.getQuestItemsCount(1287) >= 2L) {
                  st.takeItems(1287, st.getQuestItemsCount(1287));
                  st.takeItems(1286, st.getQuestItemsCount(1286));
                  if (st.getQuestItemsCount(1288) == 0L) {
                     st.giveItems(1288, 1L);
                  }

                  st.set("cond", "10");
                  htmltext = "30413-03.htm";
               } else if (npcId == 30413 && st.getInt("cond") != 0 && st.getQuestItemsCount(1288) != 0L) {
                  htmltext = "30413-04.htm";
               } else if (npcId == 30409
                  && st.getInt("cond") != 0
                  && st.getQuestItemsCount(1288) != 0L
                  && st.getQuestItemsCount(1289) == 0L
                  && st.getQuestItemsCount(1291) == 0L) {
                  if (st.getQuestItemsCount(1289) == 0L) {
                     st.giveItems(1289, 1L);
                  }

                  st.set("cond", "11");
                  htmltext = "30409-01.htm";
               } else if (npcId == 30409 && st.getInt("cond") != 0 && st.getQuestItemsCount(1289) != 0L && st.getQuestItemsCount(1290) == 0L) {
                  htmltext = "30409-02.htm";
               } else if (npcId == 30409 && st.getInt("cond") != 0 && st.getQuestItemsCount(1289) != 0L && st.getQuestItemsCount(1290) != 0L) {
                  st.takeItems(1290, st.getQuestItemsCount(1290));
                  st.takeItems(1289, st.getQuestItemsCount(1289));
                  if (st.getQuestItemsCount(1291) == 0L) {
                     st.giveItems(1291, 1L);
                  }

                  st.set("cond", "13");
                  htmltext = "30409-03.htm";
               } else if (npcId == 30409 && st.getInt("cond") != 0 && st.getQuestItemsCount(1291) != 0L) {
                  htmltext = "30409-03.htm";
               } else if (npcId == 30391
                  && st.getInt("cond") != 0
                  && st.getQuestItemsCount(1282) != 0L
                  && st.getQuestItemsCount(1285) != 0L
                  && st.getQuestItemsCount(1288) != 0L
                  && st.getQuestItemsCount(1291) != 0L) {
                  st.set("cond", "0");
                  st.saveGlobalQuestVar("1ClassQuestFinished", "1");
                  st.takeItems(1282, st.getQuestItemsCount(1282));
                  st.takeItems(1285, st.getQuestItemsCount(1285));
                  st.takeItems(1288, st.getQuestItemsCount(1288));
                  st.takeItems(1291, st.getQuestItemsCount(1291));
                  st.addExpAndSp(228064, 3520);
                  if (st.getQuestItemsCount(1292) == 0L) {
                     st.giveItems(1292, 1L);
                  }

                  st.giveItems(57, 163800L);
                  st.exitQuest(false);
                  st.playSound("ItemSound.quest_finish");
                  htmltext = "30391-06.htm";
               }
            } else {
               htmltext = "30391-05.htm";
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_404_PathToWizard");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         if (npcId == 20359) {
            st.set("id", "0");
            if (st.getInt("cond") == 2) {
               st.giveItems(1281, 1L);
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "3");
            }
         } else if (npcId == 27030) {
            st.set("id", "0");
            if (st.getInt("cond") == 8 && st.getQuestItemsCount(1287) < 2L) {
               st.giveItems(1287, 1L);
               if (st.getQuestItemsCount(1287) == 2L) {
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "9");
               } else {
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20021) {
            st.set("id", "0");
            if (st.getInt("cond") == 11) {
               st.giveItems(1290, 1L);
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "12");
            }
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _404_PathToWizard(404, "_404_PathToWizard", "");
   }
}
