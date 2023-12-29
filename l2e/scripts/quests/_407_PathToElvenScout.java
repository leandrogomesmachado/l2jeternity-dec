package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.SocialAction;

public class _407_PathToElvenScout extends Quest {
   private static final String qn = "_407_PathToElvenScout";
   private static final int REISA = 30328;
   private static final int BABENCO = 30334;
   private static final int MORETTI = 30337;
   private static final int PRIAS = 30426;
   private static final int[] TALKERS = new int[]{30328, 30334, 30337, 30426};
   private static final int OL_MAHUM_SENTRY = 27031;
   private static final int OL_MAHUM_PATROL = 20053;
   private static final int[] MOBS = new int[]{27031, 20053};
   private static final int REORIA_LETTER2 = 1207;
   private static final int PRIGUNS_TEAR_LETTER1 = 1208;
   private static final int PRIGUNS_TEAR_LETTER2 = 1209;
   private static final int PRIGUNS_TEAR_LETTER3 = 1210;
   private static final int PRIGUNS_TEAR_LETTER4 = 1211;
   private static final int MORETTIS_HERB = 1212;
   private static final int MORETTIS_LETTER = 1214;
   private static final int PRIGUNS_LETTER = 1215;
   private static final int HONORARY_GUARD = 1216;
   private static final int RUSTED_KEY = 1293;
   private static final int REORIA_RECOMMENDATION = 1217;
   private static final int[] QUESTITEMS = new int[]{1207, 1208, 1209, 1210, 1211, 1212, 1214, 1215, 1216, 1293};

