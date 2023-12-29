package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _037_PleaseMakeMeFormalWear extends Quest {
   public _037_PleaseMakeMeFormalWear(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30842);
      this.addTalkId(30842);
      this.addTalkId(31520);
      this.addTalkId(31521);
      this.addTalkId(31627);
      this.questItemIds = new int[]{7159, 7160, 7164};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30842-1.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31520-1.htm")) {
            st.giveItems(7164, 1L);
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("31521-1.htm")) {
            st.giveItems(7160, 1L);
            st.setCond(3, true);
         } else if (event.equalsIgnoreCase("31627-1.htm")) {
            if (st.getQuestItemsCount(7160) > 0L) {
               st.takeItems(7160, 1L);
               st.setCond(4, true);
            } else {
               htmltext = "no_items.htm";
            }
         } else if (event.equalsIgnoreCase("31521-3.htm")) {
            st.giveItems(7159, 1L);
            st.setCond(5, true);
         } else if (event.equalsIgnoreCase("31520-3.htm")) {
            st.setCond(6, true);
         } else if (event.equalsIgnoreCase("31520-5.htm")) {
            if (st.getQuestItemsCount(7076) > 0L && st.getQuestItemsCount(7077) > 0L && st.getQuestItemsCount(7078) > 0L) {
               st.takeItems(7076, 1L);
               st.takeItems(7077, 1L);
               st.takeItems(7078, 1L);
               st.setCond(7, true);
            } else {
               htmltext = "no_items.htm";
            }
         } else if (event.equalsIgnoreCase("31520-7.htm")) {
            if (st.getQuestItemsCount(7113) > 0L) {
               st.takeItems(7113, 1L);
               st.calcReward(this.getId());
               st.exitQuest(false, true);
            } else {
               htmltext = "no_items.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      int npcId = npc.getId();
      int cond = st.getCond();
      if (st.isCompleted()) {
         htmltext = getAlreadyCompletedMsg(player);
      }

      if (npcId == 30842) {
         if (cond == 0) {
            if (player.getLevel() >= 60) {
               htmltext = "30842-0.htm";
            } else {
               htmltext = "30842-2.htm";
               st.exitQuest(true);
            }
         } else if (cond == 1) {
            htmltext = "30842-2a.htm";
         }
      } else if (npcId == 31520) {
         if (cond == 1) {
            htmltext = "31520-0.htm";
         } else if (cond == 2 || cond == 3 || cond == 4 || cond == 5) {
            if (st.getQuestItemsCount(7159) > 0L) {
               st.takeItems(7159, 1L);
               htmltext = "31520-2.htm";
            } else {
               htmltext = "31520-1a.htm";
            }
         } else if (cond == 6) {
            if (st.getQuestItemsCount(7076) > 0L && st.getQuestItemsCount(7077) > 0L && st.getQuestItemsCount(7078) > 0L) {
               htmltext = "31520-4.htm";
            } else {
               htmltext = "31520-3a.htm";
            }
         } else if (cond == 7) {
            if (st.getQuestItemsCount(7113) > 0L) {
               htmltext = "31520-6.htm";
            } else {
               htmltext = "31520-5a.htm";
            }
         }
      } else if (npcId == 31521) {
         if (st.getQuestItemsCount(7164) > 0L) {
            st.takeItems(7164, 1L);
            htmltext = "31521-0.htm";
         } else if (cond == 3) {
            htmltext = "31521-1a.htm";
         } else if (cond == 4) {
            htmltext = "31521-2.htm";
         } else if (cond == 5) {
            htmltext = "31521-3a.htm";
         }
      } else if (npcId == 31627) {
         if (st.getQuestItemsCount(7160) > 0L) {
            htmltext = "31627-0.htm";
         }

         if (cond == 4) {
            htmltext = "31627-1a.htm";
         }
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _037_PleaseMakeMeFormalWear(37, _037_PleaseMakeMeFormalWear.class.getSimpleName(), "");
   }
}
