package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _354_ConquestOfAlligatorIsland extends Quest {
   private static final String qn = "_354_ConquestOfAlligatorIsland";
   private static final int ALLIGATOR_TOOTH = 5863;
   private static final int TORN_MAP_FRAGMENT = 5864;
   private static final int PIRATES_TREASURE_MAP = 5915;
   private static final int KLUCK = 30895;
   public final int[][] RANDOM_REWARDS = new int[][]{
      {736, 15}, {1061, 20}, {734, 10}, {735, 5}, {1878, 25}, {1875, 10}, {1879, 10}, {1880, 10}, {956, 1}, {955, 1}
   };

   public _354_ConquestOfAlligatorIsland(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30895);
      this.addTalkId(30895);
      this.addKillId(new int[]{20804, 20805, 20806, 20807, 20808, 20991});
      this.questItemIds = new int[]{5863, 5864};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_354_ConquestOfAlligatorIsland");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30895-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30895-03.htm")) {
            if (st.getQuestItemsCount(5864) > 0L) {
               htmltext = "30895-03a.htm";
            }
         } else if (event.equalsIgnoreCase("30895-05.htm")) {
            if (st.getQuestItemsCount(5863) > 99L) {
               st.giveItems(57, st.getQuestItemsCount(5863) * 300L);
               st.takeItems(5863, -1L);
               st.playSound("ItemSound.quest_itemget");
               int random = getRandom(this.RANDOM_REWARDS.length);
               st.giveItems(this.RANDOM_REWARDS[random][0], (long)this.RANDOM_REWARDS[random][1]);
               htmltext = "30895-05b.htm";
            } else {
               st.giveItems(57, st.getQuestItemsCount(5863) * 100L);
               st.takeItems(5863, -1L);
               st.playSound("ItemSound.quest_itemget");
               htmltext = "30895-05a.htm";
            }
         } else if (event.equalsIgnoreCase("30895-07.htm")) {
            if (st.getQuestItemsCount(5864) >= 10L) {
               htmltext = "30895-08.htm";
               st.takeItems(5864, 10L);
               st.giveItems(5915, 1L);
               st.playSound("ItemSound.quest_itemget");
            }
         } else if (event.equalsIgnoreCase("30895-09.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_354_ConquestOfAlligatorIsland");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 38 && player.getLevel() <= 49) {
                  htmltext = "30895-01.htm";
               } else {
                  htmltext = "30895-00.htm";
               }
               break;
            case 1:
               if (st.getQuestItemsCount(5864) > 0L) {
                  htmltext = "30895-03a.htm";
               } else {
                  htmltext = "30895-03.htm";
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
         QuestState st = partyMember.getQuestState("_354_ConquestOfAlligatorIsland");
         int random = st.getRandom(100);
         if (random < 45) {
            st.giveItems(5863, 1L);
            if (random < 10) {
               st.giveItems(5864, 1L);
               st.playSound("ItemSound.quest_middle");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _354_ConquestOfAlligatorIsland(354, "_354_ConquestOfAlligatorIsland", "");
   }
}
