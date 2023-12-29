package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _359_ForSleeplessDeadmen extends Quest {
   private static final String qn = "_359_ForSleeplessDeadmen";
   private static final int ORVEN = 30857;
   private static final int REMAINS = 5869;
   private static final int[] REWARD = new int[]{6341, 6342, 6343, 6344, 6345, 6346, 5494, 5495};

   public _359_ForSleeplessDeadmen(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30857);
      this.addTalkId(30857);
      this.addKillId(new int[]{21006, 21007, 21008});
      this.questItemIds = new int[]{5869};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_359_ForSleeplessDeadmen");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30857-06.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30857-10.htm")) {
            st.giveItems(REWARD[getRandom(REWARD.length)], 4L);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_359_ForSleeplessDeadmen");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 60) {
                  htmltext = "30857-02.htm";
               } else {
                  htmltext = "30857-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               if (cond == 1) {
                  htmltext = "30857-07.htm";
               } else if (cond == 2) {
                  htmltext = "30857-08.htm";
                  st.set("cond", "3");
                  st.playSound("ItemSound.quest_middle");
                  st.takeItems(5869, -1L);
               } else if (cond == 3) {
                  htmltext = "30857-09.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = this.checkPlayerCondition(player, npc, "cond", "1");
      if (st == null) {
         return null;
      } else {
         if (st.dropItems(5869, 1, 60L, 100000)) {
            st.set("cond", "2");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _359_ForSleeplessDeadmen(359, "_359_ForSleeplessDeadmen", "");
   }
}
