package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _028_ChestCaughtWithABaitOfIcyAir extends Quest {
   public _028_ChestCaughtWithABaitOfIcyAir(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31572);
      this.addTalkId(31572);
      this.addTalkId(31442);
      this.questItemIds = new int[]{7626};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31572-04.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31572-07.htm")) {
            if (st.getQuestItemsCount(6503) > 0L) {
               st.takeItems(6503, 1L);
               st.giveItems(7626, 1L);
               st.setCond(2, true);
            } else {
               htmltext = "31572-08.htm";
            }
         } else if (event.equalsIgnoreCase("31442-02.htm")) {
            if (st.getQuestItemsCount(7626) == 1L) {
               htmltext = "31442-02.htm";
               st.takeItems(7626, -1L);
               st.calcReward(this.getId());
               st.exitQuest(false, true);
            } else {
               htmltext = "31442-03.htm";
               st.exitQuest(true);
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
         int cond = st.getCond();
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < 36) {
                  QuestState qs = player.getQuestState("_051_OFullesSpecialBait");
                  if (qs != null && qs.isCompleted()) {
                     htmltext = "31572-01.htm";
                  } else {
                     htmltext = "31572-02.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "31572-01.htm";
               }
               break;
            case 1:
               if (npc.getId() == 31572) {
                  if (cond == 1) {
                     htmltext = "31572-05.htm";
                     if (st.getQuestItemsCount(6503) == 0L) {
                        htmltext = "31572-06.htm";
                     }
                  } else if (cond == 2) {
                     htmltext = "31572-09.htm";
                  }
               } else if (npc.getId() == 31442 && cond == 2) {
                  htmltext = "31442-01.htm";
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _028_ChestCaughtWithABaitOfIcyAir(28, _028_ChestCaughtWithABaitOfIcyAir.class.getSimpleName(), "");
   }
}
