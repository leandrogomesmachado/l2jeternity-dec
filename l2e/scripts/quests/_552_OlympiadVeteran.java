package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.olympiad.CompetitionType;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _552_OlympiadVeteran extends Quest {
   private static final String qn = "_552_OlympiadVeteran";
   private static final int MANAGER = 31688;
   private static final int Team_Event_Certificate = 17241;
   private static final int Class_Free_Battle_Certificate = 17242;
   private static final int Class_Battle_Certificate = 17243;
   private static final int OLY_CHEST = 17169;

   public _552_OlympiadVeteran(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31688);
      this.addTalkId(31688);
      this.questItemIds = new int[]{17241, 17242, 17243};
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
            long count = st.getQuestItemsCount(17241) + st.getQuestItemsCount(17242) + st.getQuestItemsCount(17243);
            if (count > 0L) {
               st.giveItems(17169, count);
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
         } else if (st.isStarted()) {
            long count = st.getQuestItemsCount(17241) + st.getQuestItemsCount(17242) + st.getQuestItemsCount(17243);
            if (count == 3L) {
               htmltext = "31688-04.html";
               st.giveItems(17169, 4L);
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
            switch(type) {
               case CLASSED:
                  int matches = st.getInt("classed") + 1;
                  st.set("classed", String.valueOf(matches));
                  if (matches == 5 && !st.hasQuestItems(17243)) {
                     st.giveItems(17243, 1L);
                  }
                  break;
               case NON_CLASSED:
                  int matches = st.getInt("nonclassed") + 1;
                  st.set("nonclassed", String.valueOf(matches));
                  if (matches == 5 && !st.hasQuestItems(17242)) {
                     st.giveItems(17242, 1L);
                  }
                  break;
               case TEAMS:
                  int matches = st.getInt("teams") + 1;
                  st.set("teams", String.valueOf(matches));
                  if (matches == 5 && !st.hasQuestItems(17241)) {
                     st.giveItems(17241, 1L);
                  }
            }
         }
      }
   }

   @Override
   public void onOlympiadLose(Player loser, CompetitionType type) {
      if (loser != null) {
         QuestState st = loser.getQuestState(this.getName());
         if (st != null && st.isStarted()) {
            switch(type) {
               case CLASSED:
                  int matches = st.getInt("classed") + 1;
                  st.set("classed", String.valueOf(matches));
                  if (matches == 5) {
                     st.giveItems(17243, 1L);
                  }
                  break;
               case NON_CLASSED:
                  int matches = st.getInt("nonclassed") + 1;
                  st.set("nonclassed", String.valueOf(matches));
                  if (matches == 5) {
                     st.giveItems(17242, 1L);
                  }
                  break;
               case TEAMS:
                  int matches = st.getInt("teams") + 1;
                  st.set("teams", String.valueOf(matches));
                  if (matches == 5) {
                     st.giveItems(17241, 1L);
                  }
            }
         }
      }
   }

   public static void main(String[] args) {
      new _552_OlympiadVeteran(552, "_552_OlympiadVeteran", "");
   }
}
