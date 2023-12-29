package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _108_JumbleTumbleDiamondFuss extends Quest {
   private static final String qn = "_108_JumbleTumbleDiamondFuss";
   private static final int GOUPHS_CONTRACT = 1559;
   private static final int REEPS_CONTRACT = 1560;
   private static final int ELVEN_WINE = 1561;
   private static final int BRONPS_DICE = 1562;
   private static final int BRONPS_CONTRACT = 1563;
   private static final int AQUAMARINE = 1564;
   private static final int CHRYSOBERYL = 1565;
   private static final int GEM_BOX1 = 1566;
   private static final int COAL_PIECE = 1567;
   private static final int BRONPS_LETTER = 1568;
   private static final int BERRY_TART = 1569;
   private static final int BAT_DIAGRAM = 1570;
   private static final int STAR_DIAMOND = 1571;
   private static final int SILVERSMITH_HAMMER = 1511;
   private static final int NEWBIE_REWARD = 2;
   private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;
   private static final int SOULSHOT_FOR_BEGINNERS = 5789;

   public _108_JumbleTumbleDiamondFuss(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30523);
      this.addTalkId(30523);
      this.addTalkId(30516);
      this.addTalkId(30521);
      this.addTalkId(30522);
      this.addTalkId(30526);
      this.addTalkId(30529);
      this.addTalkId(30555);
      this.addKillId(20323);
      this.addKillId(20324);
      this.addKillId(20480);
      this.questItemIds = new int[]{1566, 1571, 1559, 1560, 1561, 1563, 1564, 1565, 1567, 1562, 1568, 1569, 1570};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_108_JumbleTumbleDiamondFuss");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30523-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.giveItems(1559, 1L);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30555-02.htm")) {
            st.takeItems(1560, 1L);
            st.giveItems(1561, 1L);
            st.set("cond", "3");
         } else if (event.equalsIgnoreCase("30526-02.htm")) {
            st.takeItems(1562, 1L);
            st.giveItems(1563, 1L);
            st.set("cond", "5");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_108_JumbleTumbleDiamondFuss");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         int id = st.getState();
         if (id == 0) {
            st.set("cond", "0");
         }

         if (npcId == 30523 && id == 2) {
            htmltext = getAlreadyCompletedMsg(player);
         } else if (npcId == 30523) {
            if (cond == 0) {
               if (player.getRace().ordinal() == 4) {
                  if (player.getLevel() >= 10) {
                     htmltext = "30523-02.htm";
                  } else {
                     htmltext = "30523-01.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30523-00.htm";
                  st.exitQuest(true);
               }
            } else if (cond == 0 && st.getQuestItemsCount(1559) > 0L) {
               htmltext = "30523-04.htm";
            } else if (cond <= 1
               || cond >= 7
               || st.getQuestItemsCount(1560) <= 0L
                  && st.getQuestItemsCount(1561) <= 0L
                  && st.getQuestItemsCount(1562) <= 0L
                  && st.getQuestItemsCount(1563) <= 0L) {
               if (cond == 7 && st.getQuestItemsCount(1566) > 0L) {
                  htmltext = "30523-06.htm";
                  st.takeItems(1566, 1L);
                  st.giveItems(1567, 1L);
                  st.set("cond", "8");
               } else if (cond <= 7
                  || cond >= 12
                  || st.getQuestItemsCount(1568) <= 0L
                     && st.getQuestItemsCount(1567) <= 0L
                     && st.getQuestItemsCount(1569) <= 0L
                     && st.getQuestItemsCount(1570) <= 0L) {
                  if (cond == 12 && st.getQuestItemsCount(1571) > 0L) {
                     int newbie = player.getNewbie();
                     if ((newbie | 2) != newbie) {
                        player.setNewbie(newbie | 2);
                        if (player.getClassId().isMage()) {
                           st.playTutorialVoice("tutorial_voice_027");
                           st.giveItems(5790, 3000L);
                        } else {
                           st.playTutorialVoice("tutorial_voice_026");
                           st.giveItems(5789, 6000L);
                        }

                        showOnScreenMsg(player, NpcStringId.ACQUISITION_OF_RACE_SPECIFIC_WEAPON_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
                     }

                     htmltext = "30523-08.htm";
                     st.takeItems(1571, 1L);
                     st.giveItems(1511, 1L);
                     st.addExpAndSp(34565, 2962);
                     st.giveItems(57, 14666L);

                     for(int ECHO_CHRYSTAL = 4412; ECHO_CHRYSTAL <= 4416; ++ECHO_CHRYSTAL) {
                        st.giveItems(ECHO_CHRYSTAL, 10L);
                     }

                     st.giveItems(1060, 100L);
                     st.set("cond", "0");
                     st.exitQuest(false);
                     st.playSound("ItemSound.quest_finish");
                  }
               } else {
                  htmltext = "30523-07.htm";
               }
            } else {
               htmltext = "30523-05.htm";
            }
         } else if (id == 1) {
            if (npcId == 30516) {
               if (cond == 1 && st.getQuestItemsCount(1559) > 0L) {
                  htmltext = "30516-01.htm";
                  st.giveItems(1560, 1L);
                  st.takeItems(1559, 1L);
                  st.set("cond", "2");
               } else if (cond >= 2) {
                  htmltext = "30516-02.htm";
               }
            } else if (npcId == 30555) {
               if (cond == 2 && st.getQuestItemsCount(1560) == 1L) {
                  htmltext = "30555-01.htm";
               } else if (cond == 3 && st.getQuestItemsCount(1561) > 0L) {
                  htmltext = "30555-03.htm";
               } else if (cond == 7 && st.getQuestItemsCount(1566) == 1L) {
                  htmltext = "30555-04.htm";
               } else {
                  htmltext = "30555-05.htm";
               }
            } else if (npcId == 30529) {
               if (cond == 3 && st.getQuestItemsCount(1561) > 0L) {
                  st.takeItems(1561, 1L);
                  st.giveItems(1562, 1L);
                  htmltext = "30529-01.htm";
                  st.set("cond", "4");
               } else if (cond == 4) {
                  htmltext = "30529-02.htm";
               } else {
                  htmltext = "30529-03.htm";
               }
            } else if (npcId == 30526) {
               if (cond == 4 && st.getQuestItemsCount(1562) > 0L) {
                  htmltext = "30526-01.htm";
               } else if (cond != 5 || st.getQuestItemsCount(1563) <= 0L || st.getQuestItemsCount(1564) >= 10L && st.getQuestItemsCount(1565) >= 10L) {
                  if (cond == 6 && st.getQuestItemsCount(1563) > 0L && st.getQuestItemsCount(1564) == 10L && st.getQuestItemsCount(1565) == 10L) {
                     htmltext = "30526-04.htm";
                     st.takeItems(1563, -1L);
                     st.takeItems(1564, -1L);
                     st.takeItems(1565, -1L);
                     st.giveItems(1566, 1L);
                     st.set("cond", "7");
                  } else if (cond == 7 && st.getQuestItemsCount(1566) > 0L) {
                     htmltext = "30526-05.htm";
                  } else if (cond == 8 && st.getQuestItemsCount(1567) > 0L) {
                     htmltext = "30526-06.htm";
                     st.takeItems(1567, 1L);
                     st.giveItems(1568, 1L);
                     st.set("cond", "9");
                  } else if (cond == 9 && st.getQuestItemsCount(1568) > 0L) {
                     htmltext = "30526-07.htm";
                  } else {
                     htmltext = "30526-08.htm";
                  }
               } else {
                  htmltext = "30526-03.htm";
               }
            } else if (npcId == 30521) {
               if (cond == 9 && st.getQuestItemsCount(1568) > 0L) {
                  htmltext = "30521-01.htm";
                  st.takeItems(1568, 1L);
                  st.giveItems(1569, 1L);
                  st.set("cond", "10");
               } else if (cond == 10 && st.getQuestItemsCount(1569) > 0L) {
                  htmltext = "30521-02.htm";
               } else {
                  htmltext = "30521-03.htm";
               }
            } else if (npcId == 30522) {
               if (cond == 10 && st.getQuestItemsCount(1569) > 0L) {
                  htmltext = "30522-01.htm";
                  st.takeItems(1569, 1L);
                  st.giveItems(1570, 1L);
                  st.set("cond", "11");
               } else if (cond == 11 && st.getQuestItemsCount(1570) > 0L) {
                  htmltext = "30522-02.htm";
               } else if (cond == 12 && st.getQuestItemsCount(1571) > 0L) {
                  htmltext = "30522-03.htm";
               } else {
                  htmltext = "30522-04.htm";
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_108_JumbleTumbleDiamondFuss");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (npcId != 20323 && npcId != 20324) {
            if (npcId == 20480 && cond == 11 && st.getQuestItemsCount(1570) > 0L && st.getQuestItemsCount(1571) == 0L && Rnd.getChance(50)) {
               st.takeItems(1570, 1L);
               st.giveItems(1571, 1L);
               st.set("cond", "12");
               st.playSound("ItemSound.quest_middle");
            }
         } else if (cond == 5 && st.getQuestItemsCount(1563) > 0L) {
            if (st.getQuestItemsCount(1564) < 10L && Rnd.getChance(80)) {
               st.giveItems(1564, 1L);
               if (st.getQuestItemsCount(1564) < 10L) {
                  st.playSound("ItemSound.quest_itemget");
               } else {
                  st.playSound("ItemSound.quest_middle");
                  if (st.getQuestItemsCount(1564) == 10L && st.getQuestItemsCount(1565) == 10L) {
                     st.set("cond", "6");
                  }
               }
            }

            if (st.getQuestItemsCount(1565) < 10L && Rnd.getChance(80)) {
               st.giveItems(1565, 1L);
               if (st.getQuestItemsCount(1565) < 10L) {
                  st.playSound("ItemSound.quest_itemget");
               } else {
                  st.playSound("ItemSound.quest_middle");
                  if (st.getQuestItemsCount(1564) == 10L && st.getQuestItemsCount(1565) == 10L) {
                     st.set("cond", "6");
                  }
               }
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _108_JumbleTumbleDiamondFuss(108, "_108_JumbleTumbleDiamondFuss", "");
   }
}
