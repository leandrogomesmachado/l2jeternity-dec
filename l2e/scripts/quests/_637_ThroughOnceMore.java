package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _637_ThroughOnceMore extends Quest {
   public _637_ThroughOnceMore(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32010);
      this.addTalkId(32010);
      this.addKillId(new int[]{21565, 21566, 21567});
      this.questItemIds = new int[]{8066};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32010-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("32010-10.htm")) {
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() > 72 && st.getQuestItemsCount(8064) > 0L && st.getQuestItemsCount(8067) == 0L) {
                  htmltext = "32010-02.htm";
               } else {
                  htmltext = "32010-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.isCond(2) && st.getQuestItemsCount(8066) >= 10L) {
                  st.takeItems(8066, -1L);
                  st.takeItems(8065, 1L);
                  st.calcReward(this.getId());
                  st.exitQuest(true, true);
                  htmltext = "32010-05.htm";
               } else {
                  htmltext = "32010-04.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st != null && st.isCond(1) && st.calcDropItems(this.getId(), 8066, npc.getId(), 10)) {
         st.setCond(2);
      }

      return null;
   }

   public static void main(String[] args) {
      new _637_ThroughOnceMore(637, _637_ThroughOnceMore.class.getSimpleName(), "");
   }
}
