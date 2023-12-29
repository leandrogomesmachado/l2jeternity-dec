package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _661_MakingTheHarvestGroundsSafe extends Quest {
   private static final String qn = "_661_MakingTheHarvestGroundsSafe";

   public _661_MakingTheHarvestGroundsSafe(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30210);
      this.addTalkId(30210);
      this.addKillId(new int[]{21095, 21096, 21097});
      this.questItemIds = new int[]{8283, 8284, 8285};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_661_MakingTheHarvestGroundsSafe");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30210-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30210-04.htm")) {
            int item1 = (int)st.getQuestItemsCount(8283);
            int item2 = (int)st.getQuestItemsCount(8284);
            int item3 = (int)st.getQuestItemsCount(8285);
            int sum = 0;
            sum = item1 * 57 + item2 * 56 + item3 * 60;
            if (item1 + item2 + item3 >= 10) {
               sum += 2871;
            }

            st.takeItems(8283, (long)item1);
            st.takeItems(8284, (long)item2);
            st.takeItems(8285, (long)item3);
            st.rewardItems(57, (long)sum);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30210-06.htm")) {
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_661_MakingTheHarvestGroundsSafe");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 21) {
                  htmltext = "30210-01.htm";
               } else {
                  htmltext = "30210-01a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(8283) < 1L && st.getQuestItemsCount(8284) < 1L && st.getQuestItemsCount(8285) < 1L) {
                  htmltext = "30210-05.htm";
               } else {
                  htmltext = "30210-03.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_661_MakingTheHarvestGroundsSafe");
      if (st == null) {
         return null;
      } else {
         if (st.isStarted() && st.getRandom(10) < 5) {
            switch(npc.getId()) {
               case 21095:
                  st.giveItems(8283, 1L);
                  break;
               case 21096:
                  st.giveItems(8284, 1L);
                  break;
               case 21097:
                  st.giveItems(8285, 1L);
            }

            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _661_MakingTheHarvestGroundsSafe(661, "_661_MakingTheHarvestGroundsSafe", "");
   }
}
