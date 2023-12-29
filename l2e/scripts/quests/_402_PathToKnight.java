package l2e.scripts.quests;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _402_PathToKnight extends Quest {
   private static final String qn = "_402_PathToKnight";
   private static final int SIR_KLAUS_VASPER = 30417;
   private static final int BIOTIN = 30031;
   private static final int LEVIAN = 30037;
   private static final int GILBERT = 30039;
   private static final int RAYMOND = 30289;
   private static final int SIR_COLLIN_WINDAWOOD = 30311;
   private static final int BATHIS = 30332;
   private static final int BEZIQUE = 30379;
   private static final int SIR_ARON_TANFORD = 30653;
   private static final int[] TALKERS = new int[]{30417, 30031, 30037, 30039, 30289, 30311, 30332, 30379, 30653};
   private static final int BUGBEAR_RAIDER = 20775;
   private static final int UNDEAD_PRIEST = 27024;
   private static final int VENOMOUS_SPIDER = 20038;
   private static final int ARACHNID_TRACKER = 20043;
   private static final int ARACHNID_PREDATOR = 20050;
   private static final int LANGK_LIZARDMAN = 20030;
   private static final int LANGK_LIZARDMAN_SCOUT = 20027;
   private static final int LANGK_LIZARDMAN_WARRIOR = 20024;
   private static final int GIANT_SPIDER = 20103;
   private static final int TALON_SPIDER = 20106;
   private static final int BLADE_SPIDER = 20108;
   private static final int SILENT_HORROR = 20404;
   private static final int[] MOBS = new int[]{20775, 27024, 20038, 20043, 20050, 20030, 20027, 20024, 20103, 20106, 20108, 20404};
   private static final int MARK_OF_ESQUIRE = 1271;
   private static final int COIN_OF_LORDS1 = 1162;
   private static final int COIN_OF_LORDS2 = 1163;
   private static final int COIN_OF_LORDS3 = 1164;
   private static final int COIN_OF_LORDS4 = 1165;
   private static final int COIN_OF_LORDS5 = 1166;
   private static final int COIN_OF_LORDS6 = 1167;
   private static final int GLUDIO_GUARDS_MARK1 = 1168;
   private static final int BUGBEAR_NECKLACE = 1169;
   private static final int EINHASAD_CHURCH_MARK1 = 1170;
   private static final int EINHASAD_CRUCIFIX = 1171;
   private static final int GLUDIO_GUARDS_MARK2 = 1172;
   private static final int POISON_SPIDER_LEG1 = 1173;
   private static final int EINHASAD_CHURCH_MARK2 = 1174;
   private static final int LIZARDMAN_TOTEM = 1175;
   private static final int GLUDIO_GUARDS_MARK3 = 1176;
   private static final int GIANT_SPIDER_HUSK = 1177;
   private static final int EINHASAD_CHURCH_MARK3 = 1178;
   private static final int HORRIBLE_SKULL = 1179;
   private static final int[] QUESTITEMS = new int[]{
      1162, 1163, 1164, 1165, 1166, 1167, 1168, 1169, 1170, 1171, 1172, 1173, 1174, 1175, 1176, 1177, 1178, 1179
   };
   private static Map<Integer, int[]> DROPLIST = new HashMap<>();
   private static final int SWORD_OF_RITUAL = 1161;

   public _402_PathToKnight(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30417);

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
      QuestState st = player.getQuestState("_402_PathToKnight");
      if (st == null) {
         return event;
      } else {
         int classId = player.getClassId().getId();
         int level = player.getLevel();
         long squire = st.getQuestItemsCount(1271);
         long coin1 = st.getQuestItemsCount(1162);
         long coin2 = st.getQuestItemsCount(1163);
         long coin3 = st.getQuestItemsCount(1164);
         long coin4 = st.getQuestItemsCount(1165);
         long coin5 = st.getQuestItemsCount(1166);
         long coin6 = st.getQuestItemsCount(1167);
         long guards_mark1 = st.getQuestItemsCount(1168);
         long guards_mark2 = st.getQuestItemsCount(1172);
         long guards_mark3 = st.getQuestItemsCount(1176);
         long church_mark1 = st.getQuestItemsCount(1170);
         long church_mark2 = st.getQuestItemsCount(1174);
         long church_mark3 = st.getQuestItemsCount(1178);
         if (event.equalsIgnoreCase("30417-02a.htm")) {
            if (classId == 0) {
               if (level >= 18) {
                  htmltext = st.getQuestItemsCount(1161) > 0L ? "30417-04.htm" : "30417-05.htm";
               } else {
                  htmltext = "30417-02.htm";
                  st.exitQuest(true);
               }
            } else if (classId != 4) {
               htmltext = "30417-03.htm";
               st.exitQuest(true);
            }
         } else if (event.equalsIgnoreCase("30417-08.htm")) {
            if (st.getInt("cond") == 0 && classId == 0 && level >= 18) {
               st.set("id", "0");
               st.set("cond", "1");
               st.setState((byte)1);
               st.playSound("ItemSound.quest_accept");
               st.giveItems(1271, 1L);
            } else {
               htmltext = getNoQuestMsg(player);
            }
         } else if (event.equalsIgnoreCase("30332-02.htm")) {
            if (squire > 0L && guards_mark1 == 0L && coin1 == 0L) {
               st.giveItems(1168, 1L);
            } else {
               htmltext = getNoQuestMsg(player);
            }
         } else if (event.equalsIgnoreCase("30289-03.htm")) {
            if (squire > 0L && church_mark1 == 0L && coin2 == 0L) {
               st.giveItems(1170, 1L);
            } else {
               htmltext = getNoQuestMsg(player);
            }
         } else if (event.equalsIgnoreCase("30379-02.htm")) {
            if (squire > 0L && guards_mark2 == 0L && coin3 == 0L) {
               st.giveItems(1172, 1L);
            } else {
               htmltext = getNoQuestMsg(player);
            }
         } else if (event.equalsIgnoreCase("30037-02.htm")) {
            if (squire > 0L && church_mark2 == 0L && coin4 == 0L) {
               st.giveItems(1174, 1L);
            } else {
               htmltext = getNoQuestMsg(player);
            }
         } else if (event.equalsIgnoreCase("30039-02.htm")) {
            if (squire > 0L && guards_mark3 == 0L && coin5 == 0L) {
               st.giveItems(1176, 1L);
            } else {
               htmltext = getNoQuestMsg(player);
            }
         } else if (event.equalsIgnoreCase("30031-02.htm")) {
            if (squire > 0L && church_mark3 == 0L && coin6 == 0L) {
               st.giveItems(1178, 1L);
            } else {
               htmltext = getNoQuestMsg(player);
            }
         } else if (event.equalsIgnoreCase("30417-13.htm")) {
            if (squire > 0L && coin1 + coin2 + coin3 + coin4 + coin5 + coin6 >= 3L) {
               st.saveGlobalQuestVar("1ClassQuestFinished", "1");
               st.set("cond", "0");

               for(int item : this.questItemIds) {
                  st.takeItems(item, -1L);
               }

               st.takeItems(1271, -1L);
               st.addExpAndSp(3200, 2450);
               st.giveItems(57, 163800L);
               st.giveItems(1161, 1L);
               st.exitQuest(false);
               st.playSound("ItemSound.quest_finish");
            } else {
               htmltext = getNoQuestMsg(player);
            }
         } else if (event.equalsIgnoreCase("30417-14.htm")) {
            if (squire > 0L && coin1 + coin2 + coin3 + coin4 + coin5 + coin6 >= 3L) {
               st.set("cond", "0");

               for(int item : this.questItemIds) {
                  st.takeItems(item, -1L);
               }

               st.takeItems(1271, -1L);
               st.addExpAndSp(3200, 2450);
               st.giveItems(1161, 1L);
               st.exitQuest(false);
               st.playSound("ItemSound.quest_finish");
            } else {
               htmltext = getNoQuestMsg(player);
            }
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player talker) {
      String htmltext = getNoQuestMsg(talker);
      QuestState st = talker.getQuestState("_402_PathToKnight");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         if (npcId != 30417 && id != 1) {
            return htmltext;
         } else {
            long squire = st.getQuestItemsCount(1271);
            long coin1 = st.getQuestItemsCount(1162);
            long coin2 = st.getQuestItemsCount(1163);
            long coin3 = st.getQuestItemsCount(1164);
            long coin4 = st.getQuestItemsCount(1165);
            long coin5 = st.getQuestItemsCount(1166);
            long coin6 = st.getQuestItemsCount(1167);
            long guards_mark1 = st.getQuestItemsCount(1168);
            long guards_mark2 = st.getQuestItemsCount(1172);
            long guards_mark3 = st.getQuestItemsCount(1176);
            long church_mark1 = st.getQuestItemsCount(1170);
            long church_mark2 = st.getQuestItemsCount(1174);
            long church_mark3 = st.getQuestItemsCount(1178);
            long coin_count = coin1 + coin2 + coin3 + coin4 + coin5 + coin6;
            int cond = st.getInt("cond");
            if (id == 2) {
               htmltext = getAlreadyCompletedMsg(talker);
            } else if (npcId == 30417) {
               if (cond == 0) {
                  htmltext = "30417-01.htm";
               } else if (cond == 1 && squire > 0L) {
                  if (coin_count < 3L) {
                     htmltext = "30417-09.htm";
                  } else if (coin_count == 3L) {
                     htmltext = "30417-10.htm";
                  } else if (coin_count < 6L) {
                     htmltext = "30417-11.htm";
                  } else if (coin_count == 6L) {
                     htmltext = "30417-12.htm";
                     st.set("cond", "0");

                     for(int item : this.questItemIds) {
                        st.takeItems(item, -1L);
                     }

                     st.takeItems(1271, -1L);
                     st.addExpAndSp(3200, 2450);
                     st.giveItems(1161, 1L);
                     st.exitQuest(false);
                     st.playSound("ItemSound.quest_finish");
                  }
               }
            } else if (npcId == 30332 && cond == 1 && squire > 0L) {
               if (guards_mark1 == 0L && coin1 == 0L) {
                  htmltext = "30332-01.htm";
               } else if (guards_mark1 > 0L) {
                  if (st.getQuestItemsCount(1169) < 10L) {
                     htmltext = "30332-03.htm";
                  } else {
                     htmltext = "30332-04.htm";
                     st.takeItems(1169, -1L);
                     st.takeItems(1168, 1L);
                     st.giveItems(1162, 1L);
                  }
               } else if (coin1 > 0L) {
                  htmltext = "30332-05.htm";
               }
            } else if (npcId == 30289 && cond == 1 && squire > 0L) {
               if (church_mark1 == 0L && coin2 == 0L) {
                  htmltext = "30289-01.htm";
               } else if (church_mark1 > 0L) {
                  if (st.getQuestItemsCount(1171) < 12L) {
                     htmltext = "30289-04.htm";
                  } else {
                     htmltext = "30289-05.htm";
                     st.takeItems(1171, -1L);
                     st.takeItems(1170, 1L);
                     st.giveItems(1163, 1L);
                  }
               } else if (coin2 > 0L) {
                  htmltext = "30289-06.htm";
               }
            } else if (npcId == 30379 && cond == 1 && squire > 0L) {
               if (coin3 == 0L && guards_mark2 == 0L) {
                  htmltext = "30379-01.htm";
               } else if (guards_mark2 > 0L) {
                  if (st.getQuestItemsCount(1173) < 20L) {
                     htmltext = "30379-03.htm";
                  } else {
                     htmltext = "30379-04.htm";
                     st.takeItems(1173, -1L);
                     st.takeItems(1172, 1L);
                     st.giveItems(1164, 1L);
                  }
               } else if (coin3 > 0L) {
                  htmltext = "30379-05.htm";
               }
            } else if (npcId == 30037 && cond == 1 && squire > 0L) {
               if (coin4 == 0L && church_mark2 == 0L) {
                  htmltext = "30037-01.htm";
               } else if (church_mark2 > 0L) {
                  if (st.getQuestItemsCount(1175) < 20L) {
                     htmltext = "30037-03.htm";
                  } else {
                     htmltext = "30037-04.htm";
                     st.takeItems(1175, -1L);
                     st.takeItems(1174, 1L);
                     st.giveItems(1165, 1L);
                  }
               } else if (coin4 > 0L) {
                  htmltext = "3007-05.htm";
               }
            } else if (npcId == 30039 && cond == 1 && squire > 0L) {
               if (guards_mark3 == 0L && coin5 == 0L) {
                  htmltext = "30039-01.htm";
               } else if (guards_mark3 > 0L) {
                  if (st.getQuestItemsCount(1177) < 20L) {
                     htmltext = "30039-03.htm";
                  } else {
                     htmltext = "30039-04.htm";
                     st.takeItems(1177, -1L);
                     st.takeItems(1176, 1L);
                     st.giveItems(1166, 1L);
                  }
               } else if (coin5 > 0L) {
                  htmltext = "30039-05.htm";
               }
            } else if (npcId == 30031 && cond == 1 && squire > 0L) {
               if (church_mark3 == 0L && coin6 == 0L) {
                  htmltext = "30031-01.htm";
               } else if (church_mark3 > 0L) {
                  if (st.getQuestItemsCount(1179) < 10L) {
                     htmltext = "30031-03.htm";
                  } else {
                     htmltext = "30031-04.htm";
                     st.takeItems(1179, -1L);
                     st.takeItems(1178, 1L);
                     st.giveItems(1167, 1L);
                  }
               } else if (coin6 > 0L) {
                  htmltext = "30031-05.htm";
               }
            } else if (npcId == 30311 && cond == 1 && squire > 0L) {
               htmltext = "30311-01.htm";
            } else if (npcId == 30653 && cond == 1 && squire > 0L) {
               htmltext = "30653-01.htm";
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_402_PathToKnight");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") > 0) {
            int npcId = npc.getId();
            int item_required = DROPLIST.get(npcId)[0];
            int item = DROPLIST.get(npcId)[1];
            int max = DROPLIST.get(npcId)[2];
            int chance = DROPLIST.get(npcId)[3];
            if (st.getQuestItemsCount(item_required) > 0L && st.getQuestItemsCount(item) < (long)max && st.getRandom(100) < chance) {
               st.giveItems(item, 1L);
               st.playSound(st.getQuestItemsCount(item) == (long)max ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
            }
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _402_PathToKnight(402, "_402_PathToKnight", "");
   }

   static {
      DROPLIST.put(20775, new int[]{1168, 1169, 10, 100});
      DROPLIST.put(27024, new int[]{1170, 1171, 12, 100});
      DROPLIST.put(20038, new int[]{1172, 1173, 20, 100});
      DROPLIST.put(20043, new int[]{1172, 1173, 20, 100});
      DROPLIST.put(20050, new int[]{1172, 1173, 20, 100});
      DROPLIST.put(20030, new int[]{1174, 1175, 20, 50});
      DROPLIST.put(20027, new int[]{1174, 1175, 20, 100});
      DROPLIST.put(20024, new int[]{1174, 1175, 20, 100});
      DROPLIST.put(20103, new int[]{1176, 1177, 20, 40});
      DROPLIST.put(20106, new int[]{1176, 1177, 20, 40});
      DROPLIST.put(20108, new int[]{1176, 1177, 20, 40});
      DROPLIST.put(20404, new int[]{1178, 1179, 10, 100});
   }
}
