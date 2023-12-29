package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _033_MakeAPairOfDressShoes extends Quest {
   public _033_MakeAPairOfDressShoes(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30838);
      this.addTalkId(30838);
      this.addTalkId(30164);
      this.addTalkId(31520);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30838-1.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31520-1.htm")) {
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("30838-3.htm")) {
            st.setCond(3, true);
         } else if (event.equalsIgnoreCase("30838-5.htm")) {
            if (st.getQuestItemsCount(1882) >= 200L && st.getQuestItemsCount(1868) >= 600L && st.getQuestItemsCount(57) >= 200000L) {
               st.takeItems(1882, 200L);
               st.takeItems(1868, 600L);
               st.takeItems(57, 200000L);
               st.setCond(4, true);
            } else {
               htmltext = "30838-3a.htm";
            }
         } else if (event.equalsIgnoreCase("30164-1.htm")) {
            if (st.getQuestItemsCount(57) >= 300000L) {
               st.takeItems(57, 300000L);
               st.setCond(5, true);
            } else {
               htmltext = "30164-1b.htm";
            }
         } else if (event.equalsIgnoreCase("30838-7.htm")) {
            st.calcReward(this.getId());
            st.exitQuest(true, true);
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

      if (npcId == 30838) {
         if (cond == 0 && st.getQuestItemsCount(7113) == 0L) {
            if (player.getLevel() >= 60) {
               QuestState fwear = player.getQuestState("_037_PleaseMakeMeFormalWear");
               if (fwear != null && fwear.get("cond") != null) {
                  if (fwear.get("cond").equals("7")) {
                     htmltext = "30838-0.htm";
                  } else {
                     htmltext = "30838-8.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30838-8.htm";
                  st.exitQuest(true);
               }
            } else {
               htmltext = "30838-8.htm";
            }
         } else if (cond == 1) {
            htmltext = "30838-1a.htm";
         } else if (cond == 2) {
            htmltext = "30838-2.htm";
         } else if (cond == 3) {
            htmltext = "30838-4.htm";
         } else if (cond == 4) {
            htmltext = "30838-5a.htm";
         } else if (cond == 5) {
            htmltext = "30838-6.htm";
         }
      } else if (npcId == 31520) {
         if (cond == 1) {
            htmltext = "31520-0.htm";
         } else if (cond == 2) {
            htmltext = "31520-1a.htm";
         }
      } else if (npcId == 30164) {
         if (cond == 4) {
            htmltext = "30164-0.htm";
         } else if (cond == 5) {
            htmltext = "30164-1a.htm";
         }
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _033_MakeAPairOfDressShoes(33, _033_MakeAPairOfDressShoes.class.getSimpleName(), "");
   }
}
