package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _413_PathToShillienOracle extends Quest {
   private static final String qn = "_413_PathToShillienOracle";
   private static final int SIDRA = 30330;
   private static final int ADONIUS = 30375;
   private static final int TALBOT = 30377;
   private static final int[] TALKERS = new int[]{30330, 30375, 30377};
   private static final int ZOMBIE_SOLDIER = 20457;
   private static final int ZOMBIE_WARRIOR = 20458;
   private static final int SHIELD_SKELETON = 20514;
   private static final int SKELETON_INFANTRYMAN = 20515;
   private static final int DARK_SUCCUBUS = 20776;
   private static final int[] KILLS = new int[]{20457, 20458, 20514, 20515, 20776};
   private static final int SIDRAS_LETTER1 = 1262;
   private static final int BLANK_SHEET1 = 1263;
   private static final int BLOODY_RUNE1 = 1264;
   private static final int GARMIEL_BOOK = 1265;
   private static final int PRAYER_OF_ADON = 1266;
   private static final int PENITENTS_MARK = 1267;
   private static final int ASHEN_BONES = 1268;
   private static final int ANDARIEL_BOOK = 1269;
   private static final int[] QUESTITEMS = new int[]{1262, 1263, 1264, 1265, 1266, 1267, 1268, 1269};
   private static final int ORB_OF_ABYSS = 1270;

   public _413_PathToShillienOracle(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30330);

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
            st.giveItems(1262, 1L);
            htmltext = "30330-06.htm";
         } else if (event.equalsIgnoreCase("413_1")) {
            if (level >= 18 && classId == 38 && st.getQuestItemsCount(1270) == 0L) {
               htmltext = "30330-05.htm";
            } else if (classId != 38) {
               htmltext = classId == 42 ? "30330-02a.htm" : "30330-03.htm";
            } else if (level < 18 && classId == 38) {
               htmltext = "30330-02.htm";
            } else if (level >= 18 && classId == 38 && st.getQuestItemsCount(1270) == 1L) {
               htmltext = "30330-04.htm";
            }
         } else if (event.equalsIgnoreCase("30377_1")) {
            st.takeItems(1262, 1L);
            st.giveItems(1263, 5L);
            st.set("cond", "2");
            htmltext = "30377-02.htm";
         } else if (event.equalsIgnoreCase("30375_1")) {
            htmltext = "30375-02.htm";
         } else if (event.equalsIgnoreCase("30375_2")) {
            htmltext = "30375-03.htm";
         } else if (event.equalsIgnoreCase("30375_3")) {
            st.takeItems(1266, 1L);
            st.giveItems(1267, 1L);
            st.set("cond", "5");
            htmltext = "30375-04.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      String htmltext = Quest.getNoQuestMsg(talker);
      QuestState st = talker.getQuestState("_413_PathToShillienOracle");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         int cond = st.getInt("cond");
         if (npcId != 30330 && id != 1) {
            return htmltext;
         } else {
            if (npcId == 30330 && cond == 0) {
               htmltext = "30330-01.htm";
            } else if (npcId == 30330 && cond > 0) {
               if (st.getQuestItemsCount(1262) == 1L) {
                  htmltext = "30330-07.htm";
               } else if (st.getQuestItemsCount(1263) > 0L || st.getQuestItemsCount(1264) == 1L) {
                  htmltext = "30330-08.htm";
               } else if (st.getQuestItemsCount(1269) == 0L
                  && st.getQuestItemsCount(1266) + st.getQuestItemsCount(1265) + st.getQuestItemsCount(1267) + st.getQuestItemsCount(1268) > 0L) {
                  htmltext = "30330-09.htm";
               } else if (st.getQuestItemsCount(1269) == 1L && st.getQuestItemsCount(1265) == 1L) {
                  st.takeItems(1269, 1L);
                  st.takeItems(1265, 1L);
                  String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
                  if (isFinished.equalsIgnoreCase("")) {
                     int level = talker.getLevel();
                     if (level >= 20) {
                        st.addExpAndSp(320534, 26532);
                     } else if (level == 19) {
                        st.addExpAndSp(456128, 33230);
                     } else {
                        st.addExpAndSp(591724, 39928);
                     }
                  }

                  st.giveItems(57, 163800L);
                  st.giveItems(1270, 1L);
                  st.saveGlobalQuestVar("1ClassQuestFinished", "1");
                  st.exitQuest(false, true);
                  htmltext = "30330-10.htm";
               }
            } else if (npcId == 30377 && cond > 0) {
               if (st.getQuestItemsCount(1262) == 1L) {
                  htmltext = "30377-01.htm";
               } else if (st.getQuestItemsCount(1263) == 5L && st.getQuestItemsCount(1264) == 0L) {
                  htmltext = "30377-03.htm";
               } else if (st.getQuestItemsCount(1264) > 0L && st.getQuestItemsCount(1264) < 5L) {
                  htmltext = "30377-04.htm";
               } else if (st.getQuestItemsCount(1264) >= 5L) {
                  st.takeItems(1264, st.getQuestItemsCount(1264));
                  st.giveItems(1265, 1L);
                  st.giveItems(1266, 1L);
                  st.set("cond", "4");
                  htmltext = "30377-05.htm";
               } else if (st.getQuestItemsCount(1266) + st.getQuestItemsCount(1267) + st.getQuestItemsCount(1268) > 0L) {
                  htmltext = "30377-06.htm";
               } else if (st.getQuestItemsCount(1269) == 1L && st.getQuestItemsCount(1265) == 1L) {
                  htmltext = "30377-07.htm";
               }
            } else if (npcId == 30375 && cond > 0) {
               if (st.getQuestItemsCount(1266) == 1L) {
                  htmltext = "30375-01.htm";
               } else if (st.getQuestItemsCount(1267) == 1L && st.getQuestItemsCount(1268) == 0L && st.getQuestItemsCount(1269) == 0L) {
                  htmltext = "30375-05.htm";
               } else if (st.getQuestItemsCount(1267) == 1L && st.getQuestItemsCount(1268) < 10L && st.getQuestItemsCount(1268) > 0L) {
                  htmltext = "30375-06.htm";
               } else if (st.getQuestItemsCount(1267) == 1L && st.getQuestItemsCount(1268) >= 10L) {
                  st.takeItems(1268, st.getQuestItemsCount(1268));
                  st.takeItems(1267, st.getQuestItemsCount(1267));
                  st.giveItems(1269, 1L);
                  st.set("cond", "7");
                  htmltext = "30375-07.htm";
               } else if (st.getQuestItemsCount(1269) == 1L) {
                  htmltext = "30375-08.htm";
               }
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_413_PathToShillienOracle");
      if (st == null) {
         return super.onKill(npc, killer, isSummon);
      } else if (st.getState() != 1) {
         return super.onKill(npc, killer, isSummon);
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (npcId == 20776) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1263) > 0L) {
               st.giveItems(1264, 1L);
               st.takeItems(1263, 1L);
               if (st.getQuestItemsCount(1263) == 0L) {
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "3");
               } else {
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20514) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1267) == 1L && st.getQuestItemsCount(1268) < 10L) {
               st.giveItems(1268, 1L);
               if (st.getQuestItemsCount(1268) == 10L) {
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "6");
               } else {
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20515) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1267) == 1L && st.getQuestItemsCount(1268) < 10L) {
               st.giveItems(1268, 1L);
               if (st.getQuestItemsCount(1268) == 10L) {
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "6");
               } else {
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20457) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1267) == 1L && st.getQuestItemsCount(1268) < 10L) {
               st.giveItems(1268, 1L);
               if (st.getQuestItemsCount(1268) == 10L) {
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "6");
               } else {
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20458) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1267) == 1L && st.getQuestItemsCount(1268) < 10L) {
               st.giveItems(1268, 1L);
               if (st.getQuestItemsCount(1268) == 10L) {
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "6");
               } else {
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _413_PathToShillienOracle(413, "_413_PathToShillienOracle", "");
   }
}
