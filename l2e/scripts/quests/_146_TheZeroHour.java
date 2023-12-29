package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _146_TheZeroHour extends Quest {
   public _146_TheZeroHour(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31554);
      this.addTalkId(31554);
      this.addKillId(25671);
      this.questItemIds = new int[]{14859};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31554-02.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("reward")) {
            if (st.getQuestItemsCount(14859) >= 1L) {
               htmltext = "31554-06.htm";
               st.takeItems(14859, 1L);
               st.calcReward(this.getId());
               st.exitQuest(true, true);
            } else {
               htmltext = "31554-05.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = this.getQuestState(player, true);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 81) {
                  QuestState st1 = player.getQuestState("_109_InSearchOfTheNest");
                  if (st1 != null && st1.isCompleted()) {
                     htmltext = "31554-01.htm";
                  } else {
                     htmltext = "31554-00.htm";
                  }
               } else {
                  htmltext = "31554-03.htm";
               }
               break;
            case 1:
               if (st.isCond(1) || st.isCond(2)) {
                  htmltext = "31554-04.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st != null && st.isCond(1) && st.calcDropItems(this.getId(), 14859, npc.getId(), 1)) {
            st.setCond(2, true);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _146_TheZeroHour(146, _146_TheZeroHour.class.getSimpleName(), "");
   }
}