   public _407_PathToElvenScout(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30328);

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
      QuestState st = player.getQuestState("_407_PathToElvenScout");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            st.set("id", "0");
            if (player.getClassId().getId() == 18) {
               if (player.getLevel() >= 18) {
                  if (st.getQuestItemsCount(1217) > 0L) {
                     htmltext = "30328-04.htm";
                  } else {
                     st.giveItems(1207, 1L);
                     st.set("cond", "1");
                     st.setState((byte)1);
                     st.playSound("ItemSound.quest_accept");
                     htmltext = "30328-05.htm";
                  }
               } else {
                  htmltext = "30328-03.htm";
               }
            } else {
               htmltext = player.getClassId().getId() == 22 ? "30328-02a.htm" : "30328-02.htm";
            }
         } else if (event.equalsIgnoreCase("30337_1")) {
            st.takeItems(1207, 1L);
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            htmltext = "30337-03.htm";
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player talker) {
      String htmltext = getNoQuestMsg(talker);
      QuestState st = talker.getQuestState("_407_PathToElvenScout");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         int cond = st.getInt("cond");
         if (npcId != 30328 && id != 1) {
            return htmltext;
         } else {
            if (npcId == 30328 && cond == 0) {
               htmltext = "30328-01.htm";
            } else if (npcId == 30328 && cond > 0 && st.getQuestItemsCount(1207) > 0L) {
               htmltext = "30328-06.htm";
            } else if (npcId == 30328 && cond > 0 && st.getQuestItemsCount(1207) == 0L && st.getQuestItemsCount(1216) == 0L) {
               htmltext = "30328-08.htm";
            } else if (npcId == 30337
               && cond > 0
               && st.getQuestItemsCount(1207) > 0L
               && st.getQuestItemsCount(1208) + st.getQuestItemsCount(1209) + st.getQuestItemsCount(1210) + st.getQuestItemsCount(1211) == 0L) {
               htmltext = "30337-01.htm";
            } else if (npcId == 30337 && st.getQuestItemsCount(1214) < 1L && st.getQuestItemsCount(1215) == 0L && st.getQuestItemsCount(1216) == 0L) {
               if (st.getQuestItemsCount(1208) + st.getQuestItemsCount(1209) + st.getQuestItemsCount(1210) + st.getQuestItemsCount(1211) < 1L) {
                  htmltext = "30337-04.htm";
               } else if (st.getQuestItemsCount(1208) + st.getQuestItemsCount(1209) + st.getQuestItemsCount(1210) + st.getQuestItemsCount(1211) > 0L
                  && st.getQuestItemsCount(1208) + st.getQuestItemsCount(1209) + st.getQuestItemsCount(1210) + st.getQuestItemsCount(1211) < 4L) {
                  htmltext = "30337-05.htm";
               } else {
                  st.takeItems(1208, 1L);
                  st.takeItems(1209, 1L);
                  st.takeItems(1210, 1L);
                  st.takeItems(1211, 1L);
                  st.giveItems(1212, 1L);
                  st.giveItems(1214, 1L);
                  st.set("cond", "4");
                  st.playSound("ItemSound.quest_middle");
                  htmltext = "30337-06.htm";
               }
            } else if (npcId == 30334 && cond > 0) {
               htmltext = "30334-01.htm";
            } else if (npcId == 30426 && cond > 0 && st.getQuestItemsCount(1214) > 0L && st.getQuestItemsCount(1212) > 0L) {
               if (st.getQuestItemsCount(1293) < 1L) {
                  st.set("cond", "5");
                  st.playSound("ItemSound.quest_middle");
                  htmltext = "30426-01.htm";
               } else {
                  st.takeItems(1293, 1L);
                  st.takeItems(1212, 1L);
                  st.takeItems(1214, 1L);
                  st.giveItems(1215, 1L);
                  st.set("cond", "7");
                  st.playSound("ItemSound.quest_middle");
                  htmltext = "30426-02.htm";
               }
            } else if (npcId == 30426 && cond > 0 && st.getQuestItemsCount(1215) > 0L) {
               htmltext = "30426-04.htm";
            } else if (npcId == 30337 && cond > 0 && st.getQuestItemsCount(1215) > 0L) {
               if (st.getQuestItemsCount(1212) > 0L) {
                  htmltext = "30337-09.htm";
               } else {
                  st.takeItems(1215, 1L);
                  st.giveItems(1216, 1L);
                  st.set("cond", "8");
                  st.playSound("ItemSound.quest_middle");
                  htmltext = "30337-07.htm";
               }
            } else if (npcId == 30337 && cond > 0 && st.getQuestItemsCount(1216) > 0L) {
               htmltext = "30337-08.htm";
            } else if (npcId == 30328 && cond > 0 && st.getQuestItemsCount(1216) > 0L) {
               st.takeItems(1216, 1L);
               String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
               if (isFinished.equalsIgnoreCase("")) {
                  st.addExpAndSp(160267, 1910);
               }

               st.giveItems(1217, 1L);
               st.giveItems(57, 163800L);
               st.saveGlobalQuestVar("1ClassQuestFinished", "1");
               st.set("cond", "0");
               talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
               st.exitQuest(false);
               st.playSound("ItemSound.quest_finish");
               htmltext = "30328-07.htm";
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_407_PathToElvenScout");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (npcId == 20053) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1208) + st.getQuestItemsCount(1209) + st.getQuestItemsCount(1210) + st.getQuestItemsCount(1211) < 4L) {
               if (st.getQuestItemsCount(1208) < 1L) {
                  st.giveItems(1208, 1L);
                  if (st.getQuestItemsCount(1208) + st.getQuestItemsCount(1209) + st.getQuestItemsCount(1210) + st.getQuestItemsCount(1211) == 4L) {
                     st.playSound("ItemSound.quest_middle");
                     st.set("cond", "3");
                  } else {
                     st.playSound("ItemSound.quest_itemget");
                  }
               } else if (st.getQuestItemsCount(1209) < 1L) {
                  st.giveItems(1209, 1L);
                  if (st.getQuestItemsCount(1208) + st.getQuestItemsCount(1209) + st.getQuestItemsCount(1210) + st.getQuestItemsCount(1211) == 4L) {
                     st.playSound("ItemSound.quest_middle");
                     st.set("cond", "3");
                  } else {
                     st.playSound("ItemSound.quest_itemget");
                  }
               } else if (st.getQuestItemsCount(1210) < 1L) {
                  st.giveItems(1210, 1L);
                  if (st.getQuestItemsCount(1208) + st.getQuestItemsCount(1209) + st.getQuestItemsCount(1210) + st.getQuestItemsCount(1211) == 4L) {
                     st.playSound("ItemSound.quest_middle");
                     st.set("cond", "3");
                  } else {
                     st.playSound("ItemSound.quest_itemget");
                  }
               } else if (st.getQuestItemsCount(1211) < 1L) {
                  st.giveItems(1211, 1L);
                  if (st.getQuestItemsCount(1208) + st.getQuestItemsCount(1209) + st.getQuestItemsCount(1210) + st.getQuestItemsCount(1211) == 4L) {
                     st.playSound("ItemSound.quest_middle");
                     st.set("cond", "3");
                  } else {
                     st.playSound("ItemSound.quest_itemget");
                  }
               }
            }
         } else if (npcId == 27031) {
            st.set("id", "0");
            if (cond > 0
               && st.getQuestItemsCount(1212) == 1L
               && st.getQuestItemsCount(1214) == 1L
               && st.getQuestItemsCount(1293) == 0L
               && st.getRandom(10) < 6) {
               st.giveItems(1293, 1L);
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "6");
            }
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _407_PathToElvenScout(407, "_407_PathToElvenScout", "");
   }
}
