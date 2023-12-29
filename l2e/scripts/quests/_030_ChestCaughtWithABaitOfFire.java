package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _030_ChestCaughtWithABaitOfFire extends Quest {
   public _030_ChestCaughtWithABaitOfFire(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31577);
      this.addTalkId(31577);
      this.addTalkId(30629);
      this.questItemIds = new int[]{7628};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31577-04.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31577-07.htm")) {
            if (st.getQuestItemsCount(6511) > 0L) {
               st.takeItems(6511, 1L);
               st.giveItems(7628, 1L);
               st.setCond(2, true);
            } else {
               htmltext = "31577-08.htm";
            }
         } else if (event.equalsIgnoreCase("30629-02.htm")) {
            if (st.getQuestItemsCount(7628) == 1L) {
               st.takeItems(7628, -1L);
               st.calcReward(this.getId());
               st.exitQuest(false, true);
            } else {
               htmltext = "30629-03.htm";
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
               if (player.getLevel() < 60) {
                  QuestState qs = player.getQuestState("_053_LinnaeusSpecialBait");
                  if (qs != null) {
                     if (qs.isCompleted()) {
                        htmltext = "31577-01.htm";
                     } else {
                        htmltext = "31577-02.htm";
                        st.exitQuest(true);
                     }
                  } else {
                     htmltext = "31577-03.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "31577-01.htm";
               }
               break;
            case 1:
               if (npc.getId() == 31577) {
                  if (cond == 1) {
                     htmltext = "31577-05.htm";
                     if (st.getQuestItemsCount(6511) == 0L) {
                        htmltext = "31577-06.htm";
                     }
                  } else if (cond == 2) {
                     htmltext = "31577-09.htm";
                  }
               } else if (npc.getId() == 30629 && cond == 2) {
                  htmltext = "30629-01.htm";
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _030_ChestCaughtWithABaitOfFire(30, _030_ChestCaughtWithABaitOfFire.class.getSimpleName(), "");
   }
}
