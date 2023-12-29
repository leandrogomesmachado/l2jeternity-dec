package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.olympiad.CompetitionType;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _553_OlympiadUndefeated extends Quest {
   private static final String qn = "_553_OlympiadUndefeated";
   private static final int MANAGER = 31688;
   private static final int WIN_CONF_2 = 17244;
   private static final int WIN_CONF_5 = 17245;
   private static final int WIN_CONF_10 = 17246;
   private static final int OLY_CHEST = 17169;
   private static final int MEDAL_OF_GLORY = 21874;

   public _553_OlympiadUndefeated(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31688);
      this.addTalkId(31688);
      this.questItemIds = new int[]{17244, 17245, 17246};
      this.setOlympiadUse(true);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         String htmltext = event;
         if (event.equalsIgnoreCase("31688-03.html")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("31688-04.html")) {
            long count = st.getQuestItemsCount(17244) + st.getQuestItemsCount(17245);
            if (count > 0L) {
               st.giveItems(17169, count);
               if (count == 2L) {
                  st.giveItems(21874, 3L);
               }

               st.playSound("ItemSound.quest_finish");
               st.exitQuest(QuestState.QuestType.DAILY);
            } else {
               htmltext = getNoQuestMsg(player);
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         if (player.getLevel() < 75 || !player.isNoble()) {
            htmltext = "31688-00.htm";
         } else if (st.isCreated()) {
            htmltext = "31688-01.htm";
         } else if (st.isCompleted()) {
            if (st.isNowAvailable()) {
               st.setState((byte)0);
               if (player.getLevel() < 75 || !player.isNoble()) {
                  htmltext = "31688-00.htm";
               }
            } else {
               htmltext = "31688-05.html";
            }
         } else {
            long count = st.getQuestItemsCount(17244) + st.getQuestItemsCount(17245) + st.getQuestItemsCount(17246);
            if (count == 3L && st.getInt("cond") == 2) {
               htmltext = "31688-04.html";
               st.giveItems(17169, 4L);
               st.giveItems(21874, 5L);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(QuestState.QuestType.DAILY);
            } else {
               htmltext = "31688-w" + count + ".html";
            }
         }

         return htmltext;
      }
   }

   @Override
   public void onOlympiadWin(Player winner, CompetitionType type) {
      if (winner != null) {
         QuestState st = winner.getQuestState(this.getName());
         if (st != null && st.isStarted() && st.getInt("cond") == 1) {
            int matches = st.getInt("undefeatable") + 1;
            st.set("undefeatable", String.valueOf(matches));
            switch(matches) {
               case 2:
                  if (!st.hasQuestItems(17244)) {
                     st.giveItems(17244, 1L);
                  }
                  break;
               case 5:
                  if (!st.hasQuestItems(17245)) {
                     st.giveItems(17245, 1L);
                  }
                  break;
               case 10:
                  if (!st.hasQuestItems(17246)) {
                     st.giveItems(17246, 1L);
                     st.set("cond", "2");
                  }
            }
         }
      }
   }

   @Override
   public void onOlympiadLose(Player loser, CompetitionType type) {
      if (loser != null) {
         QuestState st = loser.getQuestState(this.getName());
         if (st != null && st.isStarted() && st.getInt("cond") == 1) {
            st.unset("undefeatable");
            st.takeItems(17244, -1L);
            st.takeItems(17245, -1L);
            st.takeItems(17246, -1L);
         }
      }
   }

   public static void main(String[] args) {
      new _553_OlympiadUndefeated(553, "_553_OlympiadUndefeated", "");
   }
}
