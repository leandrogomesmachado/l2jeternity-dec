package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _252_ItSmellsDelicious extends Quest {
   public _252_ItSmellsDelicious(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30200);
      this.addTalkId(30200);
      this.addKillId(new int[]{22786, 22787, 22788, 18908});
      this.questItemIds = new int[]{15500, 15501};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30200-05.htm")) {
            st.startQuest();
         }

         if (event.equalsIgnoreCase("30200-08.htm")) {
            st.calcExpAndSp(this.getId());
            st.calcReward(this.getId());
            st.exitQuest(false, true);
         }

         return event;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      int cond = st.getCond();
      String htmltext = getNoQuestMsg(player);
      if (npc.getId() == 30200) {
         if (st.getState() == 0 && cond == 0) {
            if (player.getLevel() >= 82) {
               htmltext = "30200-01.htm";
            } else {
               htmltext = "30200-02.htm";
               st.exitQuest(true);
            }
         } else if (st.getState() == 1 && cond == 1) {
            htmltext = "30200-06.htm";
         } else if (st.getState() == 1 && cond == 2) {
            htmltext = "30200-07.htm";
         } else if (st.getState() == 2) {
            htmltext = "30200-03.htm";
         }
      }

      return htmltext;
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st.isCond(1)) {
            if (npc.getId() == 18908) {
               st.calcDoDropItems(this.getId(), 15501, npc.getId(), 5);
            } else {
               st.calcDoDropItems(this.getId(), 15500, npc.getId(), 10);
            }

            if (st.getQuestItemsCount(15501) == 5L && st.getQuestItemsCount(15500) == 10L) {
               st.setCond(2, true);
            }
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _252_ItSmellsDelicious(252, _252_ItSmellsDelicious.class.getSimpleName(), "");
   }
}
