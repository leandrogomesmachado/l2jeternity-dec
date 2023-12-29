package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

public final class _120_PavelsResearch extends Quest {
   private static final String qn = "_120_PavelsResearch";
   private static final int YUMI = 32041;
   private static final int WEATHER1 = 32042;
   private static final int WEATHER2 = 32043;
   private static final int WEATHER3 = 32044;
   private static final int BOOKSHELF = 32045;
   private static final int STONES = 32046;
   private static final int WENDY = 32047;
   private static final int EARPHOENIX = 6324;
   private static final int REPORT = 8058;
   private static final int REPORT2 = 8059;
   private static final int ENIGMA = 8060;
   private static final int FLOWER = 8290;
   private static final int HEART = 8291;
   private static final int NECKLACE = 8292;

   public _120_PavelsResearch(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32046);
      this.addTalkId(new int[]{32046, 32045, 32042, 32043, 32044, 32047, 32041});
      this.questItemIds = new int[]{8058, 8059, 8060, 8290, 8291, 8292};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_120_PavelsResearch");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32041-03.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32041-04.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32041-12.htm")) {
            st.set("cond", "8");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32041-16.htm")) {
            st.set("cond", "16");
            st.giveItems(8060, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32041-22.htm")) {
            st.set("cond", "17");
            st.takeItems(8060, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32041-32.htm")) {
            st.takeItems(8292, 1L);
            st.giveItems(6324, 1L);
            st.giveItems(57, 783720L);
            st.addExpAndSp(3447315, 272615);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
         } else if (event.equalsIgnoreCase("32042-06.htm")) {
            if (st.getInt("cond") == 10) {
               if (st.getInt("talk") + st.getInt("talk1") == 2) {
                  st.set("cond", "11");
                  st.set("talk", "0");
                  st.set("talk1", "0");
                  st.playSound("ItemSound.quest_middle");
               } else {
                  htmltext = "32042-03.htm";
               }
            }
         } else if (event.equalsIgnoreCase("32042-10.htm")) {
            if (st.getInt("talk") + st.getInt("talk1") + st.getInt("talk2") == 3) {
               htmltext = "32042-14.htm";
            }
         } else if (event.equalsIgnoreCase("32042-11.htm")) {
            if (st.getInt("talk") == 0) {
               st.set("talk", "1");
            }
         } else if (event.equalsIgnoreCase("32042-12.htm")) {
            if (st.getInt("talk1") == 0) {
               st.set("talk1", "1");
            }
         } else if (event.equalsIgnoreCase("32042-13.htm")) {
            if (st.getInt("talk2") == 0) {
               st.set("talk2", "1");
            }
         } else if (event.equalsIgnoreCase("32042-15.htm")) {
            st.set("cond", "12");
            st.set("talk", "0");
            st.set("talk1", "0");
            st.set("talk2", "0");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32043-06.htm")) {
            if (st.getInt("cond") == 17) {
               if (st.getInt("talk") + st.getInt("talk1") == 2) {
                  st.set("cond", "18");
                  st.set("talk", "0");
                  st.set("talk1", "0");
                  st.playSound("ItemSound.quest_middle");
               } else {
                  htmltext = "32043-03.htm";
               }
            }
         } else if (event.equalsIgnoreCase("32043-15.htm")) {
            if (st.getInt("talk") + st.getInt("talk1") == 2) {
               htmltext = "32043-29.htm";
            }
         } else if (event.equalsIgnoreCase("32043-18.htm")) {
            if (st.getInt("talk") == 1) {
               htmltext = "32043-21.htm";
            }
         } else if (event.equalsIgnoreCase("32043-20.htm")) {
            st.set("talk", "1");
            st.playSound("AmbSound.ed_drone_02");
         } else if (event.equalsIgnoreCase("32043-28.htm")) {
            st.set("talk1", "1");
         } else if (event.equalsIgnoreCase("32043-30.htm")) {
            st.set("cond", "19");
            st.set("talk", "0");
            st.set("talk1", "0");
         } else if (event.equalsIgnoreCase("32044-06.htm")) {
            if (st.getInt("cond") == 20) {
               if (st.getInt("talk") + st.getInt("talk1") == 2) {
                  st.set("cond", "21");
                  st.set("talk", "0");
                  st.set("talk1", "0");
                  st.playSound("ItemSound.quest_middle");
               } else {
                  htmltext = "32044-03.htm";
               }
            }
         } else if (event.equalsIgnoreCase("32044-08.htm")) {
            if (st.getInt("talk") + st.getInt("talk1") == 2) {
               htmltext = "32044-11.htm";
            }
         } else if (event.equalsIgnoreCase("32044-09.htm")) {
            if (st.getInt("talk") == 0) {
               st.set("talk", "1");
            }
         } else if (event.equalsIgnoreCase("32044-10.htm")) {
            if (st.getInt("talk1") == 0) {
               st.set("talk1", "1");
            }
         } else if (event.equalsIgnoreCase("32044-17.htm")) {
            st.set("cond", "22");
            st.set("talk", "0");
            st.set("talk1", "0");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32045-02.htm")) {
            st.set("cond", "15");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(8058, 1L);
            npc.broadcastPacket(new MagicSkillUse(npc, player, 5073, 5, 1500, 0));
         } else if (event.equalsIgnoreCase("32046-04.htm") || event.equalsIgnoreCase("32046-05.htm")) {
            st.exitQuest(true);
         } else if (event.equalsIgnoreCase("32046-06.htm")) {
            if (player.getLevel() >= 70) {
               st.set("cond", "1");
               st.setState((byte)1);
               st.playSound("ItemSound.quest_accept");
            } else {
               htmltext = "32046-00.htm";
               st.exitQuest(true);
            }
         } else if (event.equalsIgnoreCase("32046-08.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32046-12.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(8290, 1L);
         } else if (event.equalsIgnoreCase("32046-22.htm")) {
            st.set("cond", "10");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32046-29.htm")) {
            st.set("cond", "13");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32046-35.htm")) {
            st.set("cond", "20");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32046-38.htm")) {
            st.set("cond", "23");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(8291, 1L);
         } else if (event.equalsIgnoreCase("32047-06.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32047-10.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(8290, 1L);
         } else if (event.equalsIgnoreCase("32047-15.htm")) {
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32047-18.htm")) {
            st.set("cond", "14");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32047-26.htm")) {
            st.set("cond", "24");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(8291, 1L);
         } else if (event.equalsIgnoreCase("32047-32.htm")) {
            st.set("cond", "25");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(8292, 1L);
         } else if (event.equalsIgnoreCase("w1_1")) {
            st.set("talk", "1");
            htmltext = "32042-04.htm";
         } else if (event.equalsIgnoreCase("w1_2")) {
            st.set("talk1", "1");
            htmltext = "32042-05.htm";
         } else if (event.equalsIgnoreCase("w2_1")) {
            st.set("talk", "1");
            htmltext = "32043-04.htm";
         } else if (event.equalsIgnoreCase("w2_2")) {
            st.set("talk1", "1");
            htmltext = "32043-05.htm";
         } else if (event.equalsIgnoreCase("w3_1")) {
            st.set("talk", "1");
            htmltext = "32044-04.htm";
         } else if (event.equalsIgnoreCase("w3_2")) {
            st.set("talk1", "1");
            htmltext = "32044-05.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_120_PavelsResearch");
      if (st == null) {
         return htmltext;
      } else {
         QuestState qs = player.getQuestState("_114_ResurrectionOfAnOldManager");
         int cond = st.getInt("cond");
         int npcId = npc.getId();
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 70 && qs != null && qs.isCompleted()) {
                  htmltext = "32046-01.htm";
               } else {
                  htmltext = "32046-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (npcId == 32046) {
                  if (cond == 1) {
                     htmltext = "32046-06.htm";
                  } else if (cond == 2) {
                     htmltext = "32046-09.htm";
                  } else if (cond == 5) {
                     htmltext = "32046-10.htm";
                  } else if (cond == 6) {
                     htmltext = "32046-13.htm";
                  } else if (cond == 9) {
                     htmltext = "32046-14.htm";
                  } else if (cond == 10) {
                     htmltext = "32046-23.htm";
                  } else if (cond == 12) {
                     htmltext = "32046-26.htm";
                  } else if (cond == 13) {
                     htmltext = "32046-30.htm";
                  } else if (cond == 19) {
                     htmltext = "32046-31.htm";
                  } else if (cond == 20) {
                     htmltext = "32046-36.htm";
                  } else if (cond == 22) {
                     htmltext = "32046-37.htm";
                  } else if (cond == 23) {
                     htmltext = "32046-39.htm";
                  }
               } else if (npcId == 32047) {
                  if (cond == 2 || cond == 3 || cond == 4) {
                     htmltext = "32047-01.htm";
                  } else if (cond == 5) {
                     htmltext = "32047-07.htm";
                  } else if (cond == 6) {
                     htmltext = "32047-08.htm";
                  } else if (cond == 7) {
                     htmltext = "32047-11.htm";
                  } else if (cond == 8) {
                     htmltext = "32047-12.htm";
                  } else if (cond == 9) {
                     htmltext = "32047-15.htm";
                  } else if (cond == 13) {
                     htmltext = "32047-16.htm";
                  } else if (cond == 14) {
                     htmltext = "32047-19.htm";
                  } else if (cond == 15) {
                     htmltext = "32047-20.htm";
                  } else if (cond == 23) {
                     htmltext = "32047-21.htm";
                  } else if (cond == 24) {
                     htmltext = "32047-26.htm";
                  } else if (cond == 25) {
                     htmltext = "32047-33.htm";
                  }
               } else if (npcId == 32041) {
                  if (cond == 2) {
                     htmltext = "32041-01.htm";
                  } else if (cond == 3) {
                     htmltext = "32041-05.htm";
                  } else if (cond == 4) {
                     htmltext = "32041-06.htm";
                  } else if (cond == 7) {
                     htmltext = "32041-07.htm";
                  } else if (cond == 8) {
                     htmltext = "32041-13.htm";
                  } else if (cond == 15) {
                     htmltext = "32041-14.htm";
                  } else if (cond == 16) {
                     if (st.getQuestItemsCount(8059) == 0L) {
                        htmltext = "32041-17.htm";
                     } else {
                        htmltext = "32041-18.htm";
                     }
                  } else if (cond == 17) {
                     htmltext = "32041-22.htm";
                  } else if (cond == 25) {
                     htmltext = "32041-26.htm";
                  }
               } else if (npcId == 32042) {
                  if (cond == 10) {
                     htmltext = "32042-01.htm";
                  } else if (cond == 11) {
                     if (st.getInt("talk") + st.getInt("talk1") + st.getInt("talk2") == 3) {
                        htmltext = "32042-14.htm";
                     } else {
                        htmltext = "32042-06.htm";
                     }
                  } else if (cond == 12) {
                     htmltext = "32042-15.htm";
                  }
               } else if (npcId == 32043) {
                  if (cond == 17) {
                     htmltext = "32043-01.htm";
                  } else if (cond == 18) {
                     if (st.getInt("talk") + st.getInt("talk1") == 2) {
                        htmltext = "32043-29.htm";
                     } else {
                        htmltext = "32043-06.htm";
                     }
                  } else if (cond == 19) {
                     htmltext = "32043-30.htm";
                  }
               } else if (npcId == 32044) {
                  if (cond == 20) {
                     htmltext = "32044-01.htm";
                  } else if (cond == 21) {
                     htmltext = "32044-06.htm";
                  } else if (cond == 22) {
                     htmltext = "32044-18.htm";
                  }
               } else if (npcId == 32045) {
                  if (cond == 14) {
                     htmltext = "32045-01.htm";
                  } else if (cond == 15) {
                     htmltext = "32045-03.htm";
                  }
               }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _120_PavelsResearch(120, "_120_PavelsResearch", "");
   }
}
