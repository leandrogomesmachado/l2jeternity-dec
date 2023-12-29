package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _629_CleanUpTheSwampOfScreams extends Quest {
   private static final String qn = "_629_CleanUpTheSwampOfScreams";
   private static final int CAPTAIN = 31553;
   private static final int CLAWS = 7250;
   private static final int COIN = 7251;
   private static final int[][] CHANCE = new int[][]{
      {21508, 500000},
      {21509, 430000},
      {21510, 520000},
      {21511, 570000},
      {21512, 740000},
      {21513, 530000},
      {21514, 530000},
      {21515, 540000},
      {21516, 550000},
      {21517, 560000}
   };

   public _629_CleanUpTheSwampOfScreams(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31553);
      this.addTalkId(31553);

      for(int[] i : CHANCE) {
         this.addKillId(i[0]);
      }

      this.questItemIds = new int[]{7250, 7251};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_629_CleanUpTheSwampOfScreams");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31553-1.htm")) {
            if (player.getLevel() >= 66) {
               st.set("cond", "1");
               st.setState((byte)1);
               st.playSound("ItemSound.quest_accept");
            } else {
               htmltext = "31553-0a.htm";
               st.exitQuest(true);
            }
         } else if (event.equalsIgnoreCase("31553-3.htm")) {
            if (st.getQuestItemsCount(7250) >= 100L) {
               st.takeItems(7250, 100L);
               st.giveItems(7251, 20L);
            } else {
               htmltext = "31553-3a.htm";
            }
         } else if (event.equalsIgnoreCase("31553-5.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_629_CleanUpTheSwampOfScreams");
      if (st == null) {
         return htmltext;
      } else {
         if (!st.hasQuestItems(7246) && !st.hasQuestItems(7247)) {
            htmltext = "31553-6.htm";
            st.exitQuest(true);
         } else {
            switch(st.getState()) {
               case 0:
                  if (player.getLevel() >= 66) {
                     htmltext = "31553-0.htm";
                  } else {
                     htmltext = "31553-0a.htm";
                     st.exitQuest(true);
                  }
                  break;
               case 1:
                  if (st.getQuestItemsCount(7250) >= 100L) {
                     htmltext = "31553-2.htm";
                  } else {
                     htmltext = "31553-1a.htm";
                  }
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
         QuestState st = partyMember.getQuestState("_629_CleanUpTheSwampOfScreams");
         st.dropItems(7250, 1, 100L, CHANCE[npc.getId() - 21508][1]);
         return null;
      }
   }

   public static void main(String[] args) {
      new _629_CleanUpTheSwampOfScreams(629, "_629_CleanUpTheSwampOfScreams", "");
   }
}
