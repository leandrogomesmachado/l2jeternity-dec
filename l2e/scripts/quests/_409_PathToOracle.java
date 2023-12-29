package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.SocialAction;

public final class _409_PathToOracle extends Quest {
   private static final String qn = "_409_PathToOracle";
   private static final int MANUEL = 30293;
   private static final int ALLANA = 30424;
   private static final int PERRIN = 30428;
   private static final int CRYSTAL_MEDALLION = 1231;
   private static final int SWINDLERS_MONEY = 1232;
   private static final int ALLANAS_DIARY = 1233;
   private static final int LIZARD_CAPTAIN_ORDER = 1234;
   private static final int LEAF_OF_ORACLE = 1235;
   private static final int HALF_OF_DIARY = 1236;
   private static final int TAMILS_NECKLACE = 1275;
   private static final int LIZARDMAN_WARRIOR = 27032;
   private static final int LIZARDMAN_SCOUT = 27033;
   private static final int LIZARDMAN = 27034;
   private static final int TAMIL = 27035;

   private _409_PathToOracle(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30293);
      this.addTalkId(30293);
      this.addTalkId(30424);
      this.addTalkId(30428);
      this.addKillId(27032);
      this.addKillId(27033);
      this.addKillId(27034);
      this.addKillId(27035);
      this.questItemIds = new int[]{1231, 1232, 1233, 1234, 1235, 1236, 1275};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_409_PathToOracle");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            if (player.getClassId().getId() == 25 && !st.isCompleted()) {
               if (player.getLevel() > 17) {
                  if (player.getInventory().getInventoryItemCount(1235, -1) == 0L) {
                     st.setState((byte)1);
                     st.set("cond", "1");
                     st.playSound("ItemSound.quest_accept");
                     st.giveItems(1231, 1L);
                     htmltext = "30293-05.htm";
                  } else {
                     htmltext = "30293-04.htm";
                  }
               } else {
                  htmltext = "30293-03.htm";
               }
            } else if (player.getClassId().getId() == 29) {
               htmltext = "30293-02a.htm";
            } else {
               htmltext = "30293-02.htm";
            }
         } else if (!st.isCompleted()) {
            if (event.equalsIgnoreCase("30424_1")) {
               st.set("cond", "2");
               st.addSpawn(27032);
               st.addSpawn(27033);
               st.addSpawn(27034);
               return null;
            }

            if (event.equalsIgnoreCase("30428_1")) {
               htmltext = "30428-02.htm";
            } else if (event.equalsIgnoreCase("30428_2")) {
               htmltext = "30428-03.htm";
            } else if (event.equalsIgnoreCase("30428_3")) {
               st.addSpawn(27035);
               return null;
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_409_PathToOracle");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (npcId == 30293) {
            if (st.getQuestItemsCount(1231) != 0L) {
               if (st.getQuestItemsCount(1233) == 0L
                  && st.getQuestItemsCount(1234) == 0L
                  && st.getQuestItemsCount(1232) == 0L
                  && st.getQuestItemsCount(1236) == 0L) {
                  if (cond == 0) {
                     htmltext = "30293-06.htm";
                  } else {
                     htmltext = "30293-09.htm";
                  }
               } else if (st.getQuestItemsCount(1233) != 0L
                  && st.getQuestItemsCount(1234) != 0L
                  && st.getQuestItemsCount(1232) != 0L
                  && st.getQuestItemsCount(1236) == 0L) {
                  st.takeItems(1232, -1L);
                  st.takeItems(1233, -1L);
                  st.takeItems(1234, -1L);
                  st.takeItems(1231, -1L);
                  String done = st.getGlobalQuestVar("1ClassQuestFinished");
                  st.set("cond", "0");
                  st.exitQuest(false);
                  if (done.isEmpty()) {
                     if (player.getLevel() >= 20) {
                        st.addExpAndSp(320534, 20392);
                     } else if (player.getLevel() == 19) {
                        st.addExpAndSp(456128, 27090);
                     } else {
                        st.addExpAndSp(591724, 33788);
                     }

                     st.giveItems(57, 163800L);
                  }

                  st.giveItems(1235, 1L);
                  st.saveGlobalQuestVar("1ClassQuestFinished", "1");
                  st.playSound("ItemSound.quest_finish");
                  player.sendPacket(new SocialAction(player.getObjectId(), 3));
                  htmltext = "30293-08.htm";
               } else {
                  htmltext = "30293-07.htm";
               }
            } else if (cond == 0) {
               if (st.getQuestItemsCount(1235) == 0L) {
                  htmltext = "30293-01.htm";
               } else {
                  htmltext = "30293-04.htm";
               }
            }
         } else if (cond != 0 && st.getQuestItemsCount(1231) != 0L) {
            if (npcId == 30424) {
               if (st.getQuestItemsCount(1233) == 0L
                  && st.getQuestItemsCount(1234) == 0L
                  && st.getQuestItemsCount(1232) == 0L
                  && st.getQuestItemsCount(1236) == 0L) {
                  if (cond > 2) {
                     htmltext = "30424-05.htm";
                  } else {
                     htmltext = "30424-01.htm";
                  }
               } else if (st.getQuestItemsCount(1233) == 0L
                  && st.getQuestItemsCount(1234) != 0L
                  && st.getQuestItemsCount(1232) == 0L
                  && st.getQuestItemsCount(1236) == 0L) {
                  st.giveItems(1236, 1L);
                  st.set("cond", "4");
                  htmltext = "30424-02.htm";
               } else if (st.getQuestItemsCount(1233) == 0L
                  && st.getQuestItemsCount(1234) != 0L
                  && st.getQuestItemsCount(1232) == 0L
                  && st.getQuestItemsCount(1236) != 0L) {
                  if (st.getQuestItemsCount(1275) == 0L) {
                     htmltext = "30424-06.htm";
                  } else {
                     htmltext = "30424-03.htm";
                  }
               } else if (st.getQuestItemsCount(1233) == 0L
                  && st.getQuestItemsCount(1234) != 0L
                  && st.getQuestItemsCount(1232) != 0L
                  && st.getQuestItemsCount(1236) != 0L) {
                  st.takeItems(1236, -1L);
                  st.giveItems(1233, 1L);
                  st.set("cond", "7");
                  htmltext = "30424-04.htm";
               } else if (st.getQuestItemsCount(1233) != 0L
                  && st.getQuestItemsCount(1234) != 0L
                  && st.getQuestItemsCount(1232) != 0L
                  && st.getQuestItemsCount(1236) == 0L) {
                  htmltext = "30424-05.htm";
               }
            } else if (st.getQuestItemsCount(1234) != 0L) {
               if (st.getQuestItemsCount(1275) != 0L) {
                  st.takeItems(1275, -1L);
                  st.giveItems(1232, 1L);
                  st.set("cond", "6");
                  htmltext = "30428-04.htm";
               } else if (st.getQuestItemsCount(1232) == 0L) {
                  if (cond > 4) {
                     htmltext = "30428-06.htm";
                  } else {
                     htmltext = "30428-01.htm";
                  }
               } else {
                  htmltext = "30428-05.htm";
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_409_PathToOracle");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         if (npcId == 27035) {
            if (st.getQuestItemsCount(1275) == 0L) {
               st.giveItems(1275, 1L);
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "5");
            }
         } else if (st.getQuestItemsCount(1234) == 0L) {
            st.giveItems(1234, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "3");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _409_PathToOracle(409, "_409_PathToOracle", "");
   }
}
