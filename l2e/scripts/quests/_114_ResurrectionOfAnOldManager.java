package l2e.scripts.quests;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.NpcSay;

public final class _114_ResurrectionOfAnOldManager extends Quest {
   private static final String qn = "_114_ResurrectionOfAnOldManager";
   private static final int NEWYEAR = 31961;
   private static final int YUMI = 32041;
   private static final int STONES = 32046;
   private static final int WENDY = 32047;
   private static final int BOX = 32050;
   private static final int GUARDIAN = 27318;
   private static final int DETECTOR = 8090;
   private static final int DETECTOR2 = 8091;
   private static final int STARSTONE = 8287;
   private static final int LETTER = 8288;
   private static final int STARSTONE2 = 8289;
   private final Player GUARDIAN_SPAWN = null;

   public _114_ResurrectionOfAnOldManager(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32041);
      this.addTalkId(32041);
      this.addTalkId(32047);
      this.addTalkId(32050);
      this.addTalkId(32046);
      this.addTalkId(31961);
      this.addFirstTalkId(32046);
      this.addKillId(27318);
      this.questItemIds = new int[]{8090, 8091, 8287, 8288, 8289};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_114_ResurrectionOfAnOldManager");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31961-02.htm")) {
            st.set("cond", "22");
            st.takeItems(8288, 1L);
            st.giveItems(8289, 1L);
            st.playSound("ItemSound.quest_middle");
         }

         if (event.equalsIgnoreCase("32041-02.htm")) {
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.set("cond", "1");
            st.set("talk", "0");
         } else if (event.equalsIgnoreCase("32041-06.htm")) {
            st.set("talk", "1");
         } else if (event.equalsIgnoreCase("32041-07.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.set("talk", "0");
         } else if (event.equalsIgnoreCase("32041-10.htm")) {
            int choice = st.getInt("choice");
            if (choice == 1) {
               htmltext = "32041-10.htm";
            } else if (choice == 2) {
               htmltext = "32041-10a.htm";
            } else if (choice == 3) {
               htmltext = "32041-10b.htm";
            }
         } else if (event.equalsIgnoreCase("32041-11.htm")) {
            st.set("talk", "1");
         } else if (event.equalsIgnoreCase("32041-18.htm")) {
            st.set("talk", "2");
         } else if (event.equalsIgnoreCase("32041-20.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.set("talk", "0");
         } else if (event.equalsIgnoreCase("32041-25.htm")) {
            st.set("cond", "17");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(8090, 1L);
         } else if (event.equalsIgnoreCase("32041-28.htm")) {
            st.takeItems(8091, 1L);
            st.set("talk", "1");
         } else if (event.equalsIgnoreCase("32041-31.htm")) {
            int choice = st.getInt("choice");
            if (choice > 1) {
               htmltext = "32041-37.htm";
            }
         } else if (event.equalsIgnoreCase("32041-32.htm")) {
            st.set("cond", "21");
            st.giveItems(8288, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32041-36.htm")) {
            st.set("cond", "20");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32046-02.htm")) {
            st.set("cond", "19");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32046-06.htm")) {
            st.addExpAndSp(1846611, 144270);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
         } else if (event.equalsIgnoreCase("32047-01.htm")) {
            if (st.getInt("talk") + st.getInt("talk1") == 2) {
               htmltext = "32047-04.htm";
            } else if (st.getInt("talk") + st.getInt("talk1") + st.getInt("talk2") == 6) {
               htmltext = "32047-08.htm";
            }
         } else if (event.equalsIgnoreCase("32047-02.htm")) {
            if (st.getInt("talk") == 0) {
               st.set("talk", "1");
            }
         } else if (event.equalsIgnoreCase("32047-03.htm")) {
            if (st.getInt("talk1") == 0) {
               st.set("talk1", "1");
            }
         } else if (event.equalsIgnoreCase("32047-05.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.set("talk", "0");
            st.set("choice", "1");
            st.unset("talk1");
         } else if (event.equalsIgnoreCase("32047-06.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.set("talk", "0");
            st.set("choice", "2");
            st.unset("talk1");
         } else if (event.equalsIgnoreCase("32047-07.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.set("talk", "0");
            st.set("choice", "3");
            st.unset("talk1");
         } else if (event.equalsIgnoreCase("32047-13.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32047-13a.htm")) {
            st.set("cond", "10");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32047-15.htm")) {
            if (st.getInt("talk") == 0) {
               st.set("talk", "1");
            }
         } else if (event.equalsIgnoreCase("32047-15a.htm")) {
            if (this.GUARDIAN_SPAWN != null && this.GUARDIAN_SPAWN.isVisible()) {
               htmltext = "32047-19a.htm";
            } else {
               Npc GUARDIAN_SPAWN = st.addSpawn(27318, 96977, -110625, -3280, 0, false, 900000);
               NpcSay ns = new NpcSay(GUARDIAN_SPAWN.getObjectId(), 0, GUARDIAN_SPAWN.getId(), NpcStringId.YOU_S1_YOU_ATTACKED_WENDY_PREPARE_TO_DIE);
               ns.addStringParameter(player.getName());
               GUARDIAN_SPAWN.broadcastPacket(ns, 2000);
               GUARDIAN_SPAWN.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, player, Integer.valueOf(999));
            }
         } else if (event.equalsIgnoreCase("32047-17a.htm")) {
            st.set("cond", "12");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32047-20.htm")) {
            st.set("talk", "2");
         } else if (event.equalsIgnoreCase("32047-23.htm")) {
            st.set("cond", "13");
            st.playSound("ItemSound.quest_middle");
            st.set("talk", "0");
         } else if (event.equalsIgnoreCase("32047-25.htm")) {
            st.set("cond", "15");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(8287, 1L);
         } else if (event.equalsIgnoreCase("32047-30.htm")) {
            st.set("talk", "2");
         } else if (event.equalsIgnoreCase("32047-33.htm")) {
            if (st.getInt("cond") == 7) {
               st.set("cond", "8");
               st.set("talk", "0");
               st.playSound("ItemSound.quest_middle");
            } else if (st.getInt("cond") == 8) {
               st.set("cond", "9");
               st.playSound("ItemSound.quest_middle");
               htmltext = "32047-34.htm";
            }
         } else if (event.equalsIgnoreCase("32047-34.htm")) {
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32047-38.htm")) {
            st.giveItems(8289, 1L);
            st.takeItems(57, 3000L);
            st.set("cond", "26");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32050-02.htm")) {
            st.playSound("ItemSound.armor_wood_3");
            st.set("talk", "1");
         } else if (event.equalsIgnoreCase("32050-04.htm")) {
            st.set("cond", "14");
            st.giveItems(8287, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("talk", "0");
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_114_ResurrectionOfAnOldManager");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         int talk = st.getInt("talk");
         int talk1 = st.getInt("talk1");
         switch(st.getState()) {
            case 0:
               QuestState qs = player.getQuestState("_121_PavelTheGiant");
               if (player.getLevel() >= 70 && qs != null && qs.isCompleted()) {
                  htmltext = "32041-01.htm";
               } else {
                  htmltext = "32041-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (npcId == 32041) {
                  if (cond == 1) {
                     if (talk == 0) {
                        htmltext = "32041-02.htm";
                     } else {
                        htmltext = "32041-06.htm";
                     }
                  } else if (cond == 2) {
                     htmltext = "32041-08.htm";
                  } else if (cond != 3 && cond != 4 && cond != 5) {
                     if (cond == 6) {
                        htmltext = "32041-21.htm";
                     } else if (cond == 9 || cond == 12 || cond == 16) {
                        htmltext = "32041-22.htm";
                     } else if (cond == 17) {
                        htmltext = "32041-26.htm";
                     } else if (cond == 19) {
                        if (talk == 0) {
                           htmltext = "32041-27.htm";
                        } else {
                           htmltext = "32041-28.htm";
                        }
                     } else if (cond == 20) {
                        htmltext = "32041-36.htm";
                     } else if (cond == 21) {
                        htmltext = "32041-33.htm";
                     } else if (cond == 22 || cond == 26) {
                        htmltext = "32041-34.htm";
                        st.set("cond", "27");
                        st.playSound("ItemSound.quest_middle");
                     } else if (cond == 27) {
                        htmltext = "32041-35.htm";
                     }
                  } else if (talk == 0) {
                     htmltext = "32041-09.htm";
                  } else if (talk == 1) {
                     htmltext = "32041-11.htm";
                  } else {
                     htmltext = "32041-18.htm";
                  }
               } else if (npcId == 32047) {
                  if (cond == 2) {
                     if (talk + talk1 < 2) {
                        htmltext = "32047-01.htm";
                     } else if (talk + talk1 == 2) {
                        htmltext = "32047-04.htm";
                     }
                  } else if (cond == 3) {
                     htmltext = "32047-09.htm";
                  } else if (cond == 4 || cond == 5) {
                     htmltext = "32047-09a.htm";
                  } else if (cond == 6) {
                     int choice = st.getInt("choice");
                     if (choice == 1) {
                        if (talk == 0) {
                           htmltext = "32047-10.htm";
                        } else if (talk == 1) {
                           htmltext = "32047-20.htm";
                        } else {
                           htmltext = "32047-30.htm";
                        }
                     } else if (choice == 2) {
                        htmltext = "32047-10a.htm";
                     } else if (choice == 3) {
                        if (talk == 0) {
                           htmltext = "32047-14.htm";
                        } else if (talk == 1) {
                           htmltext = "32047-15.htm";
                        } else {
                           htmltext = "32047-20.htm";
                        }
                     }
                  } else if (cond == 7) {
                     if (talk == 0) {
                        htmltext = "32047-14.htm";
                     } else if (talk == 1) {
                        htmltext = "32047-15.htm";
                     } else {
                        htmltext = "32047-20.htm";
                     }
                  } else if (cond == 8) {
                     htmltext = "32047-30.htm";
                  } else if (cond == 9) {
                     htmltext = "32047-27.htm";
                  } else if (cond == 10) {
                     htmltext = "32047-14a.htm";
                  } else if (cond == 11) {
                     htmltext = "32047-16a.htm";
                  } else if (cond == 12) {
                     htmltext = "32047-18a.htm";
                  } else if (cond == 13) {
                     htmltext = "32047-23.htm";
                  } else if (cond == 14) {
                     htmltext = "32047-24.htm";
                  } else if (cond == 15) {
                     htmltext = "32047-26.htm";
                     st.set("cond", "16");
                     st.playSound("ItemSound.quest_middle");
                  } else if (cond == 16) {
                     htmltext = "32047-27.htm";
                  } else if (cond == 20) {
                     htmltext = "32047-35.htm";
                  } else if (cond == 26) {
                     htmltext = "32047-40.htm";
                  }
               } else if (npcId == 32050) {
                  if (cond == 13) {
                     if (talk == 0) {
                        htmltext = "32050-01.htm";
                     } else {
                        htmltext = "32050-03.htm";
                     }
                  } else if (cond == 14) {
                     htmltext = "32050-05.htm";
                  }
               } else if (npcId == 32046) {
                  if (cond == 18) {
                     htmltext = "32046-01.htm";
                  } else if (cond == 19) {
                     htmltext = "32046-02.htm";
                  } else if (cond == 27) {
                     htmltext = "32046-03.htm";
                  }
               } else if (npcId == 31961) {
                  if (cond == 21) {
                     htmltext = "31961-01.htm";
                  } else if (cond == 22) {
                     htmltext = "31961-03.htm";
                  }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_114_ResurrectionOfAnOldManager");
      if (npc.getId() == 32046 && st != null && st.getInt("cond") == 17) {
         st.playSound("ItemSound.quest_middle");
         st.takeItems(8090, 1L);
         st.giveItems(8091, 1L);
         st.set("cond", "18");
         player.sendPacket(new ExShowScreenMessage(NpcStringId.THE_RADIO_SIGNAL_DETECTOR_IS_RESPONDING_A_SUSPICIOUS_PILE_OF_STONES_CATCHES_YOUR_EYE, 2, 4500));
      }

      npc.showChatWindow(player);
      return "";
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_114_ResurrectionOfAnOldManager");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         if (st.getInt("cond") == 10 && npcId == 27318) {
            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npcId, NpcStringId.THIS_ENEMY_IS_FAR_TOO_POWERFUL_FOR_ME_TO_FIGHT_I_MUST_WITHDRAW), 2000);
            st.set("cond", "11");
            st.playSound("ItemSound.quest_middle");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _114_ResurrectionOfAnOldManager(114, "_114_ResurrectionOfAnOldManager", "");
   }
}
