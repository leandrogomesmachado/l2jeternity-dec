package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _601_WatchingEyes extends Quest {
   private static final String qn = "_601_WatchingEyes";
   private static int EYE_OF_ARGOS = 31683;
   private static int PROOF_OF_AVENGER = 7188;
   private static int DROP_CHANCE = 50;
   private static int[] MOBS = new int[]{21306, 21308, 21309, 21310, 21311};
   private static int[][] REWARDS = new int[][]{{6699, 90000, 0, 19}, {6698, 80000, 20, 39}, {6700, 40000, 40, 49}, {0, 230000, 50, 100}};

   public _601_WatchingEyes(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(EYE_OF_ARGOS);
      this.addTalkId(EYE_OF_ARGOS);

      for(int MOB : MOBS) {
         this.addKillId(MOB);
      }

      this.questItemIds = new int[]{PROOF_OF_AVENGER};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_601_WatchingEyes");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31683-1.htm")) {
            if (player.getLevel() < 71) {
               htmltext = "31683-0a.htm";
               st.exitQuest(true);
            } else {
               st.setState((byte)1);
               st.set("cond", "1");
               st.playSound("ItemSound.quest_accept");
            }
         } else if (event.equalsIgnoreCase("31683-4.htm")) {
            int random = getRandom(101);
            int i = 0;
            int item = 0;

            int adena;
            for(adena = 0; i < REWARDS.length; ++i) {
               item = REWARDS[i][0];
               adena = REWARDS[i][1];
               if (REWARDS[i][2] <= random && random <= REWARDS[i][3]) {
                  break;
               }
            }

            st.giveItems(57, (long)adena);
            if (item != 0) {
               st.giveItems(item, 5L);
               st.addExpAndSp(120000, 10000);
            }

            st.takeItems(PROOF_OF_AVENGER, -1L);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_601_WatchingEyes");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         if (cond == 0) {
            htmltext = "31683-0.htm";
         } else if (cond == 1) {
            htmltext = "31683-2.htm";
         } else if (cond == 2 && st.getQuestItemsCount(PROOF_OF_AVENGER) == 100L) {
            htmltext = "31683-3.htm";
         } else {
            htmltext = "31683-4a.htm";
            st.set("cond", "1");
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_601_WatchingEyes");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1) {
            long count = st.getQuestItemsCount(PROOF_OF_AVENGER);
            if (count < 100L && getRandom(100) < DROP_CHANCE) {
               st.giveItems(PROOF_OF_AVENGER, 1L);
               if (count == 99L) {
                  st.set("cond", "2");
                  st.playSound("ItemSound.quest_middle");
               } else {
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _601_WatchingEyes(601, "_601_WatchingEyes", "");
   }
}
