package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _303_CollectArrowheads extends Quest {
   private static final String qn = "_303_CollectArrowheads";
   private static final int MINIA = 30029;
   private static final int ORCISH_ARROWHEAD = 963;

   public _303_CollectArrowheads(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30029);
      this.addTalkId(30029);
      this.addKillId(20361);
      this.questItemIds = new int[]{963};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_303_CollectArrowheads");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30029-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_303_CollectArrowheads");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 10 && player.getLevel() <= 14) {
                  htmltext = "30029-02.htm";
               } else {
                  htmltext = "30029-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(963) < 10L) {
                  htmltext = "30029-04.htm";
               } else {
                  htmltext = "30029-05.htm";
                  st.takeItems(963, -1L);
                  st.rewardItems(57, 1000L);
                  st.addExpAndSp(2000, 0);
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(true);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_303_CollectArrowheads");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.dropQuestItems(963, 1, 10L, 400000, true)) {
            st.set("cond", "2");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _303_CollectArrowheads(303, "_303_CollectArrowheads", "");
   }
}
