package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _317_CatchTheWind extends Quest {
   private static final String qn = "_317_CatchTheWind";
   private static final int RIZRAELL = 30361;
   private static final int WIND_SHARD = 1078;

   public _317_CatchTheWind(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30361);
      this.addTalkId(30361);
      this.addKillId(new int[]{20036, 20044});
      this.questItemIds = new int[]{1078};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_317_CatchTheWind");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30361-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30361-08.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_317_CatchTheWind");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 18 && player.getLevel() <= 23) {
                  htmltext = "30361-03.htm";
               } else {
                  htmltext = "30361-02.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               long shards = st.getQuestItemsCount(1078);
               if (shards == 0L) {
                  htmltext = "30361-05.htm";
               } else {
                  long reward = 40L * shards + (long)(shards >= 10L ? 2988 : 0);
                  htmltext = "30361-07.htm";
                  st.takeItems(1078, -1L);
                  st.rewardItems(57, reward);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_317_CatchTheWind");
      if (st == null) {
         return null;
      } else {
         if (st.isStarted() && st.getRandom(100) < 50) {
            st.giveItems(1078, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _317_CatchTheWind(317, "_317_CatchTheWind", "");
   }
}
