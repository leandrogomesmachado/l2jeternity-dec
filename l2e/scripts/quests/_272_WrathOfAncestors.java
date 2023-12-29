package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _272_WrathOfAncestors extends Quest {
   private static final String qn = "_272_WrathOfAncestors";
   private static final int LIVINA = 30572;
   private static final int GOBLIN_GRAVE_ROBBER = 20319;
   private static final int GOBLIN_TOMB_RAIDER_LEADER = 20320;
   private static final int GRAVE_ROBBERS_HEAD = 1474;

   public _272_WrathOfAncestors(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30572);
      this.addTalkId(30572);
      this.addKillId(20319);
      this.addKillId(20320);
      this.questItemIds = new int[]{1474};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_272_WrathOfAncestors");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30572-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_272_WrathOfAncestors");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getRace().ordinal() == 3) {
                  if (player.getLevel() >= 5 && player.getLevel() <= 16) {
                     htmltext = "30572-02.htm";
                  } else {
                     htmltext = "30572-01.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30572-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(1474) < 50L) {
                  htmltext = "30572-04.htm";
               } else {
                  htmltext = "30572-05.htm";
                  st.takeItems(1474, -1L);
                  st.rewardItems(57, 1500L);
                  st.exitQuest(true);
                  st.playSound("ItemSound.quest_finish");
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
      QuestState st = player.getQuestState("_272_WrathOfAncestors");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.getQuestItemsCount(1474) < 50L) {
            st.giveItems(1474, 1L);
            if (st.getQuestItemsCount(1474) < 49L) {
               st.playSound("ItemSound.quest_itemget");
            } else {
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "2");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _272_WrathOfAncestors(272, "_272_WrathOfAncestors", "");
   }
}
