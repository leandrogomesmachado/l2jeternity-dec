package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _358_IllegitimateChildOfAGoddess extends Quest {
   private static final String qn = "_358_IllegitimateChildOfAGoddess";
   private static final int SCALE = 5868;
   private static final int[] REWARD = new int[]{6329, 6331, 6333, 6335, 6337, 6339, 5364, 5366};

   public _358_IllegitimateChildOfAGoddess(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30862);
      this.addTalkId(30862);
      this.addKillId(new int[]{20672, 20673});
      this.questItemIds = new int[]{5868};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_358_IllegitimateChildOfAGoddess");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30862-05.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30862-07.htm")) {
            if (st.getQuestItemsCount(5868) >= 108L) {
               st.takeItems(5868, -1L);
               st.giveItems(REWARD[getRandom(REWARD.length)], 1L);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(true);
            } else {
               htmltext = "30862-04.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_358_IllegitimateChildOfAGoddess");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 63) {
                  htmltext = "30862-02.htm";
               } else {
                  htmltext = "30862-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(5868) >= 108L) {
                  htmltext = "30862-03.htm";
               } else {
                  htmltext = "30862-04.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_358_IllegitimateChildOfAGoddess");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.dropQuestItems(5868, 1, 108L, 700000, true)) {
            st.set("cond", "2");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _358_IllegitimateChildOfAGoddess(358, "_358_IllegitimateChildOfAGoddess", "");
   }
}
