package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.olympiad.CompetitionType;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _551_OlympiadStarter extends Quest {
   private static final String qn = "_551_OlympiadStarter";
   private static final int MANAGER = 31688;
   private static final int CERT_3 = 17238;
   private static final int CERT_5 = 17239;
   private static final int CERT_10 = 17240;
   private static final int OLY_CHEST = 17169;
   private static final int MEDAL_OF_GLORY = 21874;

   public _551_OlympiadStarter(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31688);
      this.addTalkId(31688);
      this.questItemIds = new int[]{17238, 17239, 17240};
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
            long count = st.getQuestItemsCount(17238) + st.getQuestItemsCount(17239);
            if (count > 0L) {
               st.giveItems(17169, count);
               if (count == 2L) {
                  st.giveItems(21874, 3L);
               }

               st.playSound("ItemSound.quest_finish");
               st.exitQuest(QuestState.QuestType.DAILY);
            } else {
               htmltext = Quest.getNoQuestMsg(player);
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
         } else if (st.isStarted()) {
            long count = st.getQuestItemsCount(17238) + st.getQuestItemsCount(17239) + st.getQuestItemsCount(17240);
            if (count == 3L) {
               htmltext = "31688-04.html";
               st.giveItems(17169, 4L);
               st.giveItems(21874, 5L);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(QuestState.QuestType.DAILY);
            } else {
               htmltext = "31688-s" + count + ".html";
            }
         }

         return htmltext;
      }
   }

   @Override
   public void onOlympiadWin(Player winner, CompetitionType type) {
      if (winner != null) {
         QuestState st = winner.getQuestState(this.getName());
         if (st != null && st.isStarted()) {
            int matches = st.getInt("matches") + 1;
            switch(matches) {
               case 3:
                  if (!st.hasQuestItems(17238)) {
                     st.giveItems(17238, 1L);
                  }
                  break;
               case 5:
                  if (!st.hasQuestItems(17239)) {
                     st.giveItems(17239, 1L);
                  }
                  break;
               case 10:
                  if (!st.hasQuestItems(17240)) {
                     st.giveItems(17240, 1L);
                  }
            }

            st.set("matches", String.valueOf(matches));
         }
      }
   }

   @Override
   public void onOlympiadLose(Player loser, CompetitionType type) {
      if (loser != null) {
         QuestState st = loser.getQuestState(this.getName());
         if (st != null && st.isStarted()) {
            int matches = st.getInt("matches") + 1;
            switch(matches) {
               case 3:
                  if (!st.hasQuestItems(17238)) {
                     st.giveItems(17238, 1L);
                  }
                  break;
               case 5:
                  if (!st.hasQuestItems(17239)) {
                     st.giveItems(17239, 1L);
                  }
                  break;
               case 10:
                  if (!st.hasQuestItems(17240)) {
                     st.giveItems(17240, 1L);
                  }
            }

            st.set("matches", String.valueOf(matches));
         }
      }
   }

   public static void main(String[] args) {
      new _551_OlympiadStarter(551, "_551_OlympiadStarter", "");
   }
}
