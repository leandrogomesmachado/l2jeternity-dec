package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _372_LegacyOfInsolence extends Quest {
   private static final String qn = "_372_LegacyOfInsolence";
   private static final int WALDERAL = 30844;
   private static final int PATRIN = 30929;
   private static final int HOLLY = 30839;
   private static final int CLAUDIA = 31001;
   private static final int DESMOND = 30855;
   private static final int[][] MONSTERS_DROPS = new int[][]{
      {20817, 20821, 20825, 20829, 21069, 21063}, {5966, 5966, 5966, 5967, 5968, 5969}, {30, 40, 46, 40, 25, 25}
   };
   private static final int[][] SCROLLS = new int[][]{{5989, 6001}, {5984, 5988}, {5979, 5983}, {5972, 5978}, {5972, 5978}};
   private static final int[][][] REWARDS_MATRICE = new int[][][]{
      {{13, 5496}, {26, 5508}, {40, 5525}, {58, 5368}, {76, 5392}, {100, 5426}},
      {{13, 5497}, {26, 5509}, {40, 5526}, {58, 5370}, {76, 5394}, {100, 5428}},
      {{20, 5502}, {40, 5514}, {58, 5527}, {73, 5380}, {87, 5404}, {100, 5430}},
      {{20, 5503}, {40, 5515}, {58, 5528}, {73, 5382}, {87, 5406}, {100, 5432}},
      {{33, 5496}, {66, 5508}, {89, 5525}, {100, 57}},
      {{33, 5497}, {66, 5509}, {89, 5526}, {100, 57}},
      {{35, 5502}, {70, 5514}, {87, 5527}, {100, 57}},
      {{35, 5503}, {70, 5515}, {87, 5528}, {100, 57}}
   };

   public _372_LegacyOfInsolence(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30844);
      this.addTalkId(new int[]{30844, 30929, 30839, 31001, 30855});
      this.addKillId(MONSTERS_DROPS[0]);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_372_LegacyOfInsolence");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30844-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30844-05b.htm")) {
            if (st.getInt("cond") == 1) {
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            }
         } else if (event.equalsIgnoreCase("30844-07.htm")) {
            for(int blueprint = 5989; blueprint <= 6001; ++blueprint) {
               if (!st.hasQuestItems(blueprint)) {
                  htmltext = "30844-06.htm";
                  break;
               }
            }
         } else if (event.startsWith("30844-07-")) {
            checkAndRewardItems(st, 0, Integer.parseInt(event.substring(9, 10)), 30844);
         } else if (event.equalsIgnoreCase("30844-09.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_372_LegacyOfInsolence");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < 59) {
                  htmltext = "30844-01.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "30844-02.htm";
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30839:
                     htmltext = checkAndRewardItems(st, 1, 4, 30839);
                     break;
                  case 30844:
                     htmltext = "30844-05.htm";
                     break;
                  case 30855:
                     htmltext = checkAndRewardItems(st, 4, 7, 30855);
                     break;
                  case 30929:
                     htmltext = checkAndRewardItems(st, 2, 5, 30929);
                     break;
                  case 31001:
                     htmltext = checkAndRewardItems(st, 3, 6, 31001);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
      if (partyMember == null) {
         return null;
      } else {
         int npcId = npc.getId();

         for(int index = 0; index < MONSTERS_DROPS[0].length; ++index) {
            if (MONSTERS_DROPS[0][index] == npcId) {
               if (getRandom(100) < MONSTERS_DROPS[2][index]) {
                  QuestState st = partyMember.getQuestState("_372_LegacyOfInsolence");
                  st.rewardItems(MONSTERS_DROPS[1][index], 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
               break;
            }
         }

         return null;
      }
   }

   private static String checkAndRewardItems(QuestState st, int itemType, int rewardType, int npcId) {
      int[] itemsToCheck = SCROLLS[itemType];

      for(int item = itemsToCheck[0]; item <= itemsToCheck[1]; ++item) {
         if (!st.hasQuestItems(item)) {
            return npcId + (npcId == 30844 ? "-07a.htm" : "-01.htm");
         }
      }

      for(int item = itemsToCheck[0]; item <= itemsToCheck[1]; ++item) {
         st.takeItems(item, 1L);
      }

      int[][] rewards = REWARDS_MATRICE[rewardType];
      int chance = getRandom(100);

      for(int[] reward : rewards) {
         if (chance < reward[0]) {
            st.rewardItems(reward[1], 1L);
            return npcId + "-02.htm";
         }
      }

      return npcId + (npcId == 30844 ? "-07a.htm" : "-01.htm");
   }

   public static void main(String[] args) {
      new _372_LegacyOfInsolence(372, "_372_LegacyOfInsolence", "");
   }
}
