package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _325_GrimCollector extends Quest {
   private static final String qn = "_325_GrimCollector";
   private static final int ANATOMY_DIAGRAM = 1349;
   private static final int ZOMBIE_HEAD = 1350;
   private static final int ZOMBIE_HEART = 1351;
   private static final int ZOMBIE_LIVER = 1352;
   private static final int SKULL = 1353;
   private static final int RIB_BONE = 1354;
   private static final int SPINE = 1355;
   private static final int ARM_BONE = 1356;
   private static final int THIGH_BONE = 1357;
   private static final int COMPLETE_SKELETON = 1358;
   private static final int CURTIS = 30336;
   private static final int VARSAK = 30342;
   private static final int SAMED = 30434;

   private int getNumberOfPieces(QuestState st) {
      return (int)(
         st.getQuestItemsCount(1350)
            + st.getQuestItemsCount(1355)
            + st.getQuestItemsCount(1356)
            + st.getQuestItemsCount(1351)
            + st.getQuestItemsCount(1352)
            + st.getQuestItemsCount(1353)
            + st.getQuestItemsCount(1354)
            + st.getQuestItemsCount(1357)
            + st.getQuestItemsCount(1358)
      );
   }

   private void payback(QuestState st) {
      int count = this.getNumberOfPieces(st);
      if (count > 0) {
         int reward = (int)(
            30L * st.getQuestItemsCount(1350)
               + 20L * st.getQuestItemsCount(1351)
               + 20L * st.getQuestItemsCount(1352)
               + 100L * st.getQuestItemsCount(1353)
               + 40L * st.getQuestItemsCount(1354)
               + 14L * st.getQuestItemsCount(1355)
               + 14L * st.getQuestItemsCount(1356)
               + 14L * st.getQuestItemsCount(1357)
               + 341L * st.getQuestItemsCount(1358)
         );
         if (count > 10) {
            reward += 1629;
         }

         if (st.getQuestItemsCount(1358) > 0L) {
            reward += 543;
         }

         st.takeItems(1350, -1L);
         st.takeItems(1351, -1L);
         st.takeItems(1352, -1L);
         st.takeItems(1353, -1L);
         st.takeItems(1354, -1L);
         st.takeItems(1355, -1L);
         st.takeItems(1356, -1L);
         st.takeItems(1357, -1L);
         st.takeItems(1358, -1L);
         st.rewardItems(57, (long)reward);
      }
   }

   public _325_GrimCollector(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30336);
      this.addTalkId(new int[]{30336, 30342, 30434});
      this.addKillId(new int[]{20026, 20029, 20035, 20042, 20045, 20457, 20458, 20051, 20514, 20515});
      this.questItemIds = new int[]{1350, 1351, 1352, 1353, 1354, 1355, 1356, 1357, 1358, 1349};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_325_GrimCollector");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30336-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30434-03.htm")) {
            st.giveItems(1349, 1L);
            st.playSound("ItemSound.quest_itemget");
         } else if (event.equalsIgnoreCase("30434-06.htm")) {
            st.takeItems(1349, -1L);
            this.payback(st);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         } else if (event.equalsIgnoreCase("30434-07.htm")) {
            this.payback(st);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30434-09.htm")) {
            int skeletons = (int)st.getQuestItemsCount(1358);
            if (skeletons > 0) {
               st.takeItems(1358, -1L);
               st.playSound("ItemSound.quest_middle");
               st.rewardItems(57, (long)(543 + 341 * skeletons));
            }
         } else if (event.equalsIgnoreCase("30342-03.htm")) {
            if (st.getQuestItemsCount(1355) > 0L
               && st.getQuestItemsCount(1356) > 0L
               && st.getQuestItemsCount(1353) > 0L
               && st.getQuestItemsCount(1354) > 0L
               && st.getQuestItemsCount(1357) > 0L) {
               st.takeItems(1355, 1L);
               st.takeItems(1353, 1L);
               st.takeItems(1356, 1L);
               st.takeItems(1354, 1L);
               st.takeItems(1357, 1L);
               if (st.getRandom(10) < 9) {
                  st.giveItems(1358, 1L);
                  st.playSound("ItemSound.quest_itemget");
               } else {
                  htmltext = "30342-04.htm";
               }
            } else {
               htmltext = "30342-02.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_325_GrimCollector");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 15 && player.getLevel() <= 26) {
                  htmltext = "30336-02.htm";
               } else {
                  htmltext = "30336-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30336:
                     htmltext = st.getQuestItemsCount(1349) < 1L ? "30336-04.htm" : "30336-05.htm";
                     break;
                  case 30342:
                     htmltext = "30342-01.htm";
                     break;
                  case 30434:
                     if (st.getQuestItemsCount(1349) == 0L) {
                        htmltext = "30434-01.htm";
                     } else if (this.getNumberOfPieces(st) == 0) {
                        htmltext = "30434-04.htm";
                     } else {
                        htmltext = st.getQuestItemsCount(1358) == 0L ? "30434-05.htm" : "30434-08.htm";
                     }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_325_GrimCollector");
      if (st == null) {
         return null;
      } else {
         if (st.isStarted() && st.getQuestItemsCount(1349) > 0L) {
            int n = st.getRandom(100);
            switch(npc.getId()) {
               case 20026:
                  if (n <= 90) {
                     st.playSound("ItemSound.quest_itemget");
                     if (n <= 40) {
                        st.giveItems(1350, 1L);
                     } else if (n <= 60) {
                        st.giveItems(1351, 1L);
                     } else {
                        st.giveItems(1352, 1L);
                     }
                  }
                  break;
               case 20029:
                  st.playSound("ItemSound.quest_itemget");
                  if (n <= 44) {
                     st.giveItems(1350, 1L);
                  } else if (n <= 66) {
                     st.giveItems(1351, 1L);
                  } else {
                     st.giveItems(1352, 1L);
                  }
                  break;
               case 20035:
                  if (n <= 79) {
                     st.playSound("ItemSound.quest_itemget");
                     if (n <= 5) {
                        st.giveItems(1353, 1L);
                     } else if (n <= 15) {
                        st.giveItems(1354, 1L);
                     } else if (n <= 29) {
                        st.giveItems(1355, 1L);
                     } else {
                        st.giveItems(1357, 1L);
                     }
                  }
                  break;
               case 20042:
                  if (n <= 86) {
                     st.playSound("ItemSound.quest_itemget");
                     if (n <= 6) {
                        st.giveItems(1353, 1L);
                     } else if (n <= 19) {
                        st.giveItems(1354, 1L);
                     } else if (n <= 69) {
                        st.giveItems(1356, 1L);
                     } else {
                        st.giveItems(1357, 1L);
                     }
                  }
                  break;
               case 20045:
                  if (n <= 97) {
                     st.playSound("ItemSound.quest_itemget");
                     if (n <= 9) {
                        st.giveItems(1353, 1L);
                     } else if (n <= 59) {
                        st.giveItems(1355, 1L);
                     } else if (n <= 77) {
                        st.giveItems(1356, 1L);
                     } else {
                        st.giveItems(1357, 1L);
                     }
                  }
                  break;
               case 20051:
                  if (n <= 99) {
                     st.playSound("ItemSound.quest_itemget");
                     if (n <= 9) {
                        st.giveItems(1353, 1L);
                     } else if (n <= 59) {
                        st.giveItems(1354, 1L);
                     } else if (n <= 79) {
                        st.giveItems(1355, 1L);
                     } else {
                        st.giveItems(1356, 1L);
                     }
                  }
                  break;
               case 20457:
               case 20458:
                  st.playSound("ItemSound.quest_itemget");
                  if (n <= 42) {
                     st.giveItems(1350, 1L);
                  } else if (n <= 67) {
                     st.giveItems(1351, 1L);
                  } else {
                     st.giveItems(1352, 1L);
                  }
                  break;
               case 20514:
                  if (n <= 51) {
                     st.playSound("ItemSound.quest_itemget");
                     if (n <= 2) {
                        st.giveItems(1353, 1L);
                     } else if (n <= 8) {
                        st.giveItems(1354, 1L);
                     } else if (n <= 17) {
                        st.giveItems(1355, 1L);
                     } else if (n <= 18) {
                        st.giveItems(1356, 1L);
                     } else {
                        st.giveItems(1357, 1L);
                     }
                  }
                  break;
               case 20515:
                  if (n <= 60) {
                     st.playSound("ItemSound.quest_itemget");
                     if (n <= 3) {
                        st.giveItems(1353, 1L);
                     } else if (n <= 11) {
                        st.giveItems(1354, 1L);
                     } else if (n <= 22) {
                        st.giveItems(1355, 1L);
                     } else if (n <= 24) {
                        st.giveItems(1356, 1L);
                     } else {
                        st.giveItems(1357, 1L);
                     }
                  }
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _325_GrimCollector(325, "_325_GrimCollector", "");
   }
}
