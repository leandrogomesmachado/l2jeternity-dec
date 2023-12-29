package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _408_PathToElvenwizard extends Quest {
   private static final String qn = "_408_PathToElvenwizard";
   private static final int ROSELLA = 30414;
   private static final int GREENIS = 30157;
   private static final int THALIA = 30371;
   private static final int NORTHWIND = 30423;
   private static final int[] TALKERS = new int[]{30414, 30157, 30371, 30423};
   private static final int DRYAD_ELDER = 20019;
   private static final int PINCER_SPIDER = 20466;
   private static final int SUKAR_WERERAT_LEADER = 20047;
   private static final int[] MOBS = new int[]{20019, 20466, 20047};
   private static final int ROGELLIAS_LETTER = 1218;
   private static final int RED_DOWN = 1219;
   private static final int MAGICAL_POWERS_RUBY = 1220;
   private static final int PURE_AQUAMARINE = 1221;
   private static final int APPETIZING_APPLE = 1222;
   private static final int GOLD_LEAVES = 1223;
   private static final int IMMORTAL_LOVE = 1224;
   private static final int AMETHYST = 1225;
   private static final int NOBILITY_AMETHYST = 1226;
   private static final int FERTILITY_PERIDOT = 1229;
   private static final int CHARM_OF_GRAIN = 1272;
   private static final int SAP_OF_WORLD_TREE = 1273;
   private static final int LUCKY_POTPOURI = 1274;
   private static final int ETERNITY_DIAMOND = 1230;
   private static final int[] QUESTITEMS = new int[]{1218, 1219, 1220, 1221, 1222, 1223, 1224, 1225, 1226, 1229, 1272, 1273, 1274};

   public _408_PathToElvenwizard(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30414);

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
      QuestState st = player.getQuestState("_408_PathToElvenwizard");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            st.set("id", "0");
            if (player.getClassId().getId() != 25) {
               htmltext = player.getClassId().getId() == 26 ? "30414-02a.htm" : "30414-03.htm";
            } else if (player.getLevel() < 18) {
               htmltext = "30414-04.htm";
            } else if (st.getQuestItemsCount(1230) != 0L) {
               htmltext = "30414-05.htm";
            } else {
               st.set("cond", "1");
               st.setState((byte)1);
               st.playSound("ItemSound.quest_accept");
               if (st.getQuestItemsCount(1229) == 0L) {
                  st.giveItems(1229, 1L);
               }

               htmltext = "30414-06.htm";
            }
         } else if (event.equalsIgnoreCase("408_1")) {
            if (st.getInt("cond") != 0 && st.getQuestItemsCount(1220) != 0L) {
               htmltext = "30414-10.htm";
            } else if (st.getInt("cond") != 0 && st.getQuestItemsCount(1220) == 0L && st.getQuestItemsCount(1229) != 0L) {
               if (st.getQuestItemsCount(1218) == 0L) {
                  st.giveItems(1218, 1L);
               }

               htmltext = "30414-07.htm";
            }
         } else if (event.equalsIgnoreCase("408_4")) {
            if (st.getInt("cond") != 0 && st.getQuestItemsCount(1218) != 0L) {
               st.takeItems(1218, st.getQuestItemsCount(1218));
               if (st.getQuestItemsCount(1272) == 0L) {
                  st.giveItems(1272, 1L);
               }

               htmltext = "30157-02.htm";
            }
         } else if (event.equalsIgnoreCase("408_2")) {
            if (st.getInt("cond") != 0 && st.getQuestItemsCount(1221) != 0L) {
               htmltext = "30414-13.htm";
            } else if (st.getInt("cond") != 0 && st.getQuestItemsCount(1221) == 0L && st.getQuestItemsCount(1229) != 0L) {
               if (st.getQuestItemsCount(1222) == 0L) {
                  st.giveItems(1222, 1L);
               }

               htmltext = "30414-14.htm";
            }
         } else if (event.equalsIgnoreCase("408_5")) {
            if (st.getInt("cond") != 0 && st.getQuestItemsCount(1222) != 0L) {
               st.takeItems(1222, st.getQuestItemsCount(1222));
               if (st.getQuestItemsCount(1273) == 0L) {
                  st.giveItems(1273, 1L);
               }

               htmltext = "30371-02.htm";
            }
         } else if (event.equalsIgnoreCase("408_3")) {
            if (st.getInt("cond") != 0 && st.getQuestItemsCount(1226) != 0L) {
               htmltext = "30414-17.htm";
            } else if (st.getInt("cond") != 0 && st.getQuestItemsCount(1226) == 0L && st.getQuestItemsCount(1229) != 0L) {
               if (st.getQuestItemsCount(1224) == 0L) {
                  st.giveItems(1224, 1L);
               }

               htmltext = "30414-18.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player talker) {
      String htmltext = getNoQuestMsg(talker);
      QuestState st = talker.getQuestState("_408_PathToElvenwizard");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         if (npcId != 30414 && id != 1) {
            return htmltext;
         } else {
            if (npcId == 30414 && st.getInt("cond") == 0) {
               htmltext = "30414-01.htm";
            } else if (npcId != 30414
               || st.getInt("cond") == 0
               || st.getQuestItemsCount(1218) != 0L
               || st.getQuestItemsCount(1222) != 0L
               || st.getQuestItemsCount(1224) != 0L
               || st.getQuestItemsCount(1272) != 0L
               || st.getQuestItemsCount(1273) != 0L
               || st.getQuestItemsCount(1274) != 0L
               || st.getQuestItemsCount(1229) == 0L
               || st.getQuestItemsCount(1220) != 0L && st.getQuestItemsCount(1226) != 0L && st.getQuestItemsCount(1221) != 0L) {
               if (npcId == 30414 && st.getInt("cond") != 0 && st.getQuestItemsCount(1218) != 0L) {
                  htmltext = "30414-08.htm";
               } else if (npcId == 30157 && st.getInt("cond") != 0 && st.getQuestItemsCount(1218) != 0L) {
                  htmltext = "30157-01.htm";
               } else if (npcId == 30157 && st.getInt("cond") != 0 && st.getQuestItemsCount(1272) != 0L && st.getQuestItemsCount(1219) < 5L) {
                  htmltext = "30157-03.htm";
               } else if (npcId == 30157 && st.getInt("cond") != 0 && st.getQuestItemsCount(1272) != 0L && st.getQuestItemsCount(1219) >= 5L) {
                  st.takeItems(1219, st.getQuestItemsCount(1219));
                  st.takeItems(1272, st.getQuestItemsCount(1272));
                  if (st.getQuestItemsCount(1220) == 0L) {
                     st.giveItems(1220, 1L);
                  }

                  htmltext = "30157-04.htm";
               } else if (npcId == 30414 && st.getInt("cond") != 0 && st.getQuestItemsCount(1272) != 0L && st.getQuestItemsCount(1219) < 5L) {
                  htmltext = "30414-09.htm";
               } else if (npcId == 30414 && st.getInt("cond") != 0 && st.getQuestItemsCount(1272) != 0L && st.getQuestItemsCount(1219) >= 5L) {
                  htmltext = "30414-25.htm";
               } else if (npcId == 30414 && st.getInt("cond") != 0 && st.getQuestItemsCount(1222) != 0L) {
                  htmltext = "30414-15.htm";
               } else if (npcId == 30371 && st.getInt("cond") != 0 && st.getQuestItemsCount(1222) != 0L) {
                  htmltext = "30371-01.htm";
               } else if (npcId == 30371 && st.getInt("cond") != 0 && st.getQuestItemsCount(1273) != 0L && st.getQuestItemsCount(1223) < 5L) {
                  htmltext = "30371-03.htm";
               } else if (npcId == 30371 && st.getInt("cond") != 0 && st.getQuestItemsCount(1273) != 0L && st.getQuestItemsCount(1223) >= 5L) {
                  st.takeItems(1223, st.getQuestItemsCount(1223));
                  st.takeItems(1273, st.getQuestItemsCount(1273));
                  if (st.getQuestItemsCount(1221) == 0L) {
                     st.giveItems(1221, 1L);
                  }

                  htmltext = "30371-04.htm";
               } else if (npcId == 30414 && st.getInt("cond") != 0 && st.getQuestItemsCount(1273) != 0L && st.getQuestItemsCount(1223) < 5L) {
                  htmltext = "30414-16.htm";
               } else if (npcId == 30414 && st.getInt("cond") != 0 && st.getQuestItemsCount(1272) != 0L && st.getQuestItemsCount(1223) >= 5L) {
                  htmltext = "30414-26.htm";
               } else if (npcId == 30414 && st.getInt("cond") != 0 && st.getQuestItemsCount(1224) != 0L) {
                  htmltext = "30414-19.htm";
               } else if (npcId == 30423 && st.getInt("cond") != 0 && st.getQuestItemsCount(1224) != 0L) {
                  st.takeItems(1224, st.getQuestItemsCount(1224));
                  if (st.getQuestItemsCount(1274) == 0L) {
                     st.giveItems(1274, 1L);
                  }

                  htmltext = "30423-01.htm";
               } else if (npcId == 30423 && st.getInt("cond") != 0 && st.getQuestItemsCount(1274) != 0L && st.getQuestItemsCount(1225) < 2L) {
                  htmltext = "30423-02.htm";
               } else if (npcId == 30423 && st.getInt("cond") != 0 && st.getQuestItemsCount(1274) != 0L && st.getQuestItemsCount(1225) >= 2L) {
                  st.takeItems(1225, st.getQuestItemsCount(1225));
                  st.takeItems(1274, st.getQuestItemsCount(1274));
                  if (st.getQuestItemsCount(1226) == 0L) {
                     st.giveItems(1226, 1L);
                  }

                  htmltext = "30423-03.htm";
               } else if (npcId == 30414 && st.getInt("cond") != 0 && st.getQuestItemsCount(1274) != 0L && st.getQuestItemsCount(1225) < 2L) {
                  htmltext = "30414-20.htm";
               } else if (npcId == 30414 && st.getInt("cond") != 0 && st.getQuestItemsCount(1274) != 0L && st.getQuestItemsCount(1225) >= 2L) {
                  htmltext = "30414-27.htm";
               } else if (npcId == 30414
                  && st.getInt("cond") != 0
                  && st.getQuestItemsCount(1218) == 0L
                  && st.getQuestItemsCount(1222) == 0L
                  && st.getQuestItemsCount(1224) == 0L
                  && st.getQuestItemsCount(1272) == 0L
                  && st.getQuestItemsCount(1273) == 0L
                  && st.getQuestItemsCount(1274) == 0L
                  && st.getQuestItemsCount(1229) != 0L
                  && st.getQuestItemsCount(1220) != 0L
                  && st.getQuestItemsCount(1226) != 0L
                  && st.getQuestItemsCount(1221) != 0L) {
                  st.takeItems(1220, st.getQuestItemsCount(1220));
                  st.takeItems(1221, st.getQuestItemsCount(1221));
                  st.takeItems(1226, st.getQuestItemsCount(1226));
                  st.takeItems(1229, st.getQuestItemsCount(1229));
                  String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
                  if (isFinished.equalsIgnoreCase("")) {
                     st.addExpAndSp(228064, 3210);
                  }

                  if (st.getQuestItemsCount(1230) == 0L) {
                     st.giveItems(1230, 1L);
                  }

                  st.giveItems(57, 163800L);
                  st.saveGlobalQuestVar("1ClassQuestFinished", "1");
                  st.set("cond", "0");
                  st.exitQuest(false);
                  st.playSound("ItemSound.quest_finish");
                  htmltext = "30414-24.htm";
               }
            } else {
               htmltext = "30414-11.htm";
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_408_PathToElvenwizard");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         if (npcId == 20466) {
            st.set("id", "0");
            if (st.getInt("cond") != 0 && st.getQuestItemsCount(1272) != 0L && st.getQuestItemsCount(1219) < 5L && st.getRandom(100) < 70) {
               st.giveItems(1219, 1L);
               st.playSound(st.getQuestItemsCount(1219) == 5L ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
            }
         } else if (npcId == 20019) {
            st.set("id", "0");
            if (st.getInt("cond") != 0 && st.getQuestItemsCount(1273) != 0L && st.getQuestItemsCount(1223) < 5L && st.getRandom(100) < 40) {
               st.giveItems(1223, 1L);
               st.playSound(st.getQuestItemsCount(1223) == 5L ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
            }
         } else if (npcId == 20047) {
            st.set("id", "0");
            if (st.getInt("cond") != 0 && st.getQuestItemsCount(1274) != 0L && st.getQuestItemsCount(1225) < 2L && st.getRandom(100) < 40) {
               st.giveItems(1225, 1L);
               st.playSound(st.getQuestItemsCount(1225) == 2L ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
            }
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _408_PathToElvenwizard(408, "_408_PathToElvenwizard", "");
   }
}
