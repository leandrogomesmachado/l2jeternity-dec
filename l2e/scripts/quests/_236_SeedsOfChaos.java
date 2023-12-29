package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _236_SeedsOfChaos extends Quest {
   private static final String qn = "_236_SeedsOfChaos";
   private static final int KEKROPUS = 32138;
   private static final int WIZARD = 31522;
   private static final int KATENAR = 32333;
   private static final int ROCK = 32238;
   private static final int HARKILGAMED = 32236;
   private static final int MAO = 32190;
   private static final int RODENPICULA = 32237;
   private static final int NORNIL = 32239;
   private static final int[] NEEDLE_STAKATO_DRONES = new int[]{21516, 21517};
   private static final int[] SPLENDOR_MOBS = new int[]{
      21520, 21521, 21522, 21523, 21524, 21525, 21526, 21527, 21528, 21529, 21530, 21531, 21532, 21533, 21534, 21535, 21536, 21537, 21538, 21539, 21540, 21541
   };
   private static final int STAR_OF_DESTINY = 5011;
   private static final int SCROLL_ENCHANT_WEAPON_A = 729;
   private static final int SHINING_MEDALLION = 9743;
   private static final int BLACK_ECHO_CRYSTAL = 9745;
   protected static boolean KATENAR_SPAWNED = false;
   protected static boolean HARKILGAMED_SPAWNED = false;

   public _236_SeedsOfChaos(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32138);
      this.addTalkId(32138);
      this.addTalkId(31522);
      this.addTalkId(32333);
      this.addTalkId(32238);
      this.addTalkId(32236);
      this.addTalkId(32190);
      this.addTalkId(32237);
      this.addTalkId(32239);

      for(int kill_id : NEEDLE_STAKATO_DRONES) {
         this.addKillId(kill_id);
      }

      for(int kill_id : SPLENDOR_MOBS) {
         this.addKillId(kill_id);
      }

      this.questItemIds = new int[]{9743, 9745};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_236_SeedsOfChaos");
      if (st == null) {
         return event;
      } else {
         String htmltext;
         if (event.equalsIgnoreCase("1")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            htmltext = "32138_02b.htm";
         } else if (event.equalsIgnoreCase("1_yes")) {
            htmltext = "31522_01c.htm";
         } else if (event.equalsIgnoreCase("1_no")) {
            htmltext = "31522_01no.htm";
         } else if (event.equalsIgnoreCase("2")) {
            st.set("cond", "2");
            htmltext = "31522_02.htm";
         } else if (event.equalsIgnoreCase("31522_03b.htm") && st.getQuestItemsCount(9745) > 0L) {
            st.takeItems(9745, -1L);
            htmltext = event + ".htm";
         } else {
            if (event.equalsIgnoreCase("4")) {
               st.set("cond", "4");
               if (!KATENAR_SPAWNED) {
                  st.addSpawn(32333, 120000);
                  ThreadPoolManager.getInstance().schedule(new _236_SeedsOfChaos.OnDespawn(true), 120000L);
                  KATENAR_SPAWNED = true;
               }

               return null;
            }

            if (event.equalsIgnoreCase("5")) {
               st.set("cond", "5");
               htmltext = "32235_02.htm";
            } else {
               if (event.equalsIgnoreCase("spawn_harkil")) {
                  if (!HARKILGAMED_SPAWNED) {
                     st.addSpawn(32236, 120000);
                     ThreadPoolManager.getInstance().schedule(new _236_SeedsOfChaos.OnDespawn(false), 120000L);
                     HARKILGAMED_SPAWNED = true;
                  }

                  return null;
               }

               if (event.equalsIgnoreCase("6")) {
                  st.set("cond", "12");
                  htmltext = "32236_06.htm";
               } else if (event.equalsIgnoreCase("8")) {
                  st.set("cond", "14");
                  htmltext = "32236_08.htm";
               } else if (event.equalsIgnoreCase("9")) {
                  st.set("cond", "15");
                  htmltext = "32138_09.htm";
               } else if (event.equalsIgnoreCase("10")) {
                  st.set("cond", "16");
                  player.teleToLocation(-119534, 87176, -12593, true);
                  htmltext = "32190_02.htm";
               } else if (event.equalsIgnoreCase("11")) {
                  st.set("cond", "17");
                  htmltext = "32237_11.htm";
               } else if (event.equalsIgnoreCase("12")) {
                  st.set("cond", "18");
                  htmltext = "32239_12.htm";
               } else if (event.equalsIgnoreCase("13")) {
                  st.set("cond", "19");
                  htmltext = "32237_13.htm";
               } else if (event.equalsIgnoreCase("14")) {
                  st.set("cond", "20");
                  htmltext = "32239_14.htm";
               } else if (event.equalsIgnoreCase("15")) {
                  st.giveItems(729, 1L);
                  st.setState((byte)2);
                  htmltext = "32237_15.htm";
               } else {
                  htmltext = event + ".htm";
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_236_SeedsOfChaos");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (npcId == 32138) {
                  if (player.getRace() != Race.Kamael) {
                     st.exitQuest(true);
                     htmltext = "32138_00.htm";
                  } else if (player.getLevel() < 75) {
                     st.exitQuest(true);
                     htmltext = "32138_01.htm";
                  } else if (st.getQuestItemsCount(5011) < 1L) {
                     htmltext = "32138_01a.htm";
                     st.exitQuest(true);
                  } else {
                     htmltext = "32138_02.htm";
                  }
               }
               break;
            case 1:
               if (npcId == 32138) {
                  if (cond < 14) {
                     htmltext = "32138_02c.htm";
                  } else if (cond == 14) {
                     htmltext = "32138_08.htm";
                  } else {
                     htmltext = "32138_10.htm";
                  }
               } else if (npcId == 31522) {
                  if (cond == 1) {
                     htmltext = "31522_01.htm";
                  } else if (cond == 2) {
                     htmltext = "31522_02a.htm";
                  } else if (cond != 3 && (cond != 4 || KATENAR_SPAWNED)) {
                     htmltext = "31522_04.htm";
                  } else {
                     htmltext = "31522_03.htm";
                  }
               } else if (npcId == 32333) {
                  if (cond == 4) {
                     htmltext = "32235_01.htm";
                  } else if (cond >= 5) {
                     htmltext = "32235_02.htm";
                  }
               } else if (npcId == 32238) {
                  if (cond != 5 && cond != 13) {
                     htmltext = "32238-00.htm";
                  } else {
                     htmltext = "32238-01.htm";
                  }
               } else if (npcId == 32236) {
                  if (cond == 5) {
                     htmltext = "32236_05.htm";
                  } else if (cond == 12) {
                     htmltext = "32236_06.htm";
                  } else if (cond == 13) {
                     st.takeItems(9743, -1L);
                     htmltext = "32236_07.htm";
                  } else if (cond > 13) {
                     htmltext = "32236_09.htm";
                  }
               } else if (npcId == 32190) {
                  if (cond == 15 || cond == 16) {
                     htmltext = "32190_01.htm";
                  }
               } else if (npcId == 32237) {
                  if (cond == 16) {
                     htmltext = "32237_10.htm";
                  } else if (cond == 17) {
                     htmltext = "32237_11.htm";
                  } else if (cond == 18) {
                     htmltext = "32237_12.htm";
                  } else if (cond == 19) {
                     htmltext = "32237_13.htm";
                  } else if (cond == 20) {
                     htmltext = "32237_14.htm";
                  }
               } else if (npcId == 32239) {
                  if (cond == 17) {
                     htmltext = "32239_11.htm";
                  } else if (cond == 18) {
                     htmltext = "32239_12.htm";
                  } else if (cond == 19) {
                     htmltext = "32239_13.htm";
                  } else if (cond == 20) {
                     htmltext = "32239_14.htm";
                  }
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_236_SeedsOfChaos");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (IsInIntArray(npcId, NEEDLE_STAKATO_DRONES)) {
            if (cond == 2 && st.getQuestItemsCount(9745) == 0L && Rnd.chance((int)(15.0F * Config.RATE_QUEST_DROP))) {
               st.giveItems(9745, 1L);
               st.set("cond", "3");
               st.playSound("Itemsound.quest_middle");
            }
         } else if (IsInIntArray(npcId, SPLENDOR_MOBS) && cond == 12 && st.getQuestItemsCount(9743) < 62L && Rnd.chance((int)(20.0F * Config.RATE_QUEST_DROP))
            )
          {
            st.giveItems(9743, 1L);
            if (st.getQuestItemsCount(9743) < 62L) {
               st.playSound("ItemSound.quest_itemget");
            } else {
               st.set("cond", "13");
               st.playSound("Itemsound.quest_middle");
            }
         }

         return null;
      }
   }

   private static boolean IsInIntArray(int i, int[] a) {
      for(int _i : a) {
         if (_i == i) {
            return true;
         }
      }

      return false;
   }

   public static void main(String[] args) {
      new _236_SeedsOfChaos(236, "_236_SeedsOfChaos", "");
   }

   private static class OnDespawn implements Runnable {
      private final boolean _SUBJ_KATENAR;

      public OnDespawn(boolean SUBJ_KATENAR) {
         this._SUBJ_KATENAR = SUBJ_KATENAR;
      }

      @Override
      public void run() {
         if (this._SUBJ_KATENAR) {
            _236_SeedsOfChaos.KATENAR_SPAWNED = false;
         } else {
            _236_SeedsOfChaos.HARKILGAMED_SPAWNED = false;
         }
      }
   }
}
