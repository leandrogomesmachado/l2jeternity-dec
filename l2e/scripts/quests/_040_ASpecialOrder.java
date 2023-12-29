package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _040_ASpecialOrder extends Quest {
   public _040_ASpecialOrder(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30081);
      this.addTalkId(30081);
      this.addTalkId(31572);
      this.addTalkId(30511);
      this.questItemIds = new int[]{12764, 12765};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30081-02.htm")) {
            st.startQuest();
            int rnd = getRandom(1, 2);
            if (rnd == 1) {
               st.setCond(2, false);
               htmltext = "30081-02a.htm";
            } else {
               st.setCond(5, false);
               htmltext = "30081-02b.htm";
            }
         } else if (event.equalsIgnoreCase("30511-03.htm")) {
            st.setCond(6, true);
         } else if (event.equalsIgnoreCase("31572-03.htm")) {
            st.setCond(3, true);
         } else if (event.equalsIgnoreCase("30081-05a.htm")) {
            st.takeItems(12764, 1L);
            st.calcReward(this.getId());
            st.exitQuest(false, true);
         } else if (event.equalsIgnoreCase("30081-05b.htm")) {
            st.takeItems(12765, 1L);
            st.calcReward(this.getId());
            st.exitQuest(false, true);
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
         int npcId = npc.getId();
         int cond = st.getCond();
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         } else if (npcId == 30081) {
            if (cond == 0) {
               if (player.getLevel() >= 40) {
                  htmltext = "30081-01.htm";
               } else {
                  htmltext = "30081-00.htm";
                  st.exitQuest(true);
               }
            } else if (cond == 2 || cond == 3) {
               htmltext = "30081-03a.htm";
            } else if (cond == 4) {
               htmltext = "30081-04a.htm";
            } else if (cond == 5 || cond == 6) {
               htmltext = "30081-03b.htm";
            } else if (cond == 7) {
               htmltext = "30081-04b.htm";
            }
         } else if (npcId == 31572) {
            if (cond == 2) {
               htmltext = "31572-01.htm";
            } else if (cond == 3) {
               if (st.getQuestItemsCount(6450) >= 10L && st.getQuestItemsCount(6451) >= 10L && st.getQuestItemsCount(6452) >= 10L) {
                  st.takeItems(6450, 10L);
                  st.takeItems(6451, 10L);
                  st.takeItems(6452, 10L);
                  st.setCond(4, true);
                  st.giveItems(12764, 1L);
                  htmltext = "31572-05.htm";
               } else {
                  htmltext = "31572-04.htm";
               }
            } else if (cond == 4) {
               htmltext = "31572-06.htm";
            }
         } else if (npcId == 30511) {
            if (cond == 5) {
               htmltext = "30511-01.htm";
            } else if (cond == 6) {
               if (st.getQuestItemsCount(5079) >= 40L && st.getQuestItemsCount(5082) >= 40L && st.getQuestItemsCount(5084) >= 40L) {
                  st.takeItems(5079, 40L);
                  st.takeItems(5082, 40L);
                  st.takeItems(5084, 40L);
                  st.setCond(7, true);
                  st.giveItems(12765, 1L);
                  htmltext = "30511-05.htm";
               } else {
                  htmltext = "30511-04.htm";
               }
            } else if (cond == 7) {
               htmltext = "30511-06.htm";
            }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _040_ASpecialOrder(40, _040_ASpecialOrder.class.getSimpleName(), "");
   }
}
