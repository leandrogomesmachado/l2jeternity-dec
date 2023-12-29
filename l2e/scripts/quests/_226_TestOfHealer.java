package l2e.scripts.quests;

import java.util.HashMap;
import java.util.Map;
import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _226_TestOfHealer extends Quest {
   private static final int Bandellos = 30473;
   private static final int Perrin = 30428;
   private static final int OrphanGirl = 30659;
   private static final int Allana = 30424;
   private static final int FatherGupu = 30658;
   private static final int Windy = 30660;
   private static final int Sorius = 30327;
   private static final int Daurin = 30674;
   private static final int Piper = 30662;
   private static final int Slein = 30663;
   private static final int Kein = 30664;
   private static final int MysteryDarkElf = 30661;
   private static final int Kristina = 30665;
   private static final int REPORT_OF_PERRIN_ID = 2810;
   private static final int CRISTINAS_LETTER_ID = 2811;
   private static final int PICTURE_OF_WINDY_ID = 2812;
   private static final int GOLDEN_STATUE_ID = 2813;
   private static final int WINDYS_PEBBLES_ID = 2814;
   private static final int ORDER_OF_SORIUS_ID = 2815;
   private static final int SECRET_LETTER1_ID = 2816;
   private static final int SECRET_LETTER2_ID = 2817;
   private static final int SECRET_LETTER3_ID = 2818;
   private static final int SECRET_LETTER4_ID = 2819;
   private static final int MARK_OF_HEALER_ID = 2820;
   private static Map<Integer, Integer[]> DROPLIST = new HashMap<>();

   public _226_TestOfHealer(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30473);
      this.addTalkId(new int[]{30327, 30424, 30428, 30473, 30658, 30659, 30660, 30661, 30662, 30663, 30664, 30665, 30674});
      this.addKillId(new int[]{20150, 27123, 27124, 27125, 27127, 27134});
      this.questItemIds = new int[]{2810, 2811, 2812, 2813, 2814, 2815, 2816, 2817, 2818, 2819};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            htmltext = "30473-04.htm";
            st.startQuest();
            st.giveItems(2810, 1L);
         } else if (event.equalsIgnoreCase("30473_1")) {
            htmltext = "30473-08.htm";
         } else if (event.equalsIgnoreCase("30473_2")) {
            htmltext = "30473-09.htm";
            st.takeItems(2813, -1L);
            st.giveItems(2820, 1L);
            st.addExpAndSp(1476566, 101324);
            st.giveItems(57, 266980L);
            if (player.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
               st.giveItems(7562, 60L);
               player.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
            }

            st.exitQuest(false, true);
         } else if (event.equalsIgnoreCase("30428_1")) {
            htmltext = "30428-02.htm";
            st.setCond(2, true);
            st.addSpawn(27134);
         } else if (event.equalsIgnoreCase("30658_1")) {
            if (st.getQuestItemsCount(57) >= 100000L) {
               htmltext = "30658-02.htm";
               st.takeItems(57, 100000L);
               st.giveItems(2812, 1L);
               st.setCond(7, true);
            } else {
               htmltext = "30658-05.htm";
            }
         } else if (event.equalsIgnoreCase("30658_2")) {
            st.setCond(6, true);
            htmltext = "30658-03.htm";
         } else if (event.equalsIgnoreCase("30660-03.htm")) {
            st.takeItems(2812, 1L);
            st.giveItems(2814, 1L);
            st.setCond(8, true);
         } else if (event.equalsIgnoreCase("30674_1")) {
            htmltext = "30674-02.htm";
            st.takeItems(2815, 1L);
            st.addSpawn(27122);
            st.addSpawn(27122);
            st.addSpawn(27123);
            st.setCond(11);
            st.playSound("Itemsound.quest_before_battle");
         } else if (event.equalsIgnoreCase("30665_1")) {
            htmltext = "30665-02.htm";
            st.takeItems(2816, 1L);
            st.takeItems(2817, 1L);
            st.takeItems(2818, 1L);
            st.takeItems(2819, 1L);
            st.giveItems(2811, 1L);
            st.setCond(22, true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getCond();
         switch(st.getState()) {
            case 0:
               if (npcId == 30473) {
                  if (player.getClassId().getId() != 4
                     && player.getClassId().getId() != 15
                     && player.getClassId().getId() != 29
                     && player.getClassId().getId() != 19) {
                     htmltext = "30473-02.htm";
                     st.exitQuest(true);
                  } else if (player.getLevel() > 38) {
                     htmltext = "30473-03.htm";
                  } else {
                     htmltext = "30473-01.htm";
                  }
               }
               break;
            case 1:
               if (npcId == 30473) {
                  if (cond == 23) {
                     if (st.getQuestItemsCount(2813) == 0L) {
                        htmltext = "30473-06.htm";
                        st.giveItems(2820, 1L);
                        st.addExpAndSp(1476566, 101324);
                        st.giveItems(57, 266980L);
                        if (player.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
                           st.giveItems(7562, 60L);
                           player.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
                        }

                        st.exitQuest(false, true);
                     } else {
                        htmltext = "30473-07.htm";
                     }
                  } else {
                     htmltext = "30473-05.htm";
                  }
               } else if (npcId == 30428) {
                  if (cond == 1) {
                     htmltext = "30428-01.htm";
                  } else if (cond == 3) {
                     htmltext = "30428-03.htm";
                     st.takeItems(2810, 1L);
                     st.setCond(4, true);
                  } else if (cond != 2) {
                     htmltext = "30428-04.htm";
                  }
               } else if (npcId == 30659) {
                  int n = Rnd.get(5);
                  if (n == 0) {
                     htmltext = "30659-01.htm";
                  } else if (n == 1) {
                     htmltext = "30659-02.htm";
                  } else if (n == 2) {
                     htmltext = "30659-03.htm";
                  } else if (n == 3) {
                     htmltext = "30659-04.htm";
                  } else if (n == 4) {
                     htmltext = "30659-05.htm";
                  }
               } else if (npcId == 30424) {
                  if (cond == 4) {
                     htmltext = "30424-01.htm";
                     st.setCond(5, true);
                  } else {
                     htmltext = "30424-02.htm";
                  }
               } else if (npcId == 30658) {
                  if (cond == 5) {
                     htmltext = "30658-01.htm";
                  } else if (cond == 7) {
                     htmltext = "30658-04.htm";
                  } else if (cond == 8) {
                     htmltext = "30658-06.htm";
                     st.giveItems(2813, 1L);
                     st.takeItems(2814, 1L);
                     st.setCond(9, true);
                  } else if (cond == 6) {
                     st.setCond(9, true);
                     htmltext = "30658-07.htm";
                  } else if (cond == 9) {
                     htmltext = "30658-07.htm";
                  }
               } else if (npcId == 30660) {
                  if (cond == 7) {
                     htmltext = "30660-01.htm";
                  } else if (cond == 8) {
                     htmltext = "30660-04.htm";
                  }
               } else if (npcId == 30327) {
                  if (cond == 9) {
                     htmltext = "30327-01.htm";
                     st.giveItems(2815, 1L);
                     st.setCond(10, true);
                  } else if (cond > 9 && cond < 22) {
                     htmltext = "30327-02.htm";
                  } else if (cond == 22) {
                     htmltext = "30327-03.htm";
                     st.takeItems(2811, 1L);
                     st.setCond(23, true);
                  }
               } else if (npcId == 30674) {
                  if (cond == 10 && st.getQuestItemsCount(2815) > 0L) {
                     htmltext = "30674-01.htm";
                  } else if (cond == 12 && st.getQuestItemsCount(2816) > 0L) {
                     htmltext = "30674-03.htm";
                     st.setCond(13, true);
                  }
               } else if (npcId != 30662 && npcId != 30663 && npcId != 30664) {
                  if (npcId == 30661) {
                     if (cond == 13) {
                        htmltext = "30661-01.htm";
                        st.addSpawn(27124);
                        st.addSpawn(27124);
                        st.addSpawn(27124);
                        st.playSound("Itemsound.quest_before_battle");
                        st.setCond(14);
                     } else if (cond == 15) {
                        htmltext = "30661-02.htm";
                        st.addSpawn(27125);
                        st.addSpawn(27125);
                        st.addSpawn(27125);
                        st.playSound("Itemsound.quest_before_battle");
                        st.setCond(16);
                     } else if (cond == 17) {
                        htmltext = "30661-03.htm";
                        st.addSpawn(27126);
                        st.addSpawn(27126);
                        st.addSpawn(27127);
                        st.playSound("Itemsound.quest_before_battle");
                        st.setCond(18);
                     } else if (cond == 19) {
                        htmltext = "30661-04.htm";
                        st.setCond(20, true);
                     }
                  } else if (npcId == 30665) {
                     if (cond != 20 && cond != 21) {
                        htmltext = "30665-03.htm";
                     } else {
                        htmltext = "30665-01.htm";
                     }
                  }
               } else if (cond == 13) {
                  htmltext = npcId + "-01.htm";
               } else if (cond == 15) {
                  htmltext = npcId + "-02.htm";
               } else if (cond == 20) {
                  st.setCond(21, true);
                  htmltext = npcId + "-03.htm";
               } else if (cond == 21) {
                  htmltext = npcId + "-04.htm";
               }
               break;
            case 2:
               if (npcId == 30473) {
                  htmltext = getAlreadyCompletedMsg(player);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         Integer[] d = (Integer[])DROPLIST.get(npc.getId());
         if (st.getCond() == d[0] && (d[2] == 0 || st.getQuestItemsCount(d[2]) == 0L)) {
            if (d[2] != 0) {
               st.giveItems(d[2], 1L);
            }

            st.setCond(d[1]);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _226_TestOfHealer(226, _226_TestOfHealer.class.getSimpleName(), "");
   }

   static {
      DROPLIST.put(27134, new Integer[]{2, 3, 0});
      DROPLIST.put(27123, new Integer[]{11, 12, 2816});
      DROPLIST.put(27124, new Integer[]{14, 15, 2817});
      DROPLIST.put(27125, new Integer[]{16, 17, 2818});
      DROPLIST.put(27127, new Integer[]{18, 19, 2819});
   }
}
